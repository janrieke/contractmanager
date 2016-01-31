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
import java.sql.SQLException;

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

    private boolean upper;

	/**
	 * ct
	 *
	 * @throws RemoteException
	 */
	public SettingImpl() throws RemoteException {
		super();
	}

	@Override
	protected void init() throws SQLException {
		super.init();
		upper = Boolean.getBoolean(getService().getClass().getName() + ".uppercase");
	}

	/**
	 * @see de.willuhn.datasource.db.AbstractDBObject#getPrimaryAttribute()
	 */
	@Override
	public String getPrimaryAttribute() throws RemoteException {
		return "mkey";
	}

	@Override
	protected String getIDField() {
	    return upper ? "MKEY" : "mkey";
	}

	/**
	 * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
	 */
	@Override
	protected String getTableName() {
		return "settings";
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBProperty#getName()
	 */
	@Override
	public String getKey() throws RemoteException {
		return (String) getAttribute("mkey");
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBProperty#setValue(java.lang.String)
	 */
	@Override
	public void setKey(String key) throws RemoteException {
		setAttribute("mkey", key);
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBProperty#getValue()
	 */
	@Override
	public String getValue() throws RemoteException {
		return (String) getAttribute("value");
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBProperty#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) throws RemoteException {
		setAttribute("value", value);
	}

}