package de.janrieke.contractmanager.rmi;

import de.janrieke.contractmanager.Settings;

/**
 * Enumeration of different intervals that define contract runtimes,
 * cancellation deadlines or cost paydays.
 */
public enum IntervalType {
	//Will be sorted by order
	ONCE			(0, "undefined",	"once"),
	DAYS			(1, "Days",			"daily"),
	WEEKS			(2, "Weeks", 		"weekly"),
	MONTHS			(3, "Months", 		"monthly"),
	QUARTER_YEARS	(5, "Quarter Years","quarterly"),
	HALF_YEARS		(6, "Half Years", 	"semiannually"),
	YEARS			(4, "Years", 		"annually");

	private final int value; //used for DB storage
	private final String name;
	private final String adjective;

	private IntervalType(int value, String name, String adjective) {
		this.value = value;
		this.name = name;
		this.adjective = adjective;
	}

	public int getValue() {
		return value;
	}

	public String getName() {
		return Settings.i18n().tr(name);
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getAdjective() {
		return Settings.i18n().tr(adjective);
	}

	public static IntervalType valueOfAdjective(String adjective) {
		for (IntervalType interval : IntervalType.values()) {
			if (interval.adjective.equals(adjective) || Settings.i18n().tr(interval.adjective).equals(adjective)) {
				return interval;
			}
		}
		return ONCE;
	}

	public static IntervalType valueOfName(String name) {
		for (IntervalType interval : IntervalType.values()) {
			if (interval.name.equals(name) || Settings.i18n().tr(interval.name).equals(name)) {
				return interval;
			}
		}
		return ONCE;
	}

	public static IntervalType valueOf(int value) {
		for (IntervalType interval : IntervalType.values()) {
			if (interval.value == value) {
				return interval;
			}
		}
		return ONCE;
	}

	public static String[] getAdjectives() {
		String[] res = new String[IntervalType.values().length];
		for (IntervalType interval : IntervalType.values()) {
			res[interval.ordinal()] = interval.getAdjective();
		}
		return res;
	}

}