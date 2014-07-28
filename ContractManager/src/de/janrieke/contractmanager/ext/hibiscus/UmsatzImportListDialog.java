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
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.DelayedListener;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.rmi.Umsatz;
import de.willuhn.jameica.hbci.server.VerwendungszweckUtil;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Dialog for selecting a contract for a transaction. Uses a table and includes
 * search capabilities.
 */
public class UmsatzImportListDialog extends AbstractDialog<Contract> {
	private List<Contract> list = null;
	private Contract chosen = null;

	private TextInput search = null;
	private TablePart table = null;
	private Button apply = null;
	private Umsatz umsatz;

	/**
	 * ct.
	 * 
	 * @param position
	 * @param preselected
	 *            the preselected contract.
	 * @throws RemoteException
	 */
	public UmsatzImportListDialog(Umsatz u, Contract preSelectedContract) throws RemoteException {
		super(AbstractDialog.POSITION_CENTER, true);
		this.umsatz = u;
		this.chosen = preSelectedContract;

		this.setTitle(Settings.i18n().tr("Select contract to assign to."));
		this.setSize(370, 500);
	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
	 */
	protected Contract getData() throws Exception {
		return chosen;
	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
	 */
	protected void paint(Composite parent) throws Exception {
		this.list = this.initializeContractList();

		Container group = new SimpleContainer(parent, true);

		group.addText(
				Settings.i18n().tr(
						"Please select the contract to assign the transaction to."),
				true);
		TextInput text = this.getSearch();
		group.addInput(text);
		group.addPart(this.getTable());

		// //////////////
		// geht erst nach dem Paint
		if (this.chosen != null)
			this.getTable().select(this.chosen);

		text.getControl().addKeyListener(new DelayedAdapter());

		ButtonArea buttons = new ButtonArea();
		buttons.addButton(this.getApplyButton());
		buttons.addButton(Settings.i18n().tr("Cancel"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
				throw new OperationCanceledException();
			}
		}, null, false, "process-stop.png");

		group.addButtonArea(buttons);
	}

	/**
	 * Returns the apply button.
	 * 
	 * @return the button.
	 */
	private Button getApplyButton() {
		if (this.apply != null)
			return this.apply;

		this.apply = new Button(Settings.i18n().tr("Import"), new Apply(),
				null, true, "ok.png");
		this.apply.setEnabled(false); // initial deaktiviert
		return this.apply;
	}

	/**
	 * Returns the search input
	 * 
	 * @return the search input
	 */
	private TextInput getSearch() {
		if (this.search != null)
			return this.search;

		this.search = new TextInput("");
		this.search.focus();
		this.search.setName(Settings.i18n().tr("Keyword"));
		return this.search;
	}

	/**
	 * Returns the contract table
	 * 
	 * @return the contract table
	 */
	private TablePart getTable() {
		if (this.table != null)
			return this.table;

		this.table = new TablePart(this.list, new Apply());
		this.table.setSummary(false);
		this.table.addColumn(Settings.i18n().tr("Name of Contract"), "name");
		this.table.addColumn(Settings.i18n().tr("Contract Partner"),
				Contract.PARTNER_NAME);
		this.table.setFormatter(new TableFormatter() {
			/**
			 * @see de.willuhn.jameica.gui.formatter.TableFormatter#format(org.eclipse.swt.widgets.TableItem)
			 */
			public void format(TableItem item) {
				if (item == null)
					return;

				try {
					Contract c = (Contract) item.getData();
					if (c == null)
						return;

					Color col = null;

					boolean t = c.isActiveInMonth(new Date());
					if (!t)
						col = Settings.getNotActiveForeground();
					else
						col = de.willuhn.jameica.gui.util.Color.FOREGROUND
								.getSWTColor();
					item.setForeground(col);
				} catch (Exception e) {
					Logger.error("unable to apply color", e);
				}
			}
		});

		this.table.addSelectionListener(new Listener() {
			public void handleEvent(Event event) {
				getApplyButton().setEnabled(event.data != null);
			}
		});
		this.getApplyButton().setEnabled(this.chosen != null);
		return this.table;
	}

	/**
	 * Action for applying
	 */
	private class Apply implements Action {
		public void handleAction(Object context) throws ApplicationException {
			chosen = (Contract) getTable().getSelection();
			if (chosen != null)
				close();
		}
	}

	/**
	 * Initialize the list of contracts by calculating the probability that the
	 * contract is the intended contract. The list will be sorted by this
	 * probability.
	 * 
	 * @return initialized list
	 * @throws RemoteException
	 */
	private List<Contract> initializeContractList() throws RemoteException {
		class SortedContract implements Comparable<SortedContract> {
			private float value;
			private Contract c;
			public Contract getContract() {return c;}
			
			public SortedContract(Contract c, float value) {
				this.value = value;
				this.c = c;
			}

			@Override
			public int compareTo(SortedContract o) {
				return Float.compare(this.value, o.value); 
			}
		}

		List<SortedContract> sorted = new ArrayList<SortedContract>();
		GenericIterator contracts = ContractControl.getContracts();
		while (contracts.hasNext()) {
			Contract c = (Contract) contracts.next();
			float simliarity = calculateSimilarity(c, this.umsatz);
			sorted.add(new SortedContract(c, simliarity));
		}
		Collections.sort(sorted);

		List<Contract> l = new LinkedList<Contract>();
		for (SortedContract c : sorted) {
			l.add(c.getContract());
		}
		return l;
	}

	/**
	 * Da KeyAdapter/KeyListener nicht von swt.Listener abgeleitet sind, muessen
	 * wir leider dieses schraege Konstrukt verenden, um den DelayedListener
	 * verwenden zu koennen
	 */
	private class DelayedAdapter extends KeyAdapter {
		private Listener forward = new DelayedListener(150, new Listener() {
			/**
			 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
			 */
			public void handleEvent(Event event) {
				TablePart table = getTable();
				table.removeAll();

				String text = (String) getSearch().getValue();
				text = text.trim().toLowerCase();
				try {
					for (Contract t : list) {
						if (text.length() == 0) {
							table.addItem(t);
							continue;
						}

						if (t.getName().toLowerCase().contains(text)
								|| t.getPartnerName().toLowerCase().contains(text))
							table.addItem(t);
					}
				} catch (RemoteException re) {
					Logger.error("error while adding items to table", re);
				}
			}

		});

		/**
		 * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
		 */
		public void keyReleased(KeyEvent e) {
			forward.handleEvent(null); // Das Event-Objekt interessiert uns eh
										// nicht
		}
	}

	private float getRelativeLevenshteinDistance(String a, String b) {
		int distance = StringUtils.getLevenshteinDistance(a.toLowerCase(),
				b.toLowerCase());
		int maxLength = Math.max(a.length(), b.length());
		int minLength = Math.min(a.length(), b.length());
		int maxDistance = maxLength;
		int minDistance = maxLength - minLength;
		return (float) Math
				.sqrt(Math
						.sqrt((((float) (distance - minDistance)) / ((float) maxDistance))));
	}

	private static final int MINIMUM_TOKEN_SIZE = 3; // do not count 1 or 2
														// character tokens

	private float calculateSimilarity(Contract c, Umsatz transaction) {
		try {
			String trName = "";
			if (transaction.getGegenkontoName() != null)
				trName = transaction.getGegenkontoName();

			//No separators between lines until we have a working SEPA rewriter - 
			// lines end after 27 chars, but fields may be 35 chars long.
			String trUse = VerwendungszweckUtil.toString(transaction, "");


			String[] trNameTokens = trName.split("\\s+");
			String[] trUseTokens = trUse.split("\\s+");

			float[] distanceName = new float[trNameTokens.length];
			float[] distanceUse = new float[trUseTokens.length];

			// try finding the partner name in the owner of the opposite account
			for (int i = 0; i < trNameTokens.length; i++) {
				if (trNameTokens[i].length() < MINIMUM_TOKEN_SIZE)
					distanceName[i] = 1; // do not count small tokens
				else
					distanceName[i] = getRelativeLevenshteinDistance(
							trNameTokens[i], c.getPartnerName());
			}

			// try finding the contract name in the reason for payment
			for (int i = 0; i < trUseTokens.length; i++) {
				if (trUseTokens[i].length() < MINIMUM_TOKEN_SIZE)
					distanceUse[i] = 1; // do not count small tokens
				else
					distanceUse[i] = getRelativeLevenshteinDistance(
							trUseTokens[i], c.getName());
			}
			
			// try finding the contract numbers or SEPA refs in the reason for payment
			String customerNumber = c.getCustomerNumber();
			String contractNumber = c.getContractNumber();
			String sepaCreditorRef = c.getSepaCreditorRef();
			String sepaCustomerRef = c.getSepaCustomerRef();

			int exactHits = 0;
			if (customerNumber != null && !"".equals(customerNumber))
				if (trUse.toString().contains(customerNumber))
					exactHits++;
			if (contractNumber != null && !"".equals(contractNumber))
				if (trUse.toString().contains(contractNumber))
					exactHits++;
			if (sepaCreditorRef != null && !"".equals(sepaCreditorRef))
				if (trUse.toString().contains(sepaCreditorRef))
					exactHits++;
			if (sepaCustomerRef != null && !"".equals(sepaCustomerRef))
				if (trUse.toString().contains(sepaCustomerRef))
					exactHits++;
			
			float result = 1;
			for (int i = 0; i < trNameTokens.length; i++) {
				result *= distanceName[i];
				if (distanceName[i] == 0)
					exactHits++;
			}
			for (int i = 0; i < trUseTokens.length; i++) {
				result *= distanceUse[i];
				if (distanceUse[i] == 0)
					exactHits++;
			}
			return result - exactHits;
		} catch (RemoteException e) {
			return 1;
		}
	}
}
