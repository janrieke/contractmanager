package de.janrieke.contractmanager.calendar;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
				if (nextCancellationDeadline.after(from))
					result.add(new ContractCancellationAppointment(contract, nextCancellationDeadline));
				nextCancellationDeadline = contract.getNextCancellationDeadline(nextCancellationDeadline);
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

		/**
		 * ct.
		 * @param date 
		 * 
		 * @param schedule
		 *            der Termin.
		 */
		private ContractCancellationAppointment(Contract contract, Date date) {
			super();
			this.contract = contract;
			this.date = date;
		}

		/**
		 * @see de.willuhn.jameica.gui.calendar.AbstractAppointment#getDescription()
		 */
		public String getDescription() {
			try {
				return i18n.tr("Cancellation deadline of contract ") + contract.getName();
			} catch (RemoteException e) {
				return "Error while getting contract name";
			}
		}

		/**
		 * @see de.willuhn.jameica.gui.calendar.Appointment#getName()
		 */
		public String getName() {
			try {
				return i18n.tr("Cancellation Deadline: ") + contract.getName();
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
			return true;
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
	}
}
