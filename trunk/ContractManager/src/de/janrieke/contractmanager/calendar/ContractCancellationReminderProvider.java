package de.janrieke.contractmanager.calendar;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.RGB;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.control.ContractControl;
import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.calendar.AbstractAppointment;
import de.willuhn.jameica.gui.calendar.Appointment;
import de.willuhn.jameica.gui.calendar.AppointmentProvider;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

public class ContractCancellationReminderProvider implements
		AppointmentProvider {

	I18N i18n = Settings.i18n();

	@Override
	public String getName() {
		return i18n.tr("Cancellation Deadlines");
	}

	@Override
	public List<Appointment> getAppointments(Date from, Date to) {
		List<Appointment> result = new ArrayList<Appointment>();
		GenericIterator iterator;
		try {
			iterator = ContractControl.getContracts();
		while (iterator.hasNext()) {
			Contract contract = (Contract) iterator.next();
			Date nextCancellationDeadline = contract.getNextCancellationDeadline();
			while (nextCancellationDeadline != null && nextCancellationDeadline.before(to)) {
				if (nextCancellationDeadline.after(from)) {
					Date doNotRemindBefore = contract.getDoNotRemindBefore();
					boolean hasAlarm = !contract.getDoNotRemind() && !(doNotRemindBefore != null && nextCancellationDeadline.before(doNotRemindBefore));
					result.add(new ContractCancellationAppointment(contract, nextCancellationDeadline, hasAlarm));
				}
				Calendar next = Calendar.getInstance();
				next.setTime(nextCancellationDeadline);
				next.add(Calendar.DAY_OF_YEAR, 1);
				nextCancellationDeadline = contract.getNextCancellationDeadline(next.getTime());
			}
		}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Hilfsklasse zum Anzeigen und Oeffnen des Appointments.
	 */
	private class ContractCancellationAppointment extends AbstractAppointment {
		private Contract contract;
		private Date date;
		private boolean hasAlarm;

		/**
		 * ct.
		 * @param date 
		 * @param hasAlarm 
		 * 
		 * @param schedule
		 *            der Termin.
		 */
		private ContractCancellationAppointment(Contract contract, Date date, boolean hasAlarm) {
			super();
			this.contract = contract;
			this.date = date;
			this.hasAlarm = hasAlarm;
		}

		/**
		 * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#getDescription()
		 */
		public String getDescription() {
			try {
				if (Settings.getNamedICalExport()) { 
					return i18n.tr("Cancellation deadline of contract ") + contract.getName() + 
							i18n.tr(". Check Jameica/ContractManager for details.");
				} else {
					return i18n.tr("Cancellation deadline of a contract. Check Jameica/ContractManager for details."); 
				}
			} catch (RemoteException e) {
				return "Error while getting contract name";
			}
		}

		/**
		 * @see de.willuhn.jameica.gui.calendar.Appointment#getName()
		 */
		public String getName() {
			try {
				if (Settings.getNamedICalExport()) { 
					return i18n.tr("Cancellation Deadline: ") + contract.getName();
				} else {
					return i18n.tr("Cancellation Deadline");
				}
			} catch (RemoteException e) {
				return "Error while getting contract name";
			}
		}

		/**
		 * @see de.willuhn.jameica.hbci.calendar.AbstractAppointmentProvider.AbstractHibiscusAppointment#getColor()
		 */
		public RGB getColor() {
			return new RGB(255,0,0);
		}

		/**
		 * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#hasAlarm()
		 */
		public boolean hasAlarm() {
			return hasAlarm;
		}

		@Override
		public Date getDate() {
			return date;
		}

		@Override
		public void execute() throws ApplicationException {
			GUI.startView(de.janrieke.contractmanager.gui.view.ContractDetailView.class
					.getName(), contract);
		}

		@Override
		public int getAlarmTime() {
			// return value is seconds
			try {
				return (int) TimeUnit.DAYS.toSeconds(Settings.getExtensionWarningTime());
			} catch (RemoteException e) {
				return super.getAlarmTime();
			}
		}
	}
}
