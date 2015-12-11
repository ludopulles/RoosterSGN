package eu.ludiq.sgn.rooster;

import java.util.Calendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.widget.RemoteViews;
import eu.ludiq.sgn.rooster.exception.NoTimetableException;
import eu.ludiq.sgn.rooster.rooster.Day;
import eu.ludiq.sgn.rooster.rooster.Hour;
import eu.ludiq.sgn.rooster.rooster.Period;
import eu.ludiq.sgn.rooster.rooster.Timetable;
import eu.ludiq.sgn.rooster.rooster.Week;
import eu.ludiq.sgn.rooster.util.DataStorage;
import eu.ludiq.sgn.rooster.util.TimeUtil;

/**
 * A widget that shows the classes for that day.
 * 
 * @author Ludo Pulles
 * 
 */
public class TimetableWidget extends AppWidgetProvider {

	private static final String TAG = "Timetable Widget";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager widgetManager,
			int[] widgetIds) {
		Preferences preferences = DataStorage.readPreferences(context);
		Timetable timetable = DataStorage.readTimetable(context);
		
		RemoteViews layout = inflateWidget(context, preferences, timetable);
		for (int widgetId : widgetIds) {
			widgetManager.updateAppWidget(widgetId, layout);
		}
	}

	public static void updateWidget(Context context, Preferences prefs,
			Timetable timetable) {
		ComponentName widget = new ComponentName(context, TimetableWidget.class);
		RemoteViews layout = inflateWidget(context, prefs, timetable);
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
		widgetManager.updateAppWidget(widget, layout);
	}

	public static RemoteViews inflateWidget(Context context,
			Preferences preferences, Timetable timetable) {
		Log.i(TAG, "widget update");

		String name = context.getPackageName();
		PendingIntent clickIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, SignInActivity.class), 0);

		RemoteViews layout = new RemoteViews(name, R.layout.widget);
		layout.setOnClickPendingIntent(R.id.widget, clickIntent);
		layout.removeAllViews(R.id.widget_layout);
		layout.setInt(R.id.widget_layout, "setGravity", Gravity.NO_GRAVITY);

		if (preferences == null || timetable == null) {
			layout.setTextViewText(R.id.widget_tijd,
					context.getString(R.string.widget_no_user));
			return layout;
		}
		Week week;
		try {
			week = timetable.getWeek();
		} catch (NoTimetableException e) {
			layout.setTextViewText(R.id.widget_tijd,
					context.getString(R.string.widget_no_timetable));
			return layout;
		}

		Calendar currentDate = TimeUtil.currentDate();
		currentDate.add(Calendar.MINUTE, 15);

		TimeUtil.Hour currentClass = TimeUtil.currentHour(week, currentDate);
		Day day = week.getDay(currentClass.day);
		int lastHour = TimeUtil.lastHourOfDay(day);
		if (lastHour == -1) {
			lastHour = day.length() - 1;
		}

		String tijdText = TimeUtil.periodToString(currentClass.hour);
		layout.setTextViewText(R.id.widget_tijd, tijdText);

		for (int h = currentClass.hour; h <= lastHour; h++) {
			Hour hour = day.getHour(h);
			if (h != currentClass.hour) {
				layout.addView(R.id.widget_layout, new RemoteViews(name,
						R.layout.widget_divider));
			}

			if (hour.hasClass()) {
				for (int p = 0; p < hour.length(); p++) {
					RemoteViews period = inflateHour(name, hour, h, p);
					layout.addView(R.id.widget_layout, period);
				}
			} else {
				RemoteViews free = new RemoteViews(name, R.layout.widget_free);
				free.setTextViewText(R.id.widget_hour, (1 + h) + " ");
				free.setTextViewText(R.id.widget_free, hour.getType()
						.toString());
				layout.addView(R.id.widget_layout, free);
			}
		}

		return layout;
	}

	private static RemoteViews inflateHour(String name, Hour hour, int h, int p) {
		RemoteViews view = new RemoteViews(name, R.layout.widget_class);
		Period period = hour.getClass(p);

		view.setTextViewText(R.id.widget_hour,
				p == 0 ? (Integer.toString(1 + h) + " ") : "  ");
		view.setTextViewText(R.id.widget_subject, period.getSubject());
		view.setTextViewText(R.id.widget_classroom, period.getClassroom());
		view.setTextViewText(R.id.widget_group, period.getGroup());
		return view;
	}
}
