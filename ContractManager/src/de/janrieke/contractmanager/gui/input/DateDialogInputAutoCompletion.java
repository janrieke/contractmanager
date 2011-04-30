package de.janrieke.contractmanager.gui.input;

import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.willuhn.jameica.gui.dialogs.CalendarDialog;
import de.willuhn.jameica.gui.input.DialogInput;

public class DateDialogInputAutoCompletion extends DialogInput {

	private Control clientControl1;
	private CalendarDialog calendarDialog;
	//private String pattern = "YYYY-MM-DD";

	public DateDialogInputAutoCompletion(String value) {
		super(value);
	}

	public DateDialogInputAutoCompletion(String value, CalendarDialog d) {
		super(value, d);
		this.calendarDialog = d;
	}

	//	public void setPattern(String pattern) {
	//		this.pattern = pattern;
	//	}

	@Override
	public Control getClientControl(Composite parent) {
		clientControl1 = super.getClientControl(parent);

		clientControl1.addListener(SWT.Verify, new Listener() {
			boolean ignore;
			Text text = null;
			final Calendar calendar = Calendar.getInstance();

			@Override
			public void handleEvent(Event e) {
				if (ignore) return;

				if (text == null)
					text = (Text) clientControl1;

				e.doit = false;
				StringBuffer buffer = new StringBuffer(e.text);
				char[] chars = new char[buffer.length()];
				buffer.getChars(0, chars.length, chars, 0);
				
				//BACKSPACE and DELETE handling
				if (e.character == '\b' || e.keyCode == SWT.DEL) {
					int start = e.keyCode == SWT.DEL ? e.start : e.start;
					int end = e.keyCode == SWT.DEL ? e.end : e.end;
					for (int i = start; i < end; i++) {
						switch (i) {
						case 0: /* [Y]YYY */
						case 1: /* Y[Y]YY */
						case 2: /* YY[Y]Y */
						case 3: /* YYY[Y] */ {
							buffer.append('Y'); 	break;
						}
						case 5: /* [M]M */
						case 6: /* M[M] */{
							buffer.append('M'); break;
						}
						case 8: /* [D]D */
						case 9: /* D[D] */ {
							buffer.append('D'); break;
						}
						case 4: /* YYYY[-]MM */
						case 7: /* MM[-]DD */ {
							buffer.append('-'); break;
						}
						default:
							return;
						}
					}
					text.setSelection(e.start, e.start + buffer.length());
					ignore = true;
					text.insert(buffer.toString());
					ignore = false;
					if (e.keyCode == SWT.DEL)
						text.setSelection(e.start+1, e.start+1);
					else
						text.setSelection(e.start, e.start);
					
					//this will be no valid date, so unset the dialog's calendar and the current object
					calendarDialog.setDate(null);
					DateDialogInputAutoCompletion.this.setValue(null);
					
					return;
				}

				int start = e.start;
				if (start > 9) return;
				int index = 0;
				for (int i = 0; i < chars.length; i++) {
					if (start + index == 4 || start + index == 7) {
						if (chars[i] == '-') {
							index++;
							continue;
						}
						buffer.insert(index++, '-');
					}
					if (chars[i] < '0' || '9' < chars[i]) return;
					if (start + index == 5 &&  '1' < chars[i]) return; /* [M]M */
					if (start + index == 8 &&  '3' < chars[i]) return; /* [D]D */
					index++;
				}
				String newText = buffer.toString();
				int length = newText.length();
				StringBuffer date = new StringBuffer(text.getText());
				date.replace(e.start, e.start + length, newText);
				calendar.set(Calendar.YEAR, 1901);
				calendar.set(Calendar.MONTH, Calendar.JANUARY);
				calendar.set(Calendar.DATE, 1);
				String yyyy = date.substring(0, 4);
				if (yyyy.indexOf('Y') == -1) {
					int year = Integer.parseInt(yyyy);
					calendar.set(Calendar.YEAR, year);
				}
				String mm = date.substring(5, 7);
				if (mm.indexOf('M') == -1) {
					int month =  Integer.parseInt(mm) - 1;
					int maxMonth = calendar.getActualMaximum(Calendar.MONTH);
					if (0 > month || month > maxMonth) return;
					calendar.set(Calendar.MONTH, month);
				}
				String dd = date.substring(8, 10);
				if (dd.indexOf('D') == -1) {
					int day = Integer.parseInt(dd);
					int maxDay = calendar.getActualMaximum(Calendar.DATE);
					if (1 > day || day > maxDay) return;
					calendar.set(Calendar.DATE, day);
				} else {
					if (calendar.get(Calendar.MONTH)  == Calendar.FEBRUARY) {
						char firstChar = date.charAt(0);
						if (firstChar != 'D' && '2' < firstChar) return;
					}
				}
				text.setSelection(e.start, e.start + length);
				ignore = true;
				text.insert(newText);
				ignore = false;
				
				//this is a valid date, so set the dialog's calendar and the current object
				calendarDialog.setDate(calendar.getTime());
				DateDialogInputAutoCompletion.this.setValue(calendar.getTime());
			}
		});		

		return clientControl1;
	}
}
