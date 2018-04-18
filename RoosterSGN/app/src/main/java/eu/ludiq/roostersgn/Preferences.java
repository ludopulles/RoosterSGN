package eu.ludiq.roostersgn;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import eu.ludiq.roostersgn.util.DataStorage;
import eu.ludiq.roostersgn.util.TimeUtil;

/**
 * Preferences contains the options to sign in.
 * 
 * @author Ludo Pulles
 * 
 */
public class Preferences {

	private final String user, pass;
	private final int week;

	public Preferences(String user, String pass) {
		this(user, pass, 0);
	}

	public Preferences(String user, String pass, int week) {
		this.user = user;
		this.pass = pass;
		this.week = week;
	}

	public Preferences(SharedPreferences sp) throws Exception {
		this.week = 0;

		this.user = sp.getString(DataStorage.PREF_NAME, null);
		if (user == null) {
			throw new IllegalArgumentException("no username");
		}
		
		this.pass = sp.getString(DataStorage.PREF_PASS, null);
		if (pass == null) {
			throw new IllegalArgumentException("no password");
		}
	}

	public String getUsername() {
		return user;
	}

	public String getPassword() {
		return pass;
	}

	public int getWeek() {
		return week;
	}

	public Preferences setWeek(int week) {
		return new Preferences(user, pass, week);
	}

	public boolean isWeekSet() {
		return TimeUtil.isValidWeek(week);
	}

	public boolean save(SharedPreferences sp) {
		Editor editor = sp.edit();
		editor.putString(DataStorage.PREF_NAME, user);
		editor.putString(DataStorage.PREF_PASS, pass);
		editor.putInt(DataStorage.PREF_WEEK, week);
		return editor.commit();
	}

	@Override
	public String toString() {
		if (isWeekSet()) {
			return "{ user=" + user + ", pass=" + pass + ", week=" + week + " }";
		} else {
			return "{ user=" + user + ", pass=" + pass + " }";
		}
	}

}
