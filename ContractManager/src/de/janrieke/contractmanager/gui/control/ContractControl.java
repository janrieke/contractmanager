package de.janrieke.contractmanager.gui.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.input.DateDialogInputAutoCompletion;
import de.janrieke.contractmanager.gui.menu.ContractListMenu;
import de.janrieke.contractmanager.rmi.Address;
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Contract.IntervalType;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.DecimalInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author willuhn, jrieke
 */
public class ContractControl extends AbstractControl {

	// list of all contracts
	private TablePart contractList;

	// Input fields for the contract attributes,
	private Input name;
	private Input contractNo;
	private Input customerNo;
	private Input comment;
	private DateDialogInputAutoCompletion startDate;
	private DateDialogInputAutoCompletion endDate;
	private LabelInput nextExtension;

	private MultiInput cancellationPeriodMulti;
	private IntegerInput cancellationPeriodCount;
	private SelectInput cancellationPeriodType;

	private Input firstMinRuntimeMulti;
	private IntegerInput firstMinRuntimeCount;
	private SelectInput firstMinRuntimeType;

	private Input nextMinRuntimeMulti;
	private IntegerInput nextMinRuntimeCount;
	private SelectInput nextMinRuntimeType;

	private DecimalInput moneyOnce;
	private DecimalInput moneyPerDay;
	private DecimalInput moneyPerWeek;
	private DecimalInput moneyPerMonth;
	private DecimalInput moneyPerYear;

	private Input partnerName;
	private Input partnerStreet;
	private Input partnerNumber;
	private Input partnerStreetNumber;
	private Input partnerExtra;
	private Input partnerZipcode;
	private Input partnerCity;
	private Input partnerZipcodeCity;
	private Input partnerState;
	private Input partnerCountry;

	// list of transactions contained in this contract
	// private TablePart transactionList;

	// this is the currently opened contract
	private Contract contract;

	private LabelInput nextCancellationDeadline;

	/**
	 * ct.
	 * 
	 * @param view
	 *            this is our view (the welcome screen).
	 */
	public ContractControl(AbstractView view) {
		super(view);
	}

	/**
	 * Small helper method to get the current contract.
	 * 
	 * @return
	 */
	private Contract getContract() {
		if (contract != null)
			return contract;
		contract = (Contract) getCurrentObject();
		return contract;
	}

	/**
	 * Returns the input field for the contract name.
	 * 
	 * @return input field.
	 * @throws RemoteException
	 */
	public Input getName() throws RemoteException {
		if (name == null)
			name = new TextInput(getContract().getName(), 255);
		return name;
	}

	public Input getContractNumber() throws RemoteException {
		if (contractNo == null)
			contractNo = new TextInput(getContract().getContractNumber(), 255);
		return contractNo;
	}

	public Input getCustomerNumber() throws RemoteException {
		if (customerNo == null)
			customerNo = new TextInput(getContract().getCustomerNumber(), 255);
		return customerNo;
	}

	/**
	 * Returns the input field for the contract description.
	 * 
	 * @return input field.
	 * @throws RemoteException
	 */
	public Input getComment() throws RemoteException {
		if (comment == null)
			comment = new TextAreaInput(getContract().getComment());
		return comment;
	}

	/**
	 * Returns the input field for the contract price.
	 * 
	 * @return input field.
	 * @throws RemoteException
	 */
	public Input getMoneyOnce() throws RemoteException {
		if (moneyOnce == null) {
			moneyOnce = new DecimalInput(getContract().getMoneyOnce(),
					Settings.DECIMALFORMAT);
			moneyOnce.setComment(Settings.CURRENCY);
		}
		return moneyOnce;
	}

