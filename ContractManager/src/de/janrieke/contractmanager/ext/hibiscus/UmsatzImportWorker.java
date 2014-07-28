package de.janrieke.contractmanager.ext.hibiscus;

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
	private Umsatz[] list = null;
	private boolean forceAuto;
	private boolean silent;

	/**
	 * ct.
	 * @param list
	 * @param umsatzListMenuHibiscusExtension TODO
	 */
	UmsatzImportWorker(Umsatz[] list, boolean forceAuto, boolean silent)
	{
		this.list = list;
		this.forceAuto = forceAuto;
		this.silent = silent;
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
				monitor.setStatusText(UmsatzListMenuHibiscusExtension.i18n.tr("Buche {0} Umsätze",""+list.length));

			double factor = 100d / (double) list.length;

			int created = 0;
			int error   = 0;
			int skipped = 0;

			for (int i=0;i<list.length;++i)
			{
				if (monitor != null)
				{
					monitor.setPercentComplete((int)((i+1) * factor));
					monitor.log("  " + UmsatzListMenuHibiscusExtension.i18n.tr("Assigning transaction {0}",Integer.toString(i+1)));
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

					transaction = createAssignedTransaction(list[i], forceAuto || list.length>1);
					if (transaction != null) {
						transaction.store();
						created++;
					}

					// Mit der Benachrichtigung wird dann gleich die Buchungsnummer in der Liste
					// angezeigt. Vorher muessen wir der anderen Extension aber noch die neue
					// Buchung mitteilen
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
					Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(list[i]));
				}
				catch (Exception e)
				{
					if (!silent)
						Logger.error("unable to import umsatz",e);
					if (monitor != null)
						monitor.log("    " + UmsatzListMenuHibiscusExtension.i18n.tr("Fehler: {0}",e.getMessage()));
					error++;
				}
			}

			String text = UmsatzListMenuHibiscusExtension.i18n.tr("Import to ContractManager complete: {0} transactions imported, {1} error, {2} already assigned", new String[]{Integer.toString(created),Integer.toString(error),Integer.toString(skipped)});

			if (!silent)
				Application.getMessagingFactory().sendMessage(new StatusBarMessage(text,StatusBarMessage.TYPE_SUCCESS));
			else
				Logger.info(text);

			if (monitor != null)
			{
				monitor.setStatusText(text);
				monitor.setStatus(ProgressMonitor.STATUS_DONE);
			}

		}
		catch (Exception e)
		{
			Logger.error("error while importing objects",e);
			throw new ApplicationException(UmsatzListMenuHibiscusExtension.i18n.tr("Fehler beim Import der Umsätze"));
		}
	}
	/**
	 * Assigns a single transaction to a contract.
	 * @param u the transaction that should be assigned.
	 * @param auto If there is more than one transaction, we run in auto mode, where 
	 * we use the category and the SEPA references to identify a contract.
	 * If this does not work, we cancel with an exception.
	 * @return the assigned contract.
	 * @throws Exception
	 */
	Transaction createAssignedTransaction(Umsatz u, boolean auto) throws Exception
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
		

		if (contract == null && auto)
			throw new ApplicationException(i18n.tr("Auto-import not possible: No category assigned or no matching SEPA references found."));
		
		if (!auto) {
			contract = new UmsatzImportListDialog(u, contract).open();
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