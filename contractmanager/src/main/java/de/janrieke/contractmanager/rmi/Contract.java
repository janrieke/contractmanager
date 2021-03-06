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
package de.janrieke.contractmanager.rmi;

import java.rmi.RemoteException;
import java.time.YearMonth;
import java.util.Date;
import java.util.List;

import de.janrieke.contractmanager.models.MonthlyCosts;
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
 *   uri varchar(4096),
 *   hibiscus_category varchar(255) NULL,
 *   ignore_cancellations int(1) NOT NULL,
 *   do_not_remind_before date,
 *   sepa_creditor varchar(35),
 *   sepa_customer varchar(35),
 *   runtime_snap_to_end int(1) NOT NULL,
 *   UNIQUE (id),
 *   PRIMARY KEY (id),
 *   CONSTRAINT fk_address FOREIGN KEY (address_id) REFERENCES address (id)
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

	public String getSepaCreditorRef() throws RemoteException;
	public void setSepaCreditorRef(String ref) throws RemoteException;

	public String getSepaCustomerRef() throws RemoteException;
	public void setSepaCustomerRef(String ref) throws RemoteException;

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

	public Integer getFollowingMinRuntimeCount() throws RemoteException;
	public void setFollowingMinRuntimeCount(Integer count) throws RemoteException;

	public IntervalType getFollowingMinRuntimeType() throws RemoteException;
	public void setFollowingMinRuntimeType(IntervalType type) throws RemoteException;

	public Boolean getDoNotRemind() throws RemoteException;
	public void setDoNotRemind(Boolean value) throws RemoteException;

	public String getURI() throws RemoteException;
	public void setURI(String uri) throws RemoteException;

	public String getHibiscusCategoryID() throws RemoteException;
	public void setHibiscusCategoryID(String category) throws RemoteException;

	public Date getDoNotRemindBefore() throws RemoteException;
	public void setDoNotRemindBefore(Date date) throws RemoteException;

	public Boolean getFixedTerms() throws RemoteException;
	public void setFixedTerms(Boolean value) throws RemoteException;

	//derived features
	public DBIterator<Transaction> getTransactions() throws RemoteException;
	public DBIterator<Costs> getCosts() throws RemoteException;

	public DBIterator<Storage> getAttachedFiles() throws RemoteException;

	public static final String NEXT_TERM_BEGIN = "next_term_begin";
	public Date getNextTermBegin() throws RemoteException;

	public static final String NEXT_TERM_END = "next_term_end";
	public Date getNextTermEnd() throws RemoteException;

	public static final String NEXT_CANCEL_TERM_BEGIN = "next_cancel_term_begin";
	public Date getNextCancelableTermBegin() throws RemoteException;

	public static final String NEXT_CANCEL_TERM_END = "next_cancel_term_end";
	public Date getNextCancelableTermEnd() throws RemoteException;

	public static final String NEXT_CANCELLATION_DEADLINE = "next_cancellation_deadline";
	public Date getNextCancellationDeadline() throws RemoteException;

	public static final String MONEY_PER_TERM = "costs_per_term";
	public double getMoneyPerTerm() throws RemoteException;

	public static final String MONEY_PER_MONTH = "costs_per_month";
	public double getMoneyPerMonth() throws RemoteException;

	public static final String PARTNER_NAME = "partner_name";
	public String getPartnerName() throws RemoteException;

	public static final String CONTRACT_NAME_PLUS_PARTNER_NAME = "contract_name_plus_partner_name";
	public String getContractAndPartnerName() throws RemoteException;

	//helper methods
	public Date getNextCancellationDeadline(Date after) throws RemoteException;

	public boolean isActiveInMonth(Date month) throws RemoteException;

	public void doNotRemindAboutNextCancellation() throws RemoteException;

	public boolean isNextDeadlineWithinNoticeTime() throws RemoteException;
	public boolean isNextDeadlineWithinWarningTime() throws RemoteException;
	public boolean hasValidRuntimeInformation() throws RemoteException;
	public Date getNextTermBeginAfter(Date startDate) throws RemoteException;
	List<MonthlyCosts> getCostsInMonth(YearMonth month, boolean usePaydays) throws RemoteException;
}