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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.IGrid;
import org.swtchart.ISeriesLabel;
import org.swtchart.ITitle;
import org.swtchart.LineStyle;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ext.InteractiveChart;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ColorGenerator;

/**
 * Implementierung eines Balken-Diagramms.
 */
public class BarChart extends AbstractChart<ChartData>
{
  private Composite comp = null;
  
  /**
   * @see de.willuhn.jameica.hbci.gui.chart.Chart#redraw()
   */
  public void redraw() throws RemoteException
  {
    // redraw ohne paint() Weia ;)
    if (this.comp == null || this.comp.isDisposed())
      return;

    // Cleanup
    SWTUtil.disposeChildren(this.comp);
    this.comp.setLayout(SWTUtil.createGrid(1,false));

    this.chart = new InteractiveChart(this.comp,SWT.BORDER);
    this.chart.setLayoutData(new GridData(GridData.FILL_BOTH));
    this.chart.getLegend().setVisible(false);
    this.chart.setOrientation(SWT.VERTICAL);

    ////////////////////////////////////////////////////////////////////////////
    // Farben des Charts
    this.chart.setBackground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    this.chart.setBackgroundInPlotArea(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Titel des Charts
    {
      ITitle title = this.chart.getTitle();
      title.setText(this.getTitle());
      title.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
      title.setFont(Font.BOLD.getSWTFont());
    }
    //
    ////////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////////
    // Layout der Achsen
    Color gray = getColor(new RGB(230,230,230));
    
    // X-Achse
    {
      IAxis axis = this.chart.getAxisSet().getXAxis(0);
      axis.getTitle().setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE)); // wenn wir den auch ausblenden, geht die initiale Skalierung kaputt. Scheint ein Bug zu sein

      IGrid grid = axis.getGrid();
      grid.setStyle(LineStyle.DOT);
      grid.setForeground(gray);

      axis.getTick().setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
    }
    
    // Y-Achse
    {
      IAxis axis = this.chart.getAxisSet().getYAxis(0);
      axis.getTitle().setVisible(false);

      IGrid grid = axis.getGrid();
      grid.setStyle(LineStyle.DOT);
      grid.setForeground(gray);
      
      IAxisTick tick = axis.getTick();
      tick.setFormat(HBCI.DECIMALFORMAT);
      tick.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////////
    // Neu zeichnen
    List<ChartData> data = getData();
    for (int i=0;i<data.size();++i)
    {
      final List<String> labelLine = new LinkedList<String>();
      final List<Number> dataLine  = new LinkedList<Number>();
      
      ChartData cd          = (ChartData) data.get(i);
      List<?> list             = cd.getData();
      String dataAttribute  = cd.getDataAttribute();
      String labelAttribute = cd.getLabelAttribute();

      if (list == null || list.size() == 0 || dataAttribute == null || labelAttribute == null)
      {
        Logger.debug("skipping data line, contains no data");
        dataLine.add(new Double(0));
        labelLine.add("");
      }
      else
      {
        for (Object o:list)
        {
          Object value = BeanUtil.get(o,dataAttribute);
          Object label = BeanUtil.get(o,labelAttribute);
          
          if (label == null || value == null || !(value instanceof Number))
            continue;

          Number n = (Number) value;
          if (Math.abs(n.doubleValue()) < 0.01d)
            continue; // ueberspringen, nix drin
          dataLine.add(n);
          labelLine.add(label.toString());
        }
      }
      if (dataLine.size() == 0)
        continue; // wir haben gar keine Werte

      IAxis axis = this.chart.getAxisSet().getXAxis(0);
      axis.setCategorySeries(labelLine.toArray(new String[labelLine.size()]));
      axis.enableCategory(true);

      IBarSeries barSeries = (IBarSeries) this.chart.getSeriesSet().createSeries(SeriesType.BAR,Integer.toString(i));
      barSeries.setYSeries(toArray(dataLine));
      
      //////////////////////////////////////////////////////////////////////////
      // Layout
      int[] cValues = ColorGenerator.create(ColorGenerator.PALETTE_OFFICE + i);
      barSeries.setBarColor(getColor(new RGB(cValues[0],cValues[1],cValues[2])));
      
      ISeriesLabel label = barSeries.getLabel();
      label.setFont(Font.SMALL.getSWTFont());
      label.setFormat(HBCI.DECIMALFORMAT.toPattern()); // BUGZILLA 1123
      label.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_WHITE));
      label.setVisible(true);
      //
      //////////////////////////////////////////////////////////////////////////
    }

    // Titel aktualisieren
    ITitle title = this.chart.getTitle();
    title.setText(this.getTitle());

    this.comp.layout();
    this.chart.getAxisSet().adjustRange();
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  public void paint(Composite parent) throws RemoteException
  {
    if (this.comp != null)
      return;
    
    this.comp = new Composite(parent,SWT.NONE);
    this.comp.setLayoutData(new GridData(GridData.FILL_BOTH));
    
    redraw();
    super.paint(parent);
  }
  
  /**
   * Wandelt die Liste in ein Array von doubles um.
   * @param list die Liste.
   * @return das Array.
   */
  private double[] toArray(List<Number> list)
  {
    double[] values = new double[list.size()];
    for (int i=0;i<list.size();++i)
    {
      values[i] = list.get(i).doubleValue();
    }
    return values;
  }
}