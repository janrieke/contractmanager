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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.chart.BarChart;
import de.janrieke.contractmanager.gui.chart.ChartData;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.janrieke.contractmanager.gui.control.ExpensesAnalysisControl;
import de.janrieke.contractmanager.models.MonthlyCosts;
import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Part;

public class ExpensesAnalysisChartPart implements Part {

	private ExpensesAnalysisControl control;

	public ExpensesAnalysisChartPart(ExpensesAnalysisControl control) {
		this.control = control;
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
		addChartData();

		chart.paint(parent);
	}

	protected void addChartData() throws RemoteException {
		int month = control.getMonthNumber((String)control.getMonthSelector().getValue());
		int year = (Integer)control.getYearSelector().getValue();
		boolean usePaydays = (Boolean) control.getPayDayCheckbox().getValue();

		Calendar monthCal = Calendar.getInstance();
		monthCal.set(year, month, 1, 0, 0, 0);
		YearMonth yearMonth = YearMonth.of(year, month+1);

		GenericIterator<Contract> contracts = ContractControl.getContracts();
		while (contracts.hasNext()) {
			final Contract c = contracts.next();
			if (!c.isActiveInMonth(monthCal.getTime())) {
				continue;
			}

			List<MonthlyCosts> costsInMonth = c.getCostsInMonth(yearMonth, usePaydays);
			float negativeCosts = (float)costsInMonth.stream()
					.mapToDouble(MonthlyCosts::getMoney)
					.filter(d -> (d < 0))
					.sum();

			if (negativeCosts < 0 ) {
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
						List<ContractExpensesAnalysisData> list = new ArrayList<>();
						List<MonthlyCosts> costsInMonth = c.getCostsInMonth(yearMonth, usePaydays);
						float negativeCosts = (float)costsInMonth.stream()
								.mapToDouble(MonthlyCosts::getMoney)
								.filter(d -> (d < 0))
								.sum();
						list.add(new ContractExpensesAnalysisData(-negativeCosts, c.getName()));
						return list;
					}
				});
			}
		}
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
