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
import java.util.HashMap;
import java.util.Map;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Transaction;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Erweitert die Liste der Umsaetze in Hibiscus um eine Spalte.
 */
public class UmsatzListPartHibiscusExtension implements Extension {
	private final static I18N i18n = Settings.i18n();
	private Map<Integer, Transaction> cache = null;

	/**
	 * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
	 */
	@Override
	public void extend(Extendable extendable) {
		if (extendable == null || !(extendable instanceof TablePart)) {
			Logger.warn("invalid extendable, skipping extension");
			return;
		}

		this.cache = null; // Cache loeschen, damit die Daten neu gelesen werden

		TablePart table = (TablePart) extendable;
		table.addColumn(i18n.tr("Vertrag"), "id-int", new Formatter() {
			@Override
			public String format(Object o) {
				if (o == null || !(o instanceof Integer)) {
					return null;
				}

				DBIterator<Transaction> transactions = null;
				try {
					transactions = Settings.getDBService().createList(
							Transaction.class);
					transactions.addFilter("transaction_id = ?",
							new Object[] { o });
					if (transactions.size() > 1) {
						Logger.warn("Umsatz assigned to more than one contract. Possible DB error.");
					}
					if (transactions.hasNext()) {
						Transaction transaction = transactions
								.next();
						if (transaction.getContract() != null) {
							return transaction.getContract().getName();
						} else {
							try {
								transaction.delete();
							} catch (ApplicationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				} catch (RemoteException e) {
					Logger.error("unable to load transactions", e);
				}

				return null;
			}

		});
	}

	/**
	 * Liefert den Cache zum Lookup von Hibiscus Umsatz-ID zu Buchung.
	 *
	 * @return der Cache.
	 */
	private Map<Integer, Transaction> getCache() {
		if (this.cache != null) {
			return this.cache;
		}

		this.cache = new HashMap<Integer, Transaction>();
		try {
			DBIterator<Transaction> list = Settings.getDBService().createList(
					Transaction.class);
			while (list.hasNext()) {
				Transaction b = list.next();
				if (b.getTransactionID() == null) {
					continue;
				}
				cache.put(b.getTransactionID(), b);
			}
		} catch (Exception e) {
			Logger.error("unable to fill lookup cache", e);
		}
		return this.cache;
	}

	/**
	 * Fuegt eine Buchung manuell zum Cache hinzu.
	 *
	 * @param b
	 *            die neue Buchung.
	 * @throws RemoteException
	 */
	void add(Transaction b) throws RemoteException {
		if (b == null) {
			return;
		}

		Integer umsatzId = b.getTransactionID();
		if (umsatzId == null || umsatzId == 0)
		 {
			return; // Buchung ist gar nicht zugeordnet
		}
		getCache().put(umsatzId, b);
	}

	/**
	 * Entfernt eine Buchung manuell aus dem Cache.
	 *
	 * @param b
	 *            die neue Buchung.
	 * @throws RemoteException
	 */
	void remove(Transaction b) throws RemoteException {
		if (b == null) {
			return;
		}

		Integer umsatzId = b.getTransactionID();
		if (umsatzId == null || umsatzId == 0)
		 {
			return; // Buchung ist gar nicht zugeordnet
		}
		getCache().remove(umsatzId);
	}
}