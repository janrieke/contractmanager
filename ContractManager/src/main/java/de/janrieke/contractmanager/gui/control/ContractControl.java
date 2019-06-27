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

import static de.janrieke.contractmanager.util.DateUtils.calculateNextTermBeginAfter;

import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.gui.action.CreateNewCostEntry;
import de.janrieke.contractmanager.gui.input.DateDialogInputAutoCompletion;
import de.janrieke.contractmanager.gui.input.DateDialogInputAutoCompletion.ValidationProvider;
import de.janrieke.contractmanager.gui.input.DateDialogInputAutoCompletion.ValidationProvider.ValidationMessage;
import de.janrieke.contractmanager.gui.input.PositiveIntegerInput;
import de.janrieke.contractmanager.gui.menu.ContractListMenu;
import de.janrieke.contractmanager.gui.menu.CostsListMenu;
import de.janrieke.contractmanager.gui.parts.ContractListTreePart;
import de.janrieke.contractmanager.gui.parts.CostsListTablePart;
import de.janrieke.contractmanager.gui.parts.SizeableTablePart;
import de.janrieke.contractmanager.gui.view.ContractDetailView;
import de.janrieke.contractmanager.rmi.Address;
import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Costs;
import de.janrieke.contractmanager.rmi.IntervalType;
import de.janrieke.contractmanager.rmi.Transaction;
import de.janrieke.contractmanager.util.ValidRuntimes;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.pseudo.PseudoIterator;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.TreeFormatter;
import de.willuhn.jameica.gui.input.CheckboxInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.input.MultiInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Column;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author willuhn, jrieke
 */
public class ContractControl extends AbstractControl {

	// list of all contracts
	private ContractListTreePart contractTree;
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
	private CheckboxInput fixedTermsInput;

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

	// Checkbox for filtering non-active contracts in list
	private CheckboxInput activeFilterSwitch;

	// this is the currently opened contract
	private Contract contract;

	private LabelInput nextCancellationDeadlineLabel;

	private CostsListTablePart costsList;
	private LabelInput costsPerTerm;
	private LabelInput costsPerMonth;

	private final List<Costs> newCosts = new ArrayList<>();
	private final List<Costs> deletedCosts = new ArrayList<>();
	private final List<Transaction> newTransactions = new ArrayList<>();
	private final List<Consumer<Transaction>> transactionListeners = new ArrayList<>();

	// holds the current Hibiscus category of the selection box (used for
	// storing to DB on store button click)
	public String hibiscusCategoryID = null;

	private Address currentAddress = null;

	private GenericIterator<Costs> costsIterator;

	/**
	 * Shows a warning symbol if the end date is close to the start date. This
	 * should prevent users from misunderstanding the meaning of the end date
	 * field.
	 */
	private final ValidationProvider endDateValidationProvider = time -> {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		calendar.add(Calendar.DAY_OF_MONTH, -5); // a little tolerance
		try {
			Date startDate = getContract().getStartDate();
			if (startDate == null) {
				return Optional.empty();
			}
			Date nextTermBegin = calculateNextTermBeginAfter(startDate, startDate, false,
					ValidRuntimes.getValidRuntimes(getContract()));

			if (nextTermBegin == null) {
				return Optional.empty();
			}
			// TODO: If users are still confused, increase the warning time, like this:
			// nextTermBegin = getContract().getNextTermBeginAfter(nextTermBegin);
			if (nextTermBegin.after(calendar.getTime())) {
				return Optional
						.of(new ValidationMessage(
								Settings.i18n().tr("You have entered an end date close to the contract's start date. Are you sure this is correct?\nOnly enter an end date if the contract will definitely terminate on that date without any explicit cancellation.\nAfter the end date, you will not be reminded of cancellation deadlines any more."),
								FieldDecorationRegistry.DEC_WARNING));
			}
		} catch (RemoteException e) {
			Logger.error("Error while getting contract's dates.", e);
		}
		return Optional.empty();
	};

	/**
	 * ct.
	 *
	 * @param view
	 *            this is our view (the contract details view).
	 * @throws RemoteException
	 */
	public ContractControl(AbstractView view) throws RemoteException {
		super(view);
		if (view instanceof ContractDetailView) {
			((ContractDetailView) view)
			.setButtonActivationState(!((Contract) view
					.getCurrentObject()).isNewObject());
		}
	}

