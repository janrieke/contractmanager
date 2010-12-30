package de.janrieke.contractmanager.gui.action;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;
import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
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
						"Error while accessing settings."), e1);
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
						Settings.i18n().tr("iCalendar file" + " (*.ics)"),
						Settings.i18n().tr("All files") + " (*.*)" };
				fd.setFilterNames(names);
				String[] filters = { "*.ics", "*.*" };
				fd.setFilterExtensions(filters);
				filename = fd.open();
				if (filename == null)
					return;
			}

			Calendar ical = new Calendar();
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

				VToDo cancellation = new VToDo(
						new net.fortuna.ical4j.model.Date(
								calcCalendar.getTime()),
						new net.fortuna.ical4j.model.Date(deadline),
						namedExport ? Settings.i18n().tr(
								"Check cancellation for contract {0}",
								contract.getName()) : Settings.i18n().tr(
								"Check cancellation"));

				// Generate a UID for the event..
				UidGenerator ug = new UidGenerator("1");
				cancellation.getProperties().add(ug.generateUid());

				ical.getComponents().add(cancellation);
			}

			FileOutputStream fout = new FileOutputStream(filename);

			CalendarOutputter outputter = new CalendarOutputter();
			outputter.output(ical, fout);

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