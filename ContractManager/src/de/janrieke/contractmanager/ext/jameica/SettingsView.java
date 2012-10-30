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
package de.janrieke.contractmanager.ext.jameica;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

import de.janrieke.contractmanager.gui.control.SettingsControl;
import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.internal.views.Settings;
import de.willuhn.jameica.gui.util.ColumnLayout;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.gui.util.TabGroup;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SettingsChangedMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Erweitert die View mit dem System-Einstellungen um die Kalender-Optionen.
 */
public class SettingsView implements Extension {
	private final static I18N i18n = de.janrieke.contractmanager.Settings
			.i18n();
	private SettingsControl settingsControl;

	private MessageConsumer mc = null;

	/**
	 * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
	 */
	public void extend(Extendable extendable) {
		if (extendable == null || !(extendable instanceof Settings))
			return;

		this.mc = new MessageConsumer() {

			/**
			 * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
			 */
			public void handleMessage(Message message) throws Exception {
				handleStore();
			}

			/**
			 * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
			 */
			public Class<?>[] getExpectedMessageTypes() {
				return new Class[] { SettingsChangedMessage.class };
			}

			/**
			 * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
			 */
			public boolean autoRegister() {
				return false;
			}
		};
		Application.getMessagingFactory().registerMessageConsumer(this.mc);

		Settings settings = (Settings) extendable;

		try {
			TabGroup tab = new TabGroup(settings.getTabFolder(),
					i18n.tr("ContractManager"));

			// instanciate controller
			settingsControl = new SettingsControl(settings);

			ColumnLayout columns = new ColumnLayout(tab.getComposite(), 2);
			SimpleContainer left = new SimpleContainer(columns.getComposite());

			left.addHeadline(i18n.tr("User Information"));
			// create a bordered group
			// LabelGroup group = new LabelGroup(getParent(), i18n.tr(
			// "Contract details"));

			// all all input fields to the group.
			left.addLabelPair(i18n.tr("Name"), settingsControl.getName());
			left.addLabelPair(i18n.tr("Street"),
					settingsControl.getStreetNumber());
			left.addLabelPair(i18n.tr("Extra"), settingsControl.getExtra());
			left.addLabelPair(i18n.tr("City"), settingsControl.getZipcodeCity());
			left.addLabelPair(i18n.tr("State"), settingsControl.getState());
			left.addLabelPair(i18n.tr("Country"), settingsControl.getCountry());
			left.addLabelPair(i18n.tr("Email"), settingsControl.getEmail());
			left.addLabelPair(i18n.tr("Phone"), settingsControl.getPhone());

			SimpleContainer right = new SimpleContainer(columns.getComposite());
			right.addHeadline(i18n.tr("Contract Cancellation Reminders"));
			right.addLabelPair(i18n.tr("Extension notice time"),
					settingsControl.getNoticeTime());
			right.addLabelPair(i18n.tr("Extension warning time"),
					settingsControl.getWarningTime());
			right.addHeadline(i18n
					.tr("iCal Export of Contract Cancellation Reminders"));
			right.addLabelPair(i18n.tr("Export warnings on exit"),
					settingsControl.getICalAutoExport());
			right.addLabelPair(i18n.tr("iCal file"),
					settingsControl.getICalFileLocation());
			right.addLabelPair(i18n.tr("Export contract names"),
					settingsControl.getNamedICalExport());

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
}
