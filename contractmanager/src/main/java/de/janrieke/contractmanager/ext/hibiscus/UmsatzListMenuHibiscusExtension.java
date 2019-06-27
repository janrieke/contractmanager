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
import java.util.Arrays;
import java.util.List;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.rmi.Umsatz;
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
	@Override
	public void extend(Extendable extendable)
	{
		if (extendable == null || !(extendable instanceof ContextMenu))
		{
			Logger.warn("invalid extendable, skipping extension");
			return;
		}

		ContextMenu menu = (ContextMenu) extendable;
		menu.addItem(ContextMenuItem.SEPARATOR);

		menu.addItem(new ActiveWhenAssignedContextMenuItem(i18n.tr("Open contract"),
				this::openContract, true, false));

		menu.addItem(new ActiveWhenAssignedContextMenuItem(i18n.tr("Import to ContractManager"),
				this::performImport, false, true));

		menu.addItem(new ActiveWhenAssignedContextMenuItem(i18n.tr("Remove from ContractManager"),
				this::performRemoval, true, true));
	}

	private void openContract(Object context) throws ApplicationException {
		List<Umsatz> transactions = getTransactions(context);

		if (transactions.isEmpty()) {
			return;
		}

		try {
			TransactionUtils.getContractFor(transactions.get(0)).ifPresent(contract -> {
				GUI.startView(de.janrieke.contractmanager.gui.view.ContractDetailView.class
						.getName(), contract);
			});
		} catch (RemoteException e) {
			Logger.error("Error while accessing transactions.", e);
		}
	}

	private void performImport(Object context) throws ApplicationException {
		List<Umsatz> transactions = getTransactions(context);

		if (transactions.isEmpty()) {
			return;
		}

		// Wenn wir mehr als 1 Buchung haben, fuehren wir das
		// im Hintergrund aus.
		UmsatzImportWorker worker = new UmsatzImportWorker(transactions.toArray(new Umsatz[transactions.size()]), false);
		if (transactions.size() > 1) {
			Application.getController().start(worker);
		} else {
			worker.run(null);
		}
	}

	private void performRemoval(Object context) throws ApplicationException {
		List<Umsatz> transactions = getTransactions(context);

		if (transactions.isEmpty()) {
			return;
		}

		// Wenn wir mehr als 1 Buchung haben, fuehren wir das
		// im Hintergrund aus.
		UmsatzRemoveWorker worker = new UmsatzRemoveWorker(transactions.toArray(new Umsatz[transactions.size()]));
		if (transactions.size() > 1) {
			Application.getController().start(worker);
		} else {
			worker.run(null);
		}
	}

	private List<Umsatz> getTransactions(Object context) {
		List<Umsatz> result = new ArrayList<>();

		if (context == null) {
			return result;
		}

		if (context instanceof Umsatz) {
			result.add((Umsatz) context);
		} else if (context instanceof Umsatz[]) {
			return Arrays.asList((Umsatz[]) context);
		} else if (context instanceof List) {
			((List<?>)context).forEach(elem -> {
				if (elem instanceof Umsatz) {
					result.add((Umsatz) elem);
				}
			});
		}
		return result;
	}
}