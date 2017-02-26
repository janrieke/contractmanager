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

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Transaction;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.CheckedContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.gui.action.UmsatzDetail;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Liefert ein vorgefertigtes Kontext-Menu, welches an Listen von Umsaetzen
 * angehaengt werden kann.
 */
public class UmsatzListContextMenu extends ContextMenu
{

	private final I18N i18n;
	private final Contract contract;

	/**
	 * Context menu for the table of hibiscus transactions that are assigned to a contract.
	 * @param extension
	 * @param konto optionale Angabe des Kontos.
	 */
	public UmsatzListContextMenu(Contract contr, ContractDetailViewHibiscusCategories extension)
	{
		contract = contr;
		final ContractDetailViewHibiscusCategories ext = extension;
		i18n = Settings.i18n();

		addItem(new UmsatzItem(i18n.tr("Unassign this transaction from contract"), context -> {
			if (context instanceof Umsatz) {
				try {
					DBIterator<Transaction> transactions = contract.getTransactions();
					transactions.addFilter("transaction_id = ?",new Object[]{((Umsatz)context).getID()});
					while (transactions.hasNext()) {
						Transaction tr = (transactions.next());
						ext.removeTransactionFromTable(tr);
						tr.delete();
					}
				} catch (RemoteException e) {
					Logger.error("Error while accessing transactions.", e);
				}
			}
		},"user-trash-full.png"));
		addItem(ContextMenuItem.SEPARATOR);
		addItem(new OpenItem());
	}

	/**
	 * Pruefen, ob es sich wirklich um einen Umsatz handelt.
	 */
	private class UmsatzItem extends CheckedContextMenuItem
	{
		/**
		 * ct.
		 * @param text Label.
		 * @param action Action.
		 * @param icon optionales Icon.
		 */
		public UmsatzItem(String text, Action action, String icon)
		{
			super(text,action,icon);
		}

		/**
		 * @see de.willuhn.jameica.gui.parts.CheckedContextMenuItem#isEnabledFor(java.lang.Object)
		 */
		@Override
		public boolean isEnabledFor(Object o)
		{
			try {
				// No unassigning of transactions for new contracts.
				if (contract.isNewObject()) {
					return false;
				}
			} catch (RemoteException e) {
				return false;
			}
			if ((o instanceof Umsatz) || (o instanceof Umsatz[])) {
				return super.isEnabledFor(o);
			}
			return false;
		}

	}

	/**
	 * Ueberschrieben, um zu pruefen, ob ein Array oder ein einzelnes Element markiert ist.
	 */
	private class OpenItem extends UmsatzItem
	{
		private OpenItem()
		{
			super(i18n.tr("Show in Hibiscus"),new UmsatzDetail(),"document-open.png");
		}
		/**
		 * @see de.willuhn.jameica.gui.parts.ContextMenuItem#isEnabledFor(java.lang.Object)
		 */
		@Override
		public boolean isEnabledFor(Object o)
		{
			if (o instanceof Umsatz) {
				return super.isEnabledFor(o);
			}
			return false;
		}
	}
}