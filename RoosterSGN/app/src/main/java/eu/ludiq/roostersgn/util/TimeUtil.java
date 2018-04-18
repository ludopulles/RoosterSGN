package eu.ludiq.roostersgn.util;

import java.util.Calendar;

import eu.ludiq.roostersgn.rooster.Day;
import eu.ludiq.roostersgn.rooster.Timetable;
import eu.ludiq.roostersgn.rooster.Week;

/**
 * A utility class for things concerning timing.
 * 
 * @author Ludo Pulles
 * 
 */
public class TimeUtil {

	private static final int SPRING_FALL_BORDER = 30;
	private static final int MAX_HOURS = 8;
	private static final Time[] TIME_START = new Time[] { new Time(8, 20),
			new Time(9, 10), new Time(10, 25), new Time(11, 15),
			new Time(12, 35), new Time(13, 25), new Time(14, 25),
			new Time(15, 15) };
	private static final Time[] TIME_END = new Time[] { new Time(9, 10),
			new Time(10, 00), new Time(11, 15), new Time(12, 05),
			new Time(13, 25), new Time(14, 15), new Time(15, 15),
			new Time(16, 05) };

	/**
	 * Gives a string containing the time the period starts or ends
	 * 
	 * @param index
	 *            the nth period of the day that is requested
	 * @return e.g. 8:20 - 9:10
	 */
	public static String periodToString(int index) {
		index = Math.min(MAX_HOURS - 1, index);
		Time start = TIME_START[index], end = TIME_END[index];
		return format(start.hour, 2) + ":" + format(start.minute, 2) + " - "
				+ format(end.hour, 2) + ":" + format(end.minute, 2);
	}

	/**
	 * Fills the string with zeros so its length = stringLength.
	 * 
	 * @param number
	 *            the number to convert to a string
	 * @param stringLength
	 *            the length the string must become
	 * @return the string with stringLength length
	 */
	private static String format(int number, int stringLength) {
		StringBuilder sb = new StringBuilder();
		sb.append(number);
		while (sb.length() < stringLength) {
			sb.insert(0, '0');
		}
		return sb.toString();
	}

	public static boolean isBefore(int hour1, int minute1, int hour2,
			int minute2) {
		return hour1 < hour2 || (hour1 == hour2 && minute1 < minute2);
	}

	public static boolean isBefore(int hour, int minute, Time time) {
		return isBefore(hour, minute, time.hour, time.minute);
	}

	private static boolean isSpring(int week) {
		return week <= SPRING_FALL_BORDER;
	}

	public static boolean isValidWeek(int week) {
		return week >= 1 && week <= 53; //getWeeksInYear(getCalendar(week));
	}

	public static int prevWeek(int week) {
		if (week <= 1) {
			return getWeeksInYear(getCalendar(week));
		} else {
			return week - 1;
		}
	}

	public static int nextWeek(int week) {
		if (week >= getWeeksInYear(getCalendar(week))) {
			return 1;
		} else {
			return week + 1;
		}
	}

	// ------------------------------------------------------------------------
	// *** CALENDAR HELP METHODS **********************************************
	// ------------------------------------------------------------------------

	public static Calendar currentDate() {
		Calendar currentDate = Calendar.getInstance();
		// if debugging, this can be modified
		// currentDate.set(year, month, day);
		return currentDate;
	}

	private static boolean isSpring(Calendar calendar) {
		int week = calendar.get(Calendar.WEEK_OF_YEAR);
		Calendar copy = (Calendar) calendar.clone();
		copy.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		if (copy.get(Calendar.WEEK_OF_YEAR) != week) {
			week = copy.get(Calendar.WEEK_OF_YEAR);
		}
		return isSpring(week);
	}

	public static Calendar getCalendar(int week) {
		Calendar calendar = currentDate();
		setWeekOfYear(calendar, week);
		return calendar;
	}

	public static int getWeeksInYear(Calendar calendar) {
		return calendar.getActualMaximum(Calendar.WEEK_OF_YEAR);
	}

	public static void setWeekOfYear(Calendar calendar, int week) {
		if (isSpring(calendar) && !isSpring(week)) {
			// spring, if week is in fall, decrement one year
			calendar.add(Calendar.YEAR, -1);
		} else if (!isSpring(calendar) && isSpring(week)) {
			// fall, if week is in spring, increment one year
			calendar.add(Calendar.YEAR, 1);
		}
		// set week of year
		calendar.set(Calendar.WEEK_OF_YEAR, week);
		calendar.get(Calendar.WEEK_OF_YEAR); // bug?
	}