	/**
	 * Returns the input field for the start date.
	 * 
	 * @return input field.
	 * @throws RemoteException
	 */
	public Input getStartDate() throws RemoteException {
		if (startDate != null)
			return startDate;

		// this is a custom dialog that shows a calendar widget.
		CalendarDialog d = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
		d.setTitle(Settings.i18n().tr("Choose a start date"));

		// we have to add a close listener to display the chosen
		// date in the right format.
		d.addCloseListener(new Listener() {
			public void handleEvent(Event event) {
				if (event == null || event.data == null)
					return;

				startDate.setText(Settings.DATEFORMAT.format((Date) event.data));
			}
		});

		Date initial = getContract().getStartDate();
		String s = initial == null ? "YYYY-MM-DD" : Settings.DATEFORMAT
				.format(initial);

		// Dialog-Input is an Input field that gets its data from a dialog.
		startDate = new DateDialogInputAutoCompletion(s, d);

		// we store the initial value
		startDate.setValue(initial);

		return startDate;
	}

	/**
	 * Returns the input field for the end date.
	 * 
	 * @return input field.
	 * @throws RemoteException
	 */
	public Input getEndDate() throws RemoteException {
		if (endDate != null)
			return endDate;

		CalendarDialog d = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
		d.setTitle(Settings.i18n().tr("Choose an end date"));
		d.addCloseListener(new Listener() {
			public void handleEvent(Event event) {
				if (event == null || event.data == null)
					return;

				endDate.setText(Settings.DATEFORMAT.format((Date) event.data));
			}
		});

		Date initial = getContract().getEndDate();
		String s = initial == null ? "YYYY-MM-DD" : Settings.DATEFORMAT
				.format(initial);

		// Dialog-Input is an Input field that gets its data from a dialog.
		endDate = new DateDialogInputAutoCompletion(s, d);

		// we store the initial value
		endDate.setValue(initial);

		return endDate;
	}

	public LabelInput getNextExtension() throws RemoteException {
		if (nextExtension != null)
			return nextExtension;

		Date ne = getContract().getNextExtension();
		nextExtension = new LabelInput(ne == null ? ""
				: Settings.DATEFORMAT.format(ne));
		return nextExtension;
	}

	public LabelInput getNextCancellationDeadline() throws RemoteException {
		if (nextCancellationDeadline != null)
			return nextCancellationDeadline;

		Date ne = getContract().getNextCancellationDeadline();
		nextCancellationDeadline = new LabelInput(ne == null ? ""
				: Settings.DATEFORMAT.format(ne));
		return nextCancellationDeadline;
	}

	public Input getCancellationPeriod() throws RemoteException {
		if (cancellationPeriodMulti != null)
			return cancellationPeriodMulti;

		cancellationPeriodMulti = new MultiInput(getCancellationPeriodCount(),
				getCancellationPeriodType());

		return cancellationPeriodMulti;
	}

	public IntegerInput getCancellationPeriodCount() throws RemoteException {
		if (cancellationPeriodCount == null) {
			cancellationPeriodCount = new IntegerInput(getContract()
					.getCancellationPeriodCount());
		}
		return cancellationPeriodCount;
	}

	public SelectInput getCancellationPeriodType() throws RemoteException {
		if (cancellationPeriodType == null) {
			List<Contract.IntervalType> list = new ArrayList<Contract.IntervalType>();
			list.add(Contract.IntervalType.DAYS);
			list.add(Contract.IntervalType.WEEKS);
			list.add(Contract.IntervalType.MONTHS);
			list.add(Contract.IntervalType.YEARS);
			cancellationPeriodType = new SelectInput(list, getContract()
					.getCancellationPeriodType());
		}
		return cancellationPeriodType;
	}

	public Input getFirstMinRuntime() throws RemoteException {
		if (firstMinRuntimeMulti != null)
			return firstMinRuntimeMulti;

		firstMinRuntimeMulti = new MultiInput(getFirstMinRuntimeCount(),
				getFirstMinRuntimeType());

		return firstMinRuntimeMulti;
	}

