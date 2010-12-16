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
import de.janrieke.contractmanager.rmi.DBSupport;
import de.janrieke.contractmanager.rmi.ContractDBService;
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
		MultipleClassLoader cl = Application.getPluginLoader()
				.getPlugin(ContractManagerPlugin.class).getResources()
				.getClassLoader();
		this.setClassloader(cl);
		this.setClassFinder(cl.getClassFinder());
		if (driverClass == null)
			throw new RemoteException("no driver given");
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
	public String getName() throws RemoteException {
		I18N i18n = Application.getPluginLoader()
				.getPlugin(ContractManagerPlugin.class).getResources()
				.getI18N();
		return i18n.tr("ContractManager database service");
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#getAutoCommit()
	 */
	protected boolean getAutoCommit() throws RemoteException {
		return SETTINGS.getBoolean("autocommit", super.getAutoCommit());
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcDriver()
	 */
	protected String getJdbcDriver() throws RemoteException {
		return this.driver.getJdbcDriver();
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcPassword()
	 */
	protected String getJdbcPassword() throws RemoteException {
		return this.driver.getJdbcPassword();
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcUrl()
	 */
	protected String getJdbcUrl() throws RemoteException {
		return this.driver.getJdbcUrl();
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#getJdbcUsername()
	 */
	protected String getJdbcUsername() throws RemoteException {
		return this.driver.getJdbcUsername();
	}

	/**
	 * @see de.willuhn.jameica.ContractDBService.rmi.HBCIDBService#checkConsistency()
	 */
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
	protected Connection getConnection() throws RemoteException {
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
	public String getSQLTimestamp(String content) throws RemoteException {
		return this.driver.getSQLTimestamp(content);
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#getInsertWithID()
	 */
	protected boolean getInsertWithID() throws RemoteException {
		return this.driver.getInsertWithID();
	}

	/**
	 * @see de.willuhn.datasource.db.DBServiceImpl#checkConnection(java.sql.Connection)
	 */
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
	protected int getTransactionIsolationLevel() throws RemoteException {
		// BUGZILLA 447
		return this.driver.getTransactionIsolationLevel();
	}

	/**
	 * @see de.willuhn.jameica.ContractDBService.rmi.HBCIDBService#getDriver()
	 */
	public DBSupport getDriver() throws RemoteException {
		return this.driver;
	}
}