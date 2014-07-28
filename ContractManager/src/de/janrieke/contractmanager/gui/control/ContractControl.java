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
package de.janrieke.contractmanager.gui.control;

import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.input.DateDialogInputAutoCompletion;
import de.janrieke.contractmanager.gui.menu.ContractListMenu;
import de.janrieke.contractmanager.gui.menu.CostsListMenu;
import de.janrieke.contractmanager.gui.parts.ContractListTablePart;
import de.janrieke.contractmanager.gui.parts.CostsListTablePart;
import de.janrieke.contractmanager.gui.parts.SizeableTablePart;
import de.janrieke.contractmanager.gui.view.ContractDetailView;
import de.janrieke.contractmanager.rmi.Address;
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Contract.IntervalType;
import de.janrieke.contractmanager.rmi.Costs;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.Part;
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TableChangeListener;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author willuhn, jrieke
 */
public class ContractControl extends AbstractControl {

	// list of all contracts
	private ContractListTablePart contractList;
	// list of all contracts with cancellation warnings
	private SizeableTablePart contractListWarnings;

	// Input fields for the contract attributes,
	private Input name;
	private Input contractNo;
	private Input customerNo;
	private Input comment;
	private Input sepaCreditorRef;
	private Input sepaCustomerRef;
	private DateDialogInputAutoCompletion startDate;
	private DateDialogInputAutoCompletion endDate;
	private Input nextExtension;

	private MultiInput cancellationPeriodMulti;
	private IntegerInput cancellationPeriodCount;
	private SelectInput cancellationPeriodType;

	private Input firstRuntimeMulti;
	private IntegerInput firstRuntimeCount;
	private SelectInput firstRuntimeType;

	private Input nextRuntimeMulti;
	private IntegerInput nextRuntimeCount;
	private SelectInput nextRuntimeType;
	private CheckboxInput remindCheckbox;

	private SelectInput partnerAddress;
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

	private CostsListTablePart costsList;
	private LabelInput costsPerTerm;
	private LabelInput costsPerMonth;

	private List<Costs> newCosts = new ArrayList<Costs>();
	private List<Costs> deletedCosts = new ArrayList<Costs>();

	// holds the current Hibiscus category of the selection box (used for
	// storing to DB on store button click)
	public String hibiscusCategoryID = null;

