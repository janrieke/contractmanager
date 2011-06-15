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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
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
import de.janrieke.contractmanager.rmi.ICalUID;
import de.janrieke.contractmanager.util.ListMap;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
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
			
			if (filename == null || "".equals(filename)) {
				Logger.warn("No filename set for auto-export on close");
				return;
			}
				
			
			//Export reminders for the next 2 years
			java.util.Calendar until = java.util.Calendar.getInstance();
			until.add(java.util.Calendar.YEAR, 2);

			// Calendar apps usually modify the iCal file to store which
			// reminders have already been checked.
			// Thus, we store the Event's UIDs and load the file before
			// overwriting.
			Calendar ical = null;
			if (new File(filename).exists()) {
				FileInputStream fin = new FileInputStream(filename);
				CalendarBuilder builder = new CalendarBuilder();
				try {
					ical = builder.build(fin);
				} catch (ParserException e) {
					//then just don't care and overwrite the file
					//ical = new Calendar();
					ical = null;
				}
			}
			else {
				//ical = new Calendar();
			}
			
			//Iterate over all calendar entries and get their UIDs.
			// 1. If the UID is in the DB, check whether it's reminder 
			//  was in the past -> delete
			// 2. If UID is in the DB and in the future, leave it as it
			//  is, so that already checked reminders are not overwritten.
			// 3. If it is not in the DB, delete it (contract was deleted
			//  or modified).
			// 4. Finally, delete all unused DB entries.

			final Date today = new Date();
			
			//use the contracts' ids, because equals does not work, as several 
			// different Contract objects will be instantiated.
			final ListMap<Integer, VToDo> exportedReminders = new ListMap<Integer, VToDo>();
			
			final Set<String> usedUIDs = new HashSet<String>();
			
			final List<Component> deletionList = new ArrayList<Component>();
			
			if (ical != null) {
				Logger.info("Loading ical file with " + ical.getComponents().size() + " entries...");
				for (Object comp : ical.getComponents()) {
					if (comp instanceof VToDo) {
						Property prop = ((VToDo)comp).getProperty(Uid.UID);
						if (prop instanceof Uid) {
							String uidValue = ((Uid)prop).getValue();
							Date duedate = ((VToDo)comp).getDue().getDate();
							if (duedate.before(today)) {
								// 1. Delete past reminder from ical file
								Logger.debug("Deleting past entry " + ((VToDo)comp).getName() + " (" + uidValue + ").");
								deletionList.add((Component) comp);
							} else {
								//Search in DB for entry with that uid
								DBIterator iCalUIDDBList = Settings.getDBService().createList(ICalUID.class);
								iCalUIDDBList.addFilter("uid = '" + uidValue + "'");
								if (iCalUIDDBList.hasNext()) {
									// 2. Leave it as is, and remember entry, so that we can see   
									// during ical event creation that this reminder was already exported
									Logger.debug("Reusing entry " + ((VToDo)comp).getName() + " (" + uidValue + ").");
									ICalUID icaluid = (ICalUID) iCalUIDDBList.next(); //there should be only one
									exportedReminders.addToList(Integer.parseInt(icaluid.getContract().getID()), (VToDo) comp);
									usedUIDs.add(uidValue);
								} else {
									// 3. Delete it as its not in the DB (deleted or modified contract)
									Logger.debug("Deleting invalid entry " + ((VToDo)comp).getName() + " (" + uidValue + ").");
									deletionList.add((Component) comp);
								}
							}
						}
					}
				}
				
				for (Component entry : deletionList)
					ical.getComponents().remove(entry);
			}
			
			if (ical == null) {
				Logger.info("Creating new iCalendar file...");
				ical = new Calendar();
				ical.getProperties().add(
						new ProdId("-//ContractManager 0.1//iCal4j 1.0//EN"));
				ical.getProperties().add(Version.VERSION_2_0);
				ical.getProperties().add(CalScale.GREGORIAN);
			}
			
			// Now it's time to generate the reminders!
			int warningTime = Settings.getExtensionWarningTime();
			java.util.Calendar calcCalendar = java.util.Calendar.getInstance();
			boolean namedExport = Settings.getNamedICalExport();

			while (contracts.hasNext()) {
				Contract contract = (Contract) contracts.next();

				Logger.debug("Creating new entries for contract " + contract.getName() + "...");
				Date deadline = contract.getNextCancellationDeadline();
				if (deadline == null)
					continue;
				calcCalendar.setTime(deadline);
				calcCalendar.add(java.util.Calendar.DAY_OF_YEAR, -warningTime);

				boolean entryFound = false;
				//Create possibly multiple reminders until we reached the "until" limit. 
				while (calcCalendar.before(until)) {
					//Before creating a reminder, check whether we already have one.
					if (exportedReminders.containsKey(Integer.parseInt(contract.getID()))) {
						List<VToDo> todos = exportedReminders.getList(Integer.parseInt(contract.getID()), false);
						for (VToDo todo: todos) {
							java.util.Calendar diffCalcCalendar1 = java.util.Calendar.getInstance();
							Date d1 = todo.getStartDate().getDate();
							diffCalcCalendar1.setTime(d1);

							java.util.Calendar diffCalcCalendar2 = java.util.Calendar.getInstance();
							Date d2 = calcCalendar.getTime();
							diffCalcCalendar2.setTime(d2);
							
							long comp = diffCalcCalendar1.getTimeInMillis() - diffCalcCalendar2.getTimeInMillis();
							//Logger.debug("  DIFF: " + comp);
							
							if (comp == -79200000 || comp == -82800000) {
								//entry already exists, so skip to the next
								Logger.debug("  Reusing entry for " + calcCalendar.getTime() + " (" + todo.getUid().getValue() + ").");

								//Remember the uid, for later usage check
								usedUIDs.add(todo.getUid().getValue());
								
								entryFound = true;
								break;
							}
						}
					}

					if (!entryFound) {
						//Actual creation of the ical reminder (as a VToDo)

						//The ToDo will be due on the last possible cancellation day, and
						// start warningTime days earlier. The alarm will also set off warningTime
						// days earlier.
						String descr = namedExport ? Settings.i18n().tr(
								"Check cancellation for contract {0}",
								contract.getName()) : Settings.i18n().tr(
								"Check cancellations");
								VToDo cancellation = new VToDo(
										new net.fortuna.ical4j.model.Date(
												calcCalendar.getTime()),
												new net.fortuna.ical4j.model.Date(deadline), descr);

								//Add the alarm
								VAlarm alarm = new VAlarm(new Dur(-warningTime, 0, 0, 0));
								alarm.getProperties().add(
										net.fortuna.ical4j.model.property.Action.DISPLAY);
								alarm.getProperties().add(new Description(descr));
								cancellation.getAlarms().add(alarm);

								//Generate a UID for later identification
								UidGenerator ug = new UidGenerator(Thread.currentThread()
										.toString());
								Uid uid = ug.generateUid();
								cancellation.getProperties().add(uid);

								//Finally, add the newly create reminder to the ICalendar.
								ical.getComponents().add(cancellation);

								//Add that entry to the DB.
								ICalUID icaluid = (ICalUID) Settings.getDBService().createObject(ICalUID.class, null);
								icaluid.setContract(contract);
								icaluid.setUID(uid.getValue());
								icaluid.store();

								Logger.debug("  Created entry for " + calcCalendar.getTime() + " (" + uid.getValue() + ").");

								//Remember the uid, for later usage check
								usedUIDs.add(uid.getValue());
					} else
						entryFound = false;


					//find next cancellation deadline
					calcCalendar.setTime(deadline);
					calcCalendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
					deadline = contract.getNextCancellationDeadline(calcCalendar.getTime());
					calcCalendar.setTime(deadline);
					calcCalendar.add(java.util.Calendar.DAY_OF_YEAR, -warningTime);
				}
			}
			
			//4. Remove all unnecessary DB entries.
			DBIterator dbuidlist = Settings.getDBService().createList(ICalUID.class);
			while (dbuidlist.hasNext()) {
				ICalUID dbuid = (ICalUID) dbuidlist.next();
				if (!usedUIDs.contains(dbuid.getUID())) {
					Logger.debug("Deleting DB entry (" + dbuid.getUID() + ").");
					dbuid.delete();
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