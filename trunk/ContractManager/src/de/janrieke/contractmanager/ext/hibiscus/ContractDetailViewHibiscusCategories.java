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

/*
 * Partially copied from Hibiscus/Syntax, (c) by willuhn.webdesign
 */
package de.janrieke.contractmanager.ext.hibiscus;

import org.eclipse.swt.widgets.Composite;

import de.janrieke.contractmanager.gui.control.ContractControl;
import de.janrieke.contractmanager.gui.view.ContractDetailView;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.input.SelectInput;

/**
 * Erweitert die ContractDetails um die Zuordnung einer Umsatzkategorie aus
 * Hibiscus - jedoch nur, insofern Hibiscus installiert ist.
 */
public class ContractDetailViewHibiscusCategories implements Extension {

	/**
	 * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
	 */
	public void extend(Extendable extendable) {
		if (extendable == null || !(extendable instanceof ContractDetailView))
			return;

		ContractDetailView view = (ContractDetailView) extendable;
		ContractControl control = view.getControl();
		Composite parent = view.getParent();

		if (control == null || parent == null)
			return;

		//Contract contract = (Contract) view.getCurrentObject();

		// Auswahlfeld hinzufuegen
		final SelectInput input = new SelectInput(new String[]{"a","b"}, null);
		view.addInput(input);

		// try
		// {
		// ContractDetailView view = (ContractDetailView) extendable;
		//
		// Composite container = view.getParent();
		// ContractControl control = view.getControl();
		//
		// if (container == null || control == null)
		// return; // die View wird offensichtlich nicht mehr angezeigt
		//
		// final de.willuhn.jameica.fibu.rmi.Buchungstemplate template =
		// control.getBuchung();
		//
		// // Zugeordnete Kategorie ermitteln
		// UmsatzTyp typ = null;
		// String id = template.getHibiscusUmsatzTypID();
		// if (id != null)
		// {
		// try
		// {
		// typ = (UmsatzTyp)
		// Settings.getDBService().createObject(UmsatzTyp.class,id);
		// }
		// catch (ObjectNotFoundException e)
		// {
		// // Die Kategorie wurde in Hibiscus zwischenzeitlich geloescht,
		// ignorieren wir
		// }
		// }
		//
		// // Auswahlfeld hinzufuegen
		// final UmsatzTypInput input = new
		// UmsatzTypInput(typ,UmsatzTyp.TYP_EGAL);
		// input.addListener(new Listener() {
		// public void handleEvent(Event event)
		// {
		// try
		// {
		// UmsatzTyp t = (UmsatzTyp) input.getValue();
		// template.setHibiscusUmsatzTypID(t != null ? t.getID() : null);
		// }
		// catch (Exception e)
		// {
		// Logger.error("unable to apply hibiscus category",e);
		// }
		// }
		// });
		// container.addInput(input);
		//
		// // Ein Abfangen des Events, wenn der User auf den "Speichern"-Button
		// // klickt, ist nicht notwendig, da wir die Aenderung via Listener
		// // am Input-Feld sofort uebernehmen.
		// }
		// catch (Exception e)
		// {
		// Logger.error("unable to extend buchungstemplate",e);
		// }
	}
}