package eu.ludiq.roostersgn.util;

import android.content.Context;
import android.content.SharedPreferences;
import eu.ludiq.roostersgn.Preferences;
import eu.ludiq.roostersgn.rooster.Timetable;

/**
 * A utility class for writing and reading the timetable or preferences.
 * 
 * @author Ludo Pulles
 * 
 */
public class DataStorage {

	/**
	 * A {@link SharedPreferences} containing the username and password of the
	 * user.
	 */
	public static final String PREFERENCES = "preferences";

	/**
	 * A string containing the username
	 */
	public static final String PREF_NAME = "name";

	/**
	 * A string containing the password
	 */
	public static final String PREF_PASS = "pass";

	/**
	 * A {@link SharedPreferences} containing a timetable corresponding to the
	 * username. The timetable contains {@link #PREF_TIMESTAMP},
	 * {@link #PREF_WEEK}, {@link #PREF_TIMETABLE}, {@link #PREF_CHANGES}
	 */
	public static final String TIMETABLE = "rooster";

	/**
	 * A number indicating when the timetable was loaded for the last time
	 */
	public static final String PREF_TIMESTAMP = "timestamp";

	/**
	 * A number indicating for which week of the year the timetable is
	 */
	public static final String PREF_WEEK = "week";

	/**
	 * A string which can be parsed in JSON so it's parsable by
	 * {@link Timetable#Timetable(SharedPreferences)}, However, if it's NULL,
	 * then the user has given a wrong password, or there is no timetable for
	 * that week. rooster of fout wachtwoord
	 */
	public static final String PREF_TIMETABLE = "rooster";

	/**
	 * A string which can be parsed in JSON so it's parsable by
	 * {@link Timetable#Timetable(SharedPreferences)}. It contains the changes
	 * made to the timetable, compared to the default timetable.
	 */
	public static final String PREF_CHANGES = "wijzigingen";

	private static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
	}

	private static SharedPreferences getTimetable(Context context) {
		return context.getSharedPreferences(TIMETABLE, Context.MODE_PRIVATE);
	}

	public static void savePreferences(Context context, Preferences prefs) {
		prefs.save(getPreferences(context));
	}

	public static void saveTimetable(Context context, Timetable timetable) {
		timetable.save(getTimetable(context));
	}

	public static Preferences readPreferences(Context context) {
		try {
			return new Preferences(getPreferences(context));
		} catch (Exception e) {
			return null;
		}
	}

	public static Timetable readTimetable(Context context) {
		try {
			return new Timetable(getTimetable(context));
		} catch (Exception e) {
			return null;
		}
	}

	public static void clearData(Context context) {
		getPreferences(context).edit().clear().commit();
		getTimetable(context).edit().clear().commit();
	}
}
