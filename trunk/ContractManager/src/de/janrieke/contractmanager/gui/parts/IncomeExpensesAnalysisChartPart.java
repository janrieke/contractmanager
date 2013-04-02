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

public class IncomeExpensesAnalysisChartPart implements Part {

	public IncomeExpensesAnalysisChartPart() {
	}

    private BarChart chart = null;

    public class ContractIncomeExpensesAnalysisData {
    	
		private float amount;
		private String label;
		public ContractIncomeExpensesAnalysisData(float amount, String label) {
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
		chart.setTitle("Income/Expenses Comparison");
		chart.addData(new ChartData() {
			
			@Override
			public String getLabelAttribute() throws RemoteException {
				return Settings.i18n().tr("Label");
			}
			
			@Override
			public String getLabel() throws RemoteException {
				return Settings.i18n().tr("Income");
			}
			
			@Override
			public String getDataAttribute() throws RemoteException {
				return Settings.i18n().tr("Amount");
			}
			
			@Override
			public List<?> getData() throws RemoteException {
				List<ContractIncomeExpensesAnalysisData> list = new ArrayList<ContractIncomeExpensesAnalysisData>();
				GenericIterator contracts = ContractControl.getContracts();
				while (contracts.hasNext()) {
					Contract c = (Contract) contracts.next();
					float amount = (float)c.getMoneyPerMonth();
					if (amount > 0)
						list.add(new ContractIncomeExpensesAnalysisData(amount, c.getName()));
				}
				return list;
			}
		});
		chart.addData(new ChartData() {
			
			@Override
			public String getLabelAttribute() throws RemoteException {
				return Settings.i18n().tr("Label");
			}
			
			@Override
			public String getLabel() throws RemoteException {
				return Settings.i18n().tr("Expense");
			}
			
			@Override
			public String getDataAttribute() throws RemoteException {
				return Settings.i18n().tr("Amount");
			}
			
			@Override
			public List<?> getData() throws RemoteException {
				List<ContractIncomeExpensesAnalysisData> list = new ArrayList<ContractIncomeExpensesAnalysisData>();
				GenericIterator contracts = ContractControl.getContracts();
				while (contracts.hasNext()) {
					Contract c = (Contract) contracts.next();
					float amount = (float)c.getMoneyPerMonth();
					if (amount < 0)
						list.add(new ContractIncomeExpensesAnalysisData(-amount, c.getName()));
				}
				return list;
			}
		});

		chart.paint(parent);
	}

}
