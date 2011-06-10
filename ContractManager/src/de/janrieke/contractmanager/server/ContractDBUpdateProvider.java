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
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/HBCIUpdateProvider.java,v $
 * $Revision: 1.3 $
 * $Date: 2009-03-10 23:51:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.janrieke.contractmanager.server;

import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.rmi.Version;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.PluginResources;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.sql.version.UpdateProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Implementierung eines Update-Providers fuer Hibiscus.
 */
public class ContractDBUpdateProvider implements UpdateProvider
{
  private Version version     = null;
  private Connection conn     = null;
  private Manifest manifest   = null;
  private PluginResources res = null;

  /**
   * ct
   * @param conn Datenbank-Verbindung.
   * @param version Version der Datenbank.
   */
  protected ContractDBUpdateProvider(Connection conn, Version version)
  {
    this.conn    = conn;
    this.version = version;
    
    AbstractPlugin p = Application.getPluginLoader().getPlugin(ContractManagerPlugin.class);
    this.manifest    = p.getManifest();
    this.res         = p.getResources();
  }

  /**
   * @see de.willuhn.sql.version.UpdateProvider#getConnection()
   */
  public synchronized Connection getConnection() throws ApplicationException
  {
    return this.conn;
  }

  /**
   * @see de.willuhn.sql.version.UpdateProvider#getCurrentVersion()
   */
  public int getCurrentVersion() throws ApplicationException
  {
    try
    {
    	return this.version.getVersion();
        //return 15; //to test the db update  
    }
    catch (RemoteException re)
    {
      Logger.error("unable to read current version number");
      throw new ApplicationException(res.getI18N().tr("Unable to read current version number"));
    }
  }

  /**
   * @see de.willuhn.sql.version.UpdateProvider#getProgressMonitor()
   */
  public ProgressMonitor getProgressMonitor()
  {
    // Liefert den Splashscreen oder im Servermode einen
    // Pseudo-Monitor.
    return Application.getController().getApplicationCallback().getStartupMonitor();
  }

  /**
   * @see de.willuhn.sql.version.UpdateProvider#getUpdatePath()
   */
  public File getUpdatePath() throws ApplicationException
  {
    // Ist das Unterverzeichnis "plugins" im Plugin
    return new File(manifest.getPluginDir(),"updates");
  }

  /**
   * @see de.willuhn.sql.version.UpdateProvider#setNewVersion(int)
   */
  public void setNewVersion(int newVersion) throws ApplicationException
  {
    int current = getCurrentVersion();
    try
    {
      Logger.info("applying new version [" + this.version.getName() + "]: " + newVersion);
      this.version.setVersion(newVersion);
      this.version.store();
    }
    catch (Exception e)
    {
      // Im Fehlerfall Versionsnummer zuruecksetzen
      try
      {
        this.version.setVersion(current);
      }
      catch (Exception e2)
      {
        Logger.error("unable to rollback version",e2);
        // Werfen wir nicht, weil es sonst die eigentliche Exception verdecken wuerde
      }

      if (e instanceof ApplicationException)
        throw (ApplicationException) e;
      
      Logger.error("unable to read current version number",e);
      throw new ApplicationException(res.getI18N().tr("Unable to read current version number"));
    }
  }
  
  /**
   * Liefert die Plugin-Ressourcen.
   * @return die Plugin-Ressourcen.
   */
  public PluginResources getResources()
  {
    return this.res;
  }

}