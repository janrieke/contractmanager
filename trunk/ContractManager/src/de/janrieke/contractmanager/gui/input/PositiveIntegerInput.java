package de.janrieke.contractmanager.gui.input;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.input.IntegerInput;

public class PositiveIntegerInput extends IntegerInput {
	public PositiveIntegerInput() {
		super();
	}

	public PositiveIntegerInput(int value) {
		super(value < 0 ? 0 : value);
	}

	/**
	 * @see de.willuhn.jameica.gui.input.Input#getControl()
	 */
	public Control getControl() {
		final Control c = super.getControl();
		if (c != text)
			return c; //this should not happen
		if ((Integer)getValue() == 0)
			text.setText("");
		text.addListener(SWT.FocusOut, new Listener() {
			public void handleEvent(Event event) {
				Object value = getValue();
				if (value == null || value.toString().length() == 0)
					return;
				try {
					Integer intVal = new Integer(value.toString());
					if (intVal <= 0) {
						setValue(0);
						text.setText("");
					} else
						setValue(intVal); // normalize
				} catch (NumberFormatException e) {
					setValue(0);
					text.setText("");
				}
			}
		});
		return c;
	}

	@Override
	public Object getValue() {
		Object value = super.getValue();
		if (value == null || !(value instanceof Integer) || (Integer) value < 0)
			return 0;
		else
			return value;
	}
}
