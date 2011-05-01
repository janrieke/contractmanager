package de.janrieke.contractmanager.gui.action;

import java.rmi.RemoteException;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Action for "show contract details" or "create new contract".
 */
public class ShowContractDetailView implements Action {

	/**
	 * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
	 */
	public void handleAction(Object context) throws ApplicationException {

		Contract p = null;

		// check if the context is a contract
		if (context != null && (context instanceof Contract))
			p = (Contract) context;
		else {
			try {
				p = (Contract) Settings.getDBService().createObject(
						Contract.class, null);
			} catch (RemoteException e) {
				throw new ApplicationException(Settings.i18n().tr(
						"error while creating new contract"), e);
			}
		}

		// ok, lets start the dialog
		GUI.startView(de.janrieke.contractmanager.gui.view.ContractDetailView.class
				.getName(), p);
	}

}
