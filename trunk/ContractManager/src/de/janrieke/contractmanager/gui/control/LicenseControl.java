/**********************************************************************
 * Copied from Hibiscus
 * 
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/gui/controller/LicenseControl.java,v $
 * $Revision: 1.14 $
 * $Date: 2009-03-10 23:51:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.janrieke.contractmanager.gui.control;

import java.io.File;
import java.io.FileInputStream;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.willuhn.io.FileFinder;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.InfoReader;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Controller fuer den Dialog Lizenzinformationen.
 */
public class LicenseControl extends AbstractControl {

	private Part libList = null;
	private I18N i18n = null;

	/**
	 * ct.
	 * 
	 * @param view
	 */
	public LicenseControl(AbstractView view) {
		super(view);
		i18n = Application.getPluginLoader()
				.getPlugin(ContractManagerPlugin.class).getResources()
				.getI18N();
	}

	/**
	 * Liefert eine Liste mit allen direkt von ContractManager verwendeten Komponenten.
	 * 
	 * @return Liste der verwendeten Komponenten
	 */
	public Part getLibList() {
		if (libList != null)
			return libList;

		AbstractPlugin plugin = Application.getPluginLoader().getPlugin(
				ContractManagerPlugin.class);

		StringBuffer buffer = new StringBuffer();
		buffer.append("<form>");

		Manifest manifest = null;
		try {
			manifest = Application.getPluginLoader().getManifest(
					ContractManagerPlugin.class);
		} catch (Exception e) {
			Logger.error("unable to read info.xml from plugin hibiscus", e);
		}
		buffer.append("<p><span color=\"header\" font=\"header\">"
				+ i18n.tr("ContractManager") + "</span></p>");
		if (manifest != null) {
			buffer.append("<p>");
			buffer.append("<br/>" + i18n.tr("Version") + ": "
					+ manifest.getVersion());
			buffer.append("<br/>" + i18n.tr("Description") + ": "
					+ manifest.getDescription());
			buffer.append("<br/>" + i18n.tr("URL") + ": " + manifest.getURL());
			buffer.append("<br/>" + i18n.tr("Licence") + ": "
					+ manifest.getLicense());
			buffer.append("</p>");
		}

		String path = plugin.getManifest().getPluginDir();

		FileFinder finder = new FileFinder(new File(path + "/lib"));
		finder.matches(".*?info\\.xml$");
		File[] infos = finder.findRecursive();
		for (int i = 0; i < infos.length; ++i) {
			try {
				InfoReader ir = new InfoReader(new FileInputStream(infos[i]));
//				if (ir == null) {
//					Logger.warn("inforeader is null, skipping lib");
//					continue;
//				}
				buffer.append("<p>");
				buffer.append("<b>" + ir.getName() + "</b>");
				buffer.append("<br/>" + i18n.tr("Description") + ": "
						+ ir.getDescription());
//				buffer.append("<br/>" + i18n.tr("Path") + ": "
//						+ infos[i].getParentFile().getAbsolutePath());
				buffer.append("<br/>" + i18n.tr("URL") + ": " + ir.getUrl());
				buffer.append("<br/>" + i18n.tr("Licence") + ": "
						+ ir.getLicense());
				buffer.append("</p>");
			} catch (Exception e) {
				Logger.error("unable to parse " + infos[0], e);
			}
		}
		buffer.append("</form>");

		libList = new FormTextPart(buffer.toString());
		return libList;
	}
}