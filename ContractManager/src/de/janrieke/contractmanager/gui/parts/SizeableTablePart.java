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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class SizeableTablePart extends TablePart {

	private int heightHint = -1;
	private int orderByIndex = -1;

	@Override
	public synchronized void paint(Composite parent) throws RemoteException {
		if (heightHint != -1) {
			SimpleContainer container = new SimpleContainer(parent, true); //give the table a minimum height
			GridData gd = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
			gd.heightHint = heightHint;
			container.getComposite().setLayoutData(gd);
		    super.paint(container.getComposite());
		} else {
		    super.paint(parent);
		}
		if (orderByIndex != -1)
			super.orderBy(orderByIndex);
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
	
	@Override
	public void orderBy(int index) {
		// make method public
		this.orderByIndex  = index;
		super.orderBy(index);
	}
}
