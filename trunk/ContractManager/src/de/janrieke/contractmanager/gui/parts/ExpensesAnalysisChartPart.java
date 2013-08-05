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

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.chart.BarChart;
import de.janrieke.contractmanager.gui.chart.ChartData;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Part;

public class ExpensesAnalysisChartPart implements Part {

	public ExpensesAnalysisChartPart() {
	}

    private BarChart chart = null;

    public class ContractExpensesAnalysisData {
    	
		private float amount;
		private String label;
		public ContractExpensesAnalysisData(float amount, String label) {
			super();
			this.amount = amount;
			this.label = label;
		}
    	public double getAmount() {
			return amount;
		}
    	public String getLabel() {
			return label;
		}
    }
    
	@Override
	public void paint(Composite parent) throws RemoteException {
		chart = new BarChart();
		chart.setTitle(Settings.i18n().tr("Expenses Overview"));
		GenericIterator contracts = ContractControl.getContracts();
		while (contracts.hasNext()) {
			final Contract c = (Contract) contracts.next();
			if ((float)c.getMoneyPerMonth() < 0 ) {
				chart.addData(new ChartData() {

					@Override
					public String getLabelAttribute() throws RemoteException {
						return "Label";
					}

					@Override
					public String getLabel() throws RemoteException {
						return c.getName();
					}

					@Override
					public String getDataAttribute() throws RemoteException {
						return "Amount";
					}

					@Override
					public List<?> getData() throws RemoteException {
						List<ContractExpensesAnalysisData> list = new ArrayList<ContractExpensesAnalysisData>();
						list.add(new ContractExpensesAnalysisData(-(float)c.getMoneyPerMonth(), c.getName()));
						return list;
					}
				});
			}
		}

		chart.paint(parent);
	}

}
