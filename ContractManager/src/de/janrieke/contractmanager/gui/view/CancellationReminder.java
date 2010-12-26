package de.janrieke.contractmanager.gui.view;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.boxes.Box;

public class CancellationReminder extends AbstractView implements Box {

	@Override
	public void paint(Composite parent) throws RemoteException {
		ContractControl control = new ContractControl(this);
		control.getContractsTable().paint(parent);
	}

	@Override
	public void bind() throws Exception {
		ContractControl control = new ContractControl(this);
		control.getContractsTable().paint(this.getParent());
	}

	@Override
	public String getName() {
		return Settings.i18n().tr("Contract Cancellation Reminder");
	}

	@Override
	public boolean getDefaultEnabled() {
		return true;
	}

	@Override
	public int getDefaultIndex() {
		return 1;
	}

	private static de.willuhn.jameica.system.Settings settings = new de.willuhn.jameica.system.Settings(Box.class);

	// Wir cachen den Index. Das erspart unnoetige Zugriffe auf die
	// properties-Dateien
	private Integer index = null;
	private Boolean enabled = null;

	/**
	 * @see de.willuhn.jameica.gui.boxes.Box#isEnabled()
	 */
	public boolean isEnabled() {
		if (this.enabled == null)
			this.enabled = Boolean.valueOf(settings.getBoolean(this.getClass()
					.getName() + ".enabled", getDefaultEnabled()));
		return this.enabled.booleanValue();
	}

	/**
	 * @see de.willuhn.jameica.gui.boxes.Box#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = Boolean.valueOf(enabled);
		settings.setAttribute(this.getClass().getName() + ".enabled", enabled);
	}

	/**
	 * @see de.willuhn.jameica.gui.boxes.Box#getIndex()
	 */
	public int getIndex() {
		if (this.index == null)
			this.index = new Integer(settings.getInt(this.getClass().getName()
					+ ".index", getDefaultIndex()));
		return this.index.intValue();
	}

	/**
	 * @see de.willuhn.jameica.gui.boxes.Box#setIndex(int)
	 */
	public void setIndex(int index) {
		this.index = new Integer(index);
		settings.setAttribute(this.getClass().getName() + ".index", index);
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		if (arg0 == null || !(arg0 instanceof Box))
			return 1;
		Box other = (Box) arg0;

		int index = getIndex();
		int oindex = other.getIndex();
		if (index == oindex)
			return 0;

		return index > oindex ? 1 : -1;
	}

	/**
	 * @see de.willuhn.jameica.gui.boxes.Box#isActive()
	 */
	public boolean isActive() {
		return true;
	}

	/**
	 * Default-Implementierung mit Hoehe -1.
	 * 
	 * @see de.willuhn.jameica.gui.boxes.Box#getHeight()
	 */
	public int getHeight() {
		return -1;
	}
}
