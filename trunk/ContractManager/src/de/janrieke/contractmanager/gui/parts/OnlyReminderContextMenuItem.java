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
package de.janrieke.contractmanager.gui.parts;

import java.rmi.RemoteException;

import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.ContextMenuItem;

/**
 * ContextMenu-Element, dass nur für Verträge aktiv ist, für die gerade eine
 * Erinnerung angezeigt wird.
 */
public class OnlyReminderContextMenuItem extends ContextMenuItem {

	/**
	 * ct.
	 */
	public OnlyReminderContextMenuItem() {
		super();
	}

	/**
	 * ct.
	 * 
	 * @param text
	 *            anzuzeigender Text.
	 * @param a
	 *            Action, die beim Klick ausgeloest werden soll.
	 */
	public OnlyReminderContextMenuItem(String text, Action a) {
		super(text, a);
	}

	/**
	 * ct.
	 * 
	 * @param text
	 *            anzuzeigender Text.
	 * @param a
	 *            Action, die beim Klick ausgeloest werden soll.
	 * @param icon
	 *            optionales Icon.
	 */
	public OnlyReminderContextMenuItem(String text, Action a, String icon) {
		super(text, a, icon);
	}

	/**
	 * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
	 */
	public boolean isEnabledFor(Object o) {
		try {
			return (o != null && o instanceof Contract &&
					(((Contract) o).isNextDeadlineWithinNoticeTime() ||
					((Contract) o).isNextDeadlineWithinWarningTime()));
		} catch (RemoteException e) {
			return false;
		}
	}

}

/**********************************************************************
 * $Log: CheckedContextMenuItem.java,v $ Revision 1.3 2008/12/19 01:12:06
 * willuhn
 * 
 * @N Icons in Contextmenus
 * 
 *    Revision 1.2 2004/10/18 23:37:42 willuhn *** empty log message ***
 * 
 *    Revision 1.1 2004/07/20 21:47:44 willuhn
 * @N ContextMenu
 * 
 **********************************************************************/
