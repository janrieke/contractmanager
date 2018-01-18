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
import java.util.Date;

import de.willuhn.datasource.rmi.DBObject;
/*
 * CREATE TABLE costs (
 *   id IDENTITY,
 *   contract_id int(5),
 *   description varchar(255),
 *   money double,
 *   period int(1),
 *   payday date,
 *   UNIQUE (id),
 *   PRIMARY KEY (id)
 * );
 */
public interface Costs extends DBObject {
	public Contract getContract() throws RemoteException;
	public void setContract(Contract contract) throws RemoteException;

	public String getDescription() throws RemoteException;
	public void setDescription(String description) throws RemoteException;

	public double getMoney() throws RemoteException;
	public void setMoney(double money) throws RemoteException;

	public Contract.IntervalType getPeriod() throws RemoteException;
	public void setPeriod(Contract.IntervalType period) throws RemoteException;

	public Date getPayday() throws RemoteException;
	public void setPayday(Date payday) throws RemoteException;

	public static final String NEXT_PAYDAY = "next_payday";
	public Date getNextPayday() throws RemoteException;

}