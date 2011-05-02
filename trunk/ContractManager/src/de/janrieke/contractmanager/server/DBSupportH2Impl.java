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
 * Kopie aus Hibiscus
 * 
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/DBSupportH2Impl.java,v $
 * $Revision: 1.12 $
 * $Date: 2010-11-02 12:02:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.janrieke.contractmanager.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.util.Date;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.rmi.ContractDBService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.Base64;

/**
 * Implementierung des Datenbank-Supports fuer H2-Database
 * (http://www.h2database.com).
 */
public class DBSupportH2Impl extends AbstractDBSupportImpl {
	/**
	 * The UID.
	 */
	private static final long serialVersionUID = -5956443204738547538L;

	/**
	 * ct.
	 */
	public DBSupportH2Impl() {
		// H2-Datenbank verwendet uppercase Identifier
		Logger.info("switching dbservice to uppercase");
		System.setProperty(ContractDBServiceImpl.class.getName() + ".uppercase",
				"true");

		try {
			Class<?> c = Application.getClassLoader().load("org.h2.engine.Constants");
			Method m = c.getMethod("getVersion", (Class[]) null);
			Logger.info("h2 version: " + m.invoke(null, (Object[]) null));
		} catch (Throwable t) {
			Logger.warn("unable to determine h2 version");
		}
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcDriver()
	 */
	public String getJdbcDriver() {
		return "org.h2.Driver";
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcPassword()
	 */
	public String getJdbcPassword() {
		String password = ContractDBService.SETTINGS.getString(
				"database.driver.h2.encryption.encryptedpassword", null);
		try {
			// Existiert noch nicht. Also neu erstellen.
			if (password == null) {
				// Wir koennen als Passwort nicht so einfach das Masterpasswort
				// nehmen, weil der User es aendern kann. Wir koennen zwar
				// das Passwort der Datenbank aendern. Allerdings kriegen wir
				// hier nicht mit, wenn sich das Passwort geaendert hat.
				// Daher erzeugen wir ein selbst ein Passwort.
				Logger.info("generating new random password for database");
				byte[] data = new byte[8];
				SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
				random.setSeed((long) (new Date().getTime()));
				random.nextBytes(data);

				// Jetzt noch verschluesselt abspeichern
				Logger.info("encrypting password with system certificate");
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				Application.getSSLFactory().encrypt(
						new ByteArrayInputStream(data), bos);

				// Verschluesseltes Passwort als Base64 speichern
				ContractDBService.SETTINGS.setAttribute(
						"database.driver.h2.encryption.encryptedpassword",
						Base64.encode(bos.toByteArray()));

				// Entschluesseltes Passwort als Base64 zurueckliefern, damit
				// keine Binaer-Daten drin sind.
				// Die Datenbank will es doppelt mit Leerzeichen getrennt haben.
				// Das erste ist fuer den User. Das zweite fuer die
				// Verschluesselung.
				String encoded = Base64.encode(data);
				return encoded + " " + encoded;
			}

			Logger.debug("decrypting database password");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Application.getSSLFactory().decrypt(
					new ByteArrayInputStream(Base64.decode(password)), bos);

			String encoded = Base64.encode(bos.toByteArray());
			return encoded + " " + encoded;
		} catch (Exception e) {
			throw new RuntimeException(
					"error while determining database password", e);
		}
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUrl()
	 */
	public String getJdbcUrl() {
		String url = "jdbc:h2:"
				+ Application.getPluginLoader()
						.getPlugin(ContractManagerPlugin.class).getResources()
						.getWorkPath() + "/h2db/contract_db";

		if (ContractDBService.SETTINGS.getBoolean("database.driver.h2.encryption",
				true))
			url += ";CIPHER="
					+ ContractDBService.SETTINGS.getString(
							"database.driver.h2.encryption.algorithm", "XTEA");
		if (ContractDBService.SETTINGS.getBoolean("database.driver.h2.recover",
				false)) {
			Logger.warn("#############################################################");
			Logger.warn("## DATABASE RECOVERY ACTIVATED                             ##");
			Logger.warn("#############################################################");
			url += ";RECOVER=1";
		}
		return url;
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBSupport#getJdbcUsername()
	 */
	public String getJdbcUsername() {
		return "contractmanager";
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBSupport#getScriptPrefix()
	 */
	public String getScriptPrefix() throws RemoteException {
		return "h2-";
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBSupport#getSQLTimestamp(java.lang.String)
	 */
	public String getSQLTimestamp(String content) throws RemoteException {
		// Nicht noetig
		// return MessageFormat.format("DATEDIFF('MS','1970-01-01 00:00',{0})",
		// new Object[]{content});
		return content;
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.DBSupport#getInsertWithID()
	 */
	public boolean getInsertWithID() throws RemoteException {
		return false;
	}

	/**
	 * @see de.willuhn.jameica.hbci.server.AbstractDBSupportImpl#checkConnection(java.sql.Connection)
	 */
	public void checkConnection(Connection conn) throws RemoteException {
		// brauchen wir bei nicht, da Embedded
	}
	
}