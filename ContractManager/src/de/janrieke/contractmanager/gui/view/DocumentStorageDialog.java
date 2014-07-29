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

/*
 * Partially copied from Hibiscus/Syntax, (c) by willuhn.webdesign
 */

package de.janrieke.contractmanager.gui.view;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.sql.Blob;
import java.sql.SQLException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Storage;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Dialog for selecting a contract for a transaction. Uses a table and includes
 * search capabilities.
 */
public class DocumentStorageDialog extends AbstractDialog<Contract> {
	private Contract contract = null;

	private TablePart table = null;
	private Button buttonAddFile = null;
	private Button buttonAddLocalFileLink = null;
	private Button buttonRemove = null;
	private Button buttonOpen = null;
	
	//TODO: calculate this from the maximum text width
	private static int BUTTON_HORIZONTAL_SIZE = 160;
	private static int BUTTON_VERTICAL_SIZE = SWT.DEFAULT;

	/**
	 * ct.
	 * 
	 * @param position
	 * @param preselected
	 *            the preselected contract.
	 * @throws RemoteException
	 */
	public DocumentStorageDialog(Contract c) throws RemoteException {
		super(AbstractDialog.POSITION_CENTER, true);
		this.contract = c;

		this.setTitle(Settings.i18n().tr("Document Storage"));
		this.setSize(570, 300);
	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
	 */
	protected void paint(Composite parent) throws Exception {
		Container group = new SimpleContainer(parent, true);

		Container left = new SimpleContainer(group.getComposite(), true, 1);

	
		left.addText(Settings.i18n().tr("List of files:"), true);
		left.addPart(this.getTable());

		Container right = new SimpleContainer(group.getComposite(), false, 1);
		right.getComposite().setLayoutData(new GridData(BUTTON_HORIZONTAL_SIZE+10, SWT.DEFAULT));
		right.addPart(getAddFileButton());
		right.addPart(getAddLocalLinkFileButton());
		right.addPart(getRemoveButton());
		right.addPart(getOpenButton());

		ButtonArea buttons = new ButtonArea();
		buttons.addButton(Settings.i18n().tr("Close"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
				close();
			}
		}, null, false, "process-stop.png");

		group.addButtonArea(buttons);
	}

	/**
	 * Returns the open button to open the selected storage entry.
	 * 
	 * @return the button.
	 */
	private Button getAddFileButton() {
		if (buttonAddFile != null)
			return buttonAddFile;

		buttonAddFile = new Button(Settings.i18n().tr("Add File..."),
				new OpenFile()) {
					@Override
					public void paint(Composite parent) throws RemoteException {
						super.paint(parent);
						button.setLayoutData(new GridData(BUTTON_HORIZONTAL_SIZE, BUTTON_VERTICAL_SIZE));
					}
		};
		return buttonAddFile;
	}
	
	/**
	 * Returns the open button to open the selected storage entry.
	 * 
	 * @return the button.
	 */
	private Button getAddLocalLinkFileButton() {
		if (buttonAddLocalFileLink != null)
			return buttonAddLocalFileLink;

		buttonAddLocalFileLink = new Button(Settings.i18n().tr("Add Local File Link..."),
				new AddLocalFileLink()) {
			@Override
			public void paint(Composite parent) throws RemoteException {
				super.paint(parent);
				button.setLayoutData(new GridData(BUTTON_HORIZONTAL_SIZE, BUTTON_VERTICAL_SIZE));
			}
		};
		return buttonAddLocalFileLink;
	}
	
	/**
	 * Returns the open button to open the selected storage entry.
	 * 
	 * @return the button.
	 */
	private Button getRemoveButton() {
		if (buttonRemove != null)
			return buttonRemove;

		buttonRemove = new Button(Settings.i18n().tr("Remove"),
				new OpenFile()) {
			@Override
			public void paint(Composite parent) throws RemoteException {
				super.paint(parent);
				button.setLayoutData(new GridData(BUTTON_HORIZONTAL_SIZE, BUTTON_VERTICAL_SIZE));
			}
		};
		buttonRemove.setEnabled(false); // initial deaktiviert
		return buttonRemove;
	}
	
	/**
	 * Returns the open button to open the selected storage entry.
	 * 
	 * @return the button.
	 */
	private Button getOpenButton() {
		if (buttonOpen != null)
			return buttonOpen;

		buttonOpen = new Button(Settings.i18n().tr("Open"),
				new OpenFile()) {
			@Override
			public void paint(Composite parent) throws RemoteException {
				super.paint(parent);
				button.setLayoutData(new GridData(BUTTON_HORIZONTAL_SIZE, BUTTON_VERTICAL_SIZE));
			}
		};
		buttonOpen.setEnabled(false); // initial deaktiviert
		return buttonOpen;
	}

	/**
	 * Returns the file table
	 * 
	 * @return the file table
	 */
	private TablePart getTable() {
		if (this.table != null)
			return this.table;

		try {
			this.table = new TablePart(contract.getAttachedFiles(),
					new OpenFile());
			this.table.setSummary(false);
			this.table.addColumn(Settings.i18n().tr("Description"),
					"description");
			this.table.addColumn(Settings.i18n().tr("Local file URI"), "path");
			table.addSelectionListener(new Listener() {
				@Override
				public void handleEvent(Event event) {
				}
			});
		} catch (RemoteException e) {
			Logger.error("error while getting file list", e);
		}
		return this.table;
	}

	/**
	 * Action for opening the selected entry
	 */
	private class OpenFile implements Action {
		public void handleAction(Object context) throws ApplicationException {
			Storage storage = (Storage) getTable().getSelection();
			if (storage != null) {
				Blob blob;
				try {
					blob = storage.getFile();
					if (blob != null) {
						// file has been stored in DB as blob
						String suffix = storage.getPath(); // path contains the
															// file suffix when
						File tmp = File.createTempFile(
								"jameica-contractmanager-", suffix);

						// Delete temp file when program exits
						tmp.deleteOnExit();

						//save the DB contents to file
						FileOutputStream output = new FileOutputStream(tmp);
						try {
							InputStream src = storage.getFile().getBinaryStream();
							byte[] buffer = new byte[1024];
							int len;
							while ((len = src.read(buffer)) != -1) {
							    output.write(buffer, 0, len);
							}
						} catch (IOException ex) {
							Logger.error("error while saving temporary file contents", ex);
						} finally {
						   output.close();
						}
						
						// open the temp file
						Desktop.getDesktop().open(tmp);
					} else {
						// just open the plain file link from "path" field
						String path = storage.getPath();
						if (path != null && !"".equals(path)) {
							File localfile = new File(path);
							if (localfile.exists() && localfile.isFile() && localfile.canRead()) {
								Desktop.getDesktop().open(localfile);
							}
						}
						
					}
				} catch (IOException e) {
					Logger.error("error while saving to temporary file", e);
				} catch (SQLException e) {
					Logger.error("error while retrieving file from database", e);
				}
			}
		}
	}

	/**
	 * Action for opening the selected entry
	 */
	private class AddLocalFileLink implements Action {
		public void handleAction(Object context) throws ApplicationException {
			FileDialog fd = new FileDialog(GUI.getShell(), SWT.OPEN);
			fd.setText(Settings.i18n().tr("Select File to Add as Link"));

			final String filename = fd.open();
			try {
				Storage s = (Storage) Settings.getDBService().createObject(
						Storage.class, null);
				s.setContract(contract);
				s.setDescription(Settings.i18n().tr("Enter description here"));
				s.setPath(filename);
				s.store();
				table.addItem(s);
			} catch (RemoteException e) {
				throw new ApplicationException(Settings.i18n().tr(
						"Error while creating new storage entry"), e);
			}
		}
	}
	
	@Override
	protected Contract getData() throws Exception {
		return null;
	}
}
