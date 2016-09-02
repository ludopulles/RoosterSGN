package eu.ludiq.sgn.rooster.util;

import java.util.Calendar;

/**
 * A utility class for working with days and exporting them to an integer value.
 * 
 * @author Ludo Pulles
 * 
 */
public class DayUtil {

	public static final int MONDAY = 0;
	public static final int TUESDAY = 1;
	public static final int WEDNESDAY = 2;
	public static final int THURSDAY = 3;
	public static final int FRIDAY = 4;
	public static final int SATURDAY = 5;
	public static final int SUNDAY = 6;

	public static int toCalendar(int day) {
		if (day == SUNDAY) {
			return Calendar.SUNDAY;
		} else {
			return day + 2;
		}
	}

	public static void setDayOfWeek(Calendar calendar, int day) {
		calendar.set(Calendar.DAY_OF_WEEK, toCalendar(day));
	}

	/**
	 * MONDAY: 2 -> 0, TUESDAY: 3 -> 1, WEDNESDAY: 4 -> 2, THURSDAY: 5 -> 3,
	 * FRIDAY: 6 -> 4, SATURDAY: 7 -> 5, SUNDAY: 1 -> 6
	 * 
	 * @param calendarDayOfWeek
	 * @return a day number between 0 - 6
	 */
	public static int toDayNumber(Calendar cal) {
		int day = cal.get(Calendar.DAY_OF_WEEK);
		if (day == Calendar.SUNDAY) {
			return SUNDAY;
		} else {
			return day - 2;
		}
	}

	public static boolean isWorkDay(int day) {
		return day >= MONDAY && day <= FRIDAY;
	}

	public static boolean isWeekend(int day) {
		return !isWorkDay(day);
	}

}