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

import java.rmi.RemoteException;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.boxes.Box;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class CancellationReminderBox extends AbstractView implements Box {

	private static final int HEIGHT = 140;

	@Override
	public void paint(Composite parent) throws RemoteException {
		ContractControl control = new ContractControl(this);
	    SimpleContainer container = new SimpleContainer(parent, true); //give the table a minimum height
	    GridData gd = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
	    gd.heightHint = HEIGHT;
	    container.getComposite().setLayoutData(gd);
		control.getContractsExtensionWarningTable().paint(container.getComposite());
	}

	@Override
	public void bind() throws Exception {
		paint(this.getParent());
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
		return HEIGHT;
	}
	
}
