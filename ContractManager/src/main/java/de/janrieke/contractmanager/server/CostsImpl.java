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
import java.util.Date;

import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Contract.IntervalType;
import de.janrieke.contractmanager.rmi.Costs;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.util.ApplicationException;

public class CostsImpl extends AbstractDBObject implements Costs {

	/**
	 * The UID.
	 */
	private static final long serialVersionUID = -4963699401317887171L;

	public CostsImpl() throws RemoteException {
		super();
	}

	/**
	 * We have to return the name of the sql table here.
	 *
	 * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
	 */
	@Override
	protected String getTableName() {
		return "costs";
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
		return "description";
	}

	/**
	 * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
	 */
	@Override
	protected Class<?> getForeignObject(String field) throws RemoteException {
		if ("contract_id".equals(field)) {
			return Contract.class;
		}
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
	public String getDescription() throws RemoteException {
		return (String) getAttribute("description");
	}

	@Override
	public void setDescription(String description) throws RemoteException {
		setAttribute("description", description);
	}

	@Override
	public double getMoney() throws RemoteException {
		Double d = (Double) getAttribute("money");
		return d == null ? 0.0 : d.doubleValue();
	}

	@Override
	public void setMoney(double money) throws RemoteException {
		setAttribute("money", money);
	}

	@Override
	public IntervalType getPeriod() throws RemoteException {
		Object period = getAttribute("period");
		return period == null ? IntervalType.DAYS
				: IntervalType.valueOf((Integer) period);
	}

	@Override
	public void setPeriod(IntervalType period)
			throws RemoteException {
		setAttribute("period", period.getValue());
	}

	@Override
	public Date getPayday() throws RemoteException {
		return (Date) getAttribute("payday");
	}

	@Override
	public void setPayday(Date payday) throws RemoteException {
		setAttribute("payday", payday);
	}
}
