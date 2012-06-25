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
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Contract.IntervalType;
import de.janrieke.contractmanager.rmi.Costs;
import de.willuhn.jameica.gui.Action;
import de.willuhn.util.ApplicationException;

/**
 * Action for "delete contract".
 */
public class CreateNewCostEntry implements Action {

	private ContractControl contract;

	public CreateNewCostEntry(ContractControl contract) {
		this.contract = contract;
	}

	/**
	 * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
	 */
	public void handleAction(Object context) throws ApplicationException {
		try {
			Costs c = (Costs) Settings.getDBService().createObject(
					Costs.class, null);
			c.setContract((Contract) contract.getCurrentObject());
			c.setDescription("");
			c.setMoney(0);
			c.setPeriod(IntervalType.YEARS);
			contract.addTemporaryCostEntry(c);
		} catch (RemoteException e) {
			throw new ApplicationException(Settings.i18n().tr(
					"Error while creating new cost entry"), e);
		}
	}
}