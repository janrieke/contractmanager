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

/**
 * Interface of the business object for settings. According to the SQL table, we
 * define some getters and setters here.
 * 
 * <pre>
CREATE TABLE settings (
  key varchar(255) NOT NULL,
  value varchar(255),
  UNIQUE (key),
  PRIMARY KEY (key)
);
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
 * @author jrieke
 */
public interface Setting extends DBObject {
	
	public String getKey() throws RemoteException;
	public void setKey(String key) throws RemoteException;
	
	public String getValue() throws RemoteException;
	public void setValue(String value) throws RemoteException;
}