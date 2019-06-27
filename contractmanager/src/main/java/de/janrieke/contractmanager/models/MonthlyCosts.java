package de.janrieke.contractmanager.models;

import de.janrieke.contractmanager.rmi.IntervalType;

/**
 * Helper class to provide a model for drawing monthly costs statistics.
 */
public class MonthlyCosts {
	private final String description;
	private final double money;

	public MonthlyCosts(String description, double money) {
		super();
		this.description = description;
		this.money = money;
	}

	public static double averageToMonthly(double money, IntervalType interval) {
		switch (interval) {
		case DAYS:
			return money * 30;
		case WEEKS:
			return money * (52d/12);
		case MONTHS:
			return money;
		case QUARTER_YEARS:
			return money / 3;
		case HALF_YEARS:
			return money / 6;
		case YEARS:
			return money / 12;

		default:
			return 0.0f;
		}
	}

	public static MonthlyCosts averageToMonthlyCosts(String description, double money, IntervalType interval) {
		return new MonthlyCosts(description, averageToMonthly(money, interval));
	}

	public String getDescription() {
		return description;
	}

	public double getMoney() {
		return money;
	}
}