	public IntegerInput getFirstMinRuntimeCount() throws RemoteException {
		if (firstMinRuntimeCount == null) {
			firstMinRuntimeCount = new IntegerInput(getContract()
					.getFirstMinRuntimeCount());
		}
		return firstMinRuntimeCount;
	}

	public SelectInput getFirstMinRuntimeType() throws RemoteException {
		if (firstMinRuntimeType == null) {
			List<Contract.IntervalType> list = new ArrayList<Contract.IntervalType>();
			list.add(Contract.IntervalType.DAYS);
			list.add(Contract.IntervalType.WEEKS);
			list.add(Contract.IntervalType.MONTHS);
			list.add(Contract.IntervalType.YEARS);
			firstMinRuntimeType = new SelectInput(list, getContract()
					.getFirstMinRuntimeType());
		}
		return firstMinRuntimeType;
	}

	public Input getNextMinRuntime() throws RemoteException {
		if (nextMinRuntimeMulti != null)
			return nextMinRuntimeMulti;

		nextMinRuntimeMulti = new MultiInput(getNextMinRuntimeCount(),
				getNextMinRuntimeType());

		return nextMinRuntimeMulti;
	}

	public IntegerInput getNextMinRuntimeCount() throws RemoteException {
		if (nextMinRuntimeCount == null) {
			nextMinRuntimeCount = new IntegerInput(getContract()
					.getNextMinRuntimeCount());
		}
		return nextMinRuntimeCount;
	}

	public SelectInput getNextMinRuntimeType() throws RemoteException {
		if (nextMinRuntimeType == null) {
			List<Contract.IntervalType> list = new ArrayList<Contract.IntervalType>();
			list.add(Contract.IntervalType.DAYS);
			list.add(Contract.IntervalType.WEEKS);
			list.add(Contract.IntervalType.MONTHS);
			list.add(Contract.IntervalType.YEARS);
			nextMinRuntimeType = new SelectInput(list, getContract()
					.getNextMinRuntimeType());
		}
		return nextMinRuntimeType;
	}

	public DecimalInput getMoneyPerDay() throws RemoteException {
		if (moneyPerDay == null) {
			moneyPerDay = new DecimalInput(getContract().getMoneyPerDay(),
					Settings.DECIMALFORMAT);
			moneyPerDay.setComment(Settings.CURRENCY);
		}
		return moneyPerDay;
	}

	public DecimalInput getMoneyPerWeek() throws RemoteException {
		if (moneyPerWeek == null) {
			moneyPerWeek = new DecimalInput(getContract().getMoneyPerWeek(),
					Settings.DECIMALFORMAT);
			moneyPerWeek.setComment(Settings.CURRENCY);
		}
		return moneyPerWeek;
	}

	public DecimalInput getMoneyPerMonth() throws RemoteException {
		if (moneyPerMonth == null) {
			moneyPerMonth = new DecimalInput(getContract().getMoneyPerMonth(),
					Settings.DECIMALFORMAT);
			moneyPerMonth.setComment(Settings.CURRENCY);
		}
		return moneyPerMonth;
	}

	public DecimalInput getMoneyPerYear() throws RemoteException {
		if (moneyPerYear == null) {
			moneyPerYear = new DecimalInput(getContract().getMoneyPerYear(),
					Settings.DECIMALFORMAT);
			moneyPerYear.setComment(Settings.CURRENCY);
		}
		return moneyPerYear;
	}

	public Input getPartnerName() throws RemoteException {
		if (partnerName == null)
			partnerName = new TextInput(getContract().getAddress().getName(),
					255);
		return partnerName;
	}

	public Input getPartnerStreet() throws RemoteException {
		if (partnerStreet == null)
			partnerStreet = new TextInput(getContract().getAddress()
					.getStreet(), 255);
		return partnerStreet;
	}

	public Input getPartnerNumber() throws RemoteException {
		if (partnerNumber == null)
			partnerNumber = new TextInput(getContract().getAddress()
					.getNumber(), 255);
		return partnerNumber;
	}

