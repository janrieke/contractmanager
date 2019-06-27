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
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/HBCIDBServiceImpl.java,v $
 * $Revision: 1.31 $
 * $Date: 2010-11-02 11:32:09 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.janrieke.contractmanager.server;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.rmi.ContractDBService;
import de.janrieke.contractmanager.rmi.DBSupport;
import de.willuhn.datasource.db.DBServiceImpl;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.sql.version.Updater;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.MultipleClassLoader;
import de.willuhn.util.ProgressMonitor;

/**
 * @author willuhn
 */
public class ContractDBServiceImpl extends DBServiceImpl implements ContractDBService {

	/**
	 * The UID.
	 */
	private static final long serialVersionUID = 5173741078003839220L;

	private DBSupport driver = null;

	/**
	 * @throws RemoteException
	 */
	public ContractDBServiceImpl() throws RemoteException {
		this(SETTINGS.getString("database.driver",
				DBSupportH2Impl.class.getName()));
	}

	/**
	 * Konstruktor mit expliziter Angabe des Treibers.
	 *
	 * @param driverClass
	 *            der zu verwendende Treiber.
	 * @throws RemoteException
	 */
	public ContractDBServiceImpl(String driverClass) throws RemoteException {
		super();
		System.setProperty(ContractDBServiceImpl.class.getName() + ".schema","PUBLIC");
		MultipleClassLoader cl = Application.getPluginLoader()
				.getPlugin(ContractManagerPlugin.class).getManifest()
				.getClassLoader();
		this.setClassloader(cl);
		this.setClassFinder(cl.getClassFinder());
		if (driverClass == null) {
			throw new RemoteException("no driver given");
		}
		Logger.info("loading database driver: " + driverClass);
		try {
			Class<?> c = cl.load(driverClass);
			this.driver = (DBSupport) c.newInstance();
		} catch (Throwable t) {
			throw new RemoteException("unable to load database driver "
					+ driverClass, t);
		}
	}

	/**
	 * @see de.willuhn.datasource.Service#getName()
	 */
	@Override
	public String getName() throws RemoteException {
		I18N i18n = Application.getPluginLoader()
				.getPlugin(ContractManagerPlugin.class).getResources()
				.getI18N();
		return i18n.tr("ContractManager database service");
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#getAutoCommit()
	 */
	@Override
	protected boolean getAutoCommit() throws RemoteException {
		return SETTINGS.getBoolean("autocommit", super.getAutoCommit());
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcDriver()
	 */
	@Override
	protected String getJdbcDriver() throws RemoteException {
		return this.driver.getJdbcDriver();
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcPassword()
	 */
	@Override
	protected String getJdbcPassword() throws RemoteException {
		return this.driver.getJdbcPassword();
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcUrl()
	 */
	@Override
	protected String getJdbcUrl() throws RemoteException {
		return this.driver.getJdbcUrl();
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcUsername()
	 */
	@Override
	protected String getJdbcUsername() throws RemoteException {
		return this.driver.getJdbcUsername();
	}

	/**
	 * @see de.willuhn.jameica.ContractDBService.rmi.HBCIDBService#checkConsistency()
	 */
	@Override
	public void checkConsistency() throws RemoteException, ApplicationException {
		Logger.info("init update provider");
		UpdateProvider provider = new ContractDBUpdateProvider(getConnection(),
				VersionUtil.getVersion(this, "contract_db"));
		Updater updater = new Updater(provider);
		updater.execute();
		Logger.info("updates finished");
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#getConnection()
	 */
	@Override
	public Connection getConnection() throws RemoteException {
		try {
			return super.getConnection();
		} catch (RemoteException re) {
			// Wir benachrichtigen Jameica ueber den Fehler, damit beim Shutdown
			// kein Backup erstellt wird
			Application.getMessagingFactory()
					.getMessagingQueue("jameica.error")
					.sendMessage(new QueryMessage(re));
			throw re;
		}
	}

	/**
	 * @see de.willuhn.jameica.ContractDBService.rmi.HBCIDBService#install()
	 */
	@Override
	public void install() throws RemoteException {
		I18N i18n = Application.getPluginLoader()
				.getPlugin(ContractManagerPlugin.class).getResources()
				.getI18N();
		ProgressMonitor monitor = Application.getCallback().getStartupMonitor();
		monitor.setStatusText(i18n.tr("Installing ContractManager"));
		this.driver.install();

		Manifest mf = Application.getPluginLoader()
				.getPlugin(ContractManagerPlugin.class).getManifest();
		File file = new File(mf.getPluginDir() + File.separator + "sql",
				"create.sql");
		this.driver.execute(getConnection(), file);
	}

	/**
	 * @see de.willuhn.jameica.ContractDBService.rmi.HBCIDBService#getSQLTimestamp(java.lang.String)
	 */
	@Override
	public String getSQLTimestamp(String content) throws RemoteException {
		return this.driver.getSQLTimestamp(content);
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#getInsertWithID()
	 */
	@Override
	protected boolean getInsertWithID() throws RemoteException {
		return this.driver.getInsertWithID();
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#checkConnection(java.sql.Connection)
	 */
	@Override
	protected void checkConnection(Connection conn) throws SQLException {
		try {
			this.driver.checkConnection(conn);
		} catch (RemoteException re) {
			throw new SQLException(re.getMessage());
		}
		super.checkConnection(conn);
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#getTransactionIsolationLevel()
	 */
	@Override
	protected int getTransactionIsolationLevel() throws RemoteException {
		// BUGZILLA 447
		return this.driver.getTransactionIsolationLevel();
	}

	/**
	 * @see de.willuhn.jameica.ContractDBService.rmi.HBCIDBService#getDriver()
	 */
	@Override
	public DBSupport getDriver() throws RemoteException {
		return this.driver;
	}
}