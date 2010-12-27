package de.janrieke.contractmanager.gui.control;

import java.rmi.RemoteException;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.server.SettingsUtil;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
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
			Settings.setExtensionNoticeTime((Integer)getNoticeTime().getValue());
			Settings.setExtensionWarningTime((Integer)getWarningTime().getValue());
			GUI.getStatusBar().setSuccessText(
					Settings.i18n().tr("Settings saved."));
		} catch (ApplicationException e) {
			GUI.getView().setErrorText(e.getMessage());

		} catch (RemoteException e) {
			Logger.error("error while storing contract", e);
			GUI.getStatusBar().setErrorText(
					Settings.i18n().tr("Error while storing Contract"));
		}
	}
}