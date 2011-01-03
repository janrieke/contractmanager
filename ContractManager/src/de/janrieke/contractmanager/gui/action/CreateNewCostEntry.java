package de.janrieke.contractmanager.gui.action;

import java.rmi.RemoteException;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Costs;
import de.janrieke.contractmanager.rmi.Contract.IntervalType;
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
					"error while creating new cost entry"), e);
		}
	}
}