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
import de.janrieke.contractmanager.gui.action.NewTransaction;
import de.janrieke.contractmanager.rmi.Contract;
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
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Erweitert das Kontextmenu der Umsatzliste.
 */
public class UmsatzListMenu implements Extension
{
	private final static I18N i18n = Application.getPluginLoader().getPlugin(ContractManagerPlugin.class).getResources().getI18N();

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

		menu.addItem(new MyContextMenuItem(i18n.tr("in ContractManager übernehmen..."), new Action() {

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
				Worker worker = new Worker(umsaetze);
				if (umsaetze.length > 1)
					Application.getController().start(worker);
				else
					worker.run(null);
			}
		}));
	}

	/**
	 * Erzeugt eine einzelne Buchung.
	 * Sie wird jedoch noch nicht gespeichert.
	 * @param u die zu erzeugende Buchung.
	 * @param auto true, wenn wir mehr als eine Buchung haben und im Automatik-Modus laufen.
	 * In dem Fall wird die Erstellung der Buchung mit einer ApplicationException
	 * abgebrochen, wenn keine Umsatz-Kategorie vorhanden ist oder dieser keine
	 * Buchungsvorlage zugeordnet ist.
	 * "Keine Buchungsvorlage zugeordnet" geworfen.
	 * @return die erzeugte Buchung.
	 * @throws Exception
	 */
	private Transaction createAssignedTransaction(Umsatz u, boolean auto) throws Exception
	{
		// Checken, ob der Umsatz eine Kategorie hat
		UmsatzTyp typ = u.getUmsatzTyp();

		if (typ == null && auto)
			throw new ApplicationException(i18n.tr("Keine Kategorie zugeordnet"));

		Contract contract = null;
		if (typ != null)
		{
			// Contract suchen
			DBIterator i = Settings.getDBService().createList(Contract.class);
			i.addFilter("hibiscus_category = ?", new Object[]{typ.getID()});
			if (i.hasNext())
				contract = (Contract) i.next();
		}

		if (contract == null && auto)
			throw new ApplicationException(i18n.tr("Kein zugeordneter Vertrag ermittelbar"));

		final Transaction transaction = (Transaction) Settings.getDBService().createObject(Transaction.class,null);
		transaction.setContract(contract);
		transaction.setTransactionID(Integer.parseInt(u.getID()));

		return transaction;
	}

	/**
	 * Hilfsklasse, um den Menupunkt zu deaktivieren, wenn die Buchung bereits zugeordnet ist.
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
				Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Prüfen, ob die Buchung bereits in SynTAX zugeordnet ist"), StatusBarMessage.TYPE_ERROR));
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
	private boolean isAssigned(Umsatz u) throws Exception
	{
		DBService service = Settings.getDBService();
		DBIterator transactions = service.createList(Transaction.class);
		transactions.addFilter("transaction_id = ?",new Object[]{u.getID()});
		return transactions.hasNext();
	}


	/**
	 * Damit koennen wir lange Vorgaenge ggf. im Hintergrund laufen lassen
	 */
	private class Worker implements BackgroundTask
	{
		private boolean cancel = false;
		private Umsatz[] list = null;

		/**
		 * ct.
		 * @param list
		 */
		private Worker(Umsatz[] list)
		{
			this.list = list;
		}

		/**
		 * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
		 */
		public void interrupt()
		{
			this.cancel = true;
		}

		/**
		 * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
		 */
		public boolean isInterrupted()
		{
			return this.cancel;
		}

		/**
		 * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
		 */
		public void run(ProgressMonitor monitor) throws ApplicationException
		{
			try
			{
				if (monitor != null)
					monitor.setStatusText(i18n.tr("Buche {0} Umsätze",""+list.length));

				double factor = 100d / (double) list.length;

				int created = 0;
				int error   = 0;
				int skipped = 0;

				for (int i=0;i<list.length;++i)
				{
					if (monitor != null)
					{
						monitor.setPercentComplete((int)((i+1) * factor));
						monitor.log("  " + i18n.tr("Erstelle Buchung {0}",Integer.toString(i+1)));
					}

					Transaction transaction = null;
					try
					{
						// Checken, ob der Umsatz schon einer Buchung zugeordnet ist
						if (isAssigned(list[i]))
						{
							skipped++;
							continue;
						}

						transaction = createAssignedTransaction(list[i], list.length>1);
						transaction.store();
						created++;

						// Mit der Benachrichtigung wird dann gleich die Buchungsnummer in der Liste
						// angezeigt. Vorher muessen wir der anderen Extension aber noch die neue
						// Buchung mitteilen
						//						List<Extension> extensions = ExtensionRegistry.getExtensions("de.willuhn.jameica.hbci.gui.parts.UmsatzList");
						//						if (extensions != null)
						//						{
						//							for (Extension e:extensions)
						//							{
						//								if (e instanceof UmsatzListPart)
						//								{
						//									((UmsatzListPart)e).add(transaction);
						//									break;
						//								}
						//							}
						//						}
						//						Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(list[i]));
					}
					catch (ApplicationException ae)
					{
						// Wenn wir nur eine Buchung hatten und eine
						// ApplicationException, dann fehlen noch Eingaben
						// Da wir nur eine Buchung haben, oeffnen wir
						// die Erfassungsmaske.
						if (list.length == 1)
						{
							Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
							new NewTransaction().handleAction(transaction);
							return;
						}

						if (monitor != null)
							monitor.log("    " + ae.getMessage());
						error++;
					}
					catch (Exception e)
					{
						Logger.error("unable to import umsatz",e);
						if (monitor != null)
							monitor.log("    " + i18n.tr("Fehler: {0}",e.getMessage()));
						error++;
					}
				}

				String text = i18n.tr("Umsatz importiert");
				if (list.length > 1)
					text = i18n.tr("{0} Umsätze importiert, {1} fehlerhaft, {2} bereits vorhanden", new String[]{Integer.toString(created),Integer.toString(error),Integer.toString(skipped)});

				Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_SUCCESS));
				if (monitor != null)
				{
					monitor.setStatusText(text);
					monitor.setStatus(ProgressMonitor.STATUS_DONE);
				}

			}
			catch (Exception e)
			{
				Logger.error("error while importing objects",e);
				throw new ApplicationException(i18n.tr("Fehler beim Import der Umsätze"));
			}
		}
	}
}