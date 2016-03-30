package de.janrieke.contractmanager.ext.hibiscus;

import java.util.List;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Transaction;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.extension.ExtensionRegistry;
import de.willuhn.jameica.hbci.messaging.ObjectChangedMessage;
import de.willuhn.jameica.hbci.rmi.Umsatz;
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
class UmsatzRemoveWorker implements BackgroundTask
{
	final static I18N i18n = Application.getPluginLoader().getPlugin(ContractManagerPlugin.class).getResources().getI18N();
	private boolean cancel = false;
	private Umsatz[] list = null;

	/**
	 * ct.
	 * @param list
	 * @param noUserInteraction do not show a dialog for transactions that are not automatically assignable
	 */
	UmsatzRemoveWorker(Umsatz[] list)
	{
		this.list = list;
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
		if (list.length == 0) {
			return;
		}

		// Before removing the assignment of all transactions, we show up a confirm dialog.

		YesNoDialog d = new YesNoDialog(YesNoDialog.POSITION_CENTER);
		d.setTitle(Settings.i18n().tr("Are you sure?"));
		d.setText(Settings.i18n().tr(
				"Do you really want to remove all selected transactions from their contracts?"));

		Boolean choice = false;
		try {
			choice = (Boolean) d.open();
		} catch (OperationCanceledException e) {
			return;
		} catch (Exception e) {
			throw new ApplicationException(Settings.i18n().tr(
					"Error while removing transaction assingment"), e);
		}
		if (!choice.booleanValue()) {
			return;
		}

		try
		{
			if (monitor != null) {
				monitor.setStatusText(UmsatzListMenuHibiscusExtension.i18n.tr("Removing {0} transactions from contracts", Integer.toString(list.length)));
			}

			double factor = 100d / list.length;

			int removed = 0;
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

			for (int i=0;i<list.length;++i)
			{
				if (monitor != null)
				{
					monitor.setPercentComplete((int)((i+1) * factor));
					monitor.log("  " + UmsatzListMenuHibiscusExtension.i18n.tr("Removing transaction {0}", Integer.toString(i+1)));
				}

				try
				{
					// Checken, ob der Umsatz einer Buchung zugeordnet ist
					DBService service = Settings.getDBService();
					DBIterator<Transaction> transactions = service.createList(Transaction.class);
					transactions.addFilter("transaction_id = ?",new Object[]{list[i].getID()});
					if (transactions.hasNext()) {
						Transaction transaction = transactions.next();
						// update the UmsatzList
						if (extension != null) {
							extension.remove(transaction);
						}
						transaction.delete();
						removed++;
					} else {
						skipped++;
					}

					// Notify other extensions about the update
					Application.getMessagingFactory().sendMessage(new ObjectChangedMessage(list[i]));
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

			String text = UmsatzListMenuHibiscusExtension.i18n.tr("Transaction assingment removal complete: {0} transactions removed, {1} erroneous, {2} skipped", new String[]{Integer.toString(removed),Integer.toString(error),Integer.toString(skipped)});

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
}