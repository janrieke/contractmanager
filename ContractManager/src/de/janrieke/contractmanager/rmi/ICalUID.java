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

import de.willuhn.datasource.rmi.DBObject;
/*
 * CREATE TABLE icaluids (
 *   uid varchar(255) NOT NULL,
 *   contract_id int(5),
 *   UNIQUE (uid),
 *   PRIMARY KEY (uid)
 * );
 */
public interface ICalUID extends DBObject {
	public Contract getContract() throws RemoteException;
	public void setContract(Contract contract) throws RemoteException;

	public String getUID() throws RemoteException;
	public void setUID(String uid) throws RemoteException;
}