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
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/DBSupport.java,v $
 * $Revision: 1.9 $
 * $Date: 2010-11-02 12:02:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.janrieke.contractmanager.rmi;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Connection;

/**
 * Interface fuer eine unterstuetzte Datenbank.
 * Fuer den Suppoert einer neuen Datenbank (z.Bsp. MySQL)
 * in Hibiscus muss dieses Interface implementiert werden.
 */
public interface DBSupport extends Serializable
{
  /**
   * Liefert die JDBC-URL.
   * @return die JDBC-URL.
   */
  public String getJdbcUrl();

  /**
   * Liefert den Klassennamen des JDBC-Treibers.
   * @return der JDBC-Treiber.
   */
  public String getJdbcDriver();

  /**
   * Liefert den Usernamen des Datenbank-Users.
   * @return Username.
   */
  public String getJdbcUsername();

  /**
   * Liefert das Passwort des Datenbank-Users.
   * @return das Passwort.
   */
  public String getJdbcPassword();
  
  /**
   * Prueft die Datenbankverbindung.
   * @param conn die Datenbank-Connection.
   * @throws RemoteException Wenn die Verbindung defekt ist und vom DB-Service neu erzeugt werden muss.
   */
  public void checkConnection(Connection conn) throws RemoteException;

  /**
   * Richtet ggf. die Datenbank ein.
   * @throws RemoteException
   */
  public void install() throws RemoteException;
  
  /**
   * Fuehrt ein SQL-Update-Script auf der Datenbank aus.
   * @param conn die Datenbank-Connection.
   * @param sqlScript das SQL-Script.
   * @throws RemoteException
   */
  public void execute(Connection conn, File sqlScript) throws RemoteException;
  
  /**
   * Liefert einen Dateinamens-Prefix, der SQL-Scripts vorangestellt werden soll.
   * @return Dateinamens-Prefix.
   * @throws RemoteException
   */
  public String getScriptPrefix() throws RemoteException;

  /**
   * Liefert den Namen der SQL-Funktion, mit der die Datenbank aus einem DATE-Feld einen UNIX-Timestamp macht.
   * Bei MySQL ist das z.Bsp. "UNIX_TIMESTAMP".
   * @param content der Feld-Name.
   * @return Name der SQL-Funktion samt Parameter. Also zum Beispiel "TONUMBER(datum)".
   * @throws RemoteException
   */
  public String getSQLTimestamp(String content) throws RemoteException;
  
  /**
   * Legt fest, ob SQL-Insert-Queries mit oder ohne ID erzeugt werden sollen.
   * @return true, wenn die Insert-Queries mit ID erzeugt werden.
   * @throws RemoteException
   * Siehe auch: de.willuhn.datasource.db.DBServiceImpl#getInsertWithID()
   */
  public boolean getInsertWithID() throws RemoteException;

  /**
   * Liefert das Transaction-Isolation-Level.
   * @return das Transaction-Isolation-Level.
   * @throws RemoteException
   */
  public int getTransactionIsolationLevel() throws RemoteException;

}