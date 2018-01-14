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

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.control.IncomeExpensesAnalysisControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.SimpleContainer;

/**
 * this is the dialog for the contract details.
 */
public class IncomeExpensesAnalysisView extends AbstractView {

    private IncomeExpensesAnalysisControl control;

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#bind()
	 */
	@Override
	public void bind() throws Exception {
		// draw the title
		GUI.getView().setTitle(Settings.i18n().tr("Income/Expenses Comparison"));

		// instanciate controller

		control = new IncomeExpensesAnalysisControl(this);

	    SimpleContainer container = new SimpleContainer(getParent(), true);

	    ColumnLayout top = new ColumnLayout(container.getComposite(), 2);

	    SimpleContainer inner = new SimpleContainer(top.getComposite(), true);

	    inner.addCheckbox(control.getPayDayCheckbox(), Settings.i18n().tr("Use paydays"));
	    inner.addLabelPair(Settings.i18n().tr("Month:"), control.getMonthYearSelector());

	    top.add(new Button(Settings.i18n().tr("Update"), context -> handleReload(true),null,true,"view-refresh.png"));

	    //left.addHeadline(Settings.i18n().tr("Chart"));
	    container.addPart(control.getChartPart());
	}

	private synchronized void handleReload(boolean force)
	{
		control.redrawChart();
	}
}