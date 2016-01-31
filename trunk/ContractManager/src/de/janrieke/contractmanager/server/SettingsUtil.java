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
 Copied from Hibiscus

 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/server/DBPropertyUtil.java,v $
 * $Revision: 1.2 $
 * $Date: 2008-09-17 23:44:29 $
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
import java.sql.ResultSet;
import java.sql.SQLException;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Setting;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Hilfsklasse zum Laden und Speichern der Properties.
 */
public class SettingsUtil
{
  /**
   * Speichert ein Property.
   * @param name Name des Property.
   * @param value Wert des Property.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public static void set(String name, String value) throws RemoteException, ApplicationException
  {
    Setting prop = find(name);
    if (prop == null)
    {
      Logger.warn("parameter name " + name + " invalid");
      return;
    }
    
    prop.setValue(value);
    prop.store();
  }
  
  /**
   * Liefert den Wert des Parameters.
   * @param name Name des Parameters.
   * @param defaultValue Default-Wert, wenn der Parameter nicht existiert oder keinen Wert hat.
   * @return Wert des Parameters.
   * @throws RemoteException
   */
  public static String get(String name, String defaultValue) throws RemoteException
  {
	  Setting prop = find(name);
    if (prop == null)
      return defaultValue;
    String value = prop.getValue();
    return value != null ? value : defaultValue;
  }
  
  /**
   * Fragt einen einzelnen Parameter-Wert ab, jedoch mit einem Custom-Query.
   * @param query Das SQL-Query.
   * Ggf. mit Platzhaltern ("?") fuer das PreparedStatement versehen.
   * @param params optionale Liste der Parameter fuer das Statement.
   * @param defaultValue optionaler Default-Wert, falls das Query <code>null</code> liefert.
   * @return der Wert aus der ersten Spalte des Resultsets oder der Default-Wert, wenn der Wert des Resultsets <code>null</code> ist.
   * @throws RemoteException
   */
  static String query(String query, Object[] params, final String defaultValue) throws RemoteException
  {
    if (query == null || query.length() == 0)
      return defaultValue;
    
    return (String) Settings.getDBService().execute(query,params,new ResultSetExtractor()
    {
      public Object extract(ResultSet rs) throws RemoteException, SQLException
      {
        if (!rs.next())
          return defaultValue;
        
        String result = rs.getString(1);
        return result != null ? result : defaultValue;
      }
    });
  }
  
  /**
   * Liefert den Parameter mit dem genannten Namen.
   * Wenn er nicht existiert, wird er automatisch angelegt.
   * @param name Name des Parameters. Darf nicht <code>null</code> sein.
   * @return der Parameter oder <code>null</code>, wenn kein Name angegeben wurde.
   * @throws RemoteException
   */
  private static Setting find(String name) throws RemoteException
  {
    if (name == null)
      return null;
    
    // Mal schauen, ob wir das Property schon haben
    DBService service = Settings.getDBService();
    DBIterator i = service.createList(Setting.class);
    i.addFilter("mkey = ?",new Object[]{name});
    if (i.hasNext())
      return (Setting) i.next();

    // Ne, dann neu anlegen
    Setting prop = (Setting) service.createObject(Setting.class,null);
    prop.setKey(name);
    return prop;
  }
}