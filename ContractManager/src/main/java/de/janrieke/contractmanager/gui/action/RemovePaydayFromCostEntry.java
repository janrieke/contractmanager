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

import java.rmi.RemoteException;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.janrieke.contractmanager.rmi.Costs;
import de.willuhn.jameica.gui.Action;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action for "remove cost payday".
 */
public class RemovePaydayFromCostEntry implements Action {

	private ContractControl contractControl;

	public RemovePaydayFromCostEntry(ContractControl contract) {
		this.contractControl = contract;
	}

	/**
	 * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
	 */
	@Override
	public void handleAction(Object context) throws ApplicationException {

		// check if the context is a contract
		if (context == null || !(context instanceof Costs)) {
			throw new ApplicationException(Settings.i18n().tr(
					"Please choose a cost entry."));
		}

		Costs c = (Costs) context;

		try {
			c.setPayday(null);
			contractControl.getCostsList().updateItem(c, c);
		} catch (RemoteException e) {
			Logger.error("error while removing payday from cost entry", e);
			throw new ApplicationException(Settings.i18n().tr(
					"Error while removing payday from cost entry"));
		}
	}
}