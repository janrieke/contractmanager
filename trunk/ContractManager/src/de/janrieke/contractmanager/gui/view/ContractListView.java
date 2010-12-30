package de.janrieke.contractmanager.gui.view;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.action.ExportCancelationReminders;
import de.janrieke.contractmanager.gui.action.ShowContractDetailView;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.util.ApplicationException;

/**
 * View to show the list of existing contracts.
 */
public class ContractListView extends AbstractView {

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#bind()
	 */
	public void bind() throws Exception {

		GUI.getView().setTitle(Settings.i18n().tr("Existing contracts"));

		ContractControl control = new ContractControl(this);

		control.getContractsTable().paint(this.getParent());

		ButtonArea buttons = new ButtonArea(this.getParent(), 3);
		buttons.addButton(new Back(false));

		buttons.addButton(Settings.i18n().tr("Export cancellation reminders..."),
				new ExportCancelationReminders(), true, true, "office-calendar.png");
		buttons.addButton(Settings.i18n().tr("Create new contract"),
				new ShowContractDetailView(), null, true, "document-new.png");
	}

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#unbind()
	 */
	public void unbind() throws ApplicationException {
	}

}