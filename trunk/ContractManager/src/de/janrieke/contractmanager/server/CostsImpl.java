package de.janrieke.contractmanager.server;

import java.rmi.RemoteException;

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
	public String getDescription() throws RemoteException {
		return (String) getAttribute("description");
	}

	@Override
	public void setDescription(String description) throws RemoteException {
		setAttribute("description", description);
	}

	@Override
	public double getMoney() throws RemoteException {
		Double d = (Double) getAttribute("money_per_year");
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
				: IntervalType.values()[(Integer) period];
	}

	@Override
	public void setPeriod(IntervalType period)
			throws RemoteException {
		setAttribute("period", period.ordinal());
	}
}
