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
 * CREATE TABLE address (
 *  id NUMERIC default UNIQUEKEY('address'),
 *  name varchar(255) NOT NULL,
 *  street varchar(255),
 *  number int(4),
 *  extra varchar(255),
 *  zipcode int(4),
 *  city varchar(255),
 *  state varchar(255),
 *  country varchar(255),
 *  UNIQUE (id),
 *  PRIMARY KEY (id)
 * );
 */
public interface Address extends DBObject {
	public String getName() throws RemoteException;
	public void setName(String name) throws RemoteException;

	public String getStreet() throws RemoteException;
	public void setStreet(String street) throws RemoteException;
	
	public String getNumber() throws RemoteException;
	public void setNumber(String number) throws RemoteException;
	
	public String getExtra() throws RemoteException;
	public void setExtra(String extra) throws RemoteException;
	
	public String getZipcode() throws RemoteException;
	public void setZipcode(String zipcode) throws RemoteException;
	
	public String getCity() throws RemoteException;
	public void setCity(String city) throws RemoteException;
	
	public String getState() throws RemoteException;
	public void setState(String state) throws RemoteException;
	
	public String getCountry() throws RemoteException;
	public void setCountry(String country) throws RemoteException;
	
	//these are derived features
	public static final String CONTRACT_COUNT = "contract_count";
	public int getContractCount() throws RemoteException;
}