	public Input getPartnerStreetNumber() throws RemoteException {
		if (partnerStreetNumber != null)
			return partnerStreetNumber;

		partnerStreetNumber = new MultiInput(getPartnerStreet(),
				new LabelInput(Settings.i18n().tr("Number")),
				getPartnerNumber());

		return partnerStreetNumber;
	}

	public Input getPartnerExtra() throws RemoteException {
		if (partnerExtra == null)
			partnerExtra = new TextInput(getContract().getAddress().getExtra(),
					255);
		return partnerExtra;
	}

	public Input getPartnerZipcode() throws RemoteException {
		if (partnerZipcode == null)
			partnerZipcode = new TextInput(getContract().getAddress()
					.getZipcode(), 255);
		return partnerZipcode;
	}

	public Input getPartnerCity() throws RemoteException {
		if (partnerCity == null)
			partnerCity = new TextInput(getContract().getAddress().getCity(),
					255);
		return partnerCity;
	}

	public Input getPartnerZipcodeCity() throws RemoteException {
		if (partnerZipcodeCity != null)
			return partnerZipcodeCity;

		partnerZipcodeCity = new MultiInput(getPartnerZipcode(),
				new LabelInput(Settings.i18n().tr("City")), getPartnerCity());

		return partnerZipcodeCity;
	}

	public Input getPartnerState() throws RemoteException {
		if (partnerState == null)
			partnerState = new TextInput(getContract().getAddress().getState(),
					255);
		return partnerState;
	}

	public Input getPartnerCountry() throws RemoteException {
		if (partnerCountry == null)
			partnerCountry = new TextInput(getContract().getAddress()
					.getCountry(), 255);
		return partnerCountry;
	}

	/**
	 * Creates a table containing all contracts.
	 * 
	 * @return a table with contracts.
	 * @throws RemoteException
	 */
	public Part getContractsTable() throws RemoteException {
		if (contractList != null)
			return contractList;

		// 1) get the dataservice
		DBService service = Settings.getDBService();

		// 2) now we can create the contract list.
		// We do not need to specify the implementing class for
		// the interface "Contract". Jameica's classloader knows
		// all classes an finds the right implementation automatically. ;)
		DBIterator contracts = service.createList(Contract.class);

		// 4) create the table
		contractList = new TablePart(
				contracts,
				new de.janrieke.contractmanager.gui.action.ShowContractDetailView());

		// 5) now we have to add some columns.
		contractList.addColumn(Settings.i18n().tr("Name of contract"), "name");

		// 6) the following fields are a date fields. So we add a date
		// formatter.
		contractList.addColumn(Settings.i18n().tr("Start date"), "startdate",
				new DateFormatter(Settings.DATEFORMAT));
		contractList.addColumn(Settings.i18n().tr("End date"), "enddate",
				new DateFormatter(Settings.DATEFORMAT));
		contractList.addColumn(
				Settings.i18n().tr("Next cancellation deadline"),
				"nextCancellationDeadline", new DateFormatter(
						Settings.DATEFORMAT));
		contractList.addColumn(Settings.i18n().tr("Costs per Term"),
				"costsPerPeriod", new CurrencyFormatter(Settings.CURRENCY,
						Settings.DECIMALFORMAT));

		// 7) we are adding a context menu
		contractList.setContextMenu(new ContractListMenu());

		contractList.setFormatter(new TableFormatter() {

			@Override
			public void format(TableItem item) {
				if (item.getData() instanceof Contract) {
					Contract contract = (Contract) item.getData();

					Calendar today = Calendar.getInstance();
					Calendar calendar = Calendar.getInstance();
					try {
						calendar.setTime(contract.getNextCancellationDeadline());
						calendar.add(Calendar.DAY_OF_YEAR,
								-Settings.getExtensionWarningTime());
						if (calendar.before(today))
							item.setBackground(Color.ERROR.getSWTColor());
						else {
							calendar.setTime(contract
									.getNextCancellationDeadline());
							calendar.add(Calendar.DAY_OF_YEAR,
									-Settings.getExtensionNoticeTime());
							if (calendar.before(today))
								item.setBackground(Color.MANDATORY_BG
										.getSWTColor());
						}
					} catch (RemoteException e) {
					}
				}
			}
		});
		return contractList;
	}

