package eu.ludiq.sgn.rooster;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * A service for keeping the timetable in synchronisation.
 * 
 * @author Ludo Pulles
 * 
 */
public class SyncService extends Service {

	private static final String TAG = "Sync Service";

	/**
	 * The time between two executions in minutes
	 */
	private static final int REFRESH_RATE = 120 * 60;

	/**
	 * The time between the first execution and the time the service started
	 */
	private static final int INITAL_DELAY = REFRESH_RATE;
	
	private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

	/**
	 * This executor will make sure that the time between executions will be the
	 * same as {@link SyncService#REFRESH_RATE}
	 */
	private ScheduledExecutorService mExecutor;

	// ------------------------------------------------------------------------
	// *** SERVICE LIFECYCLE **************************************************
	// ------------------------------------------------------------------------

	@Override
	public void onCreate() {
		super.onCreate();
		createService();
	}

	@Override
	public void onDestroy() {
		stopService();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new Binder();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		startService();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startService();
		return START_NOT_STICKY;
	}

	// ------------------------------------------------------------------------
	// *** ACTIONS ************************************************************
	// ------------------------------------------------------------------------

	private void createService() {
		Log.i(TAG, "service create");
		mExecutor = Executors.newSingleThreadScheduledExecutor();
	}

	private void startService() {
		Log.i(TAG, "service start");
		TimetableSyncer syncer = new TimetableSyncer(this);
		mExecutor.scheduleAtFixedRate(syncer, INITAL_DELAY, REFRESH_RATE, TIME_UNIT);
	}

	private void stopService() {
		Log.i(TAG, "service stop");
		mExecutor.shutdownNow();
	}
}