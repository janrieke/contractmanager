/**********************************************************************
 * Kopie aus Hibiscus
 * 
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/VersionUtil.java,v $
 * $Revision: 1.3 $
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

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.janrieke.contractmanager.rmi.Version;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Statische Hilfsklasse zum Auslesen von Versionsnummern.
 */
public class VersionUtil
{
  /**
   * Liefert die Version zum angegebenen Namen.
   * Wenn das Versions-Objekt noch nicht existiert, wird es automatisch erstellt.
   * @param service der Datenbank-Service.
   * @param name Name der Version.
   * @return die Version.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static Version getVersion(DBService service, String name) throws RemoteException, ApplicationException
  {
    if (name == null || name.length() == 0)
    {
      I18N i18n = Application.getPluginLoader().getPlugin(ContractManagerPlugin.class).getResources().getI18N();
      throw new ApplicationException(i18n.tr("No name for version"));
    }
    DBIterator list = service.createList(Version.class);
    list.addFilter("name = ?", new String[]{name});
    if (list.hasNext())
      return (Version) list.next();
    
    // Neue Version erstellen
    Version v = (Version) service.createObject(Version.class,null);
    v.setName(name);
    v.store();
    return v;
  }
}