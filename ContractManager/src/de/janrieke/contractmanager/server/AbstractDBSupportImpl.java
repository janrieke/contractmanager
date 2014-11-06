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
 * Copy from Hibiscus
 * Copyright (c) by willuhn software & services
 * All rights reserved
 **********************************************************************/

package de.janrieke.contractmanager.server;

import java.io.File;
import java.io.FileReader;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.janrieke.contractmanager.rmi.DBSupport;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;

/**
 * Abstrakte Basisklasse fuer den Datenbank-Support.
 */
public abstract class AbstractDBSupportImpl implements DBSupport {

	/**
	 * The UID.
	 */
	private static final long serialVersionUID = -2724899073627593741L;

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBSupport#execute(java.sql.Connection,
	 *      java.io.File)
	 */
	@Override
	public void execute(Connection conn, File sqlScript) throws RemoteException {
		if (sqlScript == null)
			return;

		// Wir schreiben unseren Prefix davor.
		sqlScript = new File(sqlScript.getParent(), getScriptPrefix()
				+ sqlScript.getName());
		if (!sqlScript.exists()) {
			Logger.debug("file " + sqlScript + " does not exist, skipping");
			return;
		}

		if (!sqlScript.canRead() || !sqlScript.exists())
			return;

		Logger.info("executing sql script: " + sqlScript.getAbsolutePath());

		FileReader reader = null;

		try {
			reader = new FileReader(sqlScript);
			ScriptExecutor.execute(reader, conn);
		} catch (RemoteException re) {
			throw re;
		} catch (Exception e) {
			throw new RemoteException("error while executing sql script "
					+ sqlScript, e);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (Exception e3) {
				Logger.error("error while closing file " + sqlScript, e3);
			}
		}
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBSupport#install()
	 */
	@Override
	public void install() throws RemoteException {
		// Leere Dummy-Implementierung
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBSupport#getTransactionIsolationLevel()
	 */
	@Override
	public int getTransactionIsolationLevel() throws RemoteException {
		return -1;
	}

	private long lastCheck = 0;

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBSupport#checkConnection(java.sql.Connection)
	 */
	@Override
	public void checkConnection(Connection conn) throws RemoteException {
		long newCheck = System.currentTimeMillis();
		if ((newCheck - lastCheck) < (10 * 1000L))
			return; // Wir checken hoechstens alle 10 Sekunden

		Statement s = null;
		ResultSet rs = null;
		try {
			s = conn.createStatement();
			rs = s.executeQuery("select 1");
			lastCheck = newCheck;
		} catch (SQLException e) {
			// das Ding liefert in getMessage() den kompletten Stacktrace mit,
			// den brauchen wir
			// nicht (das muellt uns nur das Log voll) Also fangen wir sie und
			// werfen eine neue
			// saubere mit kurzem Fehlertext
			String msg = e.getMessage();
			if (msg != null && msg.indexOf("\n") != -1)
				msg = msg.substring(0, msg.indexOf("\n"));
			throw new RemoteException(msg);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (s != null)
					s.close();
			} catch (Exception e) {
				throw new RemoteException(
						"unable to close statement/resultset", e);
			}
		}
	}

}