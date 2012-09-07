/**********************************************************************
 * $Source: /cvsroot/syntax/syntax/src/de/willuhn/jameica/fibu/gui/dialogs/ExportDialog.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/08/27 11:19:40 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.janrieke.contractmanager.ext.hibiscus;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

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
import de.willuhn.jameica.hbci.HBCI;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog, ueber den Daten exportiert werden koennen.
 */
public class UmsatzImportDialog extends AbstractDialog<Contract> {
	private final static I18N i18n = Application.getPluginLoader()
			.getPlugin(HBCI.class).getResources().getI18N();

	private final static int WINDOW_WIDTH = 420;

	private Input contractList = null;
	private Contract selectedContract = null;

	/**
	 * ct.
	 * 
	 * @param umsatz
	 *            Zu importierender Umsatz.
	 */
	public UmsatzImportDialog() {
		super(POSITION_CENTER);

		setTitle(i18n.tr("Umsatz-Import"));
		this.setSize(WINDOW_WIDTH, SWT.DEFAULT);
	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
	 */
	protected void paint(Composite parent) throws Exception {
		LabelGroup group = new LabelGroup(parent,
				i18n.tr("Auswahl des Vertrags"));
		group.addText(
				i18n.tr("Bitte wählen Sie den Vertrag aus, dem der gewählte Umsatz zugeordnet werden soll."),
				true);

		Input formats = getContractList();
		group.addLabelPair(i18n.tr("Verfügbare Formate:"), formats);

		ButtonArea buttons = new ButtonArea(parent, 2);
		Button button = new Button(i18n.tr("Importieren"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
				importUmsatz();
			}
		}, null, true);
		buttons.addButton(button);
		buttons.addButton(i18n.tr("Abbrechen"), new Action() {
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
			contractList = new LabelInput(
					i18n.tr("Keine Verträge vorhanden."));
			return contractList;
		}

		contractList = new SelectInput(contracts, null);
		return contractList;
	}

	@Override
	protected Contract getData() throws Exception {
		return selectedContract;
	}

}