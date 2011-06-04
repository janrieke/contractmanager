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
package de.janrieke.contractmanager.gui.control;

import java.rmi.RemoteException;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.menu.AddressListMenu;
import de.janrieke.contractmanager.gui.view.AddressDetailView;
import de.janrieke.contractmanager.rmi.Address;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author willuhn, jrieke
 */
public class AddressControl extends AbstractControl {

	// list of all addresses
	private TablePart addressList;

	// Input fields for the address attributes
	private Input partnerName;
	private Input partnerStreet;
	private Input partnerNumber;
	private Input partnerStreetNumber;
	private Input partnerExtra;
	private Input partnerZipcode;
	private Input partnerCity;
	private Input partnerZipcodeCity;
	private Input partnerState;
	private Input partnerCountry;

	// this is the currently opened address
	private Address address;

	/**
	 * ct.
	 * 
	 * @param view
	 *            this is our view (the address details view).
	 */
	public AddressControl(AbstractView view) {
		super(view);
		if (view instanceof AddressDetailView) {
			try {
				((AddressDetailView) view)
						.setButtonActivationState(!((Address) view
								.getCurrentObject()).isNewObject());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Small helper method to get the current address.
	 * 
	 * @return
	 */
	private Address getAddress() {
		if (address != null)
			return address;
		address = (Address) getCurrentObject();
		return address;
	}

	public Input getPartnerName() throws RemoteException {
		if (partnerName == null) {
			partnerName = new TextInput(getAddress().getName(),
					255);
			partnerName.setMandatory(true);
		}
		return partnerName;
	}

	public Input getPartnerStreet() throws RemoteException {
		if (partnerStreet == null)
			partnerStreet = new TextInput(getAddress().getStreet(), 255);
		return partnerStreet;
	}

	public Input getPartnerNumber() throws RemoteException {
		if (partnerNumber == null)
			partnerNumber = new TextInput(getAddress().getNumber(), 255);
		return partnerNumber;
	}

	public Input getPartnerStreetNumber() throws RemoteException {
		if (partnerStreetNumber != null)
			return partnerStreetNumber;

		partnerStreetNumber = new MultiInput(getPartnerStreet(),
				new LabelInput(Settings.i18n().tr("Number")),
				getPartnerNumber());

		return partnerStreetNumber;
	}

	public Input getPartnerExtra() throws RemoteException {
		if (partnerExtra == null)
			partnerExtra = new TextInput(getAddress().getExtra(),
					255);
		return partnerExtra;
	}

	public Input getPartnerZipcode() throws RemoteException {
		if (partnerZipcode == null)
			partnerZipcode = new TextInput(getAddress().getZipcode(), 255);
		return partnerZipcode;
	}

	public Input getPartnerCity() throws RemoteException {
		if (partnerCity == null)
			partnerCity = new TextInput(getAddress().getCity(),
					255);
		return partnerCity;
	}

	public Input getPartnerZipcodeCity() throws RemoteException {
		if (partnerZipcodeCity != null)
			return partnerZipcodeCity;

		partnerZipcodeCity = new MultiInput(getPartnerZipcode(),
				new LabelInput(Settings.i18n().tr("City")), getPartnerCity());

		return partnerZipcodeCity;
	}

	public Input getPartnerState() throws RemoteException {
		if (partnerState == null)
			partnerState = new TextInput(getAddress().getState(),
					255);
		return partnerState;
	}

	public Input getPartnerCountry() throws RemoteException {
		if (partnerCountry == null)
			partnerCountry = new TextInput(getAddress().getCountry(), 255);
		return partnerCountry;
	}

	/**
	 * Returns an iterator with all addresses in the database.
	 * 
	 * @throws RemoteException 
	 * @return iterator containing all addresses
	 */
	public static GenericIterator getAddresses() throws RemoteException {
		DBService service = Settings.getDBService();
		DBIterator addresses = service.createList(Address.class);
		return addresses;
	}
	
	/**
	 * Creates a table containing all addresses.
	 * 
	 * @return a table with addresss.
	 * @throws RemoteException
	 */
	public Part getAddressesTable() throws RemoteException {
		if (addressList != null)
			return addressList;

		// 1) get the dataservice
		DBService service = Settings.getDBService();

		// 2) now we can create the address list.
		// We do not need to specify the implementing class for
		// the interface "Address". Jameica's classloader knows
		// all classes an finds the right implementation automatically. ;)
		DBIterator addresses = service.createList(Address.class);

		// 4) create the table
		addressList = new TablePart(
				addresses,
				new de.janrieke.contractmanager.gui.action.ShowAddressDetailView());

		// 5) now we have to add some columns.
		addressList.addColumn(Settings.i18n().tr("Name"), "name");
		addressList.addColumn(Settings.i18n().tr("Street"), "street");
		addressList.addColumn(Settings.i18n().tr("Number"), "number");
		addressList.addColumn(Settings.i18n().tr("Zipcode"), "zipcode");
		addressList.addColumn(Settings.i18n().tr("City"), "city");
		addressList.addColumn(Settings.i18n().tr("Country"), "country");
		addressList.addColumn(Settings.i18n().tr("Used in # Contracts"), Address.CONTRACT_COUNT);

		// 6) we are adding a context menu
		addressList.setContextMenu(new AddressListMenu(true));

		return addressList;
	}

	/**
	 * This method stores the address using the current values.
	 */
	public void handleStore() {
		try {
			// get the current address.
			Address a = getAddress();

			// invoke all Setters of this address and assign the current values
			a.setName((String) getPartnerName().getValue());
			a.setStreet((String) getPartnerStreet().getValue());
			a.setNumber((String) getPartnerNumber().getValue());
			a.setExtra((String) getPartnerExtra().getValue());
			a.setZipcode((String) getPartnerZipcode().getValue());
			a.setCity((String) getPartnerCity().getValue());
			a.setState((String) getPartnerState().getValue());
			a.setCountry((String) getPartnerCountry().getValue());

			// Now, let's store the address and its address.
			// The store() method throws ApplicationExceptions if
			// insertCheck() or updateCheck() failed.
			try {
				a.store();

				updateDerivedAttributes();
				
				if (view instanceof AddressDetailView)
					((AddressDetailView) view).setButtonActivationState(true);
				
				GUI.getStatusBar().setSuccessText(
						Settings.i18n().tr("Address stored successfully"));
			} catch (ApplicationException e) {
				GUI.getView().setErrorText(e.getMessage());
			}
		} catch (RemoteException e) {
			Logger.error("error while storing address", e);
			GUI.getStatusBar().setErrorText(
					Settings.i18n().tr("Error while storing address"));
		}
	}

	private void updateDerivedAttributes() throws RemoteException {
	}

	public boolean isNewAddress() {
		try {
			return address.isNewObject();
		} catch (RemoteException e) {
			return false;
		}
	}
}