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
		return "transaction_id";
	}

}
