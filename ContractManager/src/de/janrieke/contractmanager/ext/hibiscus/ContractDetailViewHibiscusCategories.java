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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.action.ShowHibiscusSettings;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.janrieke.contractmanager.gui.parts.SizeableTablePart;
import de.janrieke.contractmanager.gui.view.ContractDetailView;
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Transaction;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.parts.PanelButton;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.hbci.HBCIProperties;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.gui.input.UmsatzTypInput;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Erweitert die ContractDetails um die Zuordnung einer Umsatzkategorie aus
 * Hibiscus sowie der zugeordneten Verträge - jedoch nur, insofern Hibiscus installiert ist.
 */
public class ContractDetailViewHibiscusCategories implements Extension {

	private SizeableTablePart umsatzList;

	private final static I18N hibiscusI18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

	/**
	 * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
	 */
	@Override
	public void extend(Extendable extendable) {
		if (extendable == null || !(extendable instanceof ContractDetailView)) {
			return;
		}

		ContractDetailView view = (ContractDetailView) extendable;
		final ContractControl control = view.getControl();
		Composite parent = view.getParent();

		if (control == null || parent == null) {
			return;
		}

	    PanelButton settings = new PanelButton("document-properties.png", new ShowHibiscusSettings(), Settings.i18n().tr("Settings"));
	    GUI.getView().addPanelButton(settings);

		final Contract contract = (Contract) view.getCurrentObject();

		ArrayList<String> labels = new ArrayList<String>(3);
		ArrayList<Input> inputs  = new ArrayList<Input>(3);

		try
		{
			// 1) add SEPA fields
			if(Settings.getShowSEPACreditorInput()) {
		    	labels.add(Settings.i18n().tr("SEPA Creditor Reference"));
		    	inputs.add(control.getSEPACreditorReference());
			}
		    if(Settings.getShowSEPACustomerInput()) {
		    	labels.add(Settings.i18n().tr("SEPA Customer Reference"));
		    	inputs.add(control.getSEPACustomerReference());
		    }

			// 2) add a category selection input
			if (Settings.getShowHibiscusCategorySelector()) {
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
				labels.add(Settings.i18n().tr("Category"));
				inputs.add(input);

				view.addExtensionInput(Settings.i18n().tr("Hibiscus"), labels, inputs);
			}

			// 3) add a container that holds the list of assigned transactions
			if (Settings.getShowHibiscusTransactionList()) {
				List<Umsatz> umsaetze = new Vector<Umsatz>();
				DBIterator<Transaction> transactions = contract.getTransactions();
				while (transactions.hasNext()) {
					Transaction transaction = transactions.next();
					Umsatz umsatz = null;
					try
					{
						umsatz = (Umsatz)
								de.willuhn.jameica.hbci.Settings.getDBService().createObject(Umsatz.class,transaction.getTransactionID().toString());
					}
					catch (ObjectNotFoundException e)
					{
						transaction.delete();
					}
					if (umsatz != null) {
						umsaetze.add(umsatz);
					}
				}
				umsatzList = new SizeableTablePart(umsaetze, new UmsatzDetail());
				umsatzList.setHeightHint(Settings.getHibiscusTransactionListHeight());
				umsatzList.setContextMenu(new UmsatzListContextMenu(contract, this));
				umsatzList.addColumn("#","id-int");
				umsatzList.addColumn(hibiscusI18n.tr("Flags"),                     "flags");
				umsatzList.addColumn(hibiscusI18n.tr("Gegenkonto"),                "empfaenger");
				umsatzList.addColumn(hibiscusI18n.tr("Verwendungszweck"),          "mergedzweck");
				umsatzList.addColumn(hibiscusI18n.tr("Datum"),                     "datum_pseudo", new DateFormatter(HBCI.DATEFORMAT));
				umsatzList.addColumn(hibiscusI18n.tr("Betrag"),                    "betrag",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
				umsatzList.addColumn(hibiscusI18n.tr("Kategorie"),                 "umsatztyp",null,false);
				umsatzList.addColumn(hibiscusI18n.tr("Zwischensumme"),             "saldo",new CurrencyFormatter(HBCIProperties.CURRENCY_DEFAULT_DE,HBCI.DECIMALFORMAT));
				umsatzList.addColumn(hibiscusI18n.tr("Notiz"),                     "kommentar",null,true);
				umsatzList.setRememberColWidths(true);
				umsatzList.orderBy("!id-int");

				view.addExtensionContainer(umsatzList, Settings.i18n().tr("Assigned Hibiscus Transactions"));
			}
		}
		catch (Exception e)
		{
			Logger.error("unable to extend ContractDetailView",e);
		}
	}

	public void removeTransactionFromTable(Transaction tr) {
		Umsatz umsatz = null;
		try
		{
			umsatz = (Umsatz)
					de.willuhn.jameica.hbci.Settings.getDBService().createObject(Umsatz.class,tr.getTransactionID().toString());
		}
		catch (ObjectNotFoundException e)
		{
			// Der Umsatz wurde in Hibiscus zwischenzeitlich geloescht
			// TODO: Transaction löschen
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (umsatz != null) {
			umsatzList.removeItem(umsatz);
		}
	}
}