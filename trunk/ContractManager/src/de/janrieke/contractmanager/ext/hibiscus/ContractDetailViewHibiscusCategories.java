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

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.janrieke.contractmanager.gui.view.ContractDetailView;
import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.input.UmsatzTypInput;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Erweitert die ContractDetails um die Zuordnung einer Umsatzkategorie aus
 * Hibiscus - jedoch nur, insofern Hibiscus installiert ist.
 */
public class ContractDetailViewHibiscusCategories implements Extension {

	private TablePart umsatzList;
	
	private final static I18N hibiscusI18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

	/**
	 * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
	 */
	public void extend(Extendable extendable) {
		if (extendable == null || !(extendable instanceof ContractDetailView))
			return;

		ContractDetailView view = (ContractDetailView) extendable;
		final ContractControl control = view.getControl();
		Composite parent = view.getParent();

		if (control == null || parent == null)
			return;

		final Contract contract = (Contract) view.getCurrentObject();

		try
		{
			// 1) add a category selection input
			
			// Zugeordnete Kategorie ermitteln
			UmsatzTyp typ = null;
			String id = contract.getHibiscusCategoryID();
			if (id != null)
			{
				try
				{
					typ = (UmsatzTyp)
							de.willuhn.jameica.hbci.Settings.getDBService().createObject(UmsatzTyp.class,id);
				}
				catch (ObjectNotFoundException e)
				{
					// Die Kategorie wurde in Hibiscus zwischenzeitlich geloescht, ignorieren wir
				}
			}

			// Auswahlfeld hinzufuegen
			final UmsatzTypInput input = new UmsatzTypInput(typ,UmsatzTyp.TYP_EGAL);

			input.addListener(new Listener() {
				@Override
				public void handleEvent(org.eclipse.swt.widgets.Event event) {
					try
					{
						UmsatzTyp t = (UmsatzTyp) input.getValue();
						//do not immediately write to DB, let the controller do 
						// this when storing everything else
						control.hibiscusCategoryID = (t != null ? t.getID() : null);
					}
					catch (RemoteException e)
					{
						Logger.error("unable to apply hibiscus category",e);
					}
				}
			});

			view.addExtensionInput("Hibiscus", Settings.i18n().tr("Category"), input);
			
			// 2) add a container that holds the list of assigned transactions
			DBIterator transactions = contract.getTransactions();
			umsatzList = new TablePart(transactions, new UmsatzDetail());
			umsatzList.setContextMenu(new UmsatzListContextMenu(contract));
			umsatzList.addColumn("#","id-int");
			umsatzList.addColumn(hibiscusI18n.tr("Flags"),                     "flags");
			umsatzList.addColumn(hibiscusI18n.tr("Gegenkonto"),                "empfaenger");
			umsatzList.addColumn(hibiscusI18n.tr("Verwendungszweck"),          "mergedzweck");
			umsatzList.addColumn(hibiscusI18n.tr("Datum"),                     "datum_pseudo", new DateFormatter(HBCI.DATEFORMAT));
			umsatzList.addColumn(hibiscusI18n.tr("Betrag"),                    "betrag",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
			umsatzList.addColumn(hibiscusI18n.tr("Kategorie"),                 "umsatztyp",null,false);
			umsatzList.addColumn(hibiscusI18n.tr("Zwischensumme"),             "saldo",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
			umsatzList.addColumn(hibiscusI18n.tr("Notiz"),                     "kommentar",null,true);
			view.addExtensionContainer(umsatzList);
		}
		catch (Exception e)
		{
			Logger.error("unable to extend ContractDetailView",e);
		}
	}
}