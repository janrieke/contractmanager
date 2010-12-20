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
 * @author willuhn, jrieke
 */
public interface Setting extends DBObject {
	
	/**
	 * Returns the name of the contract.
	 * 
	 * @return name of the contract.
	 * @throws RemoteException
	 */
	public String getKey() throws RemoteException;
	public void setKey(String key) throws RemoteException;
	
	public String getValue() throws RemoteException;
	public void setValue(String value) throws RemoteException;
}