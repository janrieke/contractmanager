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
import de.janrieke.contractmanager.rmi.Storage;
import de.janrieke.contractmanager.rmi.Transaction;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ObjectNotFoundException;
import de.willuhn.jameica.util.DateUtil;
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

	class CalendarBuilder {
		Calendar getInstance() {
			return Calendar.getInstance();
		}
	}
	private CalendarBuilder calendarBuilder = new CalendarBuilder();

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
			if (getName() == null || getName().length() == 0) {
				throw new ApplicationException(Settings.i18n().tr(
						"Please enter a contract name."));
			}

			if (getStartDate() == null && getEndDate() != null) {
				throw new ApplicationException(Settings.i18n().tr(
						"Start date must be set if end date is."));
			}

			if (getStartDate() != null && getEndDate() != null
					&& getStartDate().after(getEndDate())) {
				throw new ApplicationException(Settings.i18n().tr(
						"Start date cannot be after end date."));
			}

			if (getCancellationPeriodCount() < 0) {
				throw new ApplicationException(Settings.i18n().tr(
						"Cancellation period must not be negative."));
			}
			if (getFirstMinRuntimeCount() < 0) {
				throw new ApplicationException(Settings.i18n().tr(
						"First minimal runtime must not be negative."));
			}
			if (getFollowingMinRuntimeCount() < 0) {
				throw new ApplicationException(Settings.i18n().tr(
						"Next minimal runtime must not be negative."));
			}

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
		if ("address_id".equals(field)) {
			return Address.class;
		}
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

			DBIterator<Transaction> transactions = getTransactions();
			while (transactions.hasNext()) {
				Transaction t = transactions.next();
				t.delete();
			}

			DBIterator<Costs> costs = getCosts();
			while (costs.hasNext()) {
				Costs cost = costs.next();
				cost.delete();
			}

			super.delete(); // we delete the contract itself

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
		if (NEXT_CANCELLATION_DEADLINE.equals(arg0)) {
			return getNextCancellationDeadline();
		} else if (NEXT_TERM_BEGIN.equals(arg0)) {
			return getNextTermBegin();
		} else if (NEXT_TERM_END.equals(arg0)) {
			return getNextTermEnd();
		} else if (NEXT_CANCEL_TERM_BEGIN.equals(arg0)) {
			return getNextCancelableTermBegin();
		} else if (NEXT_CANCEL_TERM_END.equals(arg0)) {
			return getNextCancelableTermEnd();
		} else if (MONEY_PER_TERM.equals(arg0)) {
			Double money = getMoneyPerTerm();
			return money.isNaN()?null:money;
		}
		else if (MONEY_PER_MONTH.equals(arg0)) {
			return getMoneyPerMonth();
		} else if (PARTNER_NAME.equals(arg0)) {
			return getPartnerName();
		} else if (CONTRACT_NAME_PLUS_PARTNER_NAME.equals(arg0)) {
			return getContractAndPartnerName();
		} else if ("ignore_cancellations".equals(arg0)) { // prevent "null" result
			Object result = super.getAttribute(arg0);
			if (result == null) {
				return 0;
			} else {
				return result;
			}
		} else {
			return super.getAttribute(arg0);
		}
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
	public DBIterator<Transaction> getTransactions() throws RemoteException {
		try {
			// 1) Get the Database Service.
			DBService service = Settings.getDBService();

			// 2) We create the transaction list using createList(Class)
			DBIterator<Transaction> transactions = service.createList(Transaction.class);

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
				: IntervalType.valueOf((Integer) type);
	}

	@Override
	public void setCancelationPeriodType(IntervalType type)
			throws RemoteException {
		setAttribute("cancelation_period_type", type.getValue());
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
				: IntervalType.valueOf((Integer) type);
	}

	@Override
	public void setFirstMinRuntimeType(IntervalType type)
			throws RemoteException {
		setAttribute("first_min_runtime_type", type.getValue());
	}

	@Override
	public Integer getFollowingMinRuntimeCount() throws RemoteException {
		Integer i = (Integer) getAttribute("next_min_runtime_count");
		return i == null ? 0 : i;
	}

	@Override
	public void setFollowingMinRuntimeCount(Integer count) throws RemoteException {
		setAttribute("next_min_runtime_count", count);
	}

	@Override
	public IntervalType getFollowingMinRuntimeType() throws RemoteException {
		Object type = getAttribute("next_min_runtime_type");
		return type == null ? IntervalType.DAYS
				: IntervalType.valueOf((Integer) type);
	}

	@Override
	public void setFollowingMinRuntimeType(IntervalType type) throws RemoteException {
		setAttribute("next_min_runtime_type", type.getValue());
	}

	@Override
	public Boolean getDoNotRemind() throws RemoteException {
		Integer b = (Integer) getAttribute("ignore_cancellations");
		return b == null ? false : b.equals(1);
	}

	@Override
	public void setDoNotRemind(Boolean value) throws RemoteException {
		setAttribute("ignore_cancellations", value?1:0);
	}

	@Override
	public String getURI() throws RemoteException {
		return (String) getAttribute("uri");
	}

	@Override
	public void setURI(String uri) throws RemoteException {
		setAttribute("uri", uri);
	}

	@Override
	public Boolean getFixedTerms() throws RemoteException {
		Integer b = (Integer) getAttribute("runtime_snap_to_end");
		return b == null ? false : b.equals(1);
	}

	@Override
	public void setFixedTerms(Boolean value) throws RemoteException {
		setAttribute("runtime_snap_to_end", value?1:0);
	}

	@Override
	public DBIterator<Costs> getCosts() throws RemoteException {
		DBIterator<Costs> costsIterator = null;
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

	@Override
	public String getHibiscusCategoryID() throws RemoteException {
		return (String) getAttribute("hibiscus_category");
	}

	@Override
	public void setHibiscusCategoryID(String category) throws RemoteException {
		setAttribute("hibiscus_category", category);
	}

	/* Instances of this class shall only be returned if all fields are
	 * set and valid.
	 */
	class ValidRuntimes {
		IntervalType firstMinRuntimeType;
		Integer firstMinRuntimeCount;
		IntervalType followingMinRuntimeType;
		Integer followingMinRuntimeCount;
		Boolean fixedTerms;
	}

	@Override
	public boolean hasValidRuntimeInformation() throws RemoteException {
		return getValidRuntimes() != null;
	}

	/**
	 * Convenience method to find out whether all necessary info is available
	 * to calculate terms and cancellation information.
	 * @return a ValidRuntimes object if all info is available, or null otherwise
	 */
	private ValidRuntimes getValidRuntimes() throws RemoteException {
		if (getStartDate() == null) {
			return null;
		}

		ValidRuntimes result = new ValidRuntimes();

		Date startDate = DateUtil.startOfDay(getStartDate());

		Calendar calendar = calendarBuilder.getInstance();
		calendar.setTime(startDate);

		result.firstMinRuntimeType = getFirstMinRuntimeType();
		result.firstMinRuntimeCount = getFirstMinRuntimeCount();
		result.followingMinRuntimeType = getFollowingMinRuntimeType();
		result.followingMinRuntimeCount = getFollowingMinRuntimeCount();
		result.fixedTerms = getFixedTerms();

		// if one of the runtime definition is invalid, use the other one
		if (result.firstMinRuntimeType == null || result.firstMinRuntimeCount == null
				|| result.firstMinRuntimeCount <= 0) {
			result.firstMinRuntimeCount = result.followingMinRuntimeCount;
			result.firstMinRuntimeType = result.followingMinRuntimeType;
		}
		if (result.followingMinRuntimeType == null || result.followingMinRuntimeCount == null
				|| result.followingMinRuntimeCount <= 0) {
			result.followingMinRuntimeCount = result.firstMinRuntimeCount;
			result.followingMinRuntimeType = result.firstMinRuntimeType;
		}
		// do nothing if both are invalid
		if (result.followingMinRuntimeType == null || result.followingMinRuntimeCount == null
				|| result.followingMinRuntimeCount <= 0) {
			return null;
		}

		return result;
	}

	/**
	 * Calculates the next contractual term's end after the given date.
	 *
	 * @param after
	 * @param excludeFirstTerm If true, never return the start of the first term.
	 * @return The end of the term.
	 * @throws RemoteException
	 */
	private Date calculateNextTermBegin(Date after, boolean excludeFirstTerm)
			throws RemoteException {
		if (after == null) {
			return null;
		}
		ValidRuntimes rt = getValidRuntimes();
		if (rt == null) {
			return null;
		}

		Date startDate = DateUtil.startOfDay(getStartDate());
		Calendar afterCal = calendarBuilder.getInstance();
		afterCal.setTime(DateUtil.endOfDay(after));

		Calendar calendar = calendarBuilder.getInstance();
		calendar.setTime(startDate);

		// If fixed terms is true, virtually delay the start of the contract to the next period.
		if (rt.fixedTerms) {
			switch (rt.firstMinRuntimeType) {
				case WEEKS:
					calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					calendar.add(Calendar.WEEK_OF_YEAR, 1);
					break;
				case MONTHS:
					calendar.set(Calendar.DAY_OF_MONTH, 1);
					calendar.add(Calendar.MONTH, 1);
					break;
				case YEARS:
					calendar.set(Calendar.DAY_OF_YEAR, 1);
					calendar.add(Calendar.YEAR, 1);
					break;
				default:
			}

		}

		boolean first = true;

		while (!calendar.after(afterCal) || (first && excludeFirstTerm)) {
			if (first) {
				addToCalendar(calendar, rt.firstMinRuntimeType, rt.firstMinRuntimeCount);
				first = false;
			} else {
				addToCalendar(calendar, rt.followingMinRuntimeType, rt.followingMinRuntimeCount);
			}
		}

		if (getEndDate() != null && DateUtil.endOfDay(getEndDate()).before(calendar.getTime())) {
			return null; // if the end has already passed, there is no need for
						 // further cancellations
		}

		return calendar.getTime();
	}

	/**
	 * Calculates the next cancellation deadline that is <i>not before</i> the
	 * given date.
	 *
	 * @param notBefore
	 *            The returned deadline will not be before this date.
	 * @return The cancellation deadline or null if no deadline exists after the
	 *         given date.
	 * @throws RemoteException
	 */
	private Date calculateNextCancellationDeadline(Date notBefore) throws RemoteException {

		Calendar calendar = calendarBuilder.getInstance();
		calendar.setTime(DateUtil.endOfDay(notBefore));

		IntervalType cancellationPeriodType = getCancellationPeriodType();
		Integer cancellationPeriodCount = getCancellationPeriodCount();

		addToCalendar(calendar, cancellationPeriodType, cancellationPeriodCount);

		Date termEnd = calculateNextTermBegin(calendar.getTime(), true);
		if (termEnd == null) {
			return null;
		}

		calendar.setTime(termEnd);

		addToCalendar(calendar, cancellationPeriodType, -cancellationPeriodCount);

		// Term end is one day before next term's start.
		calendar.add(Calendar.DAY_OF_YEAR, -1);

		return calendar.getTime();
	}

	@Override
	public Date getNextTermBegin() throws RemoteException {
		return getNextTermBeginAfter(calendarBuilder.getInstance().getTime());
	}

	@Override
	public Date getNextTermEnd() throws RemoteException {
		Date begin = getNextTermBegin();
		if (begin == null) {
			return null;
		}
		Calendar calendar = calendarBuilder.getInstance();
		calendar.setTime(begin);
		addToCalendar(calendar, getFollowingMinRuntimeType(), getFollowingMinRuntimeCount());
		calendar.add(Calendar.DAY_OF_YEAR, -1); //term end is one day before next term's start

		return calendar.getTime();
	}

	@Override
	public Date getNextCancelableTermBegin() throws RemoteException {
		Calendar calendar = calendarBuilder.getInstance();
		addToCalendar(calendar, getCancellationPeriodType(), getCancellationPeriodCount());

		return calculateNextTermBegin(calendar.getTime(), true);
	}

	@Override
	public Date getNextCancelableTermEnd() throws RemoteException {
		Date begin = getNextCancelableTermBegin();
		if (begin == null) {
			return null;
		}
		Calendar calendar = calendarBuilder.getInstance();
		calendar.setTime(begin);
		addToCalendar(calendar, getFollowingMinRuntimeType(), getFollowingMinRuntimeCount());
		calendar.add(Calendar.DAY_OF_YEAR, -1); //term end is one day before next term's start

		return calendar.getTime();
	}

	@Override
	public Date getNextCancellationDeadline() throws RemoteException {
		Calendar calendar = calendarBuilder.getInstance();
		return calculateNextCancellationDeadline(calendar.getTime());
	}

	@Override
	public Date getNextCancellationDeadline(Date notBefore) throws RemoteException {
		return calculateNextCancellationDeadline(notBefore);
	}


	@Override
	public double getMoneyPerTerm() throws RemoteException {
		if (getFollowingMinRuntimeCount() <= 0) {
			return Double.NaN;
		}
		double costsPerDay = 0;
		double costsPerWeek = 0;
		double costsPerMonth = 0;
		double costsPerYear = 0;
		DBIterator<Costs> costsIterator = getCosts();
		while (costsIterator.hasNext()) {
			Costs costEntry = costsIterator.next();
			switch (costEntry.getPeriod()) {
			case DAYS:
				costsPerDay += costEntry.getMoney();
				break;
			case WEEKS:
				costsPerWeek += costEntry.getMoney();
				break;
			case MONTHS:
				costsPerMonth += costEntry.getMoney();
				break;
			case YEARS:
				costsPerYear += costEntry.getMoney();
				break;
			case HALF_YEARS:
				costsPerMonth += costEntry.getMoney()/6;
				break;
			case QUARTER_YEAR:
				costsPerMonth += costEntry.getMoney()/3;
				break;

			default:
				break;
			}
		}

		double result = 0;
		switch (getFollowingMinRuntimeType()) {
		case DAYS:
			result = costsPerDay + costsPerWeek/7 + costsPerMonth/30.42 + costsPerYear/365;
			break;

		case WEEKS:
			result = costsPerDay*7 + costsPerWeek + costsPerMonth/4.35 + costsPerYear/52.14;
			break;

		case MONTHS:
			result = costsPerDay*30.42 + costsPerWeek*4.35 + costsPerMonth + costsPerYear/12;
			break;

		case YEARS:
			result = costsPerDay*365 + costsPerWeek*52.14 + costsPerMonth*12 + costsPerYear;
			break;

		default:
			break;
		}


		return result * getFollowingMinRuntimeCount();
	}


	@Override
	public double getMoneyPerMonth() throws RemoteException {
		// FIXME: Calculate costs based on a real calendar
		double costsPerMonth = 0;
		DBIterator<Costs> costsIterator = getCosts();
		while (costsIterator.hasNext()) {
			Costs costEntry = costsIterator.next();
			switch (costEntry.getPeriod()) {
			case DAYS:
				costsPerMonth += costEntry.getMoney()*30.42;
				break;
			case WEEKS:
				costsPerMonth += costEntry.getMoney()*(30.42/7);
				break;
			case MONTHS:
				costsPerMonth += costEntry.getMoney();
				break;
			case QUARTER_YEAR:
				costsPerMonth += costEntry.getMoney()/3;
				break;
			case HALF_YEARS:
				costsPerMonth += costEntry.getMoney()/6;
				break;
			case YEARS:
				costsPerMonth += costEntry.getMoney()/12;
				break;

			default:
				break;
			}
		}
		return costsPerMonth;
	}


	@Override
	public String getPartnerName() throws RemoteException {
		return getAddress().getName();
	}

	@Override
	public String getContractAndPartnerName() throws RemoteException {
		return getName() + " [" + getPartnerName() + "]";
	}

	public static final boolean addToCalendar(Calendar calendar, IntervalType interval, int count) {
		// if the period is invalid, assume there is none
		if (interval != null) {
			switch (interval) {
			case DAYS:
				calendar.add(Calendar.DAY_OF_YEAR, count);
				return true;
			case WEEKS:
				calendar.add(Calendar.WEEK_OF_YEAR,
						count);
				return true;
			case MONTHS:
				calendar.add(Calendar.MONTH, count);
				return true;
			case YEARS:
				calendar.add(Calendar.YEAR, count);
				return true;
			case ONCE:
				return false;
			default:
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean isActiveInMonth(Date month) throws RemoteException {
		Calendar monthBegin = calendarBuilder.getInstance();
		monthBegin.setTime(month);
		monthBegin.set(Calendar.DAY_OF_MONTH, 1);
		monthBegin.set(Calendar.HOUR_OF_DAY, 0);
		monthBegin.set(Calendar.MINUTE, 0);
		monthBegin.set(Calendar.SECOND, 0);
		monthBegin.set(Calendar.MILLISECOND, 0);
		monthBegin.add(Calendar.MILLISECOND, -1);

		Calendar monthEnd = calendarBuilder.getInstance();
		monthEnd.setTime(month);
		monthEnd.add(Calendar.MONTH, 1);
		monthEnd.set(Calendar.DAY_OF_MONTH, 1);
		monthEnd.set(Calendar.HOUR_OF_DAY, 0);
		monthEnd.set(Calendar.MINUTE, 0);
		monthEnd.set(Calendar.SECOND, 0);
		monthEnd.set(Calendar.MILLISECOND, 0);

		if ((this.getStartDate() == null || this.getStartDate().before(monthEnd.getTime())) &&
				(this.getEndDate() == null || this.getEndDate().after(monthBegin.getTime()))) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getSepaCreditorRef() throws RemoteException {
		return (String) getAttribute("sepa_creditor");
	}

	@Override
	public void setSepaCreditorRef(String ref) throws RemoteException {
		setAttribute("sepa_creditor", ref);
	}

	@Override
	public String getSepaCustomerRef() throws RemoteException {
		return (String) getAttribute("sepa_customer");
	}

	@Override
	public void setSepaCustomerRef(String ref) throws RemoteException {
		setAttribute("sepa_customer", ref);
	}

	@Override
	public DBIterator<Storage> getAttachedFiles() throws RemoteException {
		try {
			// 1) Get the Database Service.
			DBService service = Settings.getDBService();

			// 2) We create the transaction list using createList(Class)
			DBIterator<Storage> transactions = service.createList(Storage.class);

			// 3) we add a filter to only query for tasks with our project id
			transactions.addFilter("contract_id = " + this.getID());

			return transactions;
		} catch (Exception e) {
			throw new RemoteException("unable to load file list", e);
		}
	}

	@Override
	public Date getDoNotRemindBefore() throws RemoteException {
		return (Date) getAttribute("do_not_remind_before");
	}

	@Override
	public void setDoNotRemindBefore(Date date) throws RemoteException {
		setAttribute("do_not_remind_before", date);
	}

	@Override
	public void doNotRemindAboutNextCancellation() throws RemoteException {
		Date nextDeadline = getNextCancellationDeadline();
		if (nextDeadline != null) {
			Calendar cal = calendarBuilder.getInstance();
			cal.setTime(nextDeadline);
			cal.add(Calendar.DAY_OF_YEAR, 1);
			setDoNotRemindBefore(cal.getTime());
		}
	}

	@Override
	public boolean isNextDeadlineWithinNoticeTime() throws RemoteException {
		return isNextDeadlineWithin(Settings.getExtensionNoticeTime());
	}

	@Override
	public boolean isNextDeadlineWithinWarningTime() throws RemoteException {
		return isNextDeadlineWithin(Settings.getExtensionWarningTime());
	}

	private boolean isNextDeadlineWithin(int days) throws RemoteException {
		Date doNotRemindBefore = getDoNotRemindBefore();
		Date deadline;
		if (doNotRemindBefore != null) {
			deadline = getNextCancellationDeadline(doNotRemindBefore);
		} else {
			deadline = getNextCancellationDeadline();
		}

		if (deadline == null) {
			return false;
		}

		final Calendar today = calendarBuilder.getInstance();
		Calendar calendar = calendarBuilder.getInstance();

		calendar.setTime(deadline);
		calendar.add(Calendar.DAY_OF_YEAR,
				-days);
		return calendar.before(today);
	}

	@Override
	public Date getNextTermBeginAfter(Date startDate) throws RemoteException {
		return calculateNextTermBegin(startDate, false);
	}
}