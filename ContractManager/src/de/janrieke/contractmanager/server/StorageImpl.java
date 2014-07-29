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
import java.sql.Blob;

import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Storage;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;

/**
 * CREATE TABLE transactions ( transaction_id int(5), contract_id int(5), UNIQUE
 * (transaction_id), PRIMARY KEY (transaction_id) );
 * 
 * @author Jan Rieke
 * 
 */
public class StorageImpl extends AbstractDBObject implements Storage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1175439258267587108L;

	public StorageImpl() throws RemoteException {
		super();
	}

	@Override
	protected String getTableName() {
		return "storage";
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
		return "id";
	}
	
	@Override
	public String getDescription() throws RemoteException {
		return (String) getAttribute("description");
	}

	@Override
	public void setDescription(String desc) throws RemoteException {
		setAttribute("description", desc);
	}

	@Override
	public String getPath() throws RemoteException {
		return (String) getAttribute("path");
	}

	@Override
	public void setPath(String path) throws RemoteException {
		setAttribute("path", path);
	}

	@Override
	public Blob getFile() throws RemoteException {
		Object res = getAttribute("file");
		return (Blob) res; 
	}

	@Override
	public void setFile(Blob file) throws RemoteException {
		setAttribute("file", file);
	}
}
