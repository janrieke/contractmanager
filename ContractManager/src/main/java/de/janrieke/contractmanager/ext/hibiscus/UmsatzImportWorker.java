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
	@Override
	public void interrupt()
	{
		this.cancel = true;
	}

	/**
	 * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
	 */
	@Override
	public boolean isInterrupted()
	{
		return this.cancel;
	}

	/**
	 * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
	 */
	@Override
	public void run(ProgressMonitor monitor) throws ApplicationException
	{
		try
		{
			if (monitor != null) {
				monitor.setStatusText(UmsatzListMenuHibiscusExtension.i18n.tr("Assigning {0} transaction to contracts",""+list.length));
			}

			double factor = 100d / list.length;

			int created = 0;
			int error   = 0;
			int skipped = 0;

			// Retrieve the extension to later remove the transactions from the table cell.
			List<Extension> extensions = ExtensionRegistry.getExtensions("de.willuhn.jameica.hbci.gui.parts.UmsatzList");
			UmsatzListPartHibiscusExtension extension = null;
			if (extensions != null)
			{
				for (Extension e:extensions)
				{
					if (e instanceof UmsatzListPartHibiscusExtension)
					{
						extension = (UmsatzListPartHibiscusExtension) e;
						break;
					}
				}
			}

			boolean applyForAllFurtherTransactions = false;
			Contract contract = null;

			for (int i=0;i<list.length;++i)
			{
				if (monitor != null)
				{
					monitor.setPercentComplete((int)((i+1) * factor));
					monitor.log("  " + UmsatzListMenuHibiscusExtension.i18n.tr("Assigning transaction {0}",Integer.toString(i+1)));
				}

				try
				{
					// Checken, ob der Umsatz schon einer Buchung zugeordnet ist
					if (TransactionUtils.isAssigned(list[i])) {
						skipped++;
						continue;
					}

					if (!applyForAllFurtherTransactions) {
						contract = findMatchingContract(list[i]);
					}

					if (!noUserInteraction && !applyForAllFurtherTransactions) {
						// In user-interaction mode, always show the import dialog.
						UmsatzImportListDialog umsatzImportListDialog = new UmsatzImportListDialog(list[i], contract, i<list.length-1);
						contract = umsatzImportListDialog.open();
						applyForAllFurtherTransactions = umsatzImportListDialog.isApplyForAll();
					}

					if (contract != null) {
						final Transaction transaction = (Transaction) Settings.getDBService().createObject(Transaction.class,null);
						transaction.setContract(contract);
						transaction.setTransactionID(Integer.parseInt(list[i].getID()));
						transaction.store();
						created++;
						if (monitor != null)
						{
							monitor.log("    " + UmsatzListMenuHibiscusExtension.i18n.tr("Successful."));
						}
						// update the UmsatzList
						extension.add(transaction);
					} else {
						if (monitor != null)
						{
							monitor.log("    " + UmsatzListMenuHibiscusExtension.i18n.tr("Assignment not possible."));
						}
					}

					// Notify other extensions about the update
					Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(list[i]));
				}
				catch (OperationCanceledException e) {
					if (monitor != null) {
						monitor.log("" + UmsatzListMenuHibiscusExtension.i18n.tr("Assignment cancelled by user."));
						monitor.setStatus(ProgressMonitor.STATUS_DONE);
					}
					return;
				}
				catch (Exception e)
				{
					Logger.error("unable to import umsatz", e);
					if (monitor != null) {
						monitor.log("    " + UmsatzListMenuHibiscusExtension.i18n.tr("Fehler: {0}",e.getMessage()));
					}
					error++;
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
	Contract findMatchingContract(Umsatz u) throws Exception
	{
		//1. Try to find SEPA references in transaction

		//No separators between lines until we have a working SEPA rewriter -
		// lines end after 27 chars, but fields may be 35 chars long.
		String trUse = VerwendungszweckUtil.toString(u, "");
		DBIterator<Contract> i = ContractControl.getContracts();
		while (i.hasNext()) {
			Contract c = i.next();
			if (c.getSepaCreditorRef() != null && !"".equals(c.getSepaCreditorRef())) {
				if (c.getSepaCustomerRef() != null && !"".equals(c.getSepaCustomerRef())) {
					//Both SEPA references set -> both must be contained in the use string
					if (trUse.toString().contains(c.getSepaCreditorRef()) && trUse.toString().contains(c.getSepaCustomerRef())) {
						return c;
					}
				} else {
					//Only SEPA creditor reference set -> only require this to be contained
					if (trUse.toString().contains(c.getSepaCreditorRef())) {
						return c;
					}
				}
			} else {
				if (c.getSepaCustomerRef() != null && !"".equals(c.getSepaCustomerRef())) {
					//Only SEPA customer reference set -> only require this to be contained
					if (trUse.toString().contains(c.getSepaCustomerRef())) {
						return c;
					}
				} // else { // both SEPA references not set -> nothing to do }
			}
		}

		//2. Try to find contract by Hibiscus category
		UmsatzTyp typ = u.getUmsatzTyp();
		if (typ != null)
		{
			i = ContractControl.getContracts();
			i.addFilter("hibiscus_category = ?", new Object[]{typ.getID()});
			if (i.size() == 1) {
				return i.next();
			}
		}

		return null;
	}
}