	static int index = 0;

	/**
	 * Creates a table containing all contracts in extension warning or notice
	 * time.
	 * 
	 * @return a table with contracts.
	 * @throws RemoteException
	 */
	public Part getContractsExtensionWarningTable() throws RemoteException {
		if (contractList != null)
			return contractList;

		// 1) get the dataservice
		DBService service = Settings.getDBService();

		// 2) now we can create the contract list.
		// We do not need to specify the implementing class for
		// the interface "Contract". Jameica's classloader knows
		// all classes an finds the right implementation automatically. ;)
		DBIterator contracts = service.createList(Contract.class);

		// 4) create the table
		contractList = new TablePart(
				contracts,
				new de.janrieke.contractmanager.gui.action.ShowContractDetailView());

		// 5) now we have to add some columns.
		contractList.addColumn(Settings.i18n().tr("Name of contract"), "name");

		// 6) the following fields are a date fields. So we add a date
		// formatter.
		contractList.addColumn(Settings.i18n().tr("Start date"), "startdate",
				new DateFormatter(Settings.DATEFORMAT));
		contractList.addColumn(Settings.i18n().tr("End date"), "enddate",
				new DateFormatter(Settings.DATEFORMAT));
		contractList.addColumn(
				Settings.i18n().tr("Next cancellation deadline"),
				"nextCancellationDeadline", new DateFormatter(
						Settings.DATEFORMAT));
		contractList.addColumn(Settings.i18n().tr("Costs per Term"),
				"costsPerPeriod", new CurrencyFormatter(Settings.CURRENCY,
						Settings.DECIMALFORMAT));

		// 7) we are adding a context menu
		contractList.setContextMenu(new ContractListMenu());

		contractList.setFormatter(new TableFormatter() {

			@Override
			public void format(TableItem item) {
				if (item.getData() instanceof Contract) {
					Contract contract = (Contract) item.getData();

					Calendar today = Calendar.getInstance();
					Calendar calendar = Calendar.getInstance();
					try {
						calendar.setTime(contract.getNextCancellationDeadline());
						calendar.add(Calendar.DAY_OF_YEAR,
								-Settings.getExtensionNoticeTime());
						if (calendar.before(today)) {
							calendar.setTime(contract
									.getNextCancellationDeadline());
							calendar.add(Calendar.DAY_OF_YEAR,
									-Settings.getExtensionWarningTime());
							if (calendar.before(today))
								item.setBackground(Color.ERROR.getSWTColor());
							else
								item.setBackground(Color.MANDATORY_BG.getSWTColor());
						} else {
							TableItem[] items = item.getParent().getItems();
							boolean found = false;
							index = index < items.length ? index : 0;
							for (int i = index; i < items.length; i++) {
								if (item.equals(items[i])) {
									index = i;
									found = true;
									break;
								}
							}
							if (!found) {
								for (int i = 0; i < index; i++) {
									if (item.equals(items[i])) {
										index = i;
										found = true;
										break;
									}
								}
							}
							if (found)
								item.getParent().remove(index);
						}
					} catch (RemoteException e) {
					}
				}
			}
		});
		return contractList;
	}

