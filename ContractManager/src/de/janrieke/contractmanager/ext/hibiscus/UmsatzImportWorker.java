package de.janrieke.contractmanager.ext.hibiscus;

import java.util.ArrayList;
import java.util.List;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Transaction;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.rmi.UmsatzTyp;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Worker for importing Hibiscus transactions
 */
class UmsatzImportWorker implements BackgroundTask
{
	final static I18N i18n = Application.getPluginLoader().getPlugin(ContractManagerPlugin.class).getResources().getI18N();
	private boolean cancel = false;
	private boolean noUserInteraction = false;
	private Umsatz[] list = null;

	/**
	 * ct.
	 * @param list
	 * @param noUserInteraction do not show a dialog for transactions that are not automatically assignable
	 */
	UmsatzImportWorker(Umsatz[] list, boolean noUserInteraction)
	{
		this.list = list;
		this.noUserInteraction = noUserInteraction;
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
				monitor.setStatusText(UmsatzListMenuHibiscusExtension.i18n.tr("Assigning {0} transaction to contracts",""+list.length));

			double factor = 100d / (double) list.length;

			int created = 0;
			int error   = 0;
			int skipped = 0;

			List<Umsatz> transactionsForDialog = new ArrayList<>();

			for (int i=0;i<list.length;++i)
			{
				if (monitor != null)
				{
					monitor.setPercentComplete((int)((i+1) * factor));
					monitor.log("  " + UmsatzListMenuHibiscusExtension.i18n.tr("Trying to auto-assign transaction {0}",Integer.toString(i+1)));
				}

				Transaction transaction = null;
				try
				{
					// Checken, ob der Umsatz schon einer Buchung zugeordnet ist
					if (UmsatzListMenuHibiscusExtension.isAssigned(list[i]))
					{
						skipped++;
						continue;
					}

					transaction = createAssignedTransaction(list[i]);
					if (transaction != null) {
						transaction.store();
						created++;
						if (monitor != null)
						{
							monitor.log("    " + UmsatzListMenuHibiscusExtension.i18n.tr("Successful."));
						}
					} else {
						if (monitor != null)
						{
							monitor.log("    " + UmsatzListMenuHibiscusExtension.i18n.tr("Auto-asign not possible."));
						}
						transactionsForDialog.add(list[i]);
					}

					// update the UmsatzList 
					List<Extension> extensions = ExtensionRegistry.getExtensions("de.willuhn.jameica.hbci.gui.parts.UmsatzList");
					if (extensions != null)
					{
						for (Extension e:extensions)
						{
							if (e instanceof UmsatzListPartHibiscusExtension)
							{
								((UmsatzListPartHibiscusExtension)e).add(transaction);
								break;
							}
						}
					}
					// Notify other extensions about the update
					Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(list[i]));
				}
				catch (Exception e)
				{
					Logger.error("unable to import umsatz", e);
					if (monitor != null)
						monitor.log("    " + UmsatzListMenuHibiscusExtension.i18n.tr("Fehler: {0}",e.getMessage()));
					error++;
				}
			}

			if (!noUserInteraction && !transactionsForDialog.isEmpty()) {
				if (monitor != null)
				{
					monitor.log("  " + UmsatzListMenuHibiscusExtension.i18n.tr("Interactivly assigning {0} transactions.", Integer.toString(transactionsForDialog.size())));
				}
				for (Umsatz u : transactionsForDialog) {
					try {
						Contract contract = new UmsatzImportListDialog(u, null).open();
						if (contract != null) {
							final Transaction transaction = (Transaction) Settings.getDBService().createObject(Transaction.class,null);
							transaction.setContract(contract);
							transaction.setTransactionID(Integer.parseInt(u.getID()));
							transaction.store();
							created++;
						}
					} catch (OperationCanceledException e) {
						break;
					}
				}
			}
			
			String text = UmsatzListMenuHibiscusExtension.i18n.tr("Import to ContractManager complete: {0} transactions imported, {1} erroneous, {2} skipped", new String[]{Integer.toString(created),Integer.toString(error),Integer.toString(skipped)});

			Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_SUCCESS));

			if (monitor != null)
			{
				monitor.setStatusText(text);
				monitor.setStatus(ProgressMonitor.STATUS_DONE);
			}

		}
		catch (Exception e)
		{
			Logger.error("error while importing objects", e);
			throw new ApplicationException(UmsatzListMenuHibiscusExtension.i18n.tr("Fehler beim Import der Umsätze"));
		}
	}
	/**
	 * Auto-assigns a single transaction to a contract. 
	 * It uses the category and the SEPA fields to find matching contracts.
	 * If no matching contract can be found, we return null. 
	 * @param u the transaction that should be assigned.
	 * @return the assigned contract.
	 * @throws Exception
	 */
	Transaction createAssignedTransaction(Umsatz u) throws Exception
	{
		Contract contract = null;
		//TODO: also perform auto import if SEPA references match

		//1. Try to find contract by Hibiscus category
		UmsatzTyp typ = u.getUmsatzTyp();
		if (typ != null)
		{
			DBIterator i = ContractControl.getContracts();
			i.addFilter("hibiscus_category = ?", new Object[]{typ.getID()});
			if (i.size() == 1)
				contract = (Contract) i.next();
		}
		
		
		//2. Try to find SEPA references in transaction
		
		//No separators between lines until we have a working SEPA rewriter - 
		// lines end after 27 chars, but fields may be 35 chars long.
		if (contract == null) {
			String trUse = VerwendungszweckUtil.toString(u, "");
			DBIterator i = ContractControl.getContracts();
			while (contract == null && i.hasNext()) {
				Contract c = (Contract) i.next();
				if (c.getSepaCreditorRef() != null && !"".equals(c.getSepaCreditorRef())) {
					if (c.getSepaCustomerRef() != null && !"".equals(c.getSepaCustomerRef())) {
						//Both SEPA references set -> both must be contained in the use string 
						if (trUse.toString().contains(c.getSepaCreditorRef()) && trUse.toString().contains(c.getSepaCustomerRef()))
							contract = c;
					} else {
						//Only SEPA creditor reference set -> only require this to be contained 
						if (trUse.toString().contains(c.getSepaCreditorRef()))
							contract = c;
					}
				} else {
					if (c.getSepaCustomerRef() != null && !"".equals(c.getSepaCustomerRef())) {
						//Only SEPA customer reference set -> only require this to be contained 
						if (trUse.toString().contains(c.getSepaCustomerRef()))
							contract = c;
					} // else { // both SEPA references not set -> nothing to do }
				}
			}
		}

		if (contract != null) {
			final Transaction transaction = (Transaction) Settings.getDBService().createObject(Transaction.class,null);
			transaction.setContract(contract);
			transaction.setTransactionID(Integer.parseInt(u.getID()));
			return transaction;
		}
		else 
			return null;
	}
}