package eu.ludiq.roostersgn;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import eu.ludiq.roostersgn.ui.AbstractSpinner;
import eu.ludiq.roostersgn.ui.AbstractSpinner.SpinnerListener;
import eu.ludiq.roostersgn.util.DayUtil;
import eu.ludiq.roostersgn.util.TimeUtil;

/**
 * An activity that lets the user choose see a specific date of the timetable.
 * 
 * @author Ludo Pulles
 * 
 */
public class DateActivity extends Activity {

	public static String EXTRA_CALENDAR = "calendar";

	private String[] mDays;
	private String[] mMonths;
	private String[] mMonthsShort;

	private EditText mDayEditText;
	private EditText mMonthEditText;
	private TextView mDescription;

	private Calendar mCalendar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Dag kiezen");
		setContentView(R.layout.choose_date);

		Resources res = getResources();
		mDays = res.getStringArray(R.array.days);
		mMonths = res.getStringArray(R.array.months);
		mMonthsShort = res.getStringArray(R.array.months_short);

		mDescription = (TextView) findViewById(R.id.description);
		mDayEditText = (EditText) findViewById(R.id.day_edit_text);
		mMonthEditText = (EditText) findViewById(R.id.month_edit_text);

		((AbstractSpinner) findViewById(R.id.day_spinner))
				.setListener(new SpinnerListener() {

					public void onPressed(boolean increase) {
						addDay(increase);
					}
				});
		((AbstractSpinner) findViewById(R.id.month_spinner))
				.setListener(new SpinnerListener() {

					public void onPressed(boolean increase) {
						addMonth(increase);
					}
				});

		mDayEditText.addTextChangedListener(new DayWatcher());
		mMonthEditText.addTextChangedListener(new MonthWatcher());

		setCalendar((Calendar) getIntent().getSerializableExtra(EXTRA_CALENDAR));
	}

	// ------------------------------------------------------------------------
	// *** EVENTS *************************************************************
	// ------------------------------------------------------------------------

	public void buttonOK(View view) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_CALENDAR, this.mCalendar);
		setResult(RESULT_OK, intent);
		finish();
	}

	public void buttonVandaag(View view) {
		setCalendar(TimeUtil.currentDate());
	}

	public void buttonCancel(View view) {
		setResult(RESULT_CANCELED);
		finish();
	}

	private void addDay(boolean increase) {
		mCalendar.add(Calendar.DAY_OF_MONTH, increase ? 1 : -1);
		syncYear();
		update();
	}

	private void addMonth(boolean increase) {
		mCalendar.add(Calendar.MONTH, increase ? 1 : -1);
		syncYear();
		update();
	}

	// ------------------------------------------------------------------------
	// *** HELP METHODS *******************************************************
	// ------------------------------------------------------------------------

	public void setCalendar(Calendar calendar) {
		if (calendar == null) {
			calendar = TimeUtil.currentDate();
		}
		this.mCalendar = calendar;
		syncYear();
		update();
	}

	/**
	 * This prevents that the calendar won't use an other year than the current
	 * year.
	 */
	private void syncYear() {
		TimeUtil.setThisSchoolYear(mCalendar);
	}

	private void updateIfChanged(EditText editText, String text) {
		if (!editText.getText().toString().equalsIgnoreCase(text)) {
			editText.setText(text);
		}
	}

	private void update() {
		int dayOfWeek = DayUtil.toDayNumber(mCalendar);
		int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
		int month = mCalendar.get(Calendar.MONTH);
		int year = mCalendar.get(Calendar.YEAR);

		mDescription.setText(mDays[dayOfWeek] + " " + dayOfMonth + " "
				+ mMonths[month] + " " + year);

		updateIfChanged(mDayEditText, Integer.toString(dayOfMonth));
		updateIfChanged(mMonthEditText, mMonthsShort[month]);
	}

	// ------------------------------------------------------------------------
	// *** CLASSES ************************************************************
	// ------------------------------------------------------------------------

	private class DayWatcher implements TextWatcher {

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			int highestDayInMonth = mCalendar
					.getActualMaximum(Calendar.DAY_OF_MONTH);
			try {
				int value = Integer.parseInt(s.toString());
				if (value > highestDayInMonth) {
					value = highestDayInMonth;
				} else if (value < 1) {
					value = 1;
				}
				mCalendar.set(Calendar.DAY_OF_MONTH, value);
				update();
			} catch (NumberFormatException nfe) {
			}
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void afterTextChanged(Editable s) {
		}
	}

	private class MonthWatcher implements TextWatcher {

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			for (int i = 0; i < mMonthsShort.length; i++) {
				if (mMonthsShort[i].equalsIgnoreCase(s.toString())) {
					mCalendar.set(Calendar.MONTH, i);
					update();
				}
			}
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void afterTextChanged(Editable s) {
		}
	}
}
