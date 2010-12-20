package de.janrieke.contractmanager.gui.control;

import java.rmi.RemoteException;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.server.SettingsUtil;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author willuhn, jrieke
 */
public class SettingsControl extends AbstractControl {

	// Input fields for the attributes
	private Input name;
	private Input street;
	private IntegerInput number;
	private Input extra;
	private IntegerInput zipcode;
	private Input city;
	private Input state;
	private Input country;

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

	/**
	 * This method stores the contract using the current values.
	 */
	public void handleStore() {
		try {
			SettingsUtil.set("name", (String) getName().getValue());
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