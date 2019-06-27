package de.janrieke.contractmanager.util;

import java.util.Calendar;
import java.util.Date;

import de.janrieke.contractmanager.rmi.IntervalType;
import de.willuhn.jameica.util.DateUtil;

public class DateUtils {

	/**
	 *
	 * @param calendar
	 *            the calender to modify
	 * @param interval
	 * @param count
	 * @return true if the operation was performed, false if it was impossible
	 *         due to invalid interval type.
	 */
	public static final boolean addToCalendar(Calendar calendar, IntervalType interval, int count) {
		// if the period is invalid, assume there is none
		if (interval != null) {
			switch (interval) {
			case DAYS:
				calendar.add(Calendar.DAY_OF_YEAR, count);
				return true;
			case WEEKS:
				calendar.add(Calendar.WEEK_OF_YEAR, count);
				return true;
			case MONTHS:
				calendar.add(Calendar.MONTH, count);
				return true;
			case QUARTER_YEARS:
				calendar.add(Calendar.MONTH, count * 3);
				return true;
			case HALF_YEARS:
				calendar.add(Calendar.MONTH, count * 6);
				return true;
			case YEARS:
				calendar.add(Calendar.YEAR, count);
				return true;
			case ONCE:
				return false;
			default:
				return false;
			}
		}
		return false;
	}

	/**
	 * Calculates the next contractual term's end after the given date.
	 *
	 * @param after
	 * @param excludeFirstTerm If true, never return the start of the first term.
	 * @return The end of the term, or null if no end could be calculated.
	 */
	public static Date calculateNextTermBeginAfter(Date after, Date startDate, boolean excludeFirstTerm, ValidRuntimes runtimes) {
		if (after == null) {
			return null;
		}
		if (startDate == null) {
			return null;
		}
		if (runtimes == null) {
			return null;
		}

		Calendar afterCal = Calendar.getInstance();
		afterCal.setTime(DateUtil.endOfDay(after));

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);

		// If fixed terms is true, virtually delay the start of the contract to the next period.
		if (runtimes.fixedTerms) {
			switch (runtimes.firstMinRuntimeType) {
				case WEEKS:
					calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					calendar.add(Calendar.WEEK_OF_YEAR, 1);
					break;
				case MONTHS:
					calendar.set(Calendar.DAY_OF_MONTH, 1);
					calendar.add(Calendar.MONTH, 1);
					break;
				case YEARS:
					calendar.set(Calendar.DAY_OF_YEAR, 1);
					calendar.add(Calendar.YEAR, 1);
					break;
				default:
			}

		}

		boolean first = true;
		boolean validResult = true;

		while (validResult && (!calendar.after(afterCal) || (first && excludeFirstTerm))) {
			if (first) {
				validResult = addToCalendar(calendar, runtimes.firstMinRuntimeType, runtimes.firstMinRuntimeCount);
				first = false;
			} else {
				validResult = addToCalendar(calendar, runtimes.followingMinRuntimeType, runtimes.followingMinRuntimeCount);
			}
		}

		return validResult ? calendar.getTime() : null;
	}
}
