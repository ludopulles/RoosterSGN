package eu.ludiq.roostersgn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import eu.ludiq.roostersgn.net.HttpCallback;
import eu.ludiq.roostersgn.net.TimetableRequest;
import eu.ludiq.roostersgn.rooster.Timetable;
import eu.ludiq.roostersgn.util.DataStorage;

/**
 * A class for keeping the saved timetable up-to-date. If this runnable is run,
 * it will load the timetable, compare it with the last loaded timetable, and
 * decides whether or not to show a notification about new changes.
 * 
 * @author Ludo Pulles
 * 
 */
public class TimetableSyncer implements Runnable, HttpCallback {

	private static final String TAG = "Timetable Syncer";

	private Context context;
	private Preferences preferences;
	private Timetable lastTimetable;
	private TimetableRequest request;

	public TimetableSyncer(Context context) {
		this.context = context;
		this.request = new TimetableRequest(context);
		this.request.addCallback(this);
	}

	public void run() {
		Log.i(TAG, "sync start");

		preferences = DataStorage.readPreferences(context);
		lastTimetable = DataStorage.readTimetable(context);

		if (preferences != null) {
			request.clearParameters();
			request.setPreferences(preferences);
			request.request();
		}
	}

	@Override
	public void onLoaded(Timetable timetable) {
		String[] changes = null, oldChanges = null;
		if (timetable != null) {
			changes = timetable.getChanges();
		}
		if (lastTimetable != null) {
			oldChanges = lastTimetable.getChanges();
		}

		notifyUser(changes, oldChanges);
		TimetableWidget.updateWidget(context, preferences, timetable);
		lastTimetable = timetable;
	}

	@Override
	public void handleError(int errorCode, Exception e) {
		Log.w(TAG, "sync fail: " + e.getMessage());
	}

	@Override
	public void onLoadingStopped() {
	}

	/**
	 * @param changes
	 *            has to be not NULL and a bigger length than 0
	 * @param oldChanges
	 *            the changes of the latest rooster
	 * @return whether there are differences between the two arrays of changes
	 */
	private boolean isDifferent(String[] changes, String[] oldChanges) {
		if ((changes != null && oldChanges == null)
				|| changes.length > oldChanges.length) {
			return true;
		}
		for (String change : changes) {
			boolean newChange = true;
			for (String oldChange : oldChanges) {
				if (change.equalsIgnoreCase(oldChange)) {
					newChange = false;
					break;
				}
			}
			if (newChange) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Shows a notification to the user when useful.
	 * 
	 * @param changes
	 *            the current changes made to the timetable
	 * @param oldChanges
	 *            the changes the last timetable had
	 */
	@SuppressWarnings("deprecation")
	private void notifyUser(String[] changes, String[] oldChanges) {
		NotificationManager manager = (NotificationManager) context
				.getSystemService(SyncService.NOTIFICATION_SERVICE);
		if (changes == null || changes.length == 0) {
			manager.cancel(0);
		} else if (isDifferent(changes, oldChanges)) {
			String title;
			if (changes.length > 1)
				title = String.format(
						context.getResources().getString(
								R.string.changes_plural), changes.length);
			else
				title = context.getResources().getString(
						R.string.changes_singular);

			String desc = description(changes);

			PendingIntent pending = PendingIntent.getActivity(context, 0,
					new Intent(context, SignInActivity.class),
					PendingIntent.FLAG_UPDATE_CURRENT);

			Notification notification = new Notification(
					R.drawable.ic_launcher, title, System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.setLatestEventInfo(context, title, desc, pending);

			manager.notify(0, notification);
		}
	}

	private int toDayNumber(String day) {
		String[] days = new String[] { "ma", "di", "wo", "do", "vr" };
		for (int i = 0; i < days.length; i++) {
			if (days[i].equalsIgnoreCase(day)) {
				return i;
			}
		}
		return -1;
	}

	private String capFirst(String s) {
		if (s == null || s.length() == 0)
			return s;
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private String formatChange(String[] days, String change) {
		try {
			int semicolon = change.indexOf(';');
			int day = toDayNumber(change.substring(semicolon - 2, semicolon));
			int hour = Integer.parseInt(change.substring(semicolon + 2,
					semicolon + 3));
			return String
					.format(context.getResources().getString(
							R.string.changes_one_hour), capFirst(days[day]),
							hour);
		} catch (Exception e) {
			return change;
		}
	}

	/**
	 * Returns a description that suits with the changes
	 * 
	 * @param changes
	 * @return a description of the changes
	 */
	private String description(String[] changes) {
		String[] days = context.getResources().getStringArray(R.array.days);
		if (changes.length == 1) {
			return formatChange(days, changes[0]);
		}
		List<Integer> hours = new ArrayList<Integer>();
		int day = -1;
		for (String change : changes) {
			try {
				int split = change.indexOf(';');
				int d = toDayNumber(change.substring(split - 2, split));
				int i = Integer
						.parseInt(change.substring(split + 2, split + 3));
				if (d > day) {
					day = d;
					hours.clear();
				}
				if (!hours.contains(i)) {
					hours.add(i);
				}
			} catch (Exception e) {
			}
		}

		Collections.sort(hours);

		if (day < 0 || day >= days.length || hours.size() == 0) {
			return formatChange(days, changes[0]);
		} else if (hours.size() == 1) {
			String format = context.getResources().getString(
					R.string.changes_one_hour);
			return String.format(format, capFirst(days[day]), hours.get(0));
		} else {
			StringBuilder hourStr = new StringBuilder();
			for (int i = 0; i < hours.size(); i++) {
				if (i == 0) {
					hourStr.append(hours.get(i)).append("e ");
				} else if (i == hours.size() - 1) {
					hourStr.append("en ").append(hours.get(i)).append("e");
				} else {
					hourStr.append(", ").append(hours.get(i)).append("e ");
				}
			}
			String format = context.getResources().getString(
					R.string.changes_more_than_one_hour);
			return String.format(format, capFirst(days[day]), hourStr);
		}
	}
}