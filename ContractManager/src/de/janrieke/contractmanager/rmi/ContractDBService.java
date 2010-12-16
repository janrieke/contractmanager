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

}