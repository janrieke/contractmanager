/*
 *   This file is part of ContractManager for Jameica.
 *   Copyright (C) 2010-2011  Jan Rieke
 *
 *   ContractManager is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   ContractManager is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *   
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.janrieke.contractmanager.gui.view;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.action.DeleteContract;
import de.janrieke.contractmanager.gui.action.GenerateCancelation;
import de.janrieke.contractmanager.gui.button.RestoreButton;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

/**
 * this is the dialog for the contract details.
 */
public class ContractDetailView extends AbstractView implements Extendable {

	private RestoreButton restoreButton;
	private boolean activationState;
	private Button deleteButton;
	private ContractControl control; 
			
	/**
	 * @see de.willuhn.jameica.gui.AbstractView#bind()
	 */
	public void bind() throws Exception {
		// draw the title
		GUI.getView().setTitle(Settings.i18n().tr("Contract Details"));

		// instanciate controller
		control = new ContractControl(this);

	    ScrolledContainer scroller = new ScrolledContainer(getParent());
		
	    ColumnLayout columns = new ColumnLayout(scroller.getComposite(), 2);
	    SimpleContainer left = new SimpleContainer(columns.getComposite());

	    left.addHeadline(Settings.i18n().tr("Contract Information"));
		// create a bordered group
		//LabelGroup group = new LabelGroup(getParent(), Settings.i18n().tr(
		//		"Contract details"));

		// all all input fields to the group.
	    left.addLabelPair(Settings.i18n().tr("Name of contract"), control.getName());
	    left.addLabelPair(Settings.i18n().tr("Contract number"), control.getContractNumber());
	    left.addLabelPair(Settings.i18n().tr("Customer number"), control.getCustomerNumber());

	    left.addHeadline(Settings.i18n().tr("Financial Details"));
	    left.addPart(control.getCostsList());
	    left.addLabelPair(Settings.i18n().tr("Costs per term"), control.getCostsPerTerm());
	    left.addLabelPair(Settings.i18n().tr("Costs per month"), control.getCostsPerMonth());
	    left.addHeadline(Settings.i18n().tr("Runtime"));
	    left.addLabelPair(Settings.i18n().tr("Start date"),	control.getStartDate());
	    left.addLabelPair(Settings.i18n().tr("End date"), control.getEndDate());
	    //TODO: Show how long the contract is already running
	    left.addLabelPair(Settings.i18n().tr("Cancellation period"), control.getCancellationPeriod());
	    left.addLabelPair(Settings.i18n().tr("First minimum term"), control.getFirstMinRuntime());
	    left.addLabelPair(Settings.i18n().tr("Following minimum terms"), control.getNextMinRuntime());
	    //TODO: Show complete term instead of only beginning
	    left.addLabelPair(Settings.i18n().tr("Next term"), control.getNextTerm());
	    left.addLabelPair(Settings.i18n().tr("Deadline for next cancellation"), control.getNextCancellationDeadline());

	    SimpleContainer right = new SimpleContainer(columns.getComposite(), true);
	    right.addHeadline(Settings.i18n().tr("Contractual Partner Address"));
	    right.addLabelPair(Settings.i18n().tr("Address"), control.getPartnerAddress());
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

		//buttons.addButton(new Back(false));
		buttons.addButton(Settings.i18n().tr("Generate Cancellation..."), new GenerateCancelation(), control.getCurrentObject(), false, "document-print.png");
		deleteButton = new Button(Settings.i18n().tr("Delete Contract..."),
				new DeleteContract(), control.getCurrentObject(), false, "window-close.png");
		deleteButton.setEnabled(activationState);
		buttons.addButton(deleteButton);
		restoreButton = new RestoreButton(this, control.getCurrentObject(), false);
		restoreButton.setEnabled(activationState);
		buttons.addButton(restoreButton);
		buttons.addButton(Settings.i18n().tr("Store Contract"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
				control.handleStore();
			}
		}, null, true, "document-save.png"); // "true" defines this button as the default button

		// show transactions of this contract
		//new Headline(getParent(), Settings.i18n().tr(
		//		"Transactions of this contract"));
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
	
	public void setButtonActivationState(boolean active) {
		if (restoreButton != null)
			restoreButton.setEnabled(active);
		if (deleteButton != null)
			deleteButton.setEnabled(active);
		activationState = active;
	}

	public ContractControl getControl() {
		return control;
	}

	public void addInput(Input input) {
	    ScrolledContainer scroller = new ScrolledContainer(getParent());
	    scroller.getComposite().setSize(200, 100);
	    scroller.addInput(input);
	}

	@Override
	public String getExtendableID() {
		return this.getClass().getName();
	}
}