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

package de.janrieke.contractmanager.gui.chart;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MenuItem;
import org.swtchart.ext.Messages;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Implementierung der Charts.
 * @param <T> der Typ der Chartdaten.
 */
public abstract class AbstractChart<T extends ChartData> implements Chart<T>
{
  final static I18N i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();

  private String title             = null;
  private Map<RGB,Color> colors    = new HashMap<RGB,Color>();
  private List<T> data             = new ArrayList<T>();
  org.swtchart.Chart chart         = null;

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#setTitle(java.lang.String)
   */
  public void setTitle(String title)
  {
    this.title = title;
    if (this.chart != null && !this.chart.isDisposed())
      this.chart.getTitle().setText(this.title);
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#getTitle()
   */
  public String getTitle()
  {
    return this.title;
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#addData(de.willuhn.jameica.hbci.gui.chart.ChartData)
   */
  public void addData(T data)
  {
    if (data != null)
      this.data.add(data);
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#removeData(de.willuhn.jameica.hbci.gui.chart.ChartData)
   */
  public void removeData(T data)
  {
    if (data != null)
      this.data.remove(data);
  }

  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#removeAllData()
   */
  public void removeAllData()
  {
    if (this.data != null)
      this.data.clear();
  }

  /**
   * Liefert die anzuzeigenden Daten.
   * @return Anzuzeigende Daten.
   */
  List<T> getData()
  {
    return this.data;
  }
  
  /**
   * Erzeugt eine Farbe, die automatisch disposed wird.
   * Die Funktion hat einen internen Cache. Wenn die Farbe schon im Cache
   * vorhanden ist, wird diese genommen.
   * @param rgb der Farbcode.
   * @return die Farbe.
   */
  Color getColor(RGB rgb)
  {
    // Schon im Cache?
    Color c = this.colors.get(rgb);
    if (c != null && !c.isDisposed())
      return c;
    
    c = new Color(GUI.getDisplay(),rgb);
    this.colors.put(rgb,c);
    return c;
  }
  
  /**
   * Entfernt den Menu-Eintrag "Properties" aus dem InteractiveChart,
   * weil das nur in Eclipse funktioniert. In Jameica wuerde das
   * eine Exception im Main-Loop ausloesen.
   */
  private void cleanMenu()
  {
    try
    {
      MenuItem[] items = this.chart.getPlotArea().getMenu().getItems();
      if (items == null || items.length == 0)
        return;

      for (int i=0;i<items.length;++i)
      {
        MenuItem mi = items[i];
        String text = mi.getText();
        if (text == null)
          continue;
        if (text.equals(Messages.PROPERTIES))
        {
          mi.dispose();
          
          // Den Separator davor gleich noch entfernen
          if (i > 0) items[i-1].dispose();
          return;
        }
      }
    }
    catch (Exception e)
    {
      // Dann halt nicht ;)
    }
  }
  

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    parent.addDisposeListener(new DisposeListener() {
      /**
       * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
       */
      public void widgetDisposed(DisposeEvent e)
      {
        try
        {
          Iterator<Color> i = colors.values().iterator();
          while (i.hasNext())
          {
            Color c = i.next();
            if (c != null && !c.isDisposed())
              c.dispose();
          }
        }
        finally
        {
          colors.clear();
        }
      }
    });
    cleanMenu();
  }
}