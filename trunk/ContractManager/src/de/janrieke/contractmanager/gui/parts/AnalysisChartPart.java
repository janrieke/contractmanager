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
package de.janrieke.contractmanager.gui.parts;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.janrieke.contractmanager.gui.chart.BarChart;
import de.janrieke.contractmanager.gui.chart.ChartData;
import de.willuhn.jameica.gui.Part;

public class AnalysisChartPart implements Part {

	public AnalysisChartPart() {
	}

    private BarChart chart = null;

    public class ContractAnalysisData {
    	
    	public ContractAnalysisData(float costs, String label) {
			super();
			this.costs = costs;
			this.label = label;
		}
		float costs;
    	String label;
    	public float getCosts() {return costs;};
    	public String getLabel() {return label;};
    }
    
	@Override
	public void paint(Composite parent) throws RemoteException {
		chart = new BarChart();
		chart.setTitle("Income/Expenses Comparison");
		chart.addData(new ChartData() {
			
			@Override
			public String getLabelAttribute() throws RemoteException {
				return "Label";
			}
			
			@Override
			public String getLabel() throws RemoteException {
				return "Monat";
			}
			
			@Override
			public String getDataAttribute() throws RemoteException {
				// TODO Auto-generated method stub
				return "Costs";
			}
			
			@Override
			public List<?> getData() throws RemoteException {
				List<ContractAnalysisData> list = new ArrayList<ContractAnalysisData>();
				list.add(new ContractAnalysisData(1f, "Januar"));
				list.add(new ContractAnalysisData(2f, "Februar"));
				list.add(new ContractAnalysisData(2.4f, "März"));
				return list;
			}
		});
		chart.addData(new ChartData() {
			
			@Override
			public String getLabelAttribute() throws RemoteException {
				return "Label";
			}
			
			@Override
			public String getLabel() throws RemoteException {
				return "Monat";
			}
			
			@Override
			public String getDataAttribute() throws RemoteException {
				// TODO Auto-generated method stub
				return "Costs";
			}
			
			@Override
			public List<?> getData() throws RemoteException {
				List<ContractAnalysisData> list = new ArrayList<ContractAnalysisData>();
				list.add(new ContractAnalysisData(1.2f, "Januar"));
				list.add(new ContractAnalysisData(1.7f, "Februar"));
				list.add(new ContractAnalysisData(2.2f, "März"));
				return list;
			}
		});

		chart.paint(parent);
	}

}
