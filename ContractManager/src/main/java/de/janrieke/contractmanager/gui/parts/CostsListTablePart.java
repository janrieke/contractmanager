package de.janrieke.contractmanager.gui.parts;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Costs;
import de.janrieke.contractmanager.rmi.IntervalType;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;

public class CostsListTablePart extends SizeableTablePart {

	public CostsListTablePart(GenericIterator<Costs> list, Action action) {
		// We use the double-click action to insert a new cost entry.
		super(list, action);
	}

	@Override
	protected String getControlValue(Control control) {
		if (control instanceof CCombo) {
			return ((CCombo) control).getText();
		} else if (control instanceof DateTime) {
			DateTime cal = ((DateTime) control);
			Object dateSet = cal.getData("dateSet");
			if (dateSet != null && dateSet instanceof Boolean && ((Boolean)dateSet) == true) {
				Calendar c = Calendar.getInstance(Application.getConfig().getLocale());
				c.set(cal.getYear(), cal.getMonth(), cal.getDay());
				return Settings.dateformat(c.getTime());
			} else {
				return "";
			}
		} else {
			return super.getControlValue(control);
		}
	}

	@Override
	protected Control getEditorControl(int row, TableItem item, String oldValue) {
		if (!(item.getData() instanceof Costs)) {
			return super.getEditorControl(row, item, oldValue);
		}
		Costs costs = (Costs) item.getData();

		switch (row) {
		case 1:
			// Input for the money (" €" will be stripped)
			Text newText = new Text(item.getParent(), SWT.NONE);
			String doubleString = oldValue.substring(0, oldValue.length() - 2);
			newText.setText(doubleString);
			newText.selectAll();
			newText.setFocus();
			return newText;

		case 2:
			// Interval select value
			CCombo newCombo = new CCombo(item.getParent(), SWT.FLAT | SWT.READ_ONLY);
			newCombo.setItems(IntervalType.getAdjectives());
			newCombo.setText(oldValue);
			newCombo.setFocus();
			return newCombo;

		case 3:
			// Input for the payday
			Date date = null;
			try {
				date = costs.getPayday();
			} catch (RemoteException e) {
				Logger.error("Error while reading costs item.", e);
			}

			final DateTime cal = new DateTime(item.getParent(), SWT.DATE | SWT.DROP_DOWN);
			if (date != null) {
				Calendar c = Calendar.getInstance(Application.getConfig().getLocale());
				c.setTime(date);
				cal.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
						c.get(Calendar.DAY_OF_MONTH));
				cal.setData("dateSet", true);
			}
			cal.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					// The user actively selected a date. Then we should treat it like a "set value".
					cal.setData("dateSet", true);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			cal.setFocus();
			return cal;

		case 4:
			// Read-only field.
			return null;

		default:
			// Row 0 is a normal text input for the description.
			return super.getEditorControl(row, item, oldValue);
		}
	}

	@Override
	public Object getSelection() {
		Object selection = super.getSelection();
		if (selection == null) {
			// Don't return null when no element is selected, because the action
			// will not be called when double-clicking.
			// Handling of empty arrays is performed within CreateNewCostEntry.
			return new Object[0];
		} else {
			return selection;
		}
	}
}