	/**
	 * Returns a list of tasks in this contract.
	 * 
	 * @return list of tasks in this contract
	 * @throws RemoteException
	 */
	// public Part getTransactionList() throws RemoteException {
	// if (transactionList != null)
	// return transactionList;
	//
	// GenericIterator transactions = getContract().getTransactions();
	// transactionList = new TablePart(transactions, new TransactionDetail());
	// transactionList.addColumn(Settings.i18n().tr("Task name"), "name");
	// transactionList.addColumn(Settings.i18n().tr("Effort"), "effort",
	// new Formatter() {
	// @Override
	// public String format(Object o) {
	// if (o == null)
	// return "-";
	// return o + " h";
	// }
	// });
	//
	// TransactionListMenu tlm = new TransactionListMenu();
	//
	// // we add an additional menu item to create tasks with predefined
	// // contract.
	// tlm.addItem(new ContextMenuItem(Settings.i18n().tr(
	// "Create new task within this Contract"), new Action() {
	// public void handleAction(Object context)
	// throws ApplicationException {
	// new TransactionDetail().handleAction(getContract());
	// }
	// }));
	// transactionList.setContextMenu(tlm);
	// transactionList.setSummary(false);
	// return transactionList;
	// }

	/**
	 * This method stores the contract using the current values.
	 */
	public void handleStore() {
		try {
			// get the current contract.
			Contract p = getContract();

			// invoke all Setters of this contract and assign the current values
			p.setName((String) getName().getValue());
			p.setContractNumber((String) getContractNumber().getValue());
			p.setCustomerNumber((String) getCustomerNumber().getValue());
			p.setComment((String) getComment().getValue());
			p.setStartDate((Date) getStartDate().getValue());
			p.setEndDate((Date) getEndDate().getValue());

			p.setCancelationPeriodCount((Integer) getCancellationPeriodCount()
					.getValue());
			p.setCancelationPeriodType((IntervalType) getCancellationPeriodType()
					.getValue());
			p.setFirstMinRuntimeCount((Integer) getFirstMinRuntimeCount()
					.getValue());
			p.setFirstMinRuntimeType((IntervalType) getFirstMinRuntimeType()
					.getValue());
			p.setNextMinRuntimeCount((Integer) getNextMinRuntimeCount()
					.getValue());
			p.setNextMinRuntimeType((IntervalType) getNextMinRuntimeType()
					.getValue());

			Double d = (Double) getMoneyOnce().getValue();
			p.setMoneyOnce(d == null ? 0.0 : d.doubleValue());
			d = (Double) getMoneyPerDay().getValue();
			p.setMoneyPerDay(d == null ? 0.0 : d.doubleValue());
			d = (Double) getMoneyPerWeek().getValue();
			p.setMoneyPerWeek(d == null ? 0.0 : d.doubleValue());
			d = (Double) getMoneyPerMonth().getValue();
			p.setMoneyPerMonth(d == null ? 0.0 : d.doubleValue());
			d = (Double) getMoneyPerYear().getValue();
			p.setMoneyPerYear(d == null ? 0.0 : d.doubleValue());

			Address a = getContract().getAddress();
			a.setName((String) getPartnerName().getValue());
			a.setStreet((String) getPartnerStreet().getValue());
			a.setNumber((String) getPartnerNumber().getValue());
			a.setExtra((String) getPartnerExtra().getValue());
			a.setZipcode((String) getPartnerZipcode().getValue());
			a.setCity((String) getPartnerCity().getValue());
			a.setState((String) getPartnerState().getValue());
			a.setCountry((String) getPartnerCountry().getValue());

			// Now, let's store the contract and its address.
			// The store() method throws ApplicationExceptions if
			// insertCheck() or updateCheck() failed.
			try {
				a.store();
				// We have to set the address here, because a new, unstored
				// object has no ID, yet.
				p.setAddress(a);
				p.store();
				GUI.getStatusBar().setSuccessText(
						Settings.i18n().tr("Contract stored successfully"));
			} catch (ApplicationException e) {
				GUI.getView().setErrorText(e.getMessage());
			}
		} catch (RemoteException e) {
			Logger.error("error while storing contract", e);
			GUI.getStatusBar().setErrorText(
					Settings.i18n().tr("Error while storing contract"));
		}
	}
}