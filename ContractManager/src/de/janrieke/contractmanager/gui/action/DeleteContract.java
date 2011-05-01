package de.janrieke.contractmanager.gui.action;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.view.ContractDetailView;
import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Action for "delete contract".
 */
public class DeleteContract implements Action {

	/**
	 * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
	 */
	public void handleAction(Object context) throws ApplicationException {

		// check if the context is a contract
		if (context == null || !(context instanceof Contract))
			throw new ApplicationException(Settings.i18n().tr(
					"Please choose a contract."));

		Contract p = (Contract) context;

		try {

			// before deleting the contract, we show up a confirm dialog ;)

			YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
			d.setTitle(Settings.i18n().tr("Are you sure?"));
			d.setText(Settings.i18n().tr(
					"Do you really want to delete this contract?"));

			Boolean choice = (Boolean) d.open();
			if (!choice.booleanValue())
				return;

			p.delete();
			GUI.getStatusBar().setSuccessText(
					Settings.i18n().tr("Contract deleted successfully"));
		} catch (Exception e) {
			Logger.error("error while deleting contract", e);
			throw new ApplicationException(Settings.i18n().tr(
					"Error while deleting contract"));
		}
		
		if (GUI.getCurrentView() instanceof ContractDetailView)
			GUI.startPreviousView();
	}

}