	public static int getWeekOfYear(Calendar cal) {
		int week = cal.get(Calendar.WEEK_OF_YEAR);
		int day = cal.get(Calendar.DAY_OF_WEEK);
		if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
			cal.add(Calendar.DAY_OF_WEEK, -1);
			if (week != cal.get(Calendar.WEEK_OF_YEAR)) {
				week = prevWeek(week);
			}
			cal.add(Calendar.DAY_OF_WEEK, 1);
		}
		return week;
	}

	public static void setThisSchoolYear(Calendar calendar) {
		int week = getWeekOfYear(calendar);
		Calendar preferred = getCalendar(week);
		if (week != 1 && isSpring(calendar) || !isSpring(preferred)) {
			calendar.set(Calendar.YEAR, preferred.get(Calendar.YEAR));
		}
	}

	// ------------------------------------------------------------------------
	// *** TIMETABLE HELP METHODS *********************************************
	// ------------------------------------------------------------------------

	public static int lastHourOfDay(Day day) {
		if (day != null) {
			for (int h = day.length() - 1; h >= 0; h--) {
				if (day.getHour(h).hasClass()) {
					return h;
				}
			}
		}
		return -1;
	}

	public static int firstHourOfDay(Day day) {
		if (day != null) {
			for (int h = 0; h < day.length(); h++) {
				if (day.getHour(h).hasClass()) {
					return h;
				}
			}
		}
		return -1;
	}

	private static int getSkippedWeekOfYear(Calendar cal) {
		int day = cal.get(Calendar.DAY_OF_WEEK);
		if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
			return nextWeek(getWeekOfYear(cal));
		} else {
			return getWeekOfYear(cal);
		}
	}

	public static Hour currentHour(Week week, Calendar cal) {
		int today = DayUtil.toDayNumber(cal);
		if (DayUtil.isWeekend(today)) {
			int firstHour = firstHourOfDay(week.getDay(DayUtil.MONDAY));
			if (firstHour == -1) {
				firstHour = 0;
			}
			return new Hour(DayUtil.MONDAY, firstHour);
		}

		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int firstHour = firstHourOfDay(week.getDay(today));
		int lastHour = lastHourOfDay(week.getDay(today));

		if (firstHour == -1 || lastHour == -1) {
			// there are no periods today
			if (isBefore(hour, minute, TIME_END[MAX_HOURS - 1])) {
				// 8th period is not over
				// return the first period for convenience
				return new Hour(today, 0);
			}
		} else {
			// there are periods today
			final int limit = Math.min(lastHour + 1, MAX_HOURS);
			for (int i = firstHour; i < limit; i++) {
				// if the period isn't over
				if (isBefore(hour, minute, TIME_END[i])) {
					return new Hour(today, i);
				}
			}
		}
		// return the first period of the next day.
		// if it's friday, return the last period of the week
		if (today < DayUtil.FRIDAY) {
			// all periods are over, so return the first period of the next day
			firstHour = firstHourOfDay(week.getDay(today + 1));
			if (firstHour == -1) {
				firstHour = 0;
			}
			return new Hour(today + 1, firstHour);
		} else {
			// return the last period of this week
			return new Hour(DayUtil.FRIDAY, week.getDay(today).length() - 1);
		}
	}

	public static int dayOfCurrentClass(Timetable timetable) {
		Week week;
		try {
			week = timetable.getWeek();
		} catch (Exception e) {
			return DayUtil.MONDAY;
		}
		Calendar currentDate = currentDate();
		int thisWeek = getSkippedWeekOfYear(currentDate);
		int timetableWeek = timetable.getWeekOfYear();
		if (timetableWeek > thisWeek) {
			// future week
			return DayUtil.MONDAY;
		} else if (timetableWeek < thisWeek) {
			// old week
			return DayUtil.FRIDAY;
		} else {
			// current week
			return currentHour(week, currentDate).day;
		}
	}

	// ------------------------------------------------------------------------
	// *** CLASSES ************************************************************
	// ------------------------------------------------------------------------

	public static class Time {

		public final int hour, minute;

		public Time(int hour, int minute) {
			this.hour = hour;
			this.minute = minute;
		}

	}

	public static class Hour {

		public final int day, hour;

		public Hour(int day, int hour) {
			this.day = day;
			this.hour = hour;
		}
	}

}
