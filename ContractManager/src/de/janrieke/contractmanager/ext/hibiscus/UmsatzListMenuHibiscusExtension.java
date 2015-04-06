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


import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Transaction;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Erweitert das Kontextmenu der Umsatzliste in Hibiscus.
 */
public class UmsatzListMenuHibiscusExtension implements Extension
{
	final static I18N i18n = Application.getPluginLoader().getPlugin(ContractManagerPlugin.class).getResources().getI18N();

	/**
	 * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
	 */
	public void extend(Extendable extendable)
	{
		if (extendable == null || !(extendable instanceof ContextMenu))
		{
			Logger.warn("invalid extendable, skipping extension");
			return;
		}

		ContextMenu menu = (ContextMenu) extendable;
		menu.addItem(ContextMenuItem.SEPARATOR);

		menu.addItem(new MyContextMenuItem(i18n.tr("Import to ContractManager"), new Action() {

			public void handleAction(Object context) throws ApplicationException
			{
				if (context == null)
					return;

				Umsatz[] umsaetze = null;
				if (context instanceof Umsatz)
					umsaetze = new Umsatz[]{(Umsatz)context};
				else if (context instanceof Umsatz[])
					umsaetze = (Umsatz[]) context;

				if (umsaetze == null || umsaetze.length == 0)
					return;

				// Wenn wir mehr als 1 Buchung haben, fuehren wir das
				// im Hintergrund aus. 
				UmsatzImportWorker worker = new UmsatzImportWorker(umsaetze, false);
				if (umsaetze.length > 1)
					Application.getController().start(worker);
				else
					worker.run(null);
			}
		}));
	}



	/**
	 * Helper class to deactivate the menu item if the transaction is already assigned.
	 */
	private class MyContextMenuItem extends CheckedContextMenuItem
	{
		/**
		 * ct.
		 * @param text
		 * @param a
		 */
		public MyContextMenuItem(String text, Action a)
		{
			super(text, a);
		}

		/**
		 * @see de.willuhn.jameica.gui.parts.CheckedContextMenuItem#isEnabledFor(java.lang.Object)
		 */
		public boolean isEnabledFor(Object o)
		{
			if (o == null)
				return false;

			// Wenn wir eine ganze Liste von Buchungen haben, pruefen
			// wir nicht jede einzeln, ob sie schon in ContractManager vorhanden
			// ist. Die werden dann beim Import (weiter unten) einfach ausgesiebt.
			if (o instanceof Umsatz[])
				return super.isEnabledFor(o);

			if (!(o instanceof Umsatz))
				return false;

			boolean found = false;
			try
			{
				found = isAssigned((Umsatz) o);
			}
			catch (ApplicationException ae)
			{
				Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(), StatusBarMessage.TYPE_ERROR));
			}
			catch (Exception e)
			{
				Logger.error("unable to detect if buchung is allready assigned",e);
				Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Error while checking for assigned contracts in ContractManager"), StatusBarMessage.TYPE_ERROR));
			}
			return !found && super.isEnabledFor(o);
		}

	}

	/**
	 * Prueft, ob der Umsatz bereits einem Vertrag zugeordnet ist.
	 * @param u der zu pruefende Umsatz.
	 * @return true, wenn es bereits einen Vertrag gibt.
	 * @throws Exception
	 */
	static boolean isAssigned(Umsatz u) throws Exception
	{
		DBService service = Settings.getDBService();
		DBIterator transactions = service.createList(Transaction.class);
		transactions.addFilter("transaction_id = ?",new Object[]{u.getID()});
		return transactions.hasNext();
	}
}