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

/*
 * Partially copied from Hibiscus/Syntax, (c) by willuhn.webdesign
 */
package de.janrieke.contractmanager.ext.hibiscus;

import de.janrieke.contractmanager.ContractManagerPlugin;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Erweitert die Liste der Umsaetze um eine Spalte.
 */
public class UmsatzListPart implements Extension
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(ContractManagerPlugin.class).getResources().getI18N();
//  private Map<String,Buchung> cache = null;
  
  /**
   * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
   */
  public void extend(Extendable extendable)
  {
    if (extendable == null || !(extendable instanceof TablePart))
    {
      Logger.warn("invalid extendable, skipping extension");
      return;
    }
    
//    this.cache = null; // Cache loeschen, damit die Daten neu gelesen werden
    
    TablePart table = (TablePart) extendable;
    table.addColumn(i18n.tr("Vertrag"),"id-int", new Formatter() {
      public String format(Object o)
      {
        if (o == null || !(o instanceof Integer))
          return null;

//        Buchung b = (Buchung) getCache().get(o.toString());
//        if (b == null)
//          return null;
//        try
//        {
//          return Integer.toString(b.getBelegnummer());
//        }
//        catch (RemoteException re)
//        {
//          Logger.error("unable to load beleg number",re);
//        }
        return null;
      }
    
    });
  }
  
  /**
   * Liefert den Cache zum Lookup von Hibiscus Umsatz-ID zu Buchung.
   * @return der Cache.
   */
//  private Map<String,Buchung> getCache()
//  {
//    if (this.cache != null)
//      return this.cache;
//    
//    this.cache = new HashMap<String,Buchung>();
//    try
//    {
//      Geschaeftsjahr jahr = Settings.getActiveGeschaeftsjahr();
//      if (jahr != null)
//      {
//        DBIterator list = jahr.getHauptBuchungen();
//        list.addFilter("hb_umsatz_id is not null");
//        while (list.hasNext())
//        {
//          Buchung b = (Buchung) list.next();
//          if (b.getHibiscusUmsatzID() == null)
//            continue;
//          cache.put(b.getHibiscusUmsatzID(),b);
//        }
//      }
//    }
//    catch (Exception e)
//    {
//      Logger.error("unable to fill lookup cache",e);
//    }
//    return this.cache;
//  }

  /**
   * Fuegt eine Buchung manuell zum Cache hinzu.
   * @param b die neue Buchung.
   * @throws RemoteException
   */
//  void add(Buchung b) throws RemoteException
//  {
//    if (b == null)
//      return;
//    
//    String umsatzId = b.getHibiscusUmsatzID();
//    if (umsatzId == null || umsatzId.length() == 0)
//      return; // Buchung ist gar nicht zugeordnet
//    getCache().put(umsatzId,b);
//  }
}