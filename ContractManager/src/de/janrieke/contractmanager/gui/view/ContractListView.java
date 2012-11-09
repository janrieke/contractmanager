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
package de.janrieke.contractmanager.gui.view;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.action.ShowContractDetailView;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.util.ApplicationException;

/**
 * View to show the list of existing contracts.
 */
public class ContractListView extends AbstractView {

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#bind()
	 */
	public void bind() throws Exception {

		GUI.getView().setTitle(Settings.i18n().tr("Existing contracts"));

		ContractControl control = new ContractControl(this);

		control.getContractsTable().paint(this.getParent());

		ButtonArea buttons = new ButtonArea(this.getParent(), 3);
		//buttons.addButton(new Back(false));

		buttons.addButton(Settings.i18n().tr("Create new contract..."),
				new ShowContractDetailView(), null, true, "document-new.png");
	}

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#unbind()
	 */
	public void unbind() throws ApplicationException {
	}

}