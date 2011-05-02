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
import de.janrieke.contractmanager.Settings;
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

/**
 * Controller fuer den Dialog Lizenzinformationen.
 */
public class LicenseControl extends AbstractControl {

	private Part libList = null;

	/**
	 * ct.
	 * 
	 * @param view
	 */
	public LicenseControl(AbstractView view) {
		super(view);
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
				+ Settings.i18n().tr("ContractManager") + "</span></p>");
		if (manifest != null) {
			buffer.append("<p>");
			buffer.append("<br/>" + Settings.i18n().tr("Version") + ": "
					+ manifest.getVersion());
			buffer.append("<br/>" + Settings.i18n().tr("Description") + ": "
					+ manifest.getDescription());
			buffer.append("<br/>" + Settings.i18n().tr("URL") + ": " + manifest.getURL());
			buffer.append("<br/>" + Settings.i18n().tr("Licence") + ": "
					+ manifest.getLicense());
			buffer.append("<br/><br/><br/></p>");
			buffer.append("<p><b>"+Settings.i18n().tr("Used Libraries:")+"</b></p>");
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
				buffer.append("<br/>" + Settings.i18n().tr("Description") + ": "
						+ ir.getDescription());
//				buffer.append("<br/>" + Settings.i18n()("Path") + ": "
//						+ infos[i].getParentFile().getAbsolutePath());
				buffer.append("<br/>" + Settings.i18n().tr("URL") + ": " + ir.getUrl());
				buffer.append("<br/>" + Settings.i18n().tr("Licence") + ": "
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