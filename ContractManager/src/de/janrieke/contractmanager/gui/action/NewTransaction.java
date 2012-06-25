package de.janrieke.contractmanager.gui.action;

import de.janrieke.contractmanager.gui.view.AssignTransactionDialog;
import de.janrieke.contractmanager.rmi.Transaction;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Action to assign a transaction to a contract.
 */
public class NewTransaction implements Action {

	/**
	 * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
	 */
	public void handleAction(Object context) throws ApplicationException
	{
		Transaction b = null;
		if (context != null)
		{
			if (context instanceof Transaction)
				b = (Transaction) context;
		}
		GUI.startView(AssignTransactionDialog.class,b);
	}

}