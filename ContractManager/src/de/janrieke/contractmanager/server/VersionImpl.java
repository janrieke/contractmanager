/**********************************************************************
 * Kopie aus Hibiscus
 * 
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/VersionImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2007-12-11 00:33:35 $
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

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.rmi.Version;
import de.willuhn.datasource.db.AbstractDBObject;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Implementierung eines Versionsdatensatzes.
 */
public class VersionImpl extends AbstractDBObject implements Version {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5693390324363840763L;

	/**
	 * ct
	 * 
	 * @throws RemoteException
	 */
	public VersionImpl() throws RemoteException {
		super();
	}

	/**
	 * @see de.willuhn.datasource.db.AbstractDBObject#getPrimaryAttribute()
	 */
	public String getPrimaryAttribute() throws RemoteException {
		return "name";
	}

	/**
	 * @see de.willuhn.datasource.db.AbstractDBObject#getTableName()
	 */
	protected String getTableName() {
		return "version";
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Version#getName()
	 */
	public String getName() throws RemoteException {
		return (String) getAttribute("name");
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Version#getVersion()
	 */
	public int getVersion() throws RemoteException {
		// Wir fangen bei 0 an mit dem zaehlen
		Integer i = (Integer) getAttribute("version");
		return i == null ? 0 : i.intValue();
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Version#setVersion(int)
	 */
	public void setVersion(int newVersion) throws RemoteException {
		if (newVersion < 0)
			throw new RemoteException("version cannot be smaller than zero");
		setAttribute("version", new Integer(newVersion));
	}

	/**
	 * @see de.willuhn.jameica.hbci.rmi.Version#setName(java.lang.String)
	 */
	public void setName(String name) throws RemoteException {
		setAttribute("name", name);
	}

	/**
	 * @see de.willuhn.datasource.db.AbstractDBObject#insertCheck()
	 */
	protected void insertCheck() throws ApplicationException {
		I18N i18n = Application.getPluginLoader()
				.getPlugin(ContractManagerPlugin.class).getResources()
				.getI18N();

		try {
			if (getName() == null || getName().length() == 0) {
				throw new ApplicationException(
						i18n.tr("No name for version"));
			}
		} catch (RemoteException re) {
			Logger.error("error while checking version", re);
			throw new ApplicationException(
					i18n.tr("Error while checking version"));
		}
		super.insertCheck();
	}

	/**
	 * @see de.willuhn.datasource.db.AbstractDBObject#updateCheck()
	 */
	protected void updateCheck() throws ApplicationException {
		insertCheck();
	}

	/**
	 * @see de.willuhn.datasource.db.AbstractDBObject#insert()
	 */
	public void insert() throws RemoteException, ApplicationException {
		setVersion(getVersion()); // speichert automatisch die Startnummer
		super.insert();
	}

}