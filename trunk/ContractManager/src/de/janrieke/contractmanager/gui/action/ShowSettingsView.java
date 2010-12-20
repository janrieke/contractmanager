package de.janrieke.contractmanager.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Action to open the project list.
 */
public class ShowSettingsView implements Action {

	/**
	 * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
	 */
	public void handleAction(Object context) throws ApplicationException {
		GUI.startView(de.janrieke.contractmanager.gui.view.SettingsView.class
				.getName(), null);
	}

}