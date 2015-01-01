/*
 *   This file is part of ContractManager for Jameica.
 *   Copyright (C) 2010-2011  Jan Rieke
 *
 *   ContractManager is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   ContractManager is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *   
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.janrieke.contractmanager.gui.action;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.dom.element.text.TextUserFieldDeclElement;
import org.w3c.dom.NodeList;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.server.SettingsUtil;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.ListDialog;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Generic Action for "History back" ;).
 */
public class GenerateCancelation implements Action {

	/**
	 * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
	 */
	public void handleAction(Object context) throws ApplicationException {

		// check if the context is a contract
		if (context == null || !(context instanceof Contract))
			throw new ApplicationException(Settings.i18n().tr(
					"Please choose a contract."));

		//TODO: Also generate PDFs

		Contract p = (Contract) context;
		
		// Which type of document should be generated?
		final List<Path> templates = new ArrayList<>();
		String templateFolderString;
		try {
			templateFolderString = Settings.getTemplateFolder();
		} catch (RemoteException e) {
			templateFolderString = ContractManagerPlugin.getInstance().getManifest().getPluginDir() + "/templates/";
		}
		Path templateFolderPath = Paths.get(templateFolderString);
		try {
			Files.walkFileTree(templateFolderPath, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attributes)
						throws IOException {
					if (path.getFileName().toString().endsWith(".odt")) {
						templates.add(path);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path path, IOException e)
						throws IOException {
					if (path.toString().endsWith(".odt")) {
						Logger.error("Template file retrieval failed for " + path.toString(), e);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			Logger.error("Template folder search failed for " + templateFolderString, e);
		}

		String templateBaseName;		
		if (templates.size() == 0) {
			Logger.error("No *.odt templates found in template folder " + templateFolderString);
			return;
		} else if (templates.size() > 1) {
			ListDialog templateSelectionDialog = new ListDialog(templates, 0);
			templateSelectionDialog.setPanelText(Settings.i18n().tr("Please select the template to use."));
			templateSelectionDialog.setSize(450, SWT.DEFAULT);
			templateSelectionDialog.setTitle(Settings.i18n().tr("Select template"));
			templateSelectionDialog.addColumn(Settings.i18n().tr("Template"), "fileName");
			Object dialogResult = null;
			try {
				dialogResult = templateSelectionDialog.open();
			} catch (Exception e) {
				if (e instanceof OperationCanceledException)
					return;
				Logger.error("Failed to open template selection dialog", e);
				dialogResult = templates.get(0);
			}
			
			if (dialogResult == null)
				return;

			templateBaseName = ((Path)dialogResult).getFileName().toString();
		} else {
			templateBaseName = templates.get(0).toString();
		}
		
		if (templateBaseName.contains(".")) {
			templateBaseName = templateBaseName.substring(0, templateBaseName.lastIndexOf("."));
		}
		
		// Select file name
		FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
		fd.setText(Settings.i18n().tr("Select Filename for Cancellation"));
		fd.setOverwrite(true);

		String contractName = "";
		try {
			contractName = p.getName();
		} catch (RemoteException e1) {
			throw new ApplicationException(Settings.i18n().tr(
					"Error while accessing contract"), e1);
		}

		StringBuilder suggestedFilename = new StringBuilder();
		for (char c : contractName.toCharArray()) {
			if (c=='.' || Character.isLetterOrDigit(c))
				suggestedFilename.append(c);
			else 
				suggestedFilename.append('_');
		}

		fd.setFileName(Settings.i18n().tr(templateBaseName + "-{0}-{1}.odt",
				Settings.dateformat(new Date()), suggestedFilename.toString()));

		// String path = System.getProperty("user.home");
		// if (path != null && path.length() > 0)
		// fd.setFilterPath(path);

		final String filename = fd.open();

		if (filename == null || filename.length() == 0)
			return;

		try {
			// retrieve variable values
			Map<String, String> values = new HashMap<String, String>();

			values.put("NAME", p.getAddress().getName());
			values.put("STREET", p.getAddress().getStreet());
			values.put("NUMBER", p.getAddress().getNumber());
			values.put("EXTRA", p.getAddress().getExtra());
			values.put("ZIPCODE", p.getAddress().getZipcode());
			values.put("CITY", p.getAddress().getCity());
			values.put("STATE", p.getAddress().getState());
			values.put("COUNTRY", p.getAddress().getCountry());

			values.put("FROM_NAME", SettingsUtil.get("name", ""));
			values.put("FROM_STREET", SettingsUtil.get("street", ""));
			values.put("FROM_NUMBER", SettingsUtil.get("number", ""));
			values.put("FROM_EXTRA", SettingsUtil.get("extra", ""));
			values.put("FROM_ZIPCODE", SettingsUtil.get("zipcode", ""));
			values.put("FROM_CITY", SettingsUtil.get("city", ""));
			values.put("FROM_STATE", SettingsUtil.get("state", ""));
			values.put("FROM_COUNTRY", SettingsUtil.get("country", ""));
			values.put("FROM_EMAIL", SettingsUtil.get("email", ""));
			values.put("FROM_PHONE", SettingsUtil.get("phone", ""));

			values.put("TODAY", Settings.dateformat(new Date()));
			values.put("CONTRACT_NAME", p.getName());
			values.put("CUSTOMER_NUMBER", p.getCustomerNumber());
			values.put("CONTRACT_NUMBER", p.getContractNumber());

			Date nextExtension = p.getNextCancelableTermBegin();
			if (nextExtension != null) {
				//cancel on the last day of the previous term
				Calendar cal = Calendar.getInstance();
				cal.setTime(nextExtension);
				cal.add(Calendar.DAY_OF_YEAR, -1);
				values.put("CANCELLATION_DATE",
						Settings.dateformat(cal.getTime()));
			}
			else
				values.put("CANCELLATION_DATE",
						Settings.dateformat(new Date()));

			// open template document from plugin directory
			FileInputStream fis = new FileInputStream(ContractManagerPlugin
					.getInstance().getManifest().getPluginDir()
					+ "/templates/Kuendigung.odt");
			OdfDocument doc = OdfDocument.loadDocument(fis);

			// set the values of the variables
			NodeList nodes = doc.getContentDom().getElementsByTagName(
					TextUserFieldDeclElement.ELEMENT_NAME.getQName());
			for (int i = 0; i < nodes.getLength(); i++) {
				TextUserFieldDeclElement element = (TextUserFieldDeclElement) nodes
						.item(i);
				if (values.containsKey(element.getTextNameAttribute())) {
					element.setOfficeStringValueAttribute(values.get(element
							.getTextNameAttribute()));
				}
			}

			// save the file with a new file name
			doc.save(filename);
			GUI.getStatusBar().setSuccessText(
					Settings.i18n().tr("Cancellation successfully generated."));
		} catch (Exception e) {
			throw new ApplicationException(Settings.i18n().tr(
					"Error while storing document"), e);
		}

		if (Desktop.isDesktopSupported()) {
			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(Settings.i18n().tr("Open file?"));
			d.setText(Settings.i18n().tr(
					"Would you like to open the generated cancellation file?"));

			Boolean choice;
			try {
				choice = (Boolean) d.open();
				if (choice.booleanValue()) {
					Desktop.getDesktop().open(new File(filename));
				}
			} catch (Exception e) {
				throw new ApplicationException(Settings.i18n().tr(
						"Error while opening document"), e);
			}
		}
	}

}