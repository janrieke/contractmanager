package de.janrieke.contractmanager.util;

import java.util.Calendar;

/**
 * A simple builder class for calenders. Can be mocked for testing purposes.
 */
public class CalendarBuilder {
	public Calendar getInstance() {
		return Calendar.getInstance();
	}
}