	/**
	 * Small helper method to get the current contract.
	 *
	 * @return
	 */
	public Contract getContract() {
		if (contract != null) {
			return contract;
		}
		contract = (Contract) getCurrentObject();
		return contract;
	}

	/**
	 * Returns an iterator with all contracts in the database.
	 *
	 * @throws RemoteException
	 * @return iterator containing all addresses
	 */
	public static DBIterator<Contract> getContracts() throws RemoteException {
		DBService service = Settings.getDBService();
		return service.createList(Contract.class);
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
		if (contractNo == null) {
			contractNo = new TextInput(getContract().getContractNumber(), 255);
		}
		return contractNo;
	}

	public Input getCustomerNumber() throws RemoteException {
		if (customerNo == null) {
			customerNo = new TextInput(getContract().getCustomerNumber(), 255);
		}
		return customerNo;
	}

	public Input getSEPACreditorReference() throws RemoteException {
		if (sepaCreditorRef == null) {
			sepaCreditorRef = new TextInput(getContract().getSepaCreditorRef(), 35);
		}
		return sepaCreditorRef;
	}

	public Input getSEPACustomerReference() throws RemoteException {
		if (sepaCustomerRef == null) {
			sepaCustomerRef = new TextInput(getContract().getSepaCustomerRef(), 35);
		}
		return sepaCustomerRef;
	}

	/**
	 * Returns the input field for the contract description.
	 *
	 * @return input field.
	 * @throws RemoteException
	 */
	public Input getComment() throws RemoteException {
		if (comment == null) {
			comment = new TextAreaInput(getContract().getComment(), 10000);
		}
		return comment;
	}

