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
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/rmi/Version.java,v $
 * $Revision: 1.1 $
 * $Date: 2007-12-06 17:57:21 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.janrieke.contractmanager.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBObject;

/**
 * Interface fuer einen Datensatz in der Versionstabelle.
 */
public interface Version extends DBObject
{
  
  /**
   * Liefert den Namen der Version.
   * @return Name der Version.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
  
  /**
   * Speichert den Namen der Version.
   * @param name Name der Version.
   * @throws RemoteException
   */
  public void setName(String name) throws RemoteException;
  
  /**
   * Liefert den aktuellen Stand der Version.
   * @return Stand der Version.
   * @throws RemoteException
   */
  public int getVersion() throws RemoteException;
  
  /**
   * Legt die neue Versionsnummer fest.
   * @param newVersion die neue Versionsnummer.
   * @throws RemoteException
   */
  public void setVersion(int newVersion) throws RemoteException;

}


/*********************************************************************
 * $Log: Version.java,v $
 * Revision 1.1  2007-12-06 17:57:21  willuhn
 * @N Erster Code fuer das neue Versionierungs-System
 *
 **********************************************************************/