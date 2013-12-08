package de.janrieke.contractmanager.gui.parts;

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;

import de.janrieke.contractmanager.Settings;
import de.janrieke.contractmanager.rmi.Contract;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.TableChangeListener;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

public class ContractListTablePart extends TablePart {

	private double sum;

	public ContractListTablePart(GenericIterator list, Action action) {
		super(list, action);
	    setRememberColWidths(true);
		this.addChangeListener(new TableChangeListener() {
			
			@Override
			public void itemChanged(Object object, String attribute, String newValue)
					throws ApplicationException {
				assert object instanceof Contract;
				try {
					((Contract)object).setDoNotRemind(!"\u2611".equals(newValue));
					((Contract)object).store();
				} catch (RemoteException e) {
					Logger.error("error while storing contract", e);
					GUI.getStatusBar().setErrorText(
							Settings.i18n().tr("Error while storing contract"));
				}
			}
		});
	}
	
	@Override
	protected String getControlValue(Control control) {
		if (control instanceof Button) {
			return ((Button) control).getSelection()?"\u2611":"\u2610";
		} else
			return super.getControlValue(control);
	}

	@Override
	protected Control getEditorControl(int row, TableItem item, String oldValue) {
			if (item.getData() instanceof Contract && row == 7) {
				Button newButton = new Button(item.getParent(), SWT.CHECK);
				newButton.setSelection("\u2611".equals(oldValue));
				newButton.setFocus();
			    return newButton;
			}
			else 
				return super.getEditorControl(row, item, oldValue);
		}
	
	public void setSum(double sum) {
		this.sum = sum;
	}
	
	@Override
	protected String getSummary() {
		return Settings.i18n().tr("Total in this month") + ": " + Settings.DECIMALFORMAT.format(Math.round(sum*100d)/100d) + " EUR"; 
	};
}
