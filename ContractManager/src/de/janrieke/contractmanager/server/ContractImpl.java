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
import java.util.Calendar;
import java.util.Date;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Address;
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Costs;
import de.janrieke.contractmanager.rmi.Transaction;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * This is the implementor of the project interface. You never need to
 * instantiate this class directly. Instead of this, use the dbService to find
 * the right implementor of your interface. Example:
 * 
 * AbstractPlugin p =
 * Application.getPluginLoader().getPlugin(ExamplePlugin.class); DBService
 * service = (DBService)
 * Application.getServiceFactory().lookup(p,"contract_db");
 * 
 * a) create new project Project project = (Project)
 * service.createObject(Project.class,null);
 * 
 * b) load existing project with id "4". Project project = (Project)
 * service.createObject(Project.class,"4");
 * 
 * @author willuhn, jrieke
 */
public class ContractImpl extends AbstractDBObject implements Contract {

	/**
	 * The UID.
	 */
	private static final long serialVersionUID = 1296427502015363939L;
//	private DBIterator costsIterator;

	/**
	 * @throws RemoteException
	 */
	public ContractImpl() throws RemoteException {
		super();
	}

	/**
	 * We have to return the name of the sql table here.
	 * 
	 * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
	 */
	@Override
	protected String getTableName() {
		return "contract";
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
						"Please enter a contract name."));

			if (getStartDate() == null && getEndDate() != null)
				throw new ApplicationException(Settings.i18n().tr(
						"Start date must be set if end date is."));

			if (getStartDate() != null && getEndDate() != null
					&& getStartDate().after(getEndDate()))
				throw new ApplicationException(Settings.i18n().tr(
						"Start date cannot be after end date."));

			if (getCancellationPeriodCount() < 0)
				throw new ApplicationException(Settings.i18n().tr(
						"Cancellation period must not be negative."));
			if (getFirstMinRuntimeCount() < 0)
				throw new ApplicationException(Settings.i18n().tr(
						"First minimal runtime must not be negative."));
			if (getNextMinRuntimeCount() < 0)
				throw new ApplicationException(Settings.i18n().tr(
						"Next minimal runtime must not be negative."));

		} catch (RemoteException e) {
			Logger.error("Insert check of contract failed.", e);
			throw new ApplicationException(Settings.i18n().tr(
					"Unable to store contract, please check the system log."));
		}
	}

	/**
	 * This method is invoked before every update().
	 * 
	 * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
	 */
	@Override
	protected void updateCheck() throws ApplicationException {
		// we simply call the insertCheck here
		insertCheck();
	}

	/**
	 * @see de.willuhn.datasource.db.AbstractDBObject#getForeignObject(java.lang.String)
	 */
	@Override
	protected Class<?> getForeignObject(String field) throws RemoteException {
		if ("address_id".equals(field))
			return Address.class;
		return null;
	}

	/**
	 * We overwrite the delete method to delete all assigned transactions too.
	 * 
	 * @see de.willuhn.datasource.rmi.Changeable#delete()
	 */
	@Override
	public void delete() throws RemoteException, ApplicationException {
		try {
			// we start a new transaction
			// to delete all or nothing
			this.transactionBegin();

			DBIterator transactions = getTransactions();
			while (transactions.hasNext()) {
				Transaction t = (Transaction) transactions.next();
				t.delete();
			}
			super.delete(); // we delete the contract itself
			getAddress().delete();

			// everything seems to be ok, lets commit the transaction
			this.transactionCommit();

		} catch (RemoteException re) {
			this.transactionRollback();
			throw re;
		} catch (ApplicationException ae) {
			this.transactionRollback();
			throw ae;
		} catch (Throwable t) {
			this.transactionRollback();
			throw new ApplicationException(Settings.i18n().tr(
					"error while deleting contract"), t);
		}
	}

	// FIELD DATA ACCESS
	@Override
	public Object getAttribute(String arg0) throws RemoteException {
		// check derived fields
		if (NEXT_CANCELLATION_DEADLINE.equals(arg0))
			return getNextCancellationDeadline();
		else if (NEXT_TERM_BEGIN.equals(arg0))
			return getNextTermBegin();
		else if (COSTS_PER_TERM.equals(arg0))
			return getCostsPerTerm();
		else if (NEXT_TERM_END.equals(arg0))
			return getNextTermEnd();
		else
			return super.getAttribute(arg0);
	}

	/**
	 * @see de.janrieke.contractmanager.rmi.Contract#getName()
	 */
	@Override
	public String getName() throws RemoteException {
		// We can cast this directly to String, the method getField() knows the
		// meta data of this sql table ;)
		return (String) getAttribute("name"); // "name" is the sql field name
	}

	@Override
	public String getContractNumber() throws RemoteException {
		return (String) getAttribute("contract_no");
	}

	@Override
	public void setContractNumber(String contractNo) throws RemoteException {
		setAttribute("contract_no", contractNo);
	}

	@Override
	public String getCustomerNumber() throws RemoteException {
		return (String) getAttribute("customer_no");
	}

	@Override
	public void setCustomerNumber(String customerNo) throws RemoteException {
		setAttribute("customer_no", customerNo);
	}

	/**
	 * @see de.janrieke.contractmanager.rmi.Contract#getStartDate()
	 */
	@Override
	public Date getStartDate() throws RemoteException {
		return (Date) getAttribute("startdate");
	}

	/**
	 * @see de.janrieke.contractmanager.rmi.Contract#getEndDate()
	 */
	@Override
	public Date getEndDate() throws RemoteException {
		return (Date) getAttribute("enddate");
	}

	/**
	 * @see de.janrieke.contractmanager.rmi.Contract#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) throws RemoteException {
		setAttribute("name", name);
	}

	/**
	 * @see de.janrieke.contractmanager.rmi.Contract#setStartDate(java.util.Date)
	 */
	@Override
	public void setStartDate(Date startDate) throws RemoteException {
		setAttribute("startdate", startDate);
	}

	/**
	 * @see de.janrieke.contractmanager.rmi.Contract#setEndDate(java.util.Date)
	 */
	@Override
	public void setEndDate(Date endDate) throws RemoteException {
		setAttribute("enddate", endDate);
	}

	/**
	 * @see de.janrieke.contractmanager.rmi.Contract#getTransactions()
	 */
	@Override
	public DBIterator getTransactions() throws RemoteException {
		try {
			// 1) Get the Database Service.
			DBService service = Settings.getDBService();

			// 2) We create the transaction list using createList(Class)
			DBIterator transactions = service
					.createList(de.janrieke.contractmanager.rmi.Transaction.class);

			// 3) we add a filter to only query for tasks with our project id
			transactions.addFilter("contract_id = " + this.getID());

			return transactions;
		} catch (Exception e) {
			throw new RemoteException("unable to load transactions list", e);
		}
	}

	@Override
	public Address getAddress() throws RemoteException {
		// Yes, we can cast this directly to Project, because
		// getForeignObject(String)
		// contains the mapping for this attribute.
		Address result;
		try {
			result = (Address) getAttribute("address_id");
		} catch (ObjectNotFoundException e) {
			result = null;
		}
//		if (result == null) {
//			try {
//				result = (Address) Settings.getDBService().createObject(
//						Address.class, null);
//			} catch (RemoteException e) {
//				throw new RemoteException(Settings.i18n().tr(
//						"error while creating new address"), e);
//			}
//			// setAddress(result);
//			// we set that later during store, because a new, unstored object
//			// has no ID, yet.
//		}

		return result;
	}

	/**
	 * @see de.willuhn.jameica.example.rmi.Task#setProject(de.willuhn.jameica.example.rmi.Project)
	 */
	@Override
	public void setAddress(Address address) throws RemoteException {
		// same here
		setAttribute("address_id", address);
	}

	@Override
	public String getComment() throws RemoteException {
		return (String) getAttribute("comment");
	}

	@Override
	public void setComment(String comment) throws RemoteException {
		setAttribute("comment", comment);
	}

	@Override
	public IntervalType getCancellationPeriodType() throws RemoteException {
		Object type = getAttribute("cancelation_period_type");
		return type == null ? IntervalType.DAYS
				: IntervalType.values()[(Integer) type];
	}

	@Override
	public void setCancelationPeriodType(IntervalType type)
			throws RemoteException {
		setAttribute("cancelation_period_type", type.ordinal());
	}

	@Override
	public Integer getCancellationPeriodCount() throws RemoteException {
		Integer i = (Integer) getAttribute("cancelation_period_count");
		return i == null ? 0 : i;
	}

	@Override
	public void setCancelationPeriodCount(Integer count) throws RemoteException {
		setAttribute("cancelation_period_count", count);
	}

	@Override
	public Integer getFirstMinRuntimeCount() throws RemoteException {
		Integer i = (Integer) getAttribute("first_min_runtime_count");
		return i == null ? 0 : i;
	}

	@Override
	public void setFirstMinRuntimeCount(Integer count) throws RemoteException {
		setAttribute("first_min_runtime_count", count);
	}

	@Override
	public IntervalType getFirstMinRuntimeType() throws RemoteException {
		Object type = getAttribute("first_min_runtime_type");
		return type == null ? IntervalType.DAYS
				: IntervalType.values()[(Integer) type];
	}

	@Override
	public void setFirstMinRuntimeType(IntervalType type)
			throws RemoteException {
		setAttribute("first_min_runtime_type", type.ordinal());
	}

	@Override
	public Integer getNextMinRuntimeCount() throws RemoteException {
		Integer i = (Integer) getAttribute("next_min_runtime_count");
		return i == null ? 0 : i;
	}

	@Override
	public void setNextMinRuntimeCount(Integer count) throws RemoteException {
		setAttribute("next_min_runtime_count", count);
	}

	@Override
	public IntervalType getNextMinRuntimeType() throws RemoteException {
		Object type = getAttribute("next_min_runtime_type");
		return type == null ? IntervalType.DAYS
				: IntervalType.values()[(Integer) type];
	}

	@Override
	public void setNextMinRuntimeType(IntervalType type) throws RemoteException {
		setAttribute("next_min_runtime_type", type.ordinal());
	}

	@Override
	public String getURI() throws RemoteException {
		return (String) getAttribute("uri");
	}

	@Override
	public void setURI(String uri) throws RemoteException {
		setAttribute("uri", uri);
	}

	/**
	 * @see de.willuhn.jameica.example.rmi.Project#getTasks()
	 */
	@Override
	public DBIterator getCosts() throws RemoteException {
		DBIterator costsIterator = null;
//		if (this.costsIterator == null) { 
			try {
				DBService service = this.getService();
				costsIterator = service.createList(Costs.class);
				costsIterator.addFilter("contract_id = " + this.getID());
			} catch (Exception e) {
				throw new RemoteException("unable to load costs list", e);
			}
//		}
//		costsIterator.begin();
		return costsIterator;
	}

	/**
	 * Calculates the next contractual term's end after the given date.
	 * 
	 * @param minusCancellationPeriod
	 *            If true, the cancellation date is returned.
	 * @param after
	 * @return The end of the term.
	 * @throws RemoteException
	 */
	private Date calculateTermEnd(boolean minusCancellationPeriod, Date after)
			throws RemoteException {
		if (getEndDate() != null)
			return null; // if the end is already set, there is no need for
							// further cancellations

		Date startDate = getStartDate();
		if (startDate == null)
			return null;
		Calendar today = Calendar.getInstance();
		today.setTime(after);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);

		if (minusCancellationPeriod) {
			IntervalType cancellationPeriodType = getCancellationPeriodType();
			// one more day, as this is a deadline
			Integer cancellationPeriodCount = getCancellationPeriodCount();

			// if the period is invalid, assume there is none
			if (cancellationPeriodType != null
					&& cancellationPeriodCount != null
					&& cancellationPeriodCount > 0) {
				switch (cancellationPeriodType) {
				case DAYS:
					calendar.add(Calendar.DAY_OF_YEAR, -cancellationPeriodCount);
					break;
				case WEEKS:
					calendar.add(Calendar.WEEK_OF_YEAR,
							-cancellationPeriodCount);
					break;
				case MONTHS:
					calendar.add(Calendar.MONTH, -cancellationPeriodCount);
					break;
				case YEARS:
					calendar.add(Calendar.YEAR, -cancellationPeriodCount);
					break;
				}
			}
			//minus one day, as the last day of the running term is the reference,
			// not the first of the new
			calendar.add(Calendar.DAY_OF_YEAR, -1);
		}

		IntervalType firstMinRuntimeType = getFirstMinRuntimeType();
		Integer firstMinRuntimeCount = getFirstMinRuntimeCount();
		IntervalType nextMinRuntimeType = getNextMinRuntimeType();
		Integer nextMinRuntimeCount = getNextMinRuntimeCount();

		// if one of the runtime definition is invalid, use the other one
		if (firstMinRuntimeType == null || firstMinRuntimeCount == null
				|| firstMinRuntimeCount < 0) {
			firstMinRuntimeCount = nextMinRuntimeCount;
			firstMinRuntimeType = nextMinRuntimeType;
		}
		if (nextMinRuntimeType == null || nextMinRuntimeCount == null
				|| nextMinRuntimeCount < 0) {
			nextMinRuntimeCount = firstMinRuntimeCount;
			nextMinRuntimeType = firstMinRuntimeType;
		}
		// do nothing if both are invalid
		if (nextMinRuntimeType == null || nextMinRuntimeCount == null
				|| nextMinRuntimeCount < 0)
			return null;

		if (firstMinRuntimeCount == 0)
			return null; // "0" encodes a daily runtime extension

		boolean first = true;

		while (calendar.before(today)) {
			if (first) {
				switch (firstMinRuntimeType) {
				case DAYS:
					calendar.add(Calendar.DAY_OF_YEAR, firstMinRuntimeCount);
					break;
				case WEEKS:
					calendar.add(Calendar.WEEK_OF_YEAR, firstMinRuntimeCount);
					break;
				case MONTHS:
					calendar.add(Calendar.MONTH, firstMinRuntimeCount);
					break;
				case YEARS:
					calendar.add(Calendar.YEAR, firstMinRuntimeCount);
					break;
				}
				first = false;
			} else {
				if (nextMinRuntimeCount == 0)
					return null; // "0" encodes a daily runtime extension
				switch (nextMinRuntimeType) {
				case DAYS:
					calendar.add(Calendar.DAY_OF_YEAR, nextMinRuntimeCount);
					break;
				case WEEKS:
					calendar.add(Calendar.WEEK_OF_YEAR, nextMinRuntimeCount);
					break;
				case MONTHS:
					calendar.add(Calendar.MONTH, nextMinRuntimeCount);
					break;
				case YEARS:
					calendar.add(Calendar.YEAR, nextMinRuntimeCount);
					break;
				}
			}
		}

		return calendar.getTime();
	}

	@Override
	public Date getNextTermBegin() throws RemoteException {
		return calculateTermEnd(false, new Date());
	}

	@Override
	public Date getNextTermEnd() throws RemoteException {
		Date begin = getNextTermBegin();
		if (begin == null)
			return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(begin);
		switch (getNextMinRuntimeType()) {
		case DAYS:
			calendar.add(Calendar.DAY_OF_YEAR, getNextMinRuntimeCount());
			break;
		case WEEKS:
			calendar.add(Calendar.WEEK_OF_YEAR, getNextMinRuntimeCount());
			break;
		case MONTHS:
			calendar.add(Calendar.MONTH, getNextMinRuntimeCount());
			break;
		case YEARS:
			calendar.add(Calendar.YEAR, getNextMinRuntimeCount());
			break;
		}
		return calendar.getTime();
	}

	@Override
	public Date getNextCancellationDeadline() throws RemoteException {
		return calculateTermEnd(true, new Date());
	}

	@Override
	public Date getNextCancellationDeadline(Date after) throws RemoteException {
		return calculateTermEnd(true, after);
	}

	private int getNextRuntimeDays() throws RemoteException {
		// FIXME: Calculate costs based on a real calendar
		if (getEndDate() != null)
			return 0; // if the end is already set, there is no need for
						// further cancellations

		Date startDate = getStartDate();
		if (startDate == null)
			return 0;
		IntervalType firstMinRuntimeType = getFirstMinRuntimeType();
		Integer firstMinRuntimeCount = getFirstMinRuntimeCount();
		IntervalType nextMinRuntimeType = getNextMinRuntimeType();
		Integer nextMinRuntimeCount = getNextMinRuntimeCount();

		// if one of the runtime definition is invalid, use the other one
		if (firstMinRuntimeType == null || firstMinRuntimeCount == null
				|| firstMinRuntimeCount < 0) {
			firstMinRuntimeCount = nextMinRuntimeCount;
			firstMinRuntimeType = nextMinRuntimeType;
		}
		if (nextMinRuntimeType == null || nextMinRuntimeCount == null
				|| nextMinRuntimeCount < 0) {
			nextMinRuntimeCount = firstMinRuntimeCount;
			nextMinRuntimeType = firstMinRuntimeType;
		}
		// do nothing if both are invalid
		if (nextMinRuntimeType == null || nextMinRuntimeCount == null
				|| nextMinRuntimeCount < 0)
			return 0;

		switch (nextMinRuntimeType) {
		case DAYS:
			return nextMinRuntimeCount;
		case WEEKS:
			return nextMinRuntimeCount * 7;
		case MONTHS:
			return nextMinRuntimeCount * 30;
		case YEARS:
			return nextMinRuntimeCount * 365;
		}
		return 0;
	}

	@Override
	public double getCostsPerTerm() throws RemoteException {
		// FIXME: Calculate costs based on a real calendar
		int termLength = getNextRuntimeDays();
		if (termLength == 0) {
			return 0;
		}
		double costsPerDay = 0; 
		DBIterator costsIterator = getCosts();
		while (costsIterator.hasNext()) {
			Costs costEntry = (Costs) costsIterator.next();
			switch (costEntry.getPeriod()) {
			case DAYS:
				costsPerDay += costEntry.getMoney();
				break;
			case MONTHS:
				costsPerDay += costEntry.getMoney()/30.42;
				break;
			case WEEKS:
				costsPerDay += costEntry.getMoney()/7;
				break;
			case YEARS:
				costsPerDay += costEntry.getMoney()/365;
				break;

			default:
				break;
			}
		}
		return costsPerDay*termLength;
	}
}