package de.janrieke.contractmanager.gui.action;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Costs;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action for "delete contract".
 */
public class DeleteCostEntry implements Action {

	private ContractControl contract;

	public DeleteCostEntry(ContractControl contract) {
		this.contract = contract;
	}

	/**
	 * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
	 */
	public void handleAction(Object context) throws ApplicationException {

		// check if the context is a contract
		if (context == null || !(context instanceof Contract))
			throw new ApplicationException(Settings.i18n().tr(
					"Please choose a cost entry."));

		Costs c = (Costs) context;

		try {
			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(Settings.i18n().tr("Are you sure?"));
			d.setText(Settings.i18n().tr(
					"Do you really want to delete these cost entry?"));

			Boolean choice = (Boolean) d.open();
			if (!choice.booleanValue())
				return;

			c.delete();
			contract.removeCostEntry(c);
			GUI.getStatusBar().setSuccessText(
					Settings.i18n().tr("Cost entry deleted successfully"));
		} catch (Exception e) {
			Logger.error("error while deleting costs", e);
			throw new ApplicationException(Settings.i18n().tr(
					"Error while deleting costs"));
		}
	}
}