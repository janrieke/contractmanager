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
import java.sql.SQLException;

import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.ICalUID;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.util.ApplicationException;

/*
 * CREATE TABLE icaluids (
 *   uid varchar(255) NOT NULL,
 *   contract_id int(5),
 *   UNIQUE (uid),
 *   PRIMARY KEY (uid)
 * );
 */
public class ICalUIDImpl extends AbstractDBObject implements ICalUID {

	/**
	 * The UID.
	 */
	private static final long serialVersionUID = -344175586738633833L;

    private boolean upper;

    public ICalUIDImpl() throws RemoteException {
		super();
	}

	@Override
	protected void init() throws SQLException {
		super.init();
		upper = Boolean.getBoolean(getService().getClass().getName() + ".uppercase");
	}

	/**
	 * We have to return the name of the sql table here.
	 * 
	 * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
	 */
	@Override
	protected String getTableName() {
		return "icaluids";
	}

	/**
	 * Sometimes you can display only one of the projects attributes (in combo
	 * boxes). Here you can define the name of this field. Please don't confuse
	 * this with the "primary KEY".
	 * 
	 * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
	 */
	@Override
	public String getPrimaryAttribute() throws RemoteException {
		// we choose the contract's name as primary field.
		return "uid";
	}

	/**
	 * Liefert den Namen der Spalte, in der sich der Primary-Key befindet.
	 * Default: "id".
	 * @return Name der Spalte mit dem Primary-Key.
	 */
	@Override
	protected String getIDField()
	{
		return upper ? "UID" : "uid";
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
	
	/**
	 * This method will be called before delete() is executed. Here you can make
	 * some dependency checks. If you don't want to delete the project (in case
	 * of failed dependencies) you have to throw an ApplicationException. The
	 * message of this one will be shown in users UI. So please translate the
	 * text into the users language.
	 * 
	 * @see de.willuhn.datasource.db.AbstractDBObject#deleteCheck()
	 */
	@Override
	protected void deleteCheck() throws ApplicationException {
	}

	/**
	 * This method is invoked before executing insert(). So lets check the
	 * entered data.
	 * 
	 * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
	 */
	@Override
	protected void insertCheck() throws ApplicationException {
	}

	/**
	 * This method is invoked before every update().
	 * 
	 * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
	 */
	@Override
	protected void updateCheck() throws ApplicationException {
		insertCheck();
	}
	
	//FIELD DATA ACCESS
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
	public String getUID() throws RemoteException {
		return (String) getAttribute("uid");
	}

	@Override
	public void setUID(String uid) throws RemoteException {
		setAttribute("uid", uid);
	}
}
