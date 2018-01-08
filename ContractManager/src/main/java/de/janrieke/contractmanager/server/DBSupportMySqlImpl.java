/*
 *   This file is part of ContractManager for Jameica.
 *   Copyright (C) 2010-2018  Jan Rieke
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
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * GPLv2
 *
 **********************************************************************/

package de.janrieke.contractmanager.server;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.text.MessageFormat;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.willuhn.jameica.messaging.BootMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Implementierung des Datenbank-Supports fuer MySQL.
 */
public class DBSupportMySqlImpl extends AbstractDBSupportImpl {
	private static final long serialVersionUID = 4145858401444376135L;

	private final static String DRIVER = "com.mysql.jdbc.Driver";

	@Override
	public String getJdbcDriver() {
		return ContractDBServiceImpl.SETTINGS.getString("database.driver.mysql.jdbcdriver", DRIVER);
	}

	@Override
	public String getJdbcPassword() {
		return ContractDBServiceImpl.SETTINGS.getString("database.driver.mysql.password", null);
	}

	@Override
	public String getJdbcUrl() {
		return ContractDBServiceImpl.SETTINGS.getString("database.driver.mysql.jdbcurl",
				"jdbc:mysql://localhost:3306/contractmanager?useUnicode=Yes&characterEncoding=ISO8859_1&serverTimezone=Europe/Paris");
	}

	@Override
	public String getJdbcUsername() {
		return ContractDBServiceImpl.SETTINGS.getString("database.driver.mysql.username", "contractmanager");
	}

	/**
	 * Ueberschrieben, weil SQL-Scripts bei MySQL nicht automatisch
	 * durchgefuehrt werden. Andernfalls wuerde jeder Client beim
	 * ersten Start versuchen, diese anzulegen. Das soll der Admin
	 * sicherheitshalber manuell durchfuehren. Wir hinterlassen stattdessen nur
	 * einen Hinweistext mit den auszufuehrenden SQL-Scripts.
	 */
	@Override
	public void execute(Connection conn, File sqlScript) throws RemoteException {
		if (sqlScript == null) {
			return; // Ignore
		}

		File f = new File(sqlScript.getParent(), getScriptPrefix() + sqlScript.getName());
		if (f.exists()) {
			I18N i18n = Application.getPluginLoader().getPlugin(ContractManagerPlugin.class).getResources()
					.getI18N();

			String text = i18n.tr(
					"Bei der Verwendung von MySQL wird die Datenbank "
							+ "nicht automatisch angelegt. Bitte führen Sie das folgende SQL-Script "
							+ "manuell aus, falls Sie dies nicht bereits getan haben:\n{0}",
					f.getAbsolutePath());

			BootMessage msg = new BootMessage(text);
			msg.setTitle(i18n.tr("Hinweis zur Verwendung von MySQL"));
			Application.getMessagingFactory().getMessagingQueue("jameica.boot").queueMessage(msg);
		}
	}

	@Override
	public String getScriptPrefix() throws RemoteException {
		return "mysql-";
	}

	@Override
	public String getSQLTimestamp(String content) throws RemoteException {
		return MessageFormat.format("(UNIX_TIMESTAMP({0})*1000)", new Object[] { content });
	}

	@Override
	public boolean getInsertWithID() throws RemoteException {
		return false;
	}

	@Override
	public int getTransactionIsolationLevel() throws RemoteException {
		// damit sehen wir Datenbank-Updates durch andere
		// ohne vorher ein COMMIT machen zu muessen
		// Insbesondere bei MySQL sinnvoll.
		return Connection.TRANSACTION_READ_COMMITTED;
	}
}
