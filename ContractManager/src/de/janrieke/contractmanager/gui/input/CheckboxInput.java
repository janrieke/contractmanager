package de.janrieke.contractmanager.gui.input;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Extends the {@link de.willuhn.jameica.gui.input.CheckboxInput} class with
 * tooltips.
 */
public class CheckboxInput extends de.willuhn.jameica.gui.input.CheckboxInput {

	private String tooltip;
	private Control control;

	public CheckboxInput(boolean value) {
		super(value);
	}

	@Override
	public Control getControl() {
		if (control != null) {
			return control;
		}
		control = super.getControl();
		if (tooltip != null) {
			control.setToolTipText(tooltip);
			setTooltipForLabel(tooltip);
		}
		return control;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
		if (control != null) {
			control.setToolTipText(tooltip);
			setTooltipForLabel(tooltip);
		}
	}

	private void setTooltipForLabel(String tooltip) {
		Object label = getData("jameica.label");
		if (label instanceof Label) {
			((Label) label).setToolTipText(tooltip);
		}
	}
}
