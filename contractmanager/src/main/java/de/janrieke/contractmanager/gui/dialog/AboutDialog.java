/*
 *   This file is part of ContractManager for Jameica.
 *   Copyright (C) 2010-2018  Jan Rieke
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
package de.janrieke.contractmanager.gui.dialog;

import org.eclipse.swt.widgets.Composite;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.Settings;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;

/**
 * Our "About..." dialog.
 */
public class AboutDialog extends AbstractDialog<Object> {

	int WINDOW_WIDTH = 550;
	int WINDOW_HEIGHT = 350;

	/**
	 * ct.
	 *
	 * @param position
	 */
	public AboutDialog(int position) {
		super(position);
		this.setTitle(Settings.i18n().tr("About..."));
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void paint(Composite parent) throws Exception {
		Manifest manifest = Application.getPluginLoader().getManifest(ContractManagerPlugin.class);

		FormTextPart text = new FormTextPart();
		text.setText("<form>" + "<p><b>" + manifest.getDescription() + "</b></p>"
				+ "<br/>Licence: GPL 3.0 (http://www.gnu.org/licenses/gpl-3.0.txt)"
				+ "<br/><p>Copyright by Jan Rieke [it@janrieke.de] 2010-2019</p>" + "<p>"
				+ manifest.getHomepage() + "</p>"
				+ "<br/><p>Contains code from Jameica, Jameica Example Plugin, Hibiscus, and Syntax; copyright by Olaf Willuhn [info@jameica.org], GPL</p>"
				+ "<p>http://www.jameica.org</p>" + "</form>");

		text.paint(parent);

		LabelGroup group = new LabelGroup(parent, " Information ");

		AbstractPlugin p = Application.getPluginLoader().getPlugin(ContractManagerPlugin.class);

		group.addLabelPair(Settings.i18n().tr("Version"),
				new LabelInput("" + manifest.getVersion()));
		group.addLabelPair(Settings.i18n().tr("Working directory"),
				new LabelInput("" + p.getResources().getWorkPath()));

		ButtonArea buttons = new ButtonArea();
		buttons.addButton(Settings.i18n().tr("Database Information"), context -> {
			try {
				new DebugDialog(DebugDialog.POSITION_CENTER).open();
			} catch (OperationCanceledException oce) {
				Logger.info(oce.getMessage());
				return;
			} catch (Exception e) {
				Logger.error("unable to display debug dialog", e);
				Application.getMessagingFactory()
						.sendMessage(new StatusBarMessage(
								Settings.i18n().tr("Error while showing database information"),
								StatusBarMessage.TYPE_ERROR));
			}
		}, null, false, "dialog-information.png");
		buttons.addButton(Settings.i18n().tr("Close"), context -> close(), null, true,
				"window-close.png");
		group.addButtonArea(buttons);
	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
	 */
	@Override
	protected Object getData() throws Exception {
		return null;
	}

	@Override
	protected void onEscape() {
		// Avoid exception on ESC
		close();
	}
}
