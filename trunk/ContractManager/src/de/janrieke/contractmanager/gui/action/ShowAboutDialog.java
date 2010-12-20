package de.janrieke.contractmanager.gui.action;

import de.janrieke.contractmanager.Settings;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ShowAboutDialog implements Action {

	/**
	 * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
	 */
	public void handleAction(Object context) throws ApplicationException {
		try {
			new de.janrieke.contractmanager.gui.view.AboutDialog(
					AbstractDialog.POSITION_CENTER).open();
		} catch (Exception e) {
			Logger.error("error while opening about dialog", e);
			throw new ApplicationException(Settings.i18n().tr(
					"Error while opening the About dialog"));
		}
	}

}