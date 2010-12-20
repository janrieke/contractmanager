/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/DBPropertyImpl.java,v $
 * $Revision: 1.1 $
 * $Date: 2008-05-30 14:23:48 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.janrieke.contractmanager.server;

import java.rmi.RemoteException;

import de.janrieke.contractmanager.rmi.Setting;
import de.willuhn.datasource.db.AbstractDBObject;

/**
 * Speichert ein einzelnes Property in der Datenbank.
 */
public class SettingImpl extends AbstractDBObject implements Setting {

	/**
	 * The UID.
	 */
	private static final long serialVersionUID = 2994333719211540630L;

	/**
	 * ct
	 * 
	 * @throws RemoteException
	 */
	public SettingImpl() throws RemoteException {
		super();
	}

	/**
	 * @see de.willuhn.datasource.db.AbstractDBObject#getPrimaryAttribute()
	 */
	public String getPrimaryAttribute() throws RemoteException {
		return "key";
	}

	/**
	 * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
	 */
	protected String getTableName() {
		return "settings";
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBProperty#getName()
	 */
	public String getKey() throws RemoteException {
		return (String) getAttribute("key");
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBProperty#setValue(java.lang.String)
	 */
	public void setKey(String key) throws RemoteException {
		setAttribute("key", key);
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBProperty#getValue()
	 */
	public String getValue() throws RemoteException {
		return (String) getAttribute("value");
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBProperty#setValue(java.lang.String)
	 */
	public void setValue(String value) throws RemoteException {
		setAttribute("content", value);
	}

}