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

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Address;
import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class AddressImpl extends AbstractDBObject implements Address {

	/**
	 * The UID.
	 */
	private static final long serialVersionUID = -4220981492320250443L;

	public AddressImpl() throws RemoteException {
		super();
	}

	/**
	 * We have to return the name of the sql table here.
	 * 
	 * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
	 */
	@Override
	protected String getTableName() {
		return "address";
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
		return "name";
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
		try {
			if (getName() == null || getName().length() == 0)
				throw new ApplicationException(Settings.i18n().tr(
						"Please enter a name for the address."));

		} catch (RemoteException e) {
			Logger.error("insert check of contract failed", e);
			throw new ApplicationException(Settings.i18n().tr(
					"Unable to store contract, please check the system log"));
		}
	}

	/**
	 * This method is invoked before every update().
	 * 
	 * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
	 */
	@Override
	protected void updateCheck() throws ApplicationException {
		// we simply call the insertCheck here ;)
		insertCheck();
	}

	/**
	 * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String fieldName) throws RemoteException {
		// You are able to create virtual object attributes by overwriting
		// this method. Just catch the fieldName and invent your own attributes
		// ;)
		// if ("summary".equals(fieldName)) {
		// return new Double(getPrice() * getEfforts());
		// }
		if ("contractCount".equals(fieldName))
			return getContractCount();

		return super.getAttribute(fieldName);
	}

	
	//FIELD DATA ACCESS
	
	@Override
	public String getName() throws RemoteException {
		return (String) getAttribute("name");
	}

	@Override
	public void setName(String name) throws RemoteException {
		setAttribute("name", name);
	}

	@Override
	public String getStreet() throws RemoteException {
		return (String) getAttribute("street");
	}

	@Override
	public void setStreet(String street) throws RemoteException {
		setAttribute("street", street);
	}

	@Override
	public String getNumber() throws RemoteException {
		return (String) getAttribute("number");
	}

	@Override
	public void setNumber(String number) throws RemoteException {
		setAttribute("number", number);
	}

	@Override
	public String getExtra() throws RemoteException {
		return (String) getAttribute("extra");
	}

	@Override
	public void setExtra(String extra) throws RemoteException {
		setAttribute("extra", extra);
	}

	@Override
	public String getZipcode() throws RemoteException {
		return (String) getAttribute("zipcode");
	}

	@Override
	public void setZipcode(String zipcode) throws RemoteException {
		setAttribute("zipcode", zipcode);
	}

	@Override
	public String getCity() throws RemoteException {
		return (String) getAttribute("city");
	}

	@Override
	public void setCity(String city) throws RemoteException {
		setAttribute("city", city);
	}

	@Override
	public String getState() throws RemoteException {
		return (String) getAttribute("state");
	}

	@Override
	public void setState(String state) throws RemoteException {
		setAttribute("state", state);
	}

	@Override
	public String getCountry() throws RemoteException {
		return (String) getAttribute("country");
	}

	@Override
	public void setCountry(String country) throws RemoteException {
		setAttribute("country", country);
	}

	@Override
	public int getContractCount() throws RemoteException {
		DBIterator contractIterator = null;
		try {
			DBService service = this.getService();
			contractIterator = service.createList(Contract.class);
			contractIterator.addFilter("address_id = " + this.getID());
		} catch (Exception e) {
			throw new RemoteException("unable to load contract list", e);
		}
		return contractIterator.size();
	}
}
