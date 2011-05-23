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
import de.janrieke.contractmanager.gui.action.DeleteAddress;
import de.janrieke.contractmanager.gui.button.RestoreButton;
import de.janrieke.contractmanager.gui.control.AddressControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.ScrolledContainer;
import de.willuhn.util.ApplicationException;

/**
 * this is the dialog for the address details.
 */
public class AddressDetailView extends AbstractView {

	private RestoreButton restoreButton;
	private boolean activationState;
	private Button deleteButton;

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#bind()
	 */
	public void bind() throws Exception {
		// draw the title
		GUI.getView().setTitle(Settings.i18n().tr("Address Details"));

		// instanciate controller
		final AddressControl control = new AddressControl(this);

	    ScrolledContainer scroller = new ScrolledContainer(getParent());
		
	    scroller.addHeadline(Settings.i18n().tr("Address Information"));
		// create a bordered group
		//LabelGroup group = new LabelGroup(getParent(), Settings.i18n().tr(
		//		"Address details"));

	    scroller.addLabelPair(Settings.i18n().tr("Name/Company"), control.getPartnerName());
	    scroller.addLabelPair(Settings.i18n().tr("Street"), control.getPartnerStreetNumber());
	    scroller.addLabelPair(Settings.i18n().tr("Extra"), control.getPartnerExtra());
	    scroller.addLabelPair(Settings.i18n().tr("Zipcode"), control.getPartnerZipcodeCity());
	    scroller.addLabelPair(Settings.i18n().tr("State"), control.getPartnerState());
	    scroller.addLabelPair(Settings.i18n().tr("Country"), control.getPartnerCountry());
	    
		// add some buttons
		ButtonArea buttons = new ButtonArea(getParent(), 4);

		deleteButton = new Button(Settings.i18n().tr("Delete Address..."),
				new DeleteAddress(), control.getCurrentObject(), false, "window-close.png");
		deleteButton.setEnabled(activationState);
		buttons.addButton(deleteButton);
		restoreButton = new RestoreButton(this, control.getCurrentObject(), false);
		restoreButton.setEnabled(activationState);
		buttons.addButton(restoreButton);
		buttons.addButton(Settings.i18n().tr("Store Address"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
				control.handleStore();
			}
		}, null, true, "document-save.png"); // "true" defines this button as the default button

		// show transactions of this address
		//new Headline(getParent(), Settings.i18n().tr(
		//		"Transactions of this address"));
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

}