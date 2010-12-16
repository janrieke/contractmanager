package de.janrieke.contractmanager.server;

import java.rmi.RemoteException;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Address;
import de.willuhn.datasource.db.AbstractDBObject;
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
						"Please enter a name"));

		} catch (RemoteException e) {
			Logger.error("insert check of contract failed", e);
			throw new ApplicationException(Settings.i18n().tr(
					"unable to store contract, please check the system log"));
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
	public int getNumber() throws RemoteException {
		return ((Integer) getAttribute("number")).intValue();
	}

	@Override
	public void setNumber(int number) throws RemoteException {
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
	public int getZipcode() throws RemoteException {
		return ((Integer) getAttribute("zipcode")).intValue();
	}

	@Override
	public void setZipcode(int zipcode) throws RemoteException {
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
}
