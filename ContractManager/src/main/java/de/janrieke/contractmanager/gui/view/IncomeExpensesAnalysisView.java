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
package de.janrieke.contractmanager.gui.view;

import org.eclipse.swt.widgets.Event;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.control.IncomeExpensesAnalysisControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;

/**
 * this is the dialog for the contract details.
 */
public class IncomeExpensesAnalysisView extends AbstractView {

	private IncomeExpensesAnalysisControl control;

	private int selectedYear = -1;
	private int selectedMonth = -1;
	private boolean selectedUsePaydays = false;

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#bind()
	 */
	@Override
	public void bind() throws Exception {
		// draw the title
		GUI.getView().setTitle(Settings.i18n().tr("Income/Expenses Comparison"));

		control = new IncomeExpensesAnalysisControl(this);

		// register listeners (must happen before adding the control)
		control.getPayDayCheckbox().addListener(this::handleReload);
		control.getMonthYearSelector().addListener(this::handleReload);
		control.getYearSelector().addListener(this::handleReload);
		control.getMonthSelector().addListener(this::handleReload);

		SimpleContainer container = new SimpleContainer(getParent(), true);

		ColumnLayout top = new ColumnLayout(container.getComposite(), 4);
		Container col1 = new SimpleContainer(top.getComposite());
		Container col2 = new SimpleContainer(top.getComposite());
		Container col3 = new SimpleContainer(top.getComposite());

		col1.addCheckbox(control.getPayDayCheckbox(), Settings.i18n().tr("Use paydays"));
		col2.addLabelPair(Settings.i18n().tr("Month:"), control.getMonthSelector());
		col3.addLabelPair(Settings.i18n().tr("Year:"), control.getYearSelector());
		top.add(new Button(Settings.i18n().tr("Current Month"), this::toCurrentMonth));

		container.addPart(control.getChartPart());
	}

	private synchronized void handleReload(Event event) {
		if (inputsChanged()) {
			control.redrawChart();
		}
	}

	private boolean inputsChanged() {
		boolean newUsePaydays = (Boolean) control.getPayDayCheckbox().getValue();
		int newMonth = control.getMonthNumber((String) control.getMonthSelector().getValue());
		int newYear = (Integer) control.getYearSelector().getValue();
		if (selectedUsePaydays != newUsePaydays
				|| selectedMonth != newMonth
				|| selectedYear != newYear) {
			selectedUsePaydays = newUsePaydays;
			selectedMonth = newMonth;
			selectedYear = newYear;
			return true;
		}
		return false;
	}

	private void toCurrentMonth(Object context) {
		control.resetToCurrentMonth();
		handleReload(null);
	}
}