/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/gui/internal/dialogs/ChooseBoxesDialog.java,v $
 * $Revision: 1.15 $
 * $Date: 2011/10/06 10:49:08 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.janrieke.contractmanager.gui.view;

import org.eclipse.swt.widgets.Composite;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.control.SettingsControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;

/**
 * Dialog zum Konfigurieren der Boxen.
 */
public class ContractDetailViewSettingsDialog extends AbstractDialog<Object> {

	/**
	 * @param position
	 */
	public ContractDetailViewSettingsDialog(int position) {
		super(position);
		//this.setSize(460, 400);
		setTitle(Settings.i18n().tr("Customize Contract Details View"));
	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
	 */
	protected void paint(Composite parent) throws Exception {
		final SettingsControl control = new SettingsControl(null);

		Container c = new SimpleContainer(parent, false);
		c.addHeadline(Settings.i18n().tr("Additional Input Fields"));
		
		c.addLabelPair(Settings.i18n().tr("Show SEPA Creditor Reference"), control.getShowSEPACreditorInput());
		c.addLabelPair(Settings.i18n().tr("Show SEPA Customer Reference"), control.getShowSEPACustomerInput());

		c = new SimpleContainer(parent, false);
		c.addHeadline(Settings.i18n().tr("Hibiscus Extension"));
		
		c.addLabelPair(Settings.i18n().tr("Show Category Selector"), control.getShowHibiscusCategorySelector());
		c.addLabelPair(Settings.i18n().tr("Show Transaction List"), control.getShowHibiscusTransactionList());
		c.addLabelPair(Settings.i18n().tr("Transaction List Height"), control.getHibiscusTransactionListHeight());

		ButtonArea buttons = new ButtonArea();
		buttons.addButton(i18n.tr("Übernehmen"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
				control.handleContractDetailsSettingsStore();
				close();
				AbstractView current = GUI.getCurrentView();
				GUI.startView(current,current.getCurrentObject()); // reload view
			}
		}, null, true, "ok.png");
		buttons.addButton(i18n.tr("Abbrechen"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
				throw new OperationCanceledException();
			}
		}, null, false, "process-stop.png");
		c.addButtonArea(buttons);
	}

	/**
	 * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
	 */
	protected Object getData() throws Exception {
		return null;
	}
}
