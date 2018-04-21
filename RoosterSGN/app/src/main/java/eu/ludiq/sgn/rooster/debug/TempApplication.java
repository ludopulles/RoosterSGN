package eu.ludiq.sgn.rooster.debug;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class TempApplication extends Application {

	private Thread.UncaughtExceptionHandler defaultHandler;
	private Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {

		public void uncaughtException(Thread thread, Throwable ex) {
			Log.w("TEmpApplication",
					"better check your code, pal, it's wrong! It's like shit basicallly,,,");

			SharedPreferences sp = getSharedPreferences("debug", MODE_PRIVATE);

			StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			ex.printStackTrace(printWriter);
			printWriter.flush();

			String stackTrace = writer.toString();

			sp.edit().putString("fout", stackTrace).commit();

			defaultHandler.uncaughtException(thread, ex);
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}

	public static void showException(Context context) {
		SharedPreferences temp = context.getSharedPreferences("debug",
				MODE_PRIVATE);
		if (temp.contains("fout")) {
			String s = temp.getString("fout", "");
			if (s.length() > 0) {
				Intent inti = new Intent(context, CrashActivity.class);
				inti.putExtra("fout", s);
				context.startActivity(inti);
			}
		}
	}

}
