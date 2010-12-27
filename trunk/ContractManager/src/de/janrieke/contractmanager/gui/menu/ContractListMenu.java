package de.janrieke.contractmanager.gui.menu;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.action.DeleteContract;
import de.janrieke.contractmanager.gui.action.GenerateCancelation;
import de.janrieke.contractmanager.gui.action.ShowContractDetailView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.util.ApplicationException;

/**
 * Prepared context menu for project tables.
 */
public class ContractListMenu extends ContextMenu {
	/**
	 * ct.
	 * 
	 * @param showNew
	 */
	public ContractListMenu(boolean showNew) {
		// CheckedContextMenuItems will be disabled, if the user clicks into an
		// empty space of the table
		addItem(new CheckedContextMenuItem(Settings.i18n().tr("Open..."),
				new ShowContractDetailView(), "document-open.png"));

		addItem(new CheckedContextMenuItem(Settings.i18n().tr(
				"Generate Cancellation..."), new GenerateCancelation(),
				"document-print.png"));

		if (showNew) {
			// separator
			addItem(ContextMenuItem.SEPARATOR);
			
			addItem(new ContextMenuItem(Settings.i18n().tr(
					"Create a new contract..."), new Action() {
				public void handleAction(Object context)
						throws ApplicationException {
					// we force the context to be null to create a new
					// project in any case
					new ShowContractDetailView().handleAction(null);
				}
			}, "document-new.png"));

			addItem(ContextMenuItem.SEPARATOR);
			addItem(new CheckedContextMenuItem(Settings.i18n().tr(
					"Delete contract..."), new DeleteContract(),
					"window-close.png"));
		}

	}
}