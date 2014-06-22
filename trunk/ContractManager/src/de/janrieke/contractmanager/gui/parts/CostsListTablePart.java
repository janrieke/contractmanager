package de.janrieke.contractmanager.gui.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Costs;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;

public class CostsListTablePart extends SizeableTablePart {

	public CostsListTablePart(GenericIterator list, Action action) {
		super(list, action);
	}

	@Override
	protected String getControlValue(Control control) {
		if (control instanceof CCombo) {
			return ((CCombo) control).getText();
		} else
			return super.getControlValue(control);
	}

	@Override
	protected Control getEditorControl(int row, TableItem item, String oldValue) {
			if (item.getData() instanceof Costs && row == 2) {
				CCombo newCombo = new CCombo(item.getParent(), SWT.FLAT | SWT.READ_ONLY);
			    newCombo.setItems(Contract.IntervalType.getAdjectives());
			    newCombo.setText(oldValue);
			    newCombo.setFocus();
			    return newCombo;
			}
			else if (item.getData() instanceof Costs && row == 1) {
			    Text newText = new Text(item.getParent(), SWT.NONE);
			    String doubleString = oldValue.substring(0, oldValue.length()-2);
			    newText.setText(doubleString);
			    newText.selectAll();
			    newText.setFocus();
			    return newText;
			}
			else 
				return super.getEditorControl(row, item, oldValue);
		}

}
