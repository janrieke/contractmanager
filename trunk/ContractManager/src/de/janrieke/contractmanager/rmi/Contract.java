package de.janrieke.contractmanager.rmi;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBObject;

/**
 * Interface of the business object for contracts. According to the SQL table, we
 * define some getters and setters here.
 * 
 * <pre>
 * CREATE TABLE contract (
 *   id IDENTITY,
 *   name varchar(255) NOT NULL,
 *   contract_no varchar(255),
 *   customer_no varchar(255),
 *   address_id int(5),
 *   comment varchar(10000),
 *   startdate date,
 *   enddate date,
 *   cancelation_period_count int(4),
 *   cancelation_period_type int(4),
 *   first_min_runtime_count int(4),
 *   first_min_runtime_type int(4),
 *   next_min_runtime_count int(4),
 *   next_min_runtime_type int(4),
 *   money_once double,
 *   money_per_day double,
 *   money_per_week double,
 *   money_per_month double,
 *   money_per_year double,
 *   UNIQUE (id),
 *   PRIMARY KEY (id)
 * );
 * </pre>
 * 
 * <br>
 * Getters and setters for the primary key (id) are not needed. Every one of the
 * following methods has to throw a RemoteException. <br>
 * 
 * #############################################################################
 * # IMPORTANT: # # All business objects are RMI objects. So you have to run the
 * # # rmi compiler (rmic) to create the needed stubs. # # Without that you will
 * get a # # java.lang.reflect.InvocationTargetException # # (caused by
 * java.rmi.StubNotFoundException) #
 * #############################################################################
 * 
 * @author willuhn, jrieke
 */
public interface Contract extends DBObject {
	
	public enum IntervalType {
		ONCE, DAYS, WEEKS, MONTHS, YEARS;
		
		@Override
		public String toString() {
			//only capitalize the first letter
			String s = super.toString();
			return s.substring(0, 1) + s.substring(1).toLowerCase();
		}

		public String toAdjectiveString() {
			return getAdjectives()[ordinal()];
		}
		public static String[] getAdjectives() {
			return new String[] {"once", "daily", "weekly", "monthly", "annual"};
		}
		public static IntervalType adjectiveValueOf(String text) {
			for (int i = 0; i<IntervalType.values().length; i++) {
				if (IntervalType.getAdjectives()[i].equals(text))
					return IntervalType.values()[i];
			}
			return ONCE;
		}
	};

	/**
	 * Returns the name of the contract.
	 * 
	 * @return name of the contract.
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException;
	public void setName(String name) throws RemoteException;
	
	public String getContractNumber() throws RemoteException;
	public void setContractNumber(String contractNo) throws RemoteException;
	
	public String getCustomerNumber() throws RemoteException;
	public void setCustomerNumber(String customerNo) throws RemoteException;
	
	public Address getAddress() throws RemoteException;
	public void setAddress(Address address) throws RemoteException;
	
	public String getComment() throws RemoteException;
	public void setComment(String comment) throws RemoteException;
	
	public Date getStartDate() throws RemoteException;
	public void setStartDate(Date start) throws RemoteException;
	
	public Date getEndDate() throws RemoteException;
	public void setEndDate(Date end) throws RemoteException;
	
	public Integer getCancellationPeriodCount() throws RemoteException;
	public void setCancelationPeriodCount(Integer count) throws RemoteException;
	
	public IntervalType getCancellationPeriodType() throws RemoteException;
	public void setCancelationPeriodType(IntervalType type) throws RemoteException;
	
	public Integer getFirstMinRuntimeCount() throws RemoteException;
	public void setFirstMinRuntimeCount(Integer count) throws RemoteException;
	
	public IntervalType getFirstMinRuntimeType() throws RemoteException;
	public void setFirstMinRuntimeType(IntervalType type) throws RemoteException;

	public Integer getNextMinRuntimeCount() throws RemoteException;
	public void setNextMinRuntimeCount(Integer count) throws RemoteException;
	
	public IntervalType getNextMinRuntimeType() throws RemoteException;
	public void setNextMinRuntimeType(IntervalType type) throws RemoteException;
	
	public String getURI() throws RemoteException;
	public void setURI(String uri) throws RemoteException;

	public DBIterator getTransactions() throws RemoteException;
	public DBIterator getCosts() throws RemoteException;

	//these are derived features
	public Date getNextTermBegin() throws RemoteException;
	public Date getNextTermEnd() throws RemoteException;
	public Date getNextCancellationDeadline() throws RemoteException;
	public Date getNextCancellationDeadline(Date after) throws RemoteException;
	public double getCostsPerTerm() throws RemoteException;
}