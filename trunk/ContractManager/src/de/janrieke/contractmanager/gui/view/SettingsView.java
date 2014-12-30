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

import java.rmi.RemoteException;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.action.ShowJameicaSettings;
import de.janrieke.contractmanager.gui.button.RestoreButton;
import de.janrieke.contractmanager.gui.control.SettingsControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.YesNoDialog;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.util.ButtonArea;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SettingsChangedMessage;
import de.willuhn.jameica.messaging.SettingsRestoredMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Dialog for the ContractManager settings. Serves both as a view and as an 
 * extension for the Jameica Settings view.
 */
public class SettingsView extends AbstractView implements Extension {

	private final static I18N i18n = de.janrieke.contractmanager.Settings
			.i18n();
	private SettingsControl settingsControl;
	private MessageConsumer mc;

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#bind()
	 */
	public void bind() throws Exception {
		// draw the title
		GUI.getView().setTitle(Settings.i18n().tr("Settings"));

		// instanciate controller
		settingsControl = new SettingsControl(this);
		
	    addDialogContent(getParent());
	    
		// add some buttons
	    ButtonArea buttons = new ButtonArea(getParent(), 4);

		//buttons.addButton(new Back(false));
		buttons.addButton(Settings.i18n().tr("Revert to default settings"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
		  		handleRevert(context);
			}
		}, null, true, "edit-undo.png");
		buttons.addButton(new RestoreButton(this, null, false));
		buttons.addButton(Settings.i18n().tr("Save Settings"), new Action() {
			public void handleAction(Object context)
					throws ApplicationException {
				handleStore();
			}
		}, null, true, "ok.png");
	}

	private void addDialogContent(Composite parent) throws RemoteException {
		ColumnLayout columns = new ColumnLayout(parent,2);
	    SimpleContainer left = new SimpleContainer(columns.getComposite());

	    left.addHeadline(Settings.i18n().tr("User Information"));
		// create a bordered group
		//LabelGroup group = new LabelGroup(getParent(), Settings.i18n().tr(
		//		"Contract details"));

		// all all input fields to the group.
	    left.addLabelPair(Settings.i18n().tr("Name"), settingsControl.getName());
	    left.addLabelPair(Settings.i18n().tr("Street"), settingsControl.getStreetNumber());
	    left.addLabelPair(Settings.i18n().tr("Extra"), settingsControl.getExtra());
	    left.addLabelPair(Settings.i18n().tr("City"), settingsControl.getZipcodeCity());
	    left.addLabelPair(Settings.i18n().tr("State"), settingsControl.getState());
	    left.addLabelPair(Settings.i18n().tr("Country"), settingsControl.getCountry());
	    left.addLabelPair(Settings.i18n().tr("Email"), settingsControl.getEmail());
	    left.addLabelPair(Settings.i18n().tr("Phone"), settingsControl.getPhone());

	    SimpleContainer right = new SimpleContainer(columns.getComposite());
	    right.addHeadline(Settings.i18n().tr("Contract Cancellation Reminders"));
	    right.addLabelPair(Settings.i18n().tr("Extension notice time"), settingsControl.getNoticeTime());
	    right.addLabelPair(Settings.i18n().tr("Extension warning time"), settingsControl.getWarningTime());
	    right.addHeadline(Settings.i18n().tr("iCal Export of Contract Cancellation Reminders"));
	    right.addLabelPair(Settings.i18n().tr("Export contract names"), settingsControl.getNamedICalExport());
	    
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
		    right.addLabelPair(Settings.i18n().tr("Auto-import new transactions"), settingsControl.getHibiscusAutoImportNewTransactions());
	    }
	}

	/**
	 * @see de.willuhn.jameica.gui.AbstractView#unbind()
	 */
	public void unbind() throws ApplicationException {
		// this method will be invoked when leaving the dialog.
		// You are able to interrupt the unbind by throwing an
		// ApplicationException.
	}

	/**
	 * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
	 */
	@Override
	public void extend(Extendable extendable) {
		if (extendable == null || !(extendable instanceof de.willuhn.jameica.gui.internal.views.Settings))
			return;

		this.mc = new MessageConsumer() {

			/**
			 * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
			 */
			public void handleMessage(Message message) throws Exception {
				if (message instanceof SettingsChangedMessage)
					handleStore();
				else if (message instanceof SettingsRestoredMessage)
					handleReset();
			}

			/**
			 * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
			 */
			public Class<?>[] getExpectedMessageTypes() {
				return new Class[] { SettingsChangedMessage.class, SettingsRestoredMessage.class };
			}

			/**
			 * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
			 */
			public boolean autoRegister() {
				return false;
			}
		};
		Application.getMessagingFactory().registerMessageConsumer(this.mc);

		de.willuhn.jameica.gui.internal.views.Settings settings = (de.willuhn.jameica.gui.internal.views.Settings) extendable;

		try {
			TabGroup tab = new TabGroup(settings.getTabFolder(),
					i18n.tr("ContractManager"));

			// instanciate controller
			settingsControl = new SettingsControl(settings);
			
			addDialogContent(tab.getComposite());

			// Da wir keine echte View sind, haben wir auch kein unbind zum
			// Aufraeumen. Damit wir unsere GUI-Elemente aber trotzdem disposen
			// koennen, registrieren wir einen Dispose-Listener an der Tabgroup
			tab.getComposite().addDisposeListener(new DisposeListener() {

				public void widgetDisposed(DisposeEvent e) {
					Application.getMessagingFactory()
							.unRegisterMessageConsumer(mc);
				}

			});

		} catch (Exception e) {
			Logger.error("unable to extend settings", e);
			Application
					.getMessagingFactory()
					.sendMessage(
							new StatusBarMessage(
									i18n.tr("Fehler beim Anzeigen der Kalender-Einstellungen"),
									StatusBarMessage.TYPE_ERROR));
		}
	}

	/**
	 * Speichert die Einstellungen.
	 */
	private void handleStore() {
		this.settingsControl.handleStore();
	}

	/**
	 * Speichert die Einstellungen.
	 */
	private void handleReset() {
		this.settingsControl.handleReset();
	}

	/**
	 * Reverts all settings to their default values and then reloads the view.
	 */
	private void handleRevert(Object context) {
		YesNoDialog prompt = new YesNoDialog(YesNoDialog.POSITION_CENTER);
  		prompt.setTitle(Settings.i18n().tr("Are you sure?"));
  		prompt.setText(Settings.i18n().tr("Revert all settings to default values"));
  		try {
			if (!((Boolean) prompt.open()).booleanValue())
				return;
		} catch (Exception e) {
			return;
		}
		settingsControl.handleReset();
		GUI.startView(SettingsView.this, context);
	}
}