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
package de.janrieke.contractmanager.ext.hibiscus;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, ueber den Umsätze importiert werden koennen.
 */
public class UmsatzImportDialog extends AbstractDialog<Contract> {
	private final static I18N i18n = Settings.i18n();

	private final static int WINDOW_WIDTH = 420;

	private Input contractList = null;
	private Contract selectedContract = null;

	private boolean importButtonEnabled = false;
	
	/**
	 * ct.
	 * 
	 * @param umsatz
	 *            Zu importierender Umsatz.
	 */
	public UmsatzImportDialog() {
		super(POSITION_CENTER);

		setTitle(i18n.tr("Import of Transactions"));
		this.setSize(WINDOW_WIDTH, SWT.DEFAULT);
	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
	 */
	protected void paint(Composite parent) throws Exception {
		LabelGroup group = new LabelGroup(parent,
				i18n.tr("Contract Selection"));
		group.addText(
				i18n.tr("Please select the contract that the imported transaction should be assigned to."),
				true);

		Input formats = getContractList();
		group.addLabelPair(i18n.tr("Available contracts:"), formats);

		ButtonArea buttons = new ButtonArea(parent, 2);
		Button importButton = new Button(i18n.tr("Import"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
				importUmsatz();
			}
		}, null, true);
		importButton.setEnabled(importButtonEnabled);
		buttons.addButton(importButton);
		buttons.addButton(i18n.tr("Cancel"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
				close();
			}
		});
		getShell().setMinimumSize(
				getShell().computeSize(WINDOW_WIDTH, SWT.DEFAULT));
	}

	/**
	 * Exportiert die Daten.
	 * 
	 * @throws ApplicationException
	 */
	private void importUmsatz() throws ApplicationException {
		try {
			selectedContract = (Contract) getContractList().getValue();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Dialog schliessen
		close();
	}

	/**
	 * Liefert eine Liste der verfuegbaren Contracts.
	 * 
	 * @return Liste der Contracts.
	 * @throws Exception
	 */
	private Input getContractList() throws RemoteException {
		if (contractList != null)
			return contractList;

		GenericIterator contracts = ContractControl.getContracts();

		if (contracts.size() == 0) {
			importButtonEnabled = false;
			contractList = new LabelInput(
					i18n.tr("No existing contracts."));
			return contractList;
		}

		importButtonEnabled = true;
		contractList = new SelectInput(contracts, null);
		((SelectInput)contractList).setAttribute(Contract.CONTRACT_NAME_PLUS_PARTNER_NAME); 
		return contractList;
	}

	@Override
	protected Contract getData() throws Exception {
		return selectedContract;
	}

}