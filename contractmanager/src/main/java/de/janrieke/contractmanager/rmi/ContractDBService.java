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
/*****************************************************************************
 * Kopie aus Hibiscus
 * 
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/HBCIDBService.java,v $
 * $Revision: 1.8 $
 * $Date: 2010-11-02 12:02:20 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
****************************************************************************/
package de.janrieke.contractmanager.rmi;

import java.rmi.RemoteException;
import java.sql.Connection;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;

/**
 * Interface fuer den Datenbank-Service von Hibiscus.
 * @author willuhn
 */
public interface ContractDBService extends DBService
{
  /**
   * Einstellungen fuer die DB-Services.
   */
  public final static Settings SETTINGS = new Settings(ContractDBService.class);

  /**
   * Initialisiert/erzeugt die Datenbank.
   * @throws RemoteException Wenn beim Initialisieren ein Fehler auftrat.
   */
  public void install() throws RemoteException;
  
  /**
   * Checkt die Konsistenz der Datenbank und fuehrt bei Bedarf Updates durch.
   * @throws RemoteException Wenn es beim Pruefen der Datenbank-Konsistenz zu einem Fehler kam.
   * @throws ApplicationException wenn die Datenbank-Konsistenz nicht gewaehrleistet ist.
   */
  public void checkConsistency() throws RemoteException, ApplicationException;
  
  /**
   * Liefert den verwendeten Treiber.
   * @return der Treiber.
   * @throws RemoteException
   */
  public DBSupport getDriver() throws RemoteException;
  
  /**
   * Liefert den Namen der SQL-Funktion, mit der die Datenbank aus einem DATE-Feld einen UNIX-Timestamp macht.
   * Bei MySQL ist das z.Bsp. "UNIX_TIMESTAMP".
   * @param content der Feld-Name.
   * @return Name der SQL-Funktion samt Parameter. Also zum Beispiel "TONUMBER(datum)".
   * @throws RemoteException
   */
  public String getSQLTimestamp(String content) throws RemoteException;

  /**
   * Returns the current connection.
   * @return connection
   * @throws RemoteException
   */
  public Connection getConnection() throws RemoteException;
}