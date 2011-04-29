package de.janrieke.contractmanager.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import de.janrieke.contractmanager.rmi.Contract;
import de.janrieke.contractmanager.rmi.Costs;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.SimpleContainer;

public class SizeableTablePart extends TablePart {

	@Override
	protected String getControlValue(Control control) {
		if (control instanceof Combo) {
			return ((Combo) control).getText();
		} else
			return super.getControlValue(control);
	}

	@Override
	protected Control getEditorControl(int row, TableItem item, String oldValue) {
		if (item.getData() instanceof Costs && row == 2) {
		    Combo newCombo = new Combo(item.getParent(), SWT.DROP_DOWN | SWT.READ_ONLY);
		    newCombo.setItems(Contract.IntervalType.getAdjectives());
//		    Contract.IntervalType value = Contract.IntervalType.values()[Integer.parseInt(oldValue)];
//		    newCombo.setText(value.toAdjectiveString());
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

	private int heightHint;

	@Override
	public synchronized void paint(Composite parent) throws RemoteException {
	    SimpleContainer container = new SimpleContainer(parent, true); //give the table a minimum height
	    GridData gd = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
	    gd.heightHint = heightHint;
	    container.getComposite().setLayoutData(gd);
	    super.paint(container.getComposite());
	}

	public SizeableTablePart(GenericIterator list, Action action) {
		super(list, action);
	}

	public void setHeightHint(int heightHint) {
		this.heightHint = heightHint; 
	}

}
