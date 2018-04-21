package eu.ludiq.sgn.rooster.exception;

import eu.ludiq.sgn.rooster.rooster.Timetable;

/**
 * An exception for situation where there's no timetable available for a week,
 * e.g. holiday.
 * 
 * @author Ludo Pulles
 * 
 */
public class NoTimetableException extends Exception {

	public NoTimetableException(Timetable rooster) {
		super("Er is geen rooster voor week " + rooster.getWeekOfYear());
	}
}
