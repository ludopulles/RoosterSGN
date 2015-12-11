package eu.ludiq.sgn.rooster.rooster;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import eu.ludiq.sgn.rooster.exception.NoTimetableException;
import eu.ludiq.sgn.rooster.exception.ServerException;
import eu.ludiq.sgn.rooster.exception.WrongPasswordException;
import eu.ludiq.sgn.rooster.util.DataStorage;
import eu.ludiq.sgn.rooster.util.TimeUtil;

/**
 * A timetable is a scheme for where and when classes take place.
 * 
 * @author Ludo Pulles
 * 
 */
public class Timetable {

	public static final String ERROR_CODE = "code";
	public static final String ERROR_MSG = "melding";
	public static final String TIMESTAMP = "timestamp";
	public static final String WEEK = "week";
	public static final String TIMETABLE = "rooster";
	public static final String CHANGES = "wijzigingen";

	private long timestamp = 0;
	private int week = -1;
	private Week timetable = null;
	public String[] changes = null;

	public Timetable(JSONObject response) throws WrongPasswordException,
			JSONException, ServerException {
		this.timestamp = response.optLong(TIMESTAMP, defaultTimestamp());

		if (response.has(ERROR_CODE) && response.has(ERROR_MSG)) {
			int errorCode = response.getInt(ERROR_CODE);
			String errorMsg = response.getString(ERROR_MSG);
			throw new ServerException(errorCode, errorMsg);
		}
		
		if (response.isNull(WEEK)) {
			if (response.has(TIMETABLE) && response.isNull(TIMETABLE)
					&& response.has(WEEK)) {
				throw new WrongPasswordException();
			}
			throw new ServerException();
		}
		week = response.getInt(WEEK);
		if (!TimeUtil.isValidWeek(week)) {
			throw new JSONException("invalid week");
		}

		if (!response.isNull(TIMETABLE)) {
			timetable = new Week(response.getJSONObject(TIMETABLE));
		}

		if (response.has(CHANGES) && response.optJSONArray(CHANGES) != null) {
			setChanges(response.getJSONArray(CHANGES));
		}
	}

	public Timetable(SharedPreferences sp) throws Exception {
		this.timestamp = sp.getLong(DataStorage.PREF_TIMESTAMP,
				defaultTimestamp());

		this.week = sp.getInt(DataStorage.PREF_WEEK, -2);
		if (week == -2) {
			throw new IllegalArgumentException(DataStorage.PREF_WEEK);
		}

		String rawTimetable = sp.getString(DataStorage.PREF_TIMETABLE, null);
		if (rawTimetable != null) {
			this.timetable = new Week(new JSONObject(rawTimetable));
			if (!TimeUtil.isValidWeek(week)) {
				throw new JSONException("invalid week");
			}
		}

		String rawChanges = sp.getString(DataStorage.PREF_CHANGES, null);
		if (rawChanges != null) {
			try {
				JSONArray wijzigingen = new JSONArray(rawChanges);
				setChanges(wijzigingen);
			} catch (JSONException e) {
			}
		}
	}

	private static long defaultTimestamp() {
		return System.currentTimeMillis() / 1000;
	}

	private void setChanges(JSONArray array) throws JSONException {
		String[] s = new String[array.length()];
		for (int i = 0; i < array.length(); i++) {
			s[i] = array.getString(i);
		}
		this.changes = s;
	}

	private String concatChanges() throws JSONException {
		JSONArray array = new JSONArray();
		for (int i = 0; i < changes.length; i++) {
			array.put(i, changes[i]);
		}
		return array.toString();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getWeekOfYear() {
		return week;
	}

	public String[] getChanges() {
		return changes;
	}

	public boolean hasChanges() {
		return changes != null && changes.length > 0;
	}

	public Week getWeek() throws NoTimetableException {
		if (getContentType() == ContentType.NO_TIMETABLE) {
			throw new NoTimetableException(this);
		}

		return timetable;
	}

	public ContentType getContentType() {
		if (timetable == null) {
			return ContentType.NO_TIMETABLE;
		}
		return ContentType.STANDARD;
	}

	public boolean save(SharedPreferences sp) {
		Editor editor = sp.edit();

		editor.putLong(DataStorage.PREF_TIMESTAMP, timestamp);
		editor.putInt(DataStorage.PREF_WEEK, week);

		if (timetable == null) {
			editor.remove(DataStorage.PREF_TIMETABLE);
		} else {
			editor.putString(DataStorage.PREF_TIMETABLE, timetable.toString());
		}

		if (changes == null || changes.length == 0) {
			editor.remove(DataStorage.PREF_CHANGES);
		} else {
			try {
				editor.putString(DataStorage.PREF_CHANGES, concatChanges());
			} catch (JSONException e) {
				editor.remove(DataStorage.PREF_CHANGES);
			}
		}
		return editor.commit();
	}
}
