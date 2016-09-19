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
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.chart.ChartData;
import de.janrieke.contractmanager.gui.chart.IncomeExpensesBarChart;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.janrieke.contractmanager.gui.control.IncomeExpensesAnalysisControl;
import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Part;

public class IncomeExpensesAnalysisChartPart implements Part {

	private IncomeExpensesAnalysisControl control;

	public IncomeExpensesAnalysisChartPart(IncomeExpensesAnalysisControl control) {
		this.control = control;
	}

    private IncomeExpensesBarChart chart = null;

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
		chart = new IncomeExpensesBarChart();
		chart.setTitle(Settings.i18n().tr("Income/Expenses Comparison"));
		addChartData();

		chart.paint(parent);
	}

    private double sum = 0;
	protected void addChartData() throws RemoteException {
		int month = control.getMonthNumber((String)control.getMonthSelector().getValue());
		int year = (Integer)control.getYearSelector().getValue();

		Calendar monthCal = Calendar.getInstance();
		monthCal.set(year, month, 1, 0, 0, 0);

	    sum = 0;
		GenericIterator<Contract> contracts = ContractControl.getContracts();
		while (contracts.hasNext()) {
			final Contract c = contracts.next();
			if (!c.isActiveInMonth(monthCal.getTime())) {
				continue;
			}
			sum += c.getMoneyPerMonth();

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
					List<ContractIncomeExpensesAnalysisData> list = new ArrayList<ContractIncomeExpensesAnalysisData>();
					float amount = (float)c.getMoneyPerMonth();
					if (amount > 0) {
						list.add(new ContractIncomeExpensesAnalysisData(amount, Settings.i18n().tr("Income")));
						list.add(new ContractIncomeExpensesAnalysisData(0, Settings.i18n().tr("Expenses")));
					}
					if (amount < 0) {
						list.add(new ContractIncomeExpensesAnalysisData(0, Settings.i18n().tr("Income")));
						list.add(new ContractIncomeExpensesAnalysisData(-amount, Settings.i18n().tr("Expenses")));
					}
					return list;
				}
			});
		}
		chart.setSum(sum);
	}

	public void redraw() {
        chart.removeAllData();
        try {
    		addChartData();
			chart.redraw();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
