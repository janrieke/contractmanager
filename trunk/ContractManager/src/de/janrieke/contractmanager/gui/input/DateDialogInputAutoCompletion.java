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
					for (int i = e.start; i < e.end; i++) {
						switch (i) {
						case 0: /* [Y]YYY */
						case 1: /* Y[Y]YY */
						case 2: /* YY[Y]Y */
						case 3: /* YYY[Y] */ {
							buffer.append('Y'); break;
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

				int offset = 0;
				for (int i = 0; i < chars.length && e.start+i+offset<10; i++) {
					if (e.start + offset + i == 4 || e.start + offset + i == 7) { //positions of the "-"
						if (chars[i] == '-') {
							continue;
						}
						buffer.insert(i+offset, '-'); //allow entering dates without dashes
						offset++; //increase offset, as we added a new character not contained in chars
					}
					if (chars[i] < '0' || '9' < chars[i]) return;
					if (e.start + offset + i == 5 &&  '1' < chars[i]) return; /* [M]M */
					if (e.start + offset + i == 8 &&  '3' < chars[i]) return; /* [D]D */
				}
				
				buffer.setLength(chars.length+offset);
				
				boolean validDate = true;
				boolean updateMonth = false;
				boolean updateDay = false;
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
				} else
					validDate = false;
				
				String mm = date.substring(5, 7);
				if (mm.indexOf('M') == -1) {
					int month =  Integer.parseInt(mm) - 1;
					int maxMonth = calendar.getActualMaximum(Calendar.MONTH);
					if (0 > month)
						month = 0;
					if (month > maxMonth)
						month = maxMonth;
					calendar.set(Calendar.MONTH, month);
					updateMonth = true;
				} else
					validDate = false;
				
				String dd = date.substring(8, 10);
				if (dd.indexOf('D') == -1) {
					int day = Integer.parseInt(dd);
					int maxDay = calendar.getActualMaximum(Calendar.DATE);
					if (1 > day) 
						day = 1;
					if (day > maxDay) 
						day = maxDay;
					calendar.set(Calendar.DAY_OF_MONTH, day);
					updateDay = true;
				} else {
					validDate = false;
					if (calendar.get(Calendar.MONTH)  == Calendar.FEBRUARY) {
						char firstChar = dd.charAt(0);
						if (firstChar != 'D' && '2' < firstChar) return;
					}
				}
				
				//apply the entered character
				text.setSelection(e.start, e.start + length);
				ignore = true;
				text.insert(newText);
				ignore = false;
				
				//also update month and/or day
				if (updateMonth) {
					text.setSelection(5, 7);
					ignore = true;
					String month = Integer.toString(calendar.get(Calendar.MONTH)+1);
					text.insert(month.length()==1?"0"+month:month);
					ignore = false;
				}
				if (updateDay) {
					text.setSelection(8, 10);
					ignore = true;
					String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
					text.insert(day.length()==1?"0"+day:day);
					ignore = false;
				}

				//reset the cursor position
				text.setSelection(e.start+length, e.start+length);
				
				if (validDate) {
					//this is a valid date, so set the dialog's calendar and the current object
					calendarDialog.setDate(calendar.getTime());
					DateDialogInputAutoCompletion.this.setValue(calendar.getTime());
				}
			}
		});		

		return clientControl1;
	}
}
