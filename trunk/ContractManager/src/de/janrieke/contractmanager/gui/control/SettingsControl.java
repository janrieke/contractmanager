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
import de.janrieke.contractmanager.gui.input.ICSFileInput;
import de.janrieke.contractmanager.server.SettingsUtil;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author willuhn, jrieke
 */
public class SettingsControl extends AbstractControl {

	// Input fields for the attributes
	private TextInput name;
	private TextInput street;
	private TextInput number;
	private TextInput extra;
	private TextInput zipcode;
	private TextInput city;
	private TextInput state;
	private TextInput country;
	private Input streetNumber;
	private MultiInput zipcodeCity;
	private TextInput email;
	private TextInput phone;

	private IntegerInput warningTime;
	private IntegerInput noticeTime;
	private ICSFileInput iCalFileLocation;
	private CheckboxInput iCalAutoExport;
	private CheckboxInput anonICalExport;

	/**
	 * ct.
	 * 
	 * @param view
	 *            this is our view.
	 */
	public SettingsControl(AbstractView view) {
		super(view);
	}

	/**
	 * Returns the input field for the name.
	 * 
	 * @return input field.
	 * @throws RemoteException
	 */
	public Input getName() throws RemoteException {
		if (name == null)
			name = new TextInput(SettingsUtil.get("name", ""), 255);
		return name;
	}

	public Input getStreet() throws RemoteException {
		if (street == null)
			street = new TextInput(SettingsUtil.get("street", ""), 255);
		return street;
	}

	public Input getNumber() throws RemoteException {
		if (number == null)
			number = new TextInput(SettingsUtil.get("number", ""));
		return number;
	}

	public Input getStreetNumber() throws RemoteException {
		if (streetNumber != null)
			return streetNumber;

		streetNumber = new MultiInput(getStreet(), getNumber());

		return streetNumber;
	}

	public Input getExtra() throws RemoteException {
		if (extra == null)
			extra = new TextInput(SettingsUtil.get("extra", ""), 255);
		return extra;
	}

	public Input getZipcode() throws RemoteException {
		if (zipcode == null)
			zipcode = new TextInput(SettingsUtil.get("zipcode", ""), 255);
		return zipcode;
	}

	public Input getCity() throws RemoteException {
		if (city == null)
			city = new TextInput(SettingsUtil.get("city", ""), 255);
		return city;
	}

	public Input getZipcodeCity() throws RemoteException {
		if (zipcodeCity != null)
			return zipcodeCity;

		zipcodeCity = new MultiInput(getZipcode(), getCity());

		return zipcodeCity;
	}

	public Input getState() throws RemoteException {
		if (state == null)
			state = new TextInput(SettingsUtil.get("state", ""), 255);
		return state;
	}

	public Input getCountry() throws RemoteException {
		if (country == null)
			country = new TextInput(SettingsUtil.get("country", ""), 255);
		return country;
	}

	public Input getEmail() throws RemoteException {
		if (email == null)
			email = new TextInput(SettingsUtil.get("email", ""), 255);
		return email;
	}

	public Input getPhone() throws RemoteException {
		if (phone == null)
			phone = new TextInput(SettingsUtil.get("phone", ""), 255);
		return phone;
	}

	public IntegerInput getWarningTime() throws RemoteException {
		if (warningTime == null) {
			warningTime = new IntegerInput(Settings.getExtensionWarningTime());
			warningTime.setComment(Settings.i18n().tr("Days"));
		}
		return warningTime;
	}

	public IntegerInput getNoticeTime() throws RemoteException {
		if (noticeTime == null) {
			noticeTime = new IntegerInput(Settings.getExtensionNoticeTime());
			noticeTime.setComment(Settings.i18n().tr("Days"));
		}
		return noticeTime;
	}

	public CheckboxInput getICalAutoExport() throws RemoteException {
		if (iCalAutoExport == null) {
			iCalAutoExport = new CheckboxInput(Settings.getICalAutoExport());
		}
		return iCalAutoExport;
	}

	public CheckboxInput getNamedICalExport() throws RemoteException {
		if (anonICalExport == null) {
			anonICalExport = new CheckboxInput(Settings.getNamedICalExport());
		}
		return anonICalExport;
	}

	public ICSFileInput getICalFileLocation() throws RemoteException {
		if (iCalFileLocation == null) {
			iCalFileLocation = new ICSFileInput(Settings.getICalFileLocation(), true);
		}
		return iCalFileLocation;
	}

	/**
	 * This method stores the contract using the current values.
	 */
	public void handleStore() {
		try {
			SettingsUtil.set("name", (String) getName().getValue());
			SettingsUtil.set("street", (String) getStreet().getValue());
			SettingsUtil.set("number", (String) getNumber().getValue());
			SettingsUtil.set("extra", (String) getExtra().getValue());
			SettingsUtil.set("zipcode", (String) getZipcode().getValue());
			SettingsUtil.set("city", (String) getCity().getValue());
			SettingsUtil.set("state", (String) getState().getValue());
			SettingsUtil.set("country", (String) getCountry().getValue());
			SettingsUtil.set("email", (String) getEmail().getValue());
			SettingsUtil.set("phone", (String) getPhone().getValue());
			Settings.setExtensionNoticeTime((Integer) getNoticeTime()
					.getValue());
			Settings.setExtensionWarningTime((Integer) getWarningTime()
					.getValue());
			Settings.setICalAutoExport((Boolean) getICalAutoExport().getValue());
			Settings.setNamedICalExport((Boolean) getNamedICalExport().getValue());
			Settings.setICalFileLocation((String) getICalFileLocation()
					.getValue());
			
			GUI.getStatusBar().setSuccessText(
					Settings.i18n().tr("Settings saved."));
		} catch (ApplicationException e) {
			GUI.getView().setErrorText(e.getMessage());

		} catch (RemoteException e) {
			Logger.error("Error while storing settings", e);
			GUI.getStatusBar().setErrorText(
					Settings.i18n().tr("Error while storing settings"));
		}
	}

	/**
	 * This method resets the settings to the default values.
	 */
	public void handleReset() {
		try {
			SettingsUtil.set("name", "");
			SettingsUtil.set("street", "");
			SettingsUtil.set("number", "");
			SettingsUtil.set("extra", "");
			SettingsUtil.set("zipcode", "");
			SettingsUtil.set("city", "");
			SettingsUtil.set("state", "");
			SettingsUtil.set("country", "");
			SettingsUtil.set("email", "");
			SettingsUtil.set("phone", "");
			Settings.setExtensionNoticeTime(30);
			Settings.setExtensionWarningTime(7);
			Settings.setICalAutoExport(true);
			Settings.setNamedICalExport(true);
			Settings.setICalFileLocation("");
			
			GUI.getStatusBar().setSuccessText(
					Settings.i18n().tr("Settings reset to default."));
		} catch (ApplicationException e) {
			GUI.getView().setErrorText(e.getMessage());

		} catch (RemoteException e) {
			Logger.error("Error while resetting settings", e);
			GUI.getStatusBar().setErrorText(
					Settings.i18n().tr("Error while resetting settings"));
		}
	}
}