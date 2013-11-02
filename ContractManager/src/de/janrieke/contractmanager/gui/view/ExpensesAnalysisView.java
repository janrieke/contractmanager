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
import de.janrieke.contractmanager.gui.control.ExpensesAnalysisControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

/**
 * this is the dialog for the contract details.
 */
public class ExpensesAnalysisView extends AbstractView {

	private ExpensesAnalysisControl control;

    /**
	 * @see de.willuhn.jameica.gui.AbstractView#bind()
	 */
	public void bind() throws Exception {
		// draw the title
		GUI.getView().setTitle(Settings.i18n().tr("Income/Expenses Comparison"));

		// instanciate controller
		control = new ExpensesAnalysisControl(this);
		
	    SimpleContainer container = new SimpleContainer(getParent(), true);
	    
	    ColumnLayout top = new ColumnLayout(container.getComposite(), 2);

	    SimpleContainer inner = new SimpleContainer(top.getComposite(), true);

	    inner.addLabelPair(Settings.i18n().tr("Month:"), control.getMonthYearSelector());

	    top.add(new Button(Settings.i18n().tr("Update"), new Action()
	    {
	    	public void handleAction(Object context) throws ApplicationException
	    	{
	    		handleReload(true);
	    	}
	    },null,true,"view-refresh.png"));

	    container.addPart(control.getChartPart());
	    
	}

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#unbind()
	 */
	public void unbind() throws ApplicationException {
		// this method will be invoked when leaving the dialog.
		// You are able to interrupt the unbind by throwing an
		// ApplicationException.
	}

	private synchronized void handleReload(boolean force)
	{
		control.redrawChart();
	}
}