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
package de.janrieke.contractmanager.gui.dialog;

import org.eclipse.swt.widgets.Composite;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.Settings;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;

/**
 * Dialog to assign a transaction to a contract.
 */
public class AssignTransactionDialog extends AbstractDialog<Object> {

	/**
	 * ct.
	 * 
	 * @param position
	 */
	public AssignTransactionDialog(Object context) {
		super(AbstractDialog.POSITION_CENTER);
		this.setTitle(Settings.i18n().tr("Assign Transaction to Contract"));
	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void paint(Composite parent) throws Exception {

		FormTextPart text = new FormTextPart();
		text.setText("<form>" + "<p><b>Contract Manager</b></p>"
				+ "<br/>Licence: GPL 3.0 (http://www.gnu.org/licenses/gpl-3.0.txt)"
				+ "<br/><p>Copyright by Jan Rieke [it@janrieke.de]</p>"
				+ "<p>http://www.janrieke.de/projects/contractmanager</p>"
				+ "<br/><p>Contains code from Jameica Example Plugin, Hibiscus, and Syntax; copyright by Olaf Willuhn [info@jameica.org], GPL</p>"
				+ "<p>http://www.jameica.org</p>" + "</form>");

		text.paint(parent);

		LabelGroup group = new LabelGroup(parent, " Information ");

		AbstractPlugin p = Application.getPluginLoader().getPlugin(
				ContractManagerPlugin.class);

		group.addLabelPair(Settings.i18n().tr("Version"), new LabelInput(""
				+ p.getManifest().getVersion()));
		group.addLabelPair(Settings.i18n().tr("Working directory"),
				new LabelInput("" + p.getResources().getWorkPath()));

	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
	 */
	@Override
	protected Object getData() throws Exception {
		return null;
	}

}
