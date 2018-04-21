package eu.ludiq.sgn.rooster.net;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import eu.ludiq.sgn.rooster.Preferences;
import eu.ludiq.sgn.rooster.rooster.Timetable;
import eu.ludiq.sgn.rooster.util.DataStorage;

/**
 * An refined version of {@link HttpRequest} used for loading timetables
 * 
 * @author Ludo Pulles
 * 
 */
public class TimetableRequest extends HttpRequest implements HttpCallback {

	private static String userAgent(Context context) {
		try {
			int versioncode = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
			return "RoosterSGN (v" + versioncode + ")";
		} catch (NameNotFoundException e) {
			return "RoosterSGN ()";
		}
	}

	private Context mContext;

	public TimetableRequest(Context context) {
		super();
		this.mContext = context;
		setUserAgent(userAgent(context));
		setUrl("https://ludiq.eu/services/zermelo-api.php");
		addCallback(this);
	}

	public void setPreferences(Preferences preferences) {
		addParameter("user", preferences.getUsername());
		addParameter("pass", preferences.getPassword());
		if (preferences.isWeekSet()) {
			String week = Integer.toString(preferences.getWeek());
			addParameter("week", week);
		}
	}

	@Override
	public void onLoaded(Timetable timetable) {
		DataStorage.saveTimetable(mContext, timetable);
	}

	@Override
	public void handleError(int errorCode, Exception e) {
	}

	@Override
	public void onLoadingStopped() {
	}

}
