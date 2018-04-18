package eu.ludiq.roostersgn;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import eu.ludiq.roostersgn.exception.ServerException;
import eu.ludiq.roostersgn.net.HttpCallback;
import eu.ludiq.roostersgn.net.HttpRequest;
import eu.ludiq.roostersgn.net.HttpUtils;
import eu.ludiq.roostersgn.net.TimetableRequest;
import eu.ludiq.roostersgn.rooster.Timetable;
import eu.ludiq.roostersgn.scroll.ViewFlipperCompat;
import eu.ludiq.roostersgn.ui.DateView;
import eu.ludiq.roostersgn.ui.MenuView;
import eu.ludiq.roostersgn.ui.MenuView.MenuClickListener;
import eu.ludiq.roostersgn.ui.ViewFlipperManager;
import eu.ludiq.roostersgn.util.DataStorage;
import eu.ludiq.roostersgn.util.DayUtil;
import eu.ludiq.roostersgn.util.RefreshAnimation;
import eu.ludiq.roostersgn.util.TimeUtil;

/**
 * An activity for showing the timetable.
 * 
 * @author Ludo Pulles
 * 
 */
public class TimetableActivity extends Activity implements
		ViewFlipperManager.WeekLoader, MenuClickListener {

	private static final int REQUEST_CODE_SIGN_IN = 1;
	private static final int REQUEST_CODE_CHOOSE_DATE = 2;
	private static final String KEY_DAY = "day";

	private boolean mLoading;
	private TextView mChangesBar;
	private ViewFlipperManager mFlipper;
	private ImageView mRefreshButton;
	private HttpRequest mCurrentRequest;

	// ------------------------------------------------------------------------
	// *** ACTIVITY LIFECYCLE *************************************************
	// ------------------------------------------------------------------------

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setTitle("Rooster");
		stopService(new Intent(this, SyncService.class));
		setContentView(R.layout.timetable);

		if (getIntent() != null
				&& SignInActivity.ACTION_APP_START.equals(getIntent()
						.getAction())) {
			// make a fade in animation when the app starts
			overridePendingTransition(R.anim.fade_in_enter, R.anim.fade_in_exit);
		}

		// menu bar
		MenuView menuView = (MenuView) findViewById(R.id.menu_view);
		menuView.addItem(R.drawable.ic_action_search_small);
		mRefreshButton = menuView.addItem(R.drawable.ic_action_refresh_small);
		menuView.addItem(R.drawable.ic_action_date);
		menuView.setMenuItemListener(this);

		// connect dateview and viewflipper
		DateView dateView = (DateView) findViewById(R.id.date_view);
		ViewFlipperCompat viewFlipper = (ViewFlipperCompat) findViewById(R.id.view_flipper);
		this.mFlipper = new ViewFlipperManager(this, dateView, viewFlipper);

		this.mChangesBar = (TextView) findViewById(R.id.changes);

		Preferences preferences = DataStorage.readPreferences(this);
		Timetable timetable = DataStorage.readTimetable(this);

		if (preferences == null) {
			signIn(false);
		} else if (timetable == null) {
			loadTimetable(preferences, false, new DayCallback());
		} else {
			setTimetable(timetable);
			int week = timetable.getWeekOfYear();
			int day = savedState == null ? -1 : savedState.getInt(KEY_DAY, -1);
			// not saved or invalid input, use the default day
			if (!DayUtil.isWorkDay(day)) {
				day = TimeUtil.dayOfCurrentClass(timetable);
			}
			mFlipper.setInitialWeekAndDay(week, day);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Timetable timetable = DataStorage.readTimetable(this);
		if (timetable != null) {
			// refresh the timetable if the timetable is too old.
			long refreshTime = timetable.getContentType().getRefreshTime();
			if (refreshTime != -1) {
				long time = System.currentTimeMillis() / 1000
						- timetable.getTimestamp();
				if (refreshTime >= 0 && time > refreshTime) {
					refreshTimetable(false);
				}
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// save the day with focus
		outState.putInt(KEY_DAY, mFlipper.getDay());
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		// start the sync service
		if (isFinishing()) {
			Intent service = new Intent(this, SyncService.class);
			service.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startService(service);
		}
		super.onDestroy();
	}

	// ------------------------------------------------------------------------
	// *** EVENT LISTENERS ****************************************************
	// ------------------------------------------------------------------------

	public void onChangesClicked(View view) {
		Timetable timetable = DataStorage.readTimetable(this);
		if (timetable != null && timetable.hasChanges()) {
			String[] changes = timetable.getChanges();

			Intent intent = new Intent(this, ChangesActivity.class);
			intent.putExtra(ChangesActivity.EXTRA_CHANGES, changes);
			startActivity(intent);
			onActivityStarted();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		overridePendingTransition(R.anim.down_enter, R.anim.down_exit);

		if (requestCode == REQUEST_CODE_SIGN_IN) {
			Preferences preferences = DataStorage.readPreferences(this);
			if (resultCode == RESULT_OK) {
				loadTimetable(preferences, true, new DayCallback());
			} else if (resultCode == RESULT_CANCELED && preferences == null) {
				finish();
			}
		} else if (requestCode == REQUEST_CODE_CHOOSE_DATE) {
			if (resultCode == RESULT_OK) {
				Calendar cal = (Calendar) data
						.getSerializableExtra(DateActivity.EXTRA_CALENDAR);
				if (cal != null) {
					int week = TimeUtil.getWeekOfYear(cal);
					int day = DayUtil.toDayNumber(cal);

					if (DayUtil.isWeekend(day)) {
						day = DayUtil.MONDAY;
						week = TimeUtil.nextWeek(week);
					}

					if (week == mFlipper.getWeek()) {
						day = Math.min(day, mFlipper.numberOfPages() - 1);
						mFlipper.setDay(day);
					} else {
						loadDate(week, day);
					}
				}
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	// ------------------------------------------------------------------------
	// *** OTHER METHODS ******************************************************
	// ------------------------------------------------------------------------

	/**
	 * Overrides the default animation for when a new activity is started.
	 */
	private void onActivityStarted() {
		overridePendingTransition(R.anim.up_enter, R.anim.up_exit);
	}

	private boolean isLoading() {
		return mLoading;
	}

	private void startLoading() {
		mLoading = true;
		RefreshAnimation anim = new RefreshAnimation();
		mRefreshButton.startAnimation(anim);
		mRefreshButton.setEnabled(false);
	}

	private void stopLoading(boolean beenAborted) {
		mLoading = false;
		Animation anim = mRefreshButton.getAnimation();
		if (anim != null && anim instanceof RefreshAnimation) {
			if (beenAborted) {
				((RefreshAnimation) anim).abort();
			} else {
				((RefreshAnimation) anim).stop();
			}
		}
		mRefreshButton.setEnabled(true);
	}

	// ------------------------------------------------------------------------
	// *** MENU OPTIONS *******************************************************
	// ------------------------------------------------------------------------

	public void onMenuItemClicked(int index) {
		if (index == 0) {
			signIn(false);
		} else if (index == 1) {
			refreshTimetable(true);
		} else if (index == 2) {
			chooseDate();
		}
	}

	private void signIn(boolean hasWrongPassword) {
		Intent intent = new Intent(this, SignInActivity.class);
		intent.setAction(SignInActivity.ACTION_SIGN_IN);
		intent.putExtra(SignInActivity.EXTRA_WRONG_PASS, hasWrongPassword);
		startActivityForResult(intent, REQUEST_CODE_SIGN_IN);
		onActivityStarted();
	}

	private void refreshTimetable(boolean showException) {
		Preferences preferences = DataStorage.readPreferences(this);
		if (preferences != null) {
			loadTimetable(preferences, showException, new RefreshCallback());
		}
	}

	private void chooseDate() {
		Intent intent = new Intent(this, DateActivity.class);

		Calendar currentDate = TimeUtil.currentDate();
		DayUtil.setDayOfWeek(currentDate, mFlipper.getDay());
		TimeUtil.setWeekOfYear(currentDate, mFlipper.getWeek());

		intent.putExtra(DateActivity.EXTRA_CALENDAR, currentDate);
		startActivityForResult(intent, REQUEST_CODE_CHOOSE_DATE);
		onActivityStarted();
	}

	// ------------------------------------------------------------------------
	// *** TIMETABLE LOAD METHODS *********************************************
	// ------------------------------------------------------------------------

	private void loadTimetable(Preferences preferences,
			boolean showUserException, HttpCallback callback) {
		if (!isLoading()) {
			startLoading();

			TimetableRequest request = new TimetableRequest(this);
			mCurrentRequest = request;
			request.setPreferences(preferences);
			request.addCallback(new TimetableCallback(showUserException));
			request.addCallback(callback);
			request.request();
		}
	}

	public void loadWeek(int week, boolean forward) {
		Preferences preferences = DataStorage.readPreferences(this);
		if (preferences != null) {
			loadTimetable(preferences.setWeek(week), true,
					new WeekChangeCallback(forward));
		}
	}

	public void stopLoadingTimetable() {
		if (mCurrentRequest != null) {
			mCurrentRequest.stop();
		}
	}

	public boolean canLoadWeek() {
		return !mLoading;
	}

	private void loadDate(int week, int day) {
		Preferences preferences = DataStorage.readPreferences(this);
		if (preferences != null) {
			// loads a timetable and afterwards sets the day and week
			loadTimetable(preferences.setWeek(week), true, new DayCallback(day));
		}
	}

	// ------------------------------------------------------------------------
	// *** TIMETABLE METHODS **************************************************
	// ------------------------------------------------------------------------

	public void setTimetable(Timetable timetable) {
		mFlipper.setTimetable(timetable);
		setChanges(timetable.getChanges());
	}

	private void setChanges(String[] changes) {
		if (changes == null || changes.length < 1) {
			mChangesBar.setText("");
			mChangesBar.setVisibility(View.GONE);
		} else if (changes.length == 1) {
			mChangesBar.setText(getString(R.string.changes_singular));
			mChangesBar.setVisibility(View.VISIBLE);
		} else {
			mChangesBar.setText(String.format(
					getString(R.string.changes_plural), changes.length));
			mChangesBar.setVisibility(View.VISIBLE);
		}
	}

	// ------------------------------------------------------------------------
	// *** HTTP CALLBACKS *****************************************************
	// ------------------------------------------------------------------------

	public class TimetableCallback implements HttpCallback {

		private boolean mShowUserException;

		public TimetableCallback(boolean showUserException) {
			mShowUserException = showUserException;
		}

		@Override
		public void onLoaded(Timetable timetable) {
			stopLoading(false);
			setTimetable(timetable);
		}

		@Override
		public void onLoadingStopped() {
			stopLoading(true);
		}

		@Override
		public void handleError(int errorCode, Exception e) {
			stopLoading(false);
			if (errorCode == HttpUtils.ERROR_INTERNET) {
				showMessage(R.string.exception_internet);
			} else if (errorCode == HttpUtils.ERROR_PARSING) {
				showMessage(R.string.exception_json);
			} else if (errorCode == HttpUtils.ERROR_TIMETABLE_PARSE) {
				showMessage(R.string.exception_timetable);
			} else if (errorCode == HttpUtils.ERROR_SERVER_ERROR) {
				if (e instanceof ServerException) {
					showErrorMessage((ServerException) e);
				} else {
					showMessage(R.string.exception_server_unknown);
				}
			} else if (errorCode == HttpUtils.ERROR_WRONG_PASSWORD) {
				signIn(true);
			}
		}

		private void showMessage(int resId) {
			if (mShowUserException) {
				Toast.makeText(TimetableActivity.this, resId,
						Toast.LENGTH_SHORT).show();
			}
		}

		private void showErrorMessage(ServerException e) {
			if (e.isErrorKnown()) {
				int errorCode = e.getErrorCode();
				String errorMsg = e.getErrorMessage();

				String text = "De server gaf fout '" + errorCode + "' terug: "
						+ errorMsg;
				Toast.makeText(TimetableActivity.this, text, Toast.LENGTH_SHORT)
						.show();
			} else {
				showMessage(R.string.exception_server_unknown);
			}
		}
	}

	public class DayCallback implements HttpCallback {

		private int day;

		public DayCallback() {
			this(-1);
		}

		public DayCallback(int day) {
			this.day = day;
		}

		@Override
		public void onLoaded(Timetable timetable) {
			int day = this.day;
			if (!DayUtil.isWorkDay(day)) {
				day = TimeUtil.dayOfCurrentClass(timetable);
			}
			day = Math.min(day, mFlipper.numberOfPages() - 1);
			mFlipper.setWeekAndDay(timetable.getWeekOfYear(), day);
		}

		@Override
		public void handleError(int errorCode, Exception e) {
		}

		@Override
		public void onLoadingStopped() {
		}
	}

	public class RefreshCallback extends DayCallback {

		@Override
		public void onLoaded(Timetable timetable) {
			if (timetable.getWeekOfYear() != mFlipper.getWeek()) {
				super.onLoaded(timetable);
			}
		}
	}

	public class WeekChangeCallback implements HttpCallback {

		private boolean forward;

		public WeekChangeCallback(boolean forward) {
			this.forward = forward;
		}

		@Override
		public void onLoaded(Timetable timetable) {
			if (forward) {
				mFlipper.setWeekAndDay(timetable.getWeekOfYear(),
						DayUtil.MONDAY);
			} else {
				mFlipper.setWeekAndDay(timetable.getWeekOfYear(),
						mFlipper.numberOfPages() - 1);
			}
		}

		@Override
		public void handleError(int errorCode, Exception e) {
			if (forward) {
				mFlipper.setDay(mFlipper.numberOfPages() - 1);
			} else {
				mFlipper.setDay(DayUtil.MONDAY);
			}
		}

		@Override
		public void onLoadingStopped() {
		}
	}
}
