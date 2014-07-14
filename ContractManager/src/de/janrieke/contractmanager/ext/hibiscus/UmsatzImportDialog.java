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

import org.apache.commons.lang.StringUtils;
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
import de.willuhn.jameica.hbci.rmi.Umsatz;
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

	private Umsatz transaction;
	
	/**
	 * ct.
	 * 
	 * @param umsatz
	 *            Zu importierender Umsatz.
	 */
	public UmsatzImportDialog(Umsatz transaction) {
		super(POSITION_CENTER);
		this.transaction = transaction;
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
		
		float minSimilarity = 1;
		Contract maxSimilarContract = null;
		while (contracts.hasNext()) {
			Contract c = (Contract)contracts.next();
			float similarity = calculateSimilarity(c, transaction);
			//System.out.println("Contract " + c.getName() + ": " + similarity);
			if (similarity < minSimilarity) {
				maxSimilarContract = c;
				minSimilarity = similarity;
			}
		}
		
		contracts.begin();
		importButtonEnabled = true;
		contractList = new SelectInput(contracts, maxSimilarContract);
		((SelectInput)contractList).setAttribute(Contract.CONTRACT_NAME_PLUS_PARTNER_NAME);
		return contractList;
	}
	
	private float getRelativeLevenshteinDistance(String a, String b){
		int distance = StringUtils.getLevenshteinDistance(a.toLowerCase(), b.toLowerCase());
		int maxLength = Math.max(a.length(), b.length());
		int minLength = Math.min(a.length(), b.length());
		int maxDistance = maxLength;
		int minDistance = maxLength-minLength;
		return (float) Math.sqrt(Math.sqrt((((float)(distance - minDistance))/((float)maxDistance))));
	}
	
	private static final int MINIMUM_TOKEN_SIZE = 3; // do not count 1 or 2 character tokens

	private float calculateSimilarity(Contract c, Umsatz transaction) {
		try {
			String trName = "";
			if (transaction.getGegenkontoName() != null)
				trName = transaction.getGegenkontoName();
			StringBuilder trUse = new StringBuilder();
			if (transaction.getZweck() != null)
				trUse.append(transaction.getZweck());
			if (transaction.getZweck2() != null)
				trUse.append(transaction.getZweck2());
			for (String more : transaction.getWeitereVerwendungszwecke()) {
				trUse.append(more);
			}
			
			String[] trNameTokens = trName.split("\\s+");
			String[] trUseTokens = trUse.toString().split("\\s+");
			
			float[] distanceName = new float[trNameTokens.length];
			float[] distanceUse1 = new float[trUseTokens.length];
			float[] distanceUse2 = new float[trUseTokens.length];
			float[] distanceUse3 = new float[trUseTokens.length];

			// try finding the partner name in the owner of the opposite account
			for (int i = 0; i<trNameTokens.length; i++) {
				if (trNameTokens[i].length() < MINIMUM_TOKEN_SIZE)
					distanceName[i] = 1; // do not count small tokens
				else
					distanceName[i] = getRelativeLevenshteinDistance(trNameTokens[i], c.getPartnerName());
			}
			
			// try finding the contract name or number in the reason for payment
			
			String customerNumber = c.getCustomerNumber();
			String contractNumber = c.getContractNumber();
			String sepaCreditorRef = c.getSepaCreditorRef();
			String sepaCustomerRef = c.getSepaCustomerRef();
			for (int i = 0; i<trUseTokens.length; i++) {
				if (trUseTokens[i].length() < MINIMUM_TOKEN_SIZE)
					distanceUse1[i] = 1; // do not count small tokens
				else
					distanceUse1[i] = getRelativeLevenshteinDistance(trUseTokens[i], c.getName());
				
				if (trUseTokens[i].length() < MINIMUM_TOKEN_SIZE || customerNumber == null || "".equals(customerNumber))
					distanceUse2[i] = 1; // do not count small tokens
				else
					distanceUse2[i] = getRelativeLevenshteinDistance(trUseTokens[i], customerNumber);
				
				if (trUseTokens[i].length() < MINIMUM_TOKEN_SIZE || contractNumber == null || "".equals(contractNumber))
					distanceUse3[i] = 1; // do not count small tokens
				else
					distanceUse3[i] = getRelativeLevenshteinDistance(trUseTokens[i], contractNumber);
			}
			
			float result = 1;
			int zeros = 0;
			for (int i = 0; i<trNameTokens.length; i++) {
				result *= distanceName[i];
				if (distanceName[i] == 0)
					zeros++;
			}
			for (int i = 0; i<trUseTokens.length; i++) {
				result *= distanceUse1[i];
				if (distanceUse1[i] == 0)
					zeros++;
				result *= distanceUse2[i];
				if (distanceUse2[i] == 0)
					zeros++;
				result *= distanceUse3[i];
				if (distanceUse3[i] == 0)
					zeros++;
				}
			return result-zeros;
		} catch (RemoteException e) {
			return 1;
		}
	}

	@Override
	protected Contract getData() throws Exception {
		return selectedContract;
	}

}