	/**
	 * ct.
	 * 
	 * @param view
	 *            this is our view (the contract details view).
	 */
	public ContractControl(AbstractView view) {
		super(view);
		if (view instanceof ContractDetailView) {
			try {
				((ContractDetailView) view)
						.setButtonActivationState(!((Contract) view
								.getCurrentObject()).isNewObject());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
	 * Returns an iterator with all contracts in the database.
	 * 
	 * @throws RemoteException
	 * @return iterator containing all addresses
	 */
	public static DBIterator getContracts() throws RemoteException {
		DBService service = Settings.getDBService();
		DBIterator contracts = service.createList(Contract.class);
		return contracts;
	}

	/**
	 * Returns the input field for the contract name.
	 * 
	 * @return input field.
	 * @throws RemoteException
	 */
	public Input getName() throws RemoteException {
		if (name == null) {
			name = new TextInput(getContract().getName(), 255);
			name.setMandatory(true);
		}
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

	public Input getSEPACreditorReference() throws RemoteException {
		if (sepaCreditorRef == null)
			sepaCreditorRef = new TextInput(getContract().getSepaCreditorRef(), 35);
		return sepaCreditorRef;
	}

	public Input getSEPACustomerReference() throws RemoteException {
		if (sepaCustomerRef == null)
			sepaCustomerRef = new TextInput(getContract().getSepaCustomerRef(), 35);
		return sepaCustomerRef;
	}

	/**
	 * Returns the input field for the contract description.
	 * 
	 * @return input field.
	 * @throws RemoteException
	 */
	public Input getComment() throws RemoteException {
		if (comment == null)
			comment = new TextAreaInput(getContract().getComment(), 10000);
		return comment;
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

				startDate.setText(Settings.dateformat((Date) event.data));
			}
		});

		Date initial = getContract().getStartDate();
		String s = initial == null ? "YYYY-MM-DD" : Settings
				.dateformat(initial);

		// Dialog-Input is an Input field that gets its data from a dialog.
		startDate = new DateDialogInputAutoCompletion(s, initial, d);

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

				endDate.setText(Settings.dateformat((Date) event.data));
			}
		});

		Date initial = getContract().getEndDate();
		String s = initial == null ? "YYYY-MM-DD" : Settings
				.dateformat(initial);

		// Dialog-Input is an Input field that gets its data from a dialog.
		endDate = new DateDialogInputAutoCompletion(s, initial, d);

		// we store the initial value
		endDate.setValue(initial);

		return endDate;
	}

	public Input getNextTerm() throws RemoteException {
		if (nextExtension != null)
			return nextExtension;

		Date ntb = getContract().getNextCancelableTermBegin();
		Date nte = getContract().getNextCancelableTermEnd();
		if (ntb != null && nte != null) {
			nextExtension = new MultiInput(new LabelInput(
					Settings.dateformat(ntb) + " " + Settings.i18n().tr("to")
							+ " " + Settings.dateformat(nte)));
		} else
			nextExtension = new LabelInput("");
		return nextExtension;
	}

	public LabelInput getNextCancellationDeadline() throws RemoteException {
		if (nextCancellationDeadline != null)
			return nextCancellationDeadline;

		Date ne = getContract().getNextCancellationDeadline();
		nextCancellationDeadline = new LabelInput(ne == null ? ""
				: Settings.dateformat(ne));
		return nextCancellationDeadline;
	}

	public LabelInput getCostsPerTerm() throws RemoteException {
		if (costsPerTerm != null)
			return costsPerTerm;

		double costs = getContract().getMoneyPerTerm();
		if (Double.isNaN(costs)) {
			costsPerTerm = new LabelInput("");
		} else {
			costsPerTerm = new LabelInput(Settings.DECIMALFORMAT.format(costs));
			costsPerTerm.setComment(Settings.CURRENCY);
		}
		return costsPerTerm;
	}

	public LabelInput getCostsPerMonth() throws RemoteException {
		if (costsPerMonth != null)
			return costsPerMonth;

		double costs = getContract().getMoneyPerMonth();
		costsPerMonth = new LabelInput(Settings.DECIMALFORMAT.format(costs));
		costsPerMonth.setComment(Settings.CURRENCY);
		return costsPerMonth;
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

	public Input getFirstRuntime() throws RemoteException {
		if (firstRuntimeMulti != null)
			return firstRuntimeMulti;

		firstRuntimeMulti = new MultiInput(getFirstRuntimeCount(),
				getFirstRuntimeType());

		return firstRuntimeMulti;
	}

	public IntegerInput getFirstRuntimeCount() throws RemoteException {
		if (firstRuntimeCount == null) {
			firstRuntimeCount = new IntegerInput(getContract()
					.getFirstMinRuntimeCount());
		}
		return firstRuntimeCount;
	}

	public SelectInput getFirstRuntimeType() throws RemoteException {
		if (firstRuntimeType == null) {
			List<Contract.IntervalType> list = new ArrayList<Contract.IntervalType>();
			list.add(Contract.IntervalType.DAYS);
			list.add(Contract.IntervalType.WEEKS);
			list.add(Contract.IntervalType.MONTHS);
			list.add(Contract.IntervalType.YEARS);
			firstRuntimeType = new SelectInput(list, getContract()
					.getFirstMinRuntimeType());
		}
		return firstRuntimeType;
	}

	public Input getNextRuntime() throws RemoteException {
		if (nextRuntimeMulti != null)
			return nextRuntimeMulti;

		nextRuntimeMulti = new MultiInput(getNextRuntimeCount(),
				getNextRuntimeType());

		return nextRuntimeMulti;
	}

	public IntegerInput getNextRuntimeCount() throws RemoteException {
		if (nextRuntimeCount == null) {
			nextRuntimeCount = new IntegerInput(getContract()
					.getFollowingMinRuntimeCount());
			
			// when focusing this input, transfer the values from the first runtime 
			//  as default values if no value is set, yet
			nextRuntimeCount.addListener(new Listener() {
				
				@Override
				public void handleEvent(Event event) {
					if (event.type == SWT.FocusIn) {
						if (((Integer)0).equals(nextRuntimeCount.getValue()) && Contract.IntervalType.MONTHS.equals(nextRuntimeType.getValue())) {
							nextRuntimeCount.setValue(firstRuntimeCount.getValue());
							nextRuntimeType.setValue(firstRuntimeType.getValue());
						}
					}
				}
			});
		}
		return nextRuntimeCount;
	}

	public SelectInput getNextRuntimeType() throws RemoteException {
		if (nextRuntimeType == null) {
			List<Contract.IntervalType> list = new ArrayList<Contract.IntervalType>();
			list.add(Contract.IntervalType.DAYS);
			list.add(Contract.IntervalType.WEEKS);
			list.add(Contract.IntervalType.MONTHS);
			list.add(Contract.IntervalType.YEARS);
			nextRuntimeType = new SelectInput(list, getContract()
					.getFollowingMinRuntimeType());
		}
		return nextRuntimeType;
	}

	public CheckboxInput getRemind() throws RemoteException {
		if (remindCheckbox == null) {
			remindCheckbox = new CheckboxInput(!getContract().getDoNotRemind());
		}
		return remindCheckbox;
	}

	private Address currentAddress = null;

	public SelectInput getPartnerAddress() throws RemoteException {
		if (partnerAddress == null) {
			currentAddress = getContract().getAddress();
			partnerAddress = new SelectInput(AddressControl.getAddresses(),
					currentAddress);
			partnerAddress.setPleaseChoose(Settings.i18n().tr("[New Address]"));
			partnerAddress.addListener(new Listener() {

				@Override
				public void handleEvent(Event event) {
					if (event.type == SWT.Selection) {
						Address newAddress = (Address) partnerAddress
								.getValue();
						try {
							if (newAddress == null
									|| !newAddress.equals(currentAddress)) {
								addressSwitched(newAddress);
								currentAddress = newAddress;
							}
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
		}
		return partnerAddress;
	}

	protected void addressSwitched(Address newAddress) throws RemoteException {
		if (newAddress == null) {
			getPartnerCity().setValue("");
			getPartnerCountry().setValue("");
			getPartnerExtra().setValue("");
			getPartnerName().setValue("");
			getPartnerNumber().setValue("");
			getPartnerState().setValue("");
			getPartnerStreet().setValue("");
			getPartnerZipcode().setValue("");
		} else {
			getPartnerCity().setValue(newAddress.getCity());
			getPartnerCountry().setValue(newAddress.getCountry());
			getPartnerExtra().setValue(newAddress.getExtra());
			getPartnerName().setValue(newAddress.getName());
			getPartnerNumber().setValue(newAddress.getNumber());
			getPartnerState().setValue(newAddress.getState());
			getPartnerStreet().setValue(newAddress.getStreet());
			getPartnerZipcode().setValue(newAddress.getZipcode());
		}
	}

	public Input getPartnerName() throws RemoteException {
		if (partnerName == null) {
			partnerName = new TextInput(getContract().getAddress() == null ? ""
					: getContract().getAddress().getName(), 255);
			partnerName.setMandatory(true);
		}
		return partnerName;
	}

	public Input getPartnerStreet() throws RemoteException {
		if (partnerStreet == null)
			partnerStreet = new TextInput(
					getContract().getAddress() == null ? "" : getContract()
							.getAddress().getStreet(), 255);
		return partnerStreet;
	}

	public Input getPartnerNumber() throws RemoteException {
		if (partnerNumber == null)
			partnerNumber = new TextInput(
					getContract().getAddress() == null ? "" : getContract()
							.getAddress().getNumber(), 16);
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
			partnerExtra = new TextInput(
					getContract().getAddress() == null ? "" : getContract()
							.getAddress().getExtra(), 255);
		return partnerExtra;
	}

	public Input getPartnerZipcode() throws RemoteException {
		if (partnerZipcode == null)
			partnerZipcode = new TextInput(
					getContract().getAddress() == null ? "" : getContract()
							.getAddress().getZipcode(), 5);
		return partnerZipcode;
	}

	public Input getPartnerCity() throws RemoteException {
		if (partnerCity == null)
			partnerCity = new TextInput(getContract().getAddress() == null ? ""
					: getContract().getAddress().getCity(), 255);
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
			partnerState = new TextInput(
					getContract().getAddress() == null ? "" : getContract()
							.getAddress().getState(), 255);
		return partnerState;
	}

	public Input getPartnerCountry() throws RemoteException {
		if (partnerCountry == null)
			partnerCountry = new TextInput(
					getContract().getAddress() == null ? "" : getContract()
							.getAddress().getCountry(), 255);
		return partnerCountry;
	}

	/**
	 * Creates a table containing all contracts.
	 * 
	 * @return a table with contracts.
	 * @throws RemoteException
	 */
	public ContractListTablePart getContractsTable() throws RemoteException {
		if (contractList != null)
			return contractList;

		GenericIterator contracts = getContracts();
		Date today = new Date(); 

		double total = 0d;
		while (contracts.hasNext()) {
			Contract c = (Contract) contracts.next();
			if (c.isActiveInMonth(today))
				total += c.getMoneyPerMonth();
		}
		
		contracts.begin();
		
		// 4) create the table
		contractList = new ContractListTablePart(
				contracts,
				new de.janrieke.contractmanager.gui.action.ShowContractDetailView());
		contractList.setSum(total);

		// 5) now we have to add some columns.
		contractList.addColumn(Settings.i18n().tr("Name of Contract"), "name");
		contractList.addColumn(Settings.i18n().tr("Contract Partner"), Contract.PARTNER_NAME);
		contractList.addColumn(Settings.i18n().tr("Start Date"), "startdate",
				new DateFormatter(Settings.getNewDateFormat()));
		contractList.addColumn(Settings.i18n().tr("End Date"), "enddate",
				new DateFormatter(Settings.getNewDateFormat()));
		contractList.addColumn(
				Settings.i18n().tr("Next Cancellation Deadline"),
				Contract.NEXT_CANCELLATION_DEADLINE,
				new DateFormatter(Settings.getNewDateFormat()));
		contractList.addColumn(Settings.i18n().tr("Money per Term"),
				Contract.MONEY_PER_TERM, new CurrencyFormatter(
						Settings.CURRENCY, Settings.DECIMALFORMAT), false,
				Column.ALIGN_RIGHT);
		contractList.addColumn(Settings.i18n().tr("Money per Month"),
				Contract.MONEY_PER_MONTH, new CurrencyFormatter(
						Settings.CURRENCY, Settings.DECIMALFORMAT));
		contractList.addColumn(Settings.i18n().tr("Remind?"), "ignore_cancellations", new Formatter() {
			
			@Override
			public String format(Object o) {
				if (o instanceof Integer) {
					if (((Integer)o).intValue() == 0)
						return "\u2611";
					else
						return "\u2610";
				}
				else return "";
			}
		}, true, Column.ALIGN_LEFT);

		// 7) we are adding a context menu
		contractList.setContextMenu(new ContractListMenu(true));

		contractList.setFormatter(new TableFormatter() {

			@Override
			public void format(TableItem item) {
				if (item.getData() instanceof Contract) {
					Contract contract = (Contract) item.getData();

					Calendar today = Calendar.getInstance();
					Calendar calendar = Calendar.getInstance();
					try {
						if (!contract.isActiveInMonth(new Date())) {
							item.setForeground(Settings.getNotActiveForeground());
						}

						if (contract.getDoNotRemind())
							return;
						Date deadline = contract.getNextCancellationDeadline();
						if (deadline == null)
							return;
						calendar.setTime(deadline);
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

	private GenericIterator costsIterator;

	/**
	 * Creates a table containing all contracts in extension warning or notice
	 * time.
	 * 
	 * @return a table with contracts.
	 * @throws RemoteException
	 */
	public TablePart getContractsExtensionWarningTable() throws RemoteException {
		if (contractListWarnings != null)
			return contractListWarnings;

		// 1) get the dataservice
		DBService service = Settings.getDBService();

		// 2) now we can create the contract list.
		// We do not need to specify the implementing class for
		// the interface "Contract". Jameica's classloader knows
		// all classes an finds the right implementation automatically. ;)
		DBIterator contracts = service.createList(Contract.class);

		ArrayList<Contract> filteredContracts = new ArrayList<Contract>();

		// Iterate through the list and filter
		while (contracts.hasNext()) {
			Contract contract = (Contract) contracts.next();

			Calendar today = Calendar.getInstance();
			Calendar calendar = Calendar.getInstance();
			try {
				if (contract.getDoNotRemind())
					continue;
				Date deadline = contract.getNextCancellationDeadline();
				if (deadline == null)
					continue;
				calendar.setTime(deadline);
				calendar.add(Calendar.DAY_OF_YEAR,
						-Settings.getExtensionNoticeTime());
				if (calendar.before(today)) {
					filteredContracts.add(contract);
				}
			} catch (RemoteException e) {
			}
		}
		Contract[] filteredArray = new Contract[filteredContracts.size()];
		GenericIterator filteredIterator = PseudoIterator
				.fromArray(filteredContracts.toArray(filteredArray));

		// 4) create the table
		contractListWarnings = new SizeableTablePart(
				filteredIterator,
				new de.janrieke.contractmanager.gui.action.ShowContractDetailView());

		// 5) now we have to add some columns.
		contractListWarnings.addColumn(Settings.i18n().tr("Name of Contract"),
				"name");

		// 6) the following fields are a date fields. So we add a date
		// formatter.
		contractListWarnings.addColumn(Settings.i18n().tr("Start Date"),
				"startdate", new DateFormatter(Settings.getNewDateFormat()));
		contractListWarnings.addColumn(Settings.i18n().tr("End Date"),
				"enddate", new DateFormatter(Settings.getNewDateFormat()));
		contractListWarnings.addColumn(
				Settings.i18n().tr("Next Cancellation Deadline"),
				Contract.NEXT_CANCELLATION_DEADLINE,
				new DateFormatter(Settings.getNewDateFormat()));
		contractListWarnings.addColumn(Settings.i18n().tr("Money per Term"),
				Contract.MONEY_PER_TERM, new CurrencyFormatter(
						Settings.CURRENCY, Settings.DECIMALFORMAT));
		contractListWarnings.addColumn(Settings.i18n().tr("Money per Month"),
				Contract.MONEY_PER_MONTH, new CurrencyFormatter(
						Settings.CURRENCY, Settings.DECIMALFORMAT));

		// 7) we are adding a context menu
		contractListWarnings.setContextMenu(new ContractListMenu(false));

		contractListWarnings.setFormatter(new TableFormatter() {

			@Override
			public void format(TableItem item) {
				if (item.getData() instanceof Contract) {
					Contract contract = (Contract) item.getData();
					Calendar today = Calendar.getInstance();
					Calendar calendar = Calendar.getInstance();
					try {
						Date deadline = contract.getNextCancellationDeadline();
						assert (deadline != null); // otherwise it never should
													// have been added
						calendar.setTime(deadline);
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
								item.setBackground(Color.MANDATORY_BG
										.getSWTColor());
						}
					} catch (RemoteException e) {
					}
				}
			}
		});
		contractListWarnings.orderBy(3);
		
		return contractListWarnings;
	}

	/**
	 * Returns a list of transactions in this contract.
	 * 
	 * @return list of transactions in this contract
	 * @throws RemoteException
	 */
	public Part getCostsList() throws RemoteException {
		if (costsList != null)
			return costsList;

		costsIterator = getContract().getCosts();
		costsList = new CostsListTablePart(costsIterator, null);
		costsList.setFormatter(new TableFormatter() {

			@Override
			public void format(TableItem item) {
				try {
					double money = ((Costs) item.getData()).getMoney(); // Double.parseDouble(item.getText(1));
					String text = String.format("%1$.2f", money) + " \u20AC";
					item.setText(1, text);
					item.setText(2, ((Costs) item.getData()).getPeriod()
							.getAdjective());
				} catch (RemoteException e) {
				}
			}
		});

		costsList.setHeightHint(120);
		costsList.addColumn(Settings.i18n().tr("Description"), "description",
				null, true);
		costsList.addColumn(Settings.i18n().tr("Money"), "money", null, true);
		costsList.addColumn(Settings.i18n().tr("Period"), "period", null, true);
		CostsListMenu clm = new CostsListMenu(this);
		costsList.setContextMenu(clm);
		costsList.setSummary(false);
		costsList.addChangeListener(new TableChangeListener() {

			@Override
			public void itemChanged(Object object, String attribute,
					String newValue) throws ApplicationException {
				assert object instanceof Costs;
				try {
					if ("description".equals(attribute)) {
						((Costs) object).setDescription(newValue.substring(0,
								Math.min(newValue.length(), 255)));
					} else if ("money".equals(attribute)) {
						try {
							Number num = NumberFormat.getInstance().parse(
									newValue);
							((Costs) object).setMoney(num.doubleValue());
						} catch (ParseException e) {
							((Costs) object).setMoney(0d);
						}
					} else if ("period".equals(attribute))
						((Costs) object).setPeriod(Contract.IntervalType
								.valueOfAdjective(newValue));
					else
						assert false;
				} catch (RemoteException e) {
					throw new ApplicationException(e);
				}
			}
		});
		return costsList;
	}

	/**
	 * This method stores the contract using the current values.
	 */
	public void handleStore() {
		try {
			// get the current contract.
			Contract c = getContract();

			// invoke all Setters of this contract and assign the current values
			c.setName((String) getName().getValue());
			c.setContractNumber((String) getContractNumber().getValue());
			c.setCustomerNumber((String) getCustomerNumber().getValue());
			c.setComment((String) getComment().getValue());
			c.setStartDate((Date) getStartDate().getValue());
			c.setEndDate((Date) getEndDate().getValue());

			c.setCancelationPeriodCount((Integer) getCancellationPeriodCount()
					.getValue());
			c.setCancelationPeriodType((IntervalType) getCancellationPeriodType()
					.getValue());
			c.setFirstMinRuntimeCount((Integer) getFirstRuntimeCount()
					.getValue());
			c.setFirstMinRuntimeType((IntervalType) getFirstRuntimeType()
					.getValue());
			c.setFollowingMinRuntimeCount((Integer) getNextRuntimeCount()
					.getValue());
			c.setFollowingMinRuntimeType((IntervalType) getNextRuntimeType()
					.getValue());

			c.setDoNotRemind(!(Boolean) getRemind().getValue());

			c.setHibiscusCategoryID(hibiscusCategoryID);

			Address a = (Address) getPartnerAddress().getValue();
			if (a == null) {
				a = (Address) Settings.getDBService().createObject(
						Address.class, null);
			}
			a.setName((String) getPartnerName().getValue());
			a.setStreet((String) getPartnerStreet().getValue());
			a.setNumber((String) getPartnerNumber().getValue());
			a.setExtra((String) getPartnerExtra().getValue());
			a.setZipcode((String) getPartnerZipcode().getValue());
			a.setCity((String) getPartnerCity().getValue());
			a.setState((String) getPartnerState().getValue());
			a.setCountry((String) getPartnerCountry().getValue());

			//only store these if Hibicus extension was registered
			if (sepaCreditorRef != null)
				c.setSepaCreditorRef((String) getSEPACreditorReference().getValue());
			if (sepaCustomerRef != null)
				c.setSepaCustomerRef((String) getSEPACustomerReference().getValue());

			// Now, let's store the contract and its address.
			// The store() method throws ApplicationExceptions if
			// insertCheck() or updateCheck() failed.
			try {
				a.store();
				// We have to set the address here, because a new, unstored
				// object has no ID, yet. After storage, the ID is set.
				c.setAddress(a);
				c.store();

				// We have to reuse the old iterator that has been used for the
				// costs list, because otherwise new beans will created that
				// do not contain the values changed by the inline editor.
				// DBIterator costs = p.getCosts();
				// while (costs.hasNext()) {
				costsIterator.begin();
				while (costsIterator.hasNext()) {
					Costs cost = (Costs) costsIterator.next();
					if (!deletedCosts.contains(cost))
						cost.store();
					else
						cost.delete();
				}
				for (Costs cost : newCosts) {
					// Again: Set the contract's new ID.
					cost.setContract(c);
					cost.store();
				}

				updateDerivedAttributes();

				// update the dropdown box with the saved address
				getPartnerAddress().setList(
						PseudoIterator.asList(AddressControl.getAddresses()));
				getPartnerAddress().setValue(a);
				currentAddress = a;

				if (view instanceof ContractDetailView)
					((ContractDetailView) view).setButtonActivationState(true);

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

	// private boolean equalCheck(Object o1, Object o2) {
	// return (o1 == o2 || o1 != null && o1.equals(o2));
	// }
	//
	// private boolean cancellationsChanged(Contract c) throws RemoteException {
	// return !(equalCheck(getName().getValue(), c.getName()) &&
	// equalCheck(getStartDate().getValue(), c.getStartDate()) &&
	// equalCheck(getCancellationPeriodCount().getValue(),
	// c.getCancellationPeriodCount()) &&
	// equalCheck(getCancellationPeriodType().getValue(),
	// c.getCancellationPeriodType()) &&
	// equalCheck(getEndDate().getValue(), c.getEndDate()) &&
	// equalCheck(getFirstMinRuntimeCount().getValue(),
	// c.getFirstMinRuntimeCount()) &&
	// equalCheck(getFirstMinRuntimeType().getValue(),
	// c.getFirstMinRuntimeType()) &&
	// equalCheck(getNextMinRuntimeCount().getValue(),
	// c.getNextMinRuntimeCount()) &&
	// equalCheck(getNextMinRuntimeType().getValue(),
	// c.getNextMinRuntimeType()));
	// }

	private void updateDerivedAttributes() throws RemoteException {
		double costs = getContract().getMoneyPerTerm();
		if (Double.isNaN(costs)) {
			costsPerTerm.setValue("");
		} else {
			costsPerTerm.setValue(Settings.DECIMALFORMAT.format(costs));
			costsPerTerm.setComment(Settings.CURRENCY);
		}

		costs = getContract().getMoneyPerMonth();
		costsPerMonth.setValue(Settings.DECIMALFORMAT.format(costs));

		Date ne = getContract().getNextCancellationDeadline();
		nextCancellationDeadline.setValue(ne == null ? "" : Settings
				.dateformat(ne));

		Date ntb = getContract().getNextCancelableTermBegin();
		Date nte = getContract().getNextCancelableTermEnd();
		if (ntb != null && nte != null) {
			nextExtension
					.setValue(Settings.dateformat(ntb) + " "
							+ Settings.i18n().tr("to") + " "
							+ Settings.dateformat(nte));
		} else
			nextExtension.setValue("");
	}

	public void removeCostEntry(Costs c) {
		costsList.removeItem(c);
		newCosts.remove(c);
		deletedCosts.add(c);
	}

	public void addTemporaryCostEntry(Costs c) {
		try {
			costsList.addItem(c);
			newCosts.add(c);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isNewContract() {
		try {
			return contract.isNewObject();
		} catch (RemoteException e) {
			return false;
		}
	}
}