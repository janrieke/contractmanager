package de.janrieke.contractmanager.gui.view;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.action.DeleteContract;
import de.janrieke.contractmanager.gui.action.GenerateCancelation;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.buttons.Back;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Headline;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

/**
 * this is the dialog for the contract details.
 */
public class ContractDetailView extends AbstractView {

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#bind()
	 */
	public void bind() throws Exception {
		// draw the title
		GUI.getView().setTitle(Settings.i18n().tr("Contract details"));

		// instanciate controller
		final ContractControl control = new ContractControl(this);
		
	    ColumnLayout columns = new ColumnLayout(getParent(),2);
	    SimpleContainer left = new SimpleContainer(columns.getComposite());

	    left.addHeadline(Settings.i18n().tr("Contract Information"));
		// create a bordered group
		//LabelGroup group = new LabelGroup(getParent(), Settings.i18n().tr(
		//		"Contract details"));

		// all all input fields to the group.
	    left.addLabelPair(Settings.i18n().tr("Name"), control.getName());
	    left.addLabelPair(Settings.i18n().tr("Contract Number"), control.getContractNumber());
	    left.addLabelPair(Settings.i18n().tr("Customer Number"), control.getCustomerNumber());

	    left.addHeadline(Settings.i18n().tr("Financial Details"));
	    left.addLabelPair(Settings.i18n().tr("One-time costs"), control.getMoneyOnce());
	    left.addLabelPair(Settings.i18n().tr("Daily costs"), control.getMoneyPerDay());
	    left.addLabelPair(Settings.i18n().tr("Weekly costs"), control.getMoneyPerWeek());
	    left.addLabelPair(Settings.i18n().tr("Monthly costs"), control.getMoneyPerMonth());
	    left.addLabelPair(Settings.i18n().tr("Annual costs"), control.getMoneyPerYear());
	    left.addHeadline(Settings.i18n().tr("Runtime"));
	    left.addLabelPair(Settings.i18n().tr("Start date"),	control.getStartDate());
	    left.addLabelPair(Settings.i18n().tr("End date"), control.getEndDate());
	    left.addLabelPair(Settings.i18n().tr("Cancellation period"), control.getCancellationPeriod());
	    left.addLabelPair(Settings.i18n().tr("First minimum term"), control.getFirstMinRuntime());
	    left.addLabelPair(Settings.i18n().tr("Following minimum terms"), control.getNextMinRuntime());
	    left.addLabelPair(Settings.i18n().tr("Next term extension"), control.getNextExtension());
	    left.addLabelPair(Settings.i18n().tr("Deadline for next cancellation "), control.getNextCancellationDeadline());

	    SimpleContainer right = new SimpleContainer(columns.getComposite(), true);
	    right.addHeadline(Settings.i18n().tr("Contractual Partner Address"));
	    right.addLabelPair(Settings.i18n().tr("Name/Company"), control.getPartnerName());
	    right.addLabelPair(Settings.i18n().tr("Street"), control.getPartnerStreetNumber());
	    right.addLabelPair(Settings.i18n().tr("Extra"), control.getPartnerExtra());
	    right.addLabelPair(Settings.i18n().tr("Zipcode"), control.getPartnerZipcodeCity());
	    right.addLabelPair(Settings.i18n().tr("State"), control.getPartnerState());
	    right.addLabelPair(Settings.i18n().tr("Country"), control.getPartnerCountry());
	    
	    right.addHeadline(Settings.i18n().tr("Comment"));
	    right.addPart(control.getComment());
	    
		// add some buttons
		ButtonArea buttons = new ButtonArea(getParent(), 4);

		buttons.addButton(new Back(false));
		buttons.addButton(Settings.i18n().tr("Generate Cancelation"), new GenerateCancelation(), control.getCurrentObject(), false, "document-print.png");
		buttons.addButton(Settings.i18n().tr("Delete Project"),
				new DeleteContract(), control.getCurrentObject(), false, "window-close.png");
		buttons.addButton(Settings.i18n().tr("Store Project"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
				control.handleStore();
			}
		}, null, true, "document-save.png"); // "true" defines this button as the default button

		// show transactions of this contract
		new Headline(getParent(), Settings.i18n().tr(
				"Transactions of this contract"));
		//		control.getTaskList().paint(getParent());

	}

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#unbind()
	 */
	public void unbind() throws ApplicationException {
		// this method will be invoked when leaving the dialog.
		// You are able to interrupt the unbind by throwing an
		// ApplicationException.
	}

}