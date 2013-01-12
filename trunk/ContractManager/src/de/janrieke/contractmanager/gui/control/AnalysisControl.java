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
package de.janrieke.contractmanager.gui.control;

import de.janrieke.contractmanager.gui.parts.AnalysisChartPart;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.input.Input;

/**
 * Controller fuer den Dialog Lizenzinformationen.
 */
public class AnalysisControl extends AbstractControl {

	/**
	 * ct.
	 * 
	 * @param view
	 */
	public AnalysisControl(AbstractView view) {
		super(view);
	}

	public Input getType() {
		// TODO Auto-generated method stub
		return null;
	}

	private AnalysisChartPart chart = null;
	
	public Part getChartPart() {
		if (chart == null)
			chart = new AnalysisChartPart();
		return chart;
	}

}