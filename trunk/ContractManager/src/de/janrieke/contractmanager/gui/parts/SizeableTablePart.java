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
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Costs;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class SizeableTablePart extends TablePart {

	@Override
	protected String getControlValue(Control control) {
		if (control instanceof Combo) {
			return ((Combo) control).getText();
		} else
			return super.getControlValue(control);
	}

	@Override
	protected Control getEditorControl(int row, TableItem item, String oldValue) {
		if (item.getData() instanceof Costs && row == 2) {
		    Combo newCombo = new Combo(item.getParent(), SWT.DROP_DOWN | SWT.READ_ONLY);
		    newCombo.setItems(Contract.IntervalType.getAdjectives());
//		    Contract.IntervalType value = Contract.IntervalType.values()[Integer.parseInt(oldValue)];
//		    newCombo.setText(value.toAdjectiveString());
		    newCombo.setText(oldValue);
		    newCombo.setFocus();
		    return newCombo;
		}
		else if (item.getData() instanceof Costs && row == 1) {
		    Text newText = new Text(item.getParent(), SWT.NONE);
		    String doubleString = oldValue.substring(0, oldValue.length()-2);
		    newText.setText(doubleString);
		    newText.selectAll();
		    newText.setFocus();
		    return newText;
		}
		else 
			return super.getEditorControl(row, item, oldValue);
	}

	private int heightHint;

	@Override
	public synchronized void paint(Composite parent) throws RemoteException {
	    SimpleContainer container = new SimpleContainer(parent, true); //give the table a minimum height
	    GridData gd = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
	    gd.heightHint = heightHint;
	    container.getComposite().setLayoutData(gd);
	    super.paint(container.getComposite());
	}

	public SizeableTablePart(GenericIterator list, Action action) {
		super(list, action);
	}
	
	public SizeableTablePart(@SuppressWarnings("rawtypes") List list, Action action)
	{
		super(list, action);
	}

	public synchronized void setHeightHint(int heightHint) {
		this.heightHint = heightHint; 
	}

}
