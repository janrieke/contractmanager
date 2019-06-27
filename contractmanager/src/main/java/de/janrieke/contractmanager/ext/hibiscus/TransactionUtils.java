package de.janrieke.contractmanager.ext.hibiscus;

import java.rmi.RemoteException;
import java.util.Optional;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Transaction;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.hbci.rmi.Umsatz;

public class TransactionUtils {
	private TransactionUtils() {
		throw new AssertionError("no instantiation");
	}

	/**
	 * Prueft, ob der Umsatz bereits einem Vertrag zugeordnet ist.
	 * @param u der zu pruefende Umsatz.
	 * @return true, wenn es bereits einen Vertrag gibt.
	 * @throws Exception
	 */
	public static boolean isAssigned(Umsatz u) throws RemoteException {
		return getTransactionsFor(u).hasNext();
	}

	public static Optional<Contract> getContractFor(Umsatz u) throws RemoteException {
		DBIterator<Transaction> transactions = getTransactionsFor(u);
		if (transactions.hasNext()) {
			return Optional.of(transactions.next().getContract());
		}
		return Optional.empty();
	}

	private static DBIterator<Transaction> getTransactionsFor(Umsatz u) throws RemoteException {
		DBService service = Settings.getDBService();
		DBIterator<Transaction> transactions = service.createList(Transaction.class);
		transactions.addFilter("transaction_id = ?",new Object[]{u.getID()});
		return transactions;
	}
}
