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
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.IGrid;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesLabel;
import org.swtchart.ITitle;
import org.swtchart.LineStyle;
import org.swtchart.ext.InteractiveChart;
import org.swtchart.internal.Util;
import org.swtchart.internal.series.BarSeries;
import org.swtchart.internal.series.SeriesLabel;

import de.janrieke.contractmanager.Settings;
import de.willuhn.datasource.BeanUtil;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.Font;
import de.willuhn.jameica.gui.util.SWTUtil;
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
      tick.setFormat(Settings.DECIMALFORMAT);
      tick.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    
    ////////////////////////////////////////////////////////////////////////////
    // Neu zeichnen
    List<ChartData> data = getData();
    final int[][] cValues = new int[data.size()][];
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
        //dataLine.add(new Double(0));
        //labelLine.add("");
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
          //if (Math.abs(n.doubleValue()) < 0.01d)
          //  continue; // ueberspringen, nix drin
          dataLine.add(n);
          labelLine.add(label.toString());
        }
      }
      if (dataLine.size() == 0)
        continue; // wir haben gar keine Werte

      IAxis axis = this.chart.getAxisSet().getXAxis(0);
      axis.getTick().setVisible(false);
      axis.setCategorySeries(labelLine.toArray(new String[labelLine.size()]));
      axis.enableCategory(true);
      //axis.getTitle().setText("Expenses");

      IBarSeries barSeries = (IBarSeries) this.chart.getSeriesSet().createSeries(SeriesType.BAR,cd.getLabel());
      barSeries.setYSeries(toArray(dataLine));
      cValues[i] = ColorGenerator.create(ColorGenerator.PALETTE_RICH + i);
      barSeries.setBarColor(getColor(new RGB(cValues[i][0],cValues[i][1],cValues[i][2])));

      SeriesLabel label = new SeriesLabel() {

		@Override
		protected void draw(GC gc, int h, int v, double ySeriesValue,
				int seriesIndex, int alignment) {
			if (ySeriesValue > 0.0) {
		        gc.setForeground(color);
				gc.setFont(getFont());

				// get format
				String format1 = getFormat();
				if (format1 == null || format1.equals("")) {
					return;
				}

				// get text
				String text;
				text = new DecimalFormat(format1).format(ySeriesValue);

				// draw label
				Point p = Util.getExtentInGC(font, text);
				if (h - p.x / 2d > 0) {
					gc.drawString(text, (int) (h - p.x / 2d), (int) (v - p.y / 2d), true);
				} else {
					gc.drawString(text, 2*h + 6, (int) (v - p.y / 2d), true);
				}
			}
		}
    	  
      };
      label.setFont(Font.SMALL.getSWTFont());
      label.setFormat("'"+cd.getLabel()+": '"+Settings.DECIMALFORMAT.toPattern()+" ¤");
      //label.setForeground(GUI.getDisplay().getSystemColor(SWT.COLOR_BLACK));
      label.setVisible(true);

      try {
    	  ((BarSeries)barSeries).setLabel(label);
      } catch (NoSuchMethodError e) {
    	  //wrong SWTChart version loaded, custom labels won't work
    	  GUI.getStatusBar().setErrorText(
    			  Settings.i18n().tr("Unable to set custom labels in SWTChart, charts may look weird. Wrong SWTChart version loaded?"));
    	  ISeriesLabel altlabel = ((BarSeries)barSeries).getLabel();
    	  altlabel.setFont(Font.SMALL.getSWTFont());
    	  altlabel.setFormat("'"+cd.getLabel()+": '"+Settings.DECIMALFORMAT.toPattern()+" ¤");
    	  altlabel.setVisible(true);
      }

      //barSeries.enableStack(true);
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