/*
 *   This file is part of ContractManager for Jameica.
 *   Copyright (C) 2010-2014  Jan Rieke
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
/*
 * Copy from Hibiscus
 * Copyright (c) by willuhn software & services
 * All rights reserved
 */

package de.janrieke.contractmanager.gui.dialog;

import org.eclipse.swt.widgets.Composite;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.rmi.ContractDBService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Ein Dialog, der Debug-Informationen - unter anderem ueber die Datenbank
 * anzeigt.
 */
public class DebugDialog extends AbstractDialog<Object> {
	private final static int WINDOW_WIDTH = 550;
	private final static int WINDOW_HEIGHT = 200;
	private final static I18N i18n = Application.getPluginLoader()
			.getPlugin(ContractManagerPlugin.class).getResources().getI18N();

	/**
	 * ct
	 * 
	 * @param position
	 */
	public DebugDialog(int position) {
		super(position);
		this.setTitle(i18n.tr("Database Information"));
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
	 */
	protected Object getData() throws Exception {
		return null;
	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
	 */
	protected void paint(Composite parent) throws Exception {
		ContractDBService service = (ContractDBService) Application
				.getServiceFactory().lookup(ContractManagerPlugin.class,
						"contract_db");
		de.janrieke.contractmanager.rmi.DBSupport driver = service.getDriver();

		StringBuffer sb = new StringBuffer();
		sb.append(i18n.tr("JDBC Driver: {0}\n", driver.getJdbcDriver()));
		sb.append(i18n.tr("JDBC URL: {0}\n", driver.getJdbcUrl()));
		sb.append(i18n.tr("JDBC Username: {0}\n", driver.getJdbcUsername()));
		sb.append(i18n.tr("JDBC Password: {0}\n", driver.getJdbcPassword()));

		Container container = new SimpleContainer(parent);
		container.addHeadline(i18n.tr("Database Settings"));
		TextAreaInput text = new TextAreaInput(sb.toString());
		text.setHeight(90);
		container.addPart(text);

		ButtonArea buttons = new ButtonArea();
		buttons.addButton(i18n.tr("Close"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
				close();
			}
		}, null, true, "window-close.png");
		container.addButtonArea(buttons);

		getShell().setMinimumSize(
				getShell().computeSize(WINDOW_WIDTH, WINDOW_HEIGHT));
	}

}

/*********************************************************************
 * $Log: DebugDialog.java,v $ Revision 1.2 2011/03/30 08:19:37 willuhn
 * 
 * @C Code-Cleanup
 * 
 *    Revision 1.1 2008/05/06 10:10:56 willuhn
 * @N Diagnose-Dialog, mit dem man die JDBC-Verbindungsdaten (u.a. auch das
 *    JDBC-Passwort) ausgeben kann
 * 
 **********************************************************************/
