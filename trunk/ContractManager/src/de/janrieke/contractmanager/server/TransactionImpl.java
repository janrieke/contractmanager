package de.janrieke.contractmanager.server;

import java.rmi.RemoteException;

import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Transaction;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;

/**
 * CREATE TABLE transactions ( transaction_id int(5), contract_id int(5), UNIQUE
 * (transaction_id), PRIMARY KEY (transaction_id) );
 * 
 * @author Jan Rieke
 * 
 */
public class TransactionImpl extends AbstractDBObject implements Transaction {

	public TransactionImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * The UID.
	 */
	private static final long serialVersionUID = 6231715594674008961L;

	@Override
	protected String getTableName() {
		return "transactions";
	}

	/**
	 * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
	 */
	@Override
	protected Class<?> getForeignObject(String field) throws RemoteException {
		if ("contract_id".equals(field))
			return Contract.class;
		return null;
	}


	@Override
	public Contract getContract() throws RemoteException {
		Contract result;
		try {
			result = (Contract) getAttribute("contract_id");
		} catch (ObjectNotFoundException e) {
			result = null;
		}
		return result;
	}

	@Override
	public void setContract(Contract contract) throws RemoteException {
		setAttribute("contract_id", contract);
	}

	@Override
	public String getPrimaryAttribute() throws RemoteException {
		// TODO Auto-generated method stub
		return "transaction_id";
	}

}
