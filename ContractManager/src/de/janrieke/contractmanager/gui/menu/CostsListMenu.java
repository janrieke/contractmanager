package de.janrieke.contractmanager.gui.menu;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.action.CreateNewCostEntry;
import de.janrieke.contractmanager.gui.action.DeleteCostEntry;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;

public class CostsListMenu extends ContextMenu {

	public CostsListMenu(ContractControl contractControl) {
		// CheckedContextMenuItems will be disabled, if the user clicks into an
		// empty space of the table
		addItem(new ContextMenuItem(Settings.i18n().tr(
				"Create a new cost entry"), new CreateNewCostEntry(contractControl),
		"document-new.png"));

		addItem(ContextMenuItem.SEPARATOR);
		addItem(new CheckedContextMenuItem(Settings.i18n().tr(
				"Delete cost entry..."), new DeleteCostEntry(contractControl), "window-close.png"));
	}
}
