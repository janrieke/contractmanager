package de.janrieke.contractmanager.gui.action;

import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.doc.text.OdfTextUserFieldDecl;
import org.w3c.dom.NodeList;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.server.SettingsUtil;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
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
					"Please choose a contract"));
		
		Contract p = (Contract) context;
		
	    FileDialog fd = new FileDialog(GUI.getShell(),SWT.SAVE);
	    fd.setText(Settings.i18n().tr("Select Filename for Cancellation"));
	    fd.setOverwrite(true);
	    try {
			fd.setFileName(Settings.i18n().tr("cancellation-{0}-{1}.odt",Settings.DATEFORMAT.format(new Date()), p.getName()));
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

//	    String path = System.getProperty("user.home");
//	    if (path != null && path.length() > 0)
//	      fd.setFilterPath(path);

	    final String filename = fd.open();
	    
	    if (filename == null || filename.length() == 0)
	      return;

		try {
			//retrieve variable values
			Map<String, String> values = new HashMap<String, String>();

			values.put("NAME", "");
			values.put("STREET", "");
			values.put("NUMBER", "");
			values.put("EXTRA", "");
			values.put("ZIPCODE", "");
			values.put("CITY", "");
			values.put("STATE", "");
			values.put("COUNTRY", "");

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

			values.put("TODAY", Settings.DATEFORMAT.format(new Date()));
			values.put("CONTRACT_NAME", p.getName());
			
			Date nextExtension = p.getNextExtension();
			if (nextExtension != null)
				values.put("CANCELLATION_DATE", Settings.DATEFORMAT.format(nextExtension));
			else
				values.put("CANCELLATION_DATE", Settings.DATEFORMAT.format(new Date()));


			// open template document from plugin directory
			FileInputStream fis = new FileInputStream(ContractManagerPlugin.getInstance().getManifest().getPluginDir() + "/templates/Kuendigung.odt");
			OdfDocument doc = OdfDocument.loadDocument(fis);
			
			//set the values of the variables 
			NodeList nodes = doc.getOfficeBody().getElementsByTagName(
					OdfTextUserFieldDecl.ELEMENT_NAME.getQName());
			for (int i = 0; i < nodes.getLength(); i++) {
				OdfTextUserFieldDecl element = (OdfTextUserFieldDecl) nodes
						.item(i);
				if (values.containsKey(element.getTextNameAttribute())) {
					element.setOfficeStringValueAttribute(values.get(element
							.getTextNameAttribute()));
				}
			}
			
			//save the file with a new file name
			doc.save(filename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}