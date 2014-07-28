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
import de.janrieke.contractmanager.gui.action.ShowJameicaSettings;
import de.janrieke.contractmanager.gui.button.RestoreButton;
import de.janrieke.contractmanager.gui.control.SettingsControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * this is the dialog for the contract details.
 */
public class SettingsView extends AbstractView {

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#bind()
	 */
	public void bind() throws Exception {
		// draw the title
		GUI.getView().setTitle(Settings.i18n().tr("Settings"));

		// instanciate controller
		final SettingsControl control = new SettingsControl(this);
		
	    ColumnLayout columns = new ColumnLayout(getParent(),2);
	    SimpleContainer left = new SimpleContainer(columns.getComposite());

	    left.addHeadline(Settings.i18n().tr("User Information"));
		// create a bordered group
		//LabelGroup group = new LabelGroup(getParent(), Settings.i18n().tr(
		//		"Contract details"));

		// all all input fields to the group.
	    left.addLabelPair(Settings.i18n().tr("Name"), control.getName());
	    left.addLabelPair(Settings.i18n().tr("Street"), control.getStreetNumber());
	    left.addLabelPair(Settings.i18n().tr("Extra"), control.getExtra());
	    left.addLabelPair(Settings.i18n().tr("City"), control.getZipcodeCity());
	    left.addLabelPair(Settings.i18n().tr("State"), control.getState());
	    left.addLabelPair(Settings.i18n().tr("Country"), control.getCountry());
	    left.addLabelPair(Settings.i18n().tr("Email"), control.getEmail());
	    left.addLabelPair(Settings.i18n().tr("Phone"), control.getPhone());

	    SimpleContainer right = new SimpleContainer(columns.getComposite());
	    right.addHeadline(Settings.i18n().tr("Contract Cancellation Reminders"));
	    right.addLabelPair(Settings.i18n().tr("Extension notice time"), control.getNoticeTime());
	    right.addLabelPair(Settings.i18n().tr("Extension warning time"), control.getWarningTime());
	    right.addHeadline(Settings.i18n().tr("iCal Export of Contract Cancellation Reminders"));
//	    right.addLabelPair(Settings.i18n().tr("Export warnings on exit"), control.getICalAutoExport());
	    right.addLabelPair(Settings.i18n().tr("Export contract names"), control.getNamedICalExport());
	    
	    Manifest mf = Application.getPluginLoader().getManifestByName("jameica.ical");
	    if (mf == null) {
	    	right.addText(Settings.i18n().tr("jameica.ical plugin not installed. Install the plugin to enable cancellation reminder export."), true);
	    	right.addPart(new Button(Settings.i18n().tr("Open Jameica Update Settings"), new ShowJameicaSettings("Updates")));
	    } else
	    	right.addPart(new Button(Settings.i18n().tr("Open Jameica Calendar Settings"), new ShowJameicaSettings("Kalender")));

	    //Hibiscus integration
	    mf = Application.getPluginLoader().getManifestByName("hibiscus");
	    if (mf != null) {
		    right.addHeadline(Settings.i18n().tr("Hibiscus Settings"));
		    right.addLabelPair(Settings.i18n().tr("Auto-import new transactions"), control.getHibiscusAutoImportNewTransactions());
	    }
	    
		// add some buttons
	    ButtonArea buttons = new ButtonArea(getParent(), 4);

		//buttons.addButton(new Back(false));
		buttons.addButton(Settings.i18n().tr("Revert to default settings"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
		  		YesNoDialog prompt = new YesNoDialog(YesNoDialog.POSITION_CENTER);
		  		prompt.setTitle(Settings.i18n().tr("Are you sure?"));
		  		prompt.setText(Settings.i18n().tr("Revert all settings to default values"));
		  		try {
					if (!((Boolean) prompt.open()).booleanValue())
						return;
				} catch (Exception e) {
					return;
				}
				control.handleReset();
				GUI.startView(SettingsView.this, context);
			}
		}, null, true, "edit-undo.png");
		buttons.addButton(new RestoreButton(this, null, false));
		buttons.addButton(Settings.i18n().tr("Save Settings"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
				control.handleStore();
			}
		}, null, true, "ok.png");
	}

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#unbind()
	 */
	public void unbind() throws ApplicationException {
		// this method will be invoked when leaving the dialog.
		// You are able to interrupt the unbind by throwing an
		// ApplicationException.
	}

}