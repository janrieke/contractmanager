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
package de.janrieke.contractmanager.gui.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.CalendarParser;
import net.fortuna.ical4j.data.CalendarParserFactory;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * Generic Action for "History back" ;).
 */
public class ExportCancelationReminders implements Action {

	/**
	 * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
	 */
	public void handleAction(Object context) throws ApplicationException {
		DBService service;
		try {
			service = Settings.getDBService();
			DBIterator contracts = service.createList(Contract.class);

			String filename;
			try {
				filename = Settings.getICalFileLocation();
			} catch (RemoteException e1) {
				throw new ApplicationException(Settings.i18n().tr(
						"Error while accessing settings"), e1);
			}

			if (context != null && context instanceof Boolean
					&& Boolean.TRUE.equals(context)) {
				FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
				fd.setText(Settings.i18n().tr(
						"Select iCal file for cancellation reminders"));
				fd.setOverwrite(true);
				if (filename != null && !filename.isEmpty())
					fd.setFileName(filename);
				String[] names = {
						Settings.i18n().tr("iCalendar file") + " (*.ics)",
						Settings.i18n().tr("All files") + " (*.*)" };
				fd.setFilterNames(names);
				String[] filters = { "*.ics", "*.*" };
				fd.setFilterExtensions(filters);
				filename = fd.open();
				if (filename == null)
					return;
			}
			
			//Export reminders for the next 2 years
			java.util.Calendar until = java.util.Calendar.getInstance();
			until.add(java.util.Calendar.YEAR, 2);

			// FIXME: Calendar apps usually modify the iCal file to store which
			// reminders have already been checked.
			// Thus, we should store the Event's UID and load the file before
			// overwriting.
			Calendar ical;
			if (new File(filename).exists()) {
				FileInputStream fin = new FileInputStream(filename);
				CalendarBuilder builder = new CalendarBuilder();
				try {
					ical = builder.build(fin);
				} catch (ParserException e) {
					//then just don't care and overwrite the file
					ical = new Calendar();
				}
			}
			else
				ical = new Calendar();
			
			//Iterate over all calendar entries and get their UIDs.
			// 1. If the UID is in the DB, check whether it's reminder 
			//  was in the past -> delete
			// 2. If UID is in the DB and in the future, leave it as it
			//  is, so that already checked reminders are not overwritten.
			// 3. If it is not in the DB, delete it (contract was deleted
			//  or modified).
			for (Object comp : ical.getComponents()) {
				if (comp instanceof Component) {
					Property prop = ((Component)comp).getProperty(Uid.UID);
					if (prop instanceof Uid) {
						String uidValue = ((Uid)prop).getValue();
						//DBIterator result = Settings.getDBService().createList(CalendarUID.class);
						//result.addFilter("");
					}
				}
			}
			
			ical.getProperties().add(
					new ProdId("-//ContractManager 0.1//iCal4j 1.0//EN"));
			ical.getProperties().add(Version.VERSION_2_0);
			ical.getProperties().add(CalScale.GREGORIAN);

			int warningTime = Settings.getExtensionWarningTime();
			java.util.Calendar calcCalendar = java.util.Calendar.getInstance();
			boolean namedExport = Settings.getNamedICalExport();

			while (contracts.hasNext()) {
				Contract contract = (Contract) contracts.next();

				Date deadline = contract.getNextCancellationDeadline();
				calcCalendar.setTime(deadline);
				calcCalendar.add(java.util.Calendar.DAY_OF_YEAR, -warningTime);

				while (calcCalendar.before(until)) {
					String descr = namedExport ? Settings.i18n().tr(
							"Check cancellation for contract {0}",
							contract.getName()) : Settings.i18n().tr(
							"Check cancellations");
					VToDo cancellation = new VToDo(
							new net.fortuna.ical4j.model.Date(
									calcCalendar.getTime()),
							new net.fortuna.ical4j.model.Date(deadline), descr);
					VAlarm alarm = new VAlarm(new Dur(-warningTime, 0, 0, 0));
					alarm.getProperties().add(
							net.fortuna.ical4j.model.property.Action.DISPLAY);
					alarm.getProperties().add(new Description(descr));
					cancellation.getAlarms().add(alarm);

					UidGenerator ug = new UidGenerator(Thread.currentThread()
							.toString());
					cancellation.getProperties().add(ug.generateUid());

					ical.getComponents().add(cancellation);

					//find next cancellation deadline
					calcCalendar.setTime(deadline);
					calcCalendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
					deadline = contract.getNextCancellationDeadline(calcCalendar.getTime());
					calcCalendar.setTime(deadline);
					calcCalendar.add(java.util.Calendar.DAY_OF_YEAR, -warningTime);
				}
			}

			FileOutputStream fout = new FileOutputStream(filename);

			CalendarOutputter outputter = new CalendarOutputter();
			outputter.output(ical, fout);
			GUI.getStatusBar()
					.setSuccessText(
							Settings.i18n()
									.tr("Cancellation reminders successfully exported to iCalendar file."));

		} catch (RemoteException e) {
			throw new ApplicationException(
					"Error during cancellation reminder export.", e);
		} catch (FileNotFoundException e) {
			throw new ApplicationException(
					"Error during cancellation reminder export.", e);
		} catch (IOException e) {
			throw new ApplicationException(
					"Error during cancellation reminder export.", e);
		} catch (ValidationException e) {
			throw new ApplicationException(
					"Error during cancellation reminder export.", e);
		}
	}
}