	/**
	 * Returns the input field for the start date.
	 *
	 * @return input field.
	 * @throws RemoteException
	 */
	public Input getStartDate() throws RemoteException {
		if (startDate != null) {
			return startDate;
		}

		// this is a custom dialog that shows a calendar widget.
		CalendarDialog d = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
		d.setTitle(Settings.i18n().tr("Choose a start date"));

		// we have to add a close listener to display the chosen
		// date in the right format.
		d.addCloseListener(event -> {
			if (event == null || event.data == null) {
				return;
			}

			startDate.setText(Settings.dateformat((Date) event.data));
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
		if (endDate != null) {
			return endDate;
		}

		CalendarDialog d = new CalendarDialog(CalendarDialog.POSITION_MOUSE);
		d.setTitle(Settings.i18n().tr("Choose an end date"));
		d.addCloseListener(event -> {
			if (event == null || event.data == null) {
				return;
			}

			endDate.setText(Settings.dateformat((Date) event.data));
		});

		Date initial = getContract().getEndDate();
		String s = initial == null ? "YYYY-MM-DD" : Settings
				.dateformat(initial);

		// Show a warning if the end date is within the very first cancellation deadline.
		// Reason: Many users confuse the end date with the end of the first runtime.

		// Dialog-Input is an Input field that gets its data from a dialog.
		endDate = new DateDialogInputAutoCompletion(s, initial, d, endDateValidationProvider);

		// we store the initial value
		endDate.setValue(initial);

		return endDate;
	}

	public Input getNextTerm() throws RemoteException {
		if (nextExtension != null) {
			return nextExtension;
		}

		nextExtension = new LabelInput(getNextTermValueString());
		return nextExtension;
	}

	private String getNextTermValueString() throws RemoteException {
		Date ntb = getContract().getNextCancelableTermBegin();
		Date nte = getContract().getNextCancelableTermEnd();
		if (ntb != null && nte != null) {
			return Settings.dateformat(ntb) + " " + Settings.i18n().tr("to")
					+ " " + Settings.dateformat(nte);
		} else {
			if (getContract().hasValidRuntimeInformation()) {
				return Settings.i18n().tr("Not cancellable before contract ends");
			} else {
				return Settings.i18n().tr("No information on cancellation given");
			}
		}
	}

	public LabelInput getNextCancellationDeadline() throws RemoteException {
		if (nextCancellationDeadlineLabel != null) {
			return nextCancellationDeadlineLabel;
		}

		Date ne = getContract().getNextCancellationDeadline();
		nextCancellationDeadlineLabel = new LabelInput(ne == null ? ""
				: Settings.dateformat(ne));
		return nextCancellationDeadlineLabel;
	}

	public LabelInput getCostsPerTerm() throws RemoteException {
		if (costsPerTerm != null) {
			return costsPerTerm;
		}

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
		if (costsPerMonth != null) {
			return costsPerMonth;
		}

		double costs = getContract().getMoneyPerMonth();
		costsPerMonth = new LabelInput(Settings.DECIMALFORMAT.format(costs));
		costsPerMonth.setComment(Settings.CURRENCY);
		return costsPerMonth;
	}

	public Input getCancellationPeriod() throws RemoteException {
		if (cancellationPeriodMulti != null) {
			return cancellationPeriodMulti;
		}

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
			List<IntervalType> list = new ArrayList<>();
			list.add(IntervalType.DAYS);
			list.add(IntervalType.WEEKS);
			list.add(IntervalType.MONTHS);
			list.add(IntervalType.YEARS);
			cancellationPeriodType = new SelectInput(list, getContract()
					.getCancellationPeriodType());
		}
		return cancellationPeriodType;
	}

	public Input getFirstRuntime() throws RemoteException {
		if (firstRuntimeMulti != null) {
			return firstRuntimeMulti;
		}

		firstRuntimeMulti = new MultiInput(getFirstRuntimeCount(),
				getFirstRuntimeType());

		return firstRuntimeMulti;
	}

	public IntegerInput getFirstRuntimeCount() throws RemoteException {
		if (firstRuntimeCount == null) {
			firstRuntimeCount = new PositiveIntegerInput(getContract()
					.getFirstMinRuntimeCount());
		}
		return firstRuntimeCount;
	}

	public SelectInput getFirstRuntimeType() throws RemoteException {
		if (firstRuntimeType == null) {
			List<IntervalType> list = new ArrayList<>();
			list.add(IntervalType.DAYS);
			list.add(IntervalType.WEEKS);
			list.add(IntervalType.MONTHS);
			list.add(IntervalType.YEARS);
			firstRuntimeType = new SelectInput(list, getContract()
					.getFirstMinRuntimeType());
		}
		return firstRuntimeType;
	}

	public Input getNextRuntime() throws RemoteException {
		if (nextRuntimeMulti != null) {
			return nextRuntimeMulti;
		}

		nextRuntimeMulti = new MultiInput(getNextRuntimeCount(),
				getNextRuntimeType());

		return nextRuntimeMulti;
	}

	public IntegerInput getNextRuntimeCount() throws RemoteException {
		if (nextRuntimeCount == null) {
			nextRuntimeCount = new PositiveIntegerInput(getContract()
					.getFollowingMinRuntimeCount());

			// when focusing this input, transfer the values from the first runtime
			//  as default values if no value is set, yet
			nextRuntimeCount
					.addListener(event -> {
						if ((event.type == SWT.FocusIn)
								&& (((Integer) 0).equals(nextRuntimeCount.getValue()) && IntervalType.MONTHS
										.equals(nextRuntimeType.getValue()))) {
							nextRuntimeCount.setValue(firstRuntimeCount.getValue());
							nextRuntimeType.setValue(firstRuntimeType.getValue());
						}
					});
		}
		return nextRuntimeCount;
	}

	public SelectInput getNextRuntimeType() throws RemoteException {
		if (nextRuntimeType == null) {
			List<IntervalType> list = new ArrayList<>();
			list.add(IntervalType.DAYS);
			list.add(IntervalType.WEEKS);
			list.add(IntervalType.MONTHS);
			list.add(IntervalType.YEARS);
			nextRuntimeType = new SelectInput(list, getContract()
					.getFollowingMinRuntimeType());
		}
		return nextRuntimeType;
	}

	public CheckboxInput getFixedTermsInput() throws RemoteException {
		if (fixedTermsInput == null) {
			fixedTermsInput = new CheckboxInput(getContract().getFixedTerms());
		}
		return fixedTermsInput;
	}

	public CheckboxInput getRemind() throws RemoteException {
		if (remindCheckbox == null) {
			remindCheckbox = new CheckboxInput(!getContract().getDoNotRemind());
		}
		return remindCheckbox;
	}

	public SelectInput getPartnerAddress() throws RemoteException {
		if (partnerAddress == null) {
			currentAddress = getContract().getAddress();
			partnerAddress = new SelectInput(AddressControl.getAddresses(),
					currentAddress);
			partnerAddress.setPleaseChoose(Settings.i18n().tr("[New Address]"));
			partnerAddress.addListener(event -> {
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
						Logger.error("Error while setting address", e);
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
		if (partnerStreet == null) {
			partnerStreet = new TextInput(
					getContract().getAddress() == null ? "" : getContract()
							.getAddress().getStreet(), 255);
		}
		return partnerStreet;
	}

	public Input getPartnerNumber() throws RemoteException {
		if (partnerNumber == null) {
			partnerNumber = new TextInput(
					getContract().getAddress() == null ? "" : getContract()
							.getAddress().getNumber(), 16);
		}
		return partnerNumber;
	}

	public Input getPartnerStreetNumber() throws RemoteException {
		if (partnerStreetNumber != null) {
			return partnerStreetNumber;
		}

		partnerStreetNumber = new MultiInput(getPartnerStreet(),
				new LabelInput(Settings.i18n().tr("Number")),
				getPartnerNumber());

		return partnerStreetNumber;
	}

	public Input getPartnerExtra() throws RemoteException {
		if (partnerExtra == null) {
			partnerExtra = new TextInput(
					getContract().getAddress() == null ? "" : getContract()
							.getAddress().getExtra(), 255);
		}
		return partnerExtra;
	}

	public Input getPartnerZipcode() throws RemoteException {
		if (partnerZipcode == null) {
			partnerZipcode = new TextInput(
					getContract().getAddress() == null ? "" : getContract()
							.getAddress().getZipcode(), 5);
		}
		return partnerZipcode;
	}

	public Input getPartnerCity() throws RemoteException {
		if (partnerCity == null) {
			partnerCity = new TextInput(getContract().getAddress() == null ? ""
					: getContract().getAddress().getCity(), 255);
		}
		return partnerCity;
	}

	public Input getPartnerZipcodeCity() throws RemoteException {
		if (partnerZipcodeCity != null) {
			return partnerZipcodeCity;
		}

		partnerZipcodeCity = new MultiInput(getPartnerZipcode(),
				new LabelInput(Settings.i18n().tr("City")), getPartnerCity());

		return partnerZipcodeCity;
	}

	public Input getPartnerState() throws RemoteException {
		if (partnerState == null) {
			partnerState = new TextInput(
					getContract().getAddress() == null ? "" : getContract()
							.getAddress().getState(), 255);
		}
		return partnerState;
	}

	public Input getPartnerCountry() throws RemoteException {
		if (partnerCountry == null) {
			partnerCountry = new TextInput(
					getContract().getAddress() == null ? "" : getContract()
							.getAddress().getCountry(), 255);
		}
		return partnerCountry;
	}

	/**
	 * Creates a table containing all contracts.
	 *
	 * @return a table with contracts.
	 * @throws RemoteException
	 */
	public ContractListTreePart getContractsTable() throws RemoteException {
		if (contractTree != null) {
			return contractTree;
		}

		GenericIterator<Contract> allContracts = getContracts();
		Date today = new Date();
		List<Contract> contracts = new ArrayList<>();

//		double total = 0d;
		while (allContracts.hasNext()) {
			Contract c = allContracts.next();
			if (c.isActiveInMonth(today)) {
				// Only add active contracts.
//				total += c.getMoneyPerMonth();
				contracts.add(c);
			}
		}

		// 4) create the tree
		contractTree = new ContractListTreePart(
				contracts,
				new de.janrieke.contractmanager.gui.action.ShowContractDetailView());
//		contractTree.setSum(total);

		// 5) now we have to add some columns.
		contractTree.addColumn(Settings.i18n().tr("Name of Contract"), "name");
		contractTree.addColumn(Settings.i18n().tr("Contract Partner"), Contract.PARTNER_NAME);
		contractTree.addColumn(Settings.i18n().tr("Start Date"), "startdate",
				new DateFormatter(Settings.getNewDateFormat()));
		contractTree.addColumn(Settings.i18n().tr("End Date"), "enddate",
				new DateFormatter(Settings.getNewDateFormat()));
		contractTree.addColumn(
				Settings.i18n().tr("Next Cancellation Deadline"),
				Contract.NEXT_CANCELLATION_DEADLINE,
				new DateFormatter(Settings.getNewDateFormat()));
		contractTree.addColumn(Settings.i18n().tr("Money per Term"),
				Contract.MONEY_PER_TERM, new CurrencyFormatter(
						Settings.CURRENCY, Settings.DECIMALFORMAT), false,
						Column.ALIGN_RIGHT);
		contractTree.addColumn(Settings.i18n().tr("Money per Month"),
				Contract.MONEY_PER_MONTH, new CurrencyFormatter(
						Settings.CURRENCY, Settings.DECIMALFORMAT));
		contractTree.addColumn(Settings.i18n().tr("Remind?"), "ignore_cancellations", o -> {
			if (o instanceof Integer) {
				if (((Integer)o).intValue() == 0) {
					return "\u2611";
				} else {
					return "\u2610";
				}
			} else {
				return "";
			}
		}, true, Column.ALIGN_LEFT);

		// 7) we are adding a context menu
		contractTree.setContextMenu(new ContractListMenu(null, true));

		TreeFormatter formatter = item -> {
			if (item.getData() instanceof Contract) {
				Contract contract = (Contract) item.getData();
				try {
					if (!contract.isActiveInMonth(new Date())) {
						item.setForeground(Settings.getNotActiveForegroundColor());
					}

					if (contract.getDoNotRemind()) {
						return;
					}
					if (contract.isNextDeadlineWithinWarningTime()) {
						item.setBackground(Color.ERROR.getSWTColor());
					} else if (contract.isNextDeadlineWithinNoticeTime()) {
						item.setBackground(Color.MANDATORY_BG
								.getSWTColor());
					} else {
						item.setBackground(null);
					}
				} catch (RemoteException e) {
					Logger.error("Error while formatting contract list.", e);
				}
			}
		};
		contractTree.setFormatter(formatter);
		return contractTree;
	}

	/**
	 * Checkbox in the contract list view for filtering the inactive contracts.
	 */
	public CheckboxInput getActiveFilterSwitch() {
		if (activeFilterSwitch != null) {
			return activeFilterSwitch;
		}

		activeFilterSwitch = new CheckboxInput(false);
		activeFilterSwitch.setValue(true);
		activeFilterSwitch.addListener(event -> {
			try {
				if (event.type != SWT.Selection) {
					return;
				}
				Object selection = contractTree.getSelection();
				contractTree.removeAll();
				if ((Boolean)activeFilterSwitch.getValue()) {
					// Checkbox was active; remove inactive contracts.
					Date today = new Date();
					GenericIterator<Contract> contracts = getContracts();
					List<Contract> activeContracts = new ArrayList<>();
					while (contracts.hasNext()) {
						Contract item = contracts.next();
						if (item.isActiveInMonth(today)) {
							activeContracts.add(item);
						}
					}
					contractTree.setList(activeContracts);
				} else {
					// Checkbox was inactive; add all contracts.
					GenericIterator<Contract> contracts = getContracts();
					contractTree.setList(contracts);
				}
				contractTree.select(selection);
			} catch (RemoteException e) {
				Logger.error("Error while repopulating contract table", e);
			}
		});
		return activeFilterSwitch;
	}

	/**
	 * Creates a table containing all contracts in extension warning or notice
	 * time.
	 *
	 * @return a table with contracts.
	 * @throws RemoteException
	 */
	public TablePart getContractsExtensionWarningTable() throws RemoteException {
		if (contractListWarnings != null) {
			return contractListWarnings;
		}

		// 1) get the dataservice
		DBService service = Settings.getDBService();

		// 2) now we can create the contract list.
		// We do not need to specify the implementing class for
		// the interface "Contract". Jameica's classloader knows
		// all classes an finds the right implementation automatically. ;)
		DBIterator<Contract> contracts = service.createList(Contract.class);

		ArrayList<Contract> filteredContracts = new ArrayList<>();

		// Iterate through the list and filter
		while (contracts.hasNext()) {
			Contract contract = contracts.next();
			try {
				if (contract.getDoNotRemind()) {
					continue;
				}

				if (contract.isNextDeadlineWithinNoticeTime()) {
					filteredContracts.add(contract);
				}
			} catch (RemoteException e) {
			}
		}
		Contract[] filteredArray = new Contract[filteredContracts.size()];
		GenericIterator<?> filteredIterator = PseudoIterator
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
		contractListWarnings.setContextMenu(new ContractListMenu(contractListWarnings, false));

		contractListWarnings.setFormatter(item -> {
			if (item.getData() instanceof Contract) {
				Contract contract = (Contract) item.getData();
				try {
					if (contract.isNextDeadlineWithinWarningTime()) {
						item.setBackground(Color.ERROR.getSWTColor());
					} else if (contract.isNextDeadlineWithinNoticeTime()) {
						item.setBackground(Color.MANDATORY_BG.getSWTColor());
					} else {
						//this may happen if user dismissed the next reminder
						//remove the entry in this case
						int index = item.getParent().indexOf(item);
						item.getParent().remove(index);
					}

				} catch (RemoteException e) {
				}
			}
		});
		contractListWarnings.orderBy(3);

		return contractListWarnings;
	}

	/**
	 * Returns a list of costs in this contract.
	 *
	 * @return list of costs in this contract
	 * @throws RemoteException
	 */
	public CostsListTablePart getCostsList() throws RemoteException {
		if (costsList != null) {
			return costsList;
		}

		costsIterator = getContract().getCosts();
		costsList = new CostsListTablePart(costsIterator, new CreateNewCostEntry(this));
		costsList.setFormatter(item -> {
			try {
				Costs costs = (Costs) item.getData();
				double money = costs.getMoney(); // Double.parseDouble(item.getText(1));
				String text = Settings.formatAsCurrency(money);
				item.setText(1, text);
				item.setText(2, costs.getPeriod().getAdjective());
				Date nextPayday = costs.getNextPayday();
				if (nextPayday != null) {
					item.setText(4, Settings.dateformat(nextPayday));
					item.setForeground(4, Color.COMMENT.getSWTColor());
				}
			} catch (RemoteException e) {
			}
		});

		costsList.setHeightHint(120);
		costsList.addColumn(Settings.i18n().tr("Description"), "description",
				null, true);
		costsList.addColumn(Settings.i18n().tr("Money"), "money", null, true);
		costsList.addColumn(Settings.i18n().tr("Period"), "period", null, true);
		costsList.addColumn(Settings.i18n().tr("Payday"), "payday", null, true);
		costsList.addColumn(Settings.i18n().tr("Next payday"), "next_payday", null, true);
		CostsListMenu clm = new CostsListMenu(this);
		costsList.setContextMenu(clm);
		costsList.setSummary(false);
		costsList.addChangeListener((object, attribute, newValue) -> {
			assert object instanceof Costs;
			try {
				if ("description".equals(attribute)) {
					((Costs) object).setDescription(newValue.substring(0,
							Math.min(newValue.length(), 255)));
				} else if ("money".equals(attribute)) {
					try {
						Number num = NumberFormat.getInstance().parse(newValue);
						((Costs) object).setMoney(num.doubleValue());
					} catch (ParseException e1) {
						((Costs) object).setMoney(0d);
					}
				} else if ("period".equals(attribute)) {
					((Costs) object).setPeriod(IntervalType.valueOfAdjective(newValue));
				} else if ("payday".equals(attribute)) {
					Date payday = null;
					try {
						payday = Settings.getNewDateFormat().parse(newValue);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					((Costs) object).setPayday(payday);
				} else {
					assert false;
				}
			} catch (RemoteException e2) {
				throw new ApplicationException(e2);
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

			//If dates or runtime values have changed, reset the "do not remind before" field
			// because deadlines may have changed and the user may want to be notified again.
			boolean resetDoNotRemindBefore = false;
			resetDoNotRemindBefore |= writeValueToModel(c.getStartDate(), (Date)getStartDate().getValue(), c::setStartDate);
			resetDoNotRemindBefore |= writeValueToModel(c.getEndDate(), (Date)getEndDate().getValue(), c::setEndDate);
			resetDoNotRemindBefore |= writeValueToModel(c.getCancellationPeriodCount(), (Integer)getCancellationPeriodCount().getValue(), c::setCancelationPeriodCount);
			resetDoNotRemindBefore |= writeValueToModel(c.getCancellationPeriodType(), (IntervalType)getCancellationPeriodType().getValue(), c::setCancelationPeriodType);
			resetDoNotRemindBefore |= writeValueToModel(c.getFirstMinRuntimeCount(), (Integer)getFirstRuntimeCount().getValue(), c::setFirstMinRuntimeCount);
			resetDoNotRemindBefore |= writeValueToModel(c.getFirstMinRuntimeType(), (IntervalType)getFirstRuntimeType().getValue(), c::setFirstMinRuntimeType);
			resetDoNotRemindBefore |= writeValueToModel(c.getFollowingMinRuntimeCount(), (Integer)getNextRuntimeCount().getValue(), c::setFollowingMinRuntimeCount);
			resetDoNotRemindBefore |= writeValueToModel(c.getFollowingMinRuntimeType(), (IntervalType)getNextRuntimeType().getValue(), c::setFollowingMinRuntimeType);
			resetDoNotRemindBefore |= writeValueToModel(c.getFixedTerms(), (Boolean)getFixedTermsInput().getValue(), c::setFixedTerms);

			c.setDoNotRemind(!(Boolean) getRemind().getValue());

			//At least one relevant field changed, reset the "do not remind before" field
			if (resetDoNotRemindBefore) {
				c.setDoNotRemindBefore(null);
			}

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
			if (sepaCreditorRef != null) {
				c.setSepaCreditorRef((String) getSEPACreditorReference().getValue());
			}
			if (sepaCustomerRef != null) {
				c.setSepaCustomerRef((String) getSEPACustomerReference().getValue());
			}

			// Now, let's store the contract and its address.
			// The store() method throws ApplicationExceptions if
			// insertCheck() or updateCheck() failed.
			boolean addressWasNew = false;
			try {
				addressWasNew = a.isNewObject();
				a.store();
				// We have to set the address first, because a new, unstored
				// object has no ID, yet. After storage, the ID is set.
				c.setAddress(a);
				c.store();

				// We have to reuse the old iterator that has been used for the
				// costs list, because otherwise new beans will created that
				// do not contain the values changed by the inline editor.
				costsIterator.begin();
				while (costsIterator.hasNext()) {
					Costs cost = costsIterator.next();
					if (!deletedCosts.contains(cost)) {
						cost.store();
					} else {
						cost.delete();
					}
				}
				for (Costs cost : newCosts) {
					// Again: Set the contract's new ID.
					cost.setContract(c);
					cost.store();
				}

				for (Transaction t : newTransactions) {
					t.setContract(c);
					t.store();
				};

				updateDerivedAttributes();

				// update the dropdown box with the saved address
				getPartnerAddress().setList(
						PseudoIterator.asList(AddressControl.getAddresses()));
				getPartnerAddress().setValue(a);
				currentAddress = a;

				if (view instanceof ContractDetailView) {
					((ContractDetailView) view).setButtonActivationState(true);
				}

				GUI.getStatusBar().setSuccessText(
						Settings.i18n().tr("Contract stored successfully"));
			} catch (ApplicationException e) {
				GUI.getView().setErrorText(e.getMessage());
				// Remove potentially stored objects from DB.
				if (addressWasNew) {
					try {
						a.delete();
					} catch (ApplicationException e1) {
						// ignore
					}
				}
			}
		} catch (RemoteException e) {
			Logger.error("error while storing contract", e);
			GUI.getStatusBar().setErrorText(
					Settings.i18n().tr("Error while storing contract"));
		}
	}

	@FunctionalInterface
	public interface ThrowingConsumer<T, E extends Exception> extends Consumer<T> {

	    @Override
	    default void accept(final T elem) {
	        try {
	            acceptThrows(elem);
	        } catch (final Exception e) {
	        	Logger.error("Error while storing contract.", e);
	        }
	    }

	    void acceptThrows(T elem) throws E;
	}

	/**
	 * Passes the newValue to the setter if it is different from the oldValue.
	 *
	 * @return <code>true</code> if the value changed and the setter was called,
	 *         <code>false</code> otherwise.
	 * @throws RemoteException
	 */
	private <T> boolean writeValueToModel(T oldValue, T newValue, ThrowingConsumer<T, RemoteException> setter)
			throws RemoteException {
		if (newValue != oldValue && (newValue == null || !newValue.equals(oldValue))) {
			setter.accept(newValue);
			return true;
		}
		return false;
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
		nextCancellationDeadlineLabel.setValue(ne == null ? "" : Settings
				.dateformat(ne));

		nextExtension.setValue(getNextTermValueString());
	}

	public void removeCostEntry(Costs c) {
		costsList.removeItem(c);
		newCosts.remove(c);
		deletedCosts.add(c);
	}

	public void addTemporaryCostEntry(Costs c) throws RemoteException {
		costsList.addItem(c);
		newCosts.add(c);
	}

	public void addTemporaryTransactionAssignment(Transaction t) throws RemoteException {
		newTransactions.add(t);
		transactionListeners.forEach(l -> l.accept(t));
	}

	public boolean isNewContract() {
		try {
			return contract.isNewObject();
		} catch (RemoteException e) {
			return false;
		}
	}

	public void addTransactionListener(Consumer<Transaction> listener) {
		transactionListeners.add(listener);
	}
}