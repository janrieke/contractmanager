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

import java.io.InputStream;
import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBObject;

/*
 * CREATE TABLE storage (
 * id IDENTITY,
 * contract_id int(5),
 * description varchar(255),
 * path varchar(65535),
 * file BLOB,
 * UNIQUE (id),
 * PRIMARY KEY (id),
 * CONSTRAINT fk_storage_contract FOREIGN KEY (contract_id) REFERENCES contract (id) ON DELETE CASCADE ON UPDATE CASCADE
 * );
 */
public interface Storage extends DBObject {
	public Contract getContract() throws RemoteException;
	public void setContract(Contract contract) throws RemoteException;
	public String getDescription() throws RemoteException;
	public void setDescription(String desc) throws RemoteException;
	public String getPath() throws RemoteException;
	public void setPath(String path) throws RemoteException;
	
	/**
	 * Returns a stream for reading the binary contents stored in the "file" field. 
	 * Note: All data must be read before calling any other database operation.
	 * Any other DB operation will implicitly close the stream.
	 * @return a stream with the file contents
	 * @throws RemoteException
	 */
	public InputStream getFile() throws RemoteException;
	//saving is only implemented via prepared statements 
}
