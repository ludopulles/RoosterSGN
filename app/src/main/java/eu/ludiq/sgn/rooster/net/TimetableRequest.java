package eu.ludiq.sgn.rooster.net;

import org.apache.http.message.BasicNameValuePair;

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
		setUrl("http://christiaangoossens.nl/rooster/get.php");
		addCallback(this);
	}

	public void setPreferences(Preferences preferences) {
		addParameter(new BasicNameValuePair("user", preferences.getUsername()));
		addParameter(new BasicNameValuePair("pass", preferences.getPassword()));
		if (preferences.isWeekSet()) {
			String week = Integer.toString(preferences.getWeek());
			addParameter(new BasicNameValuePair("week", week));
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
