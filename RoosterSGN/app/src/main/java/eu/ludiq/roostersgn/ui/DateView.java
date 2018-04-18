package eu.ludiq.roostersgn.ui;

import java.util.Calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import eu.ludiq.roostersgn.R;
import eu.ludiq.roostersgn.rooster.ContentType;
import eu.ludiq.roostersgn.rooster.Timetable;
import eu.ludiq.roostersgn.scroll.ScrollPosition;
import eu.ludiq.roostersgn.ui.ViewFlipperManager.DateComponent;
import eu.ludiq.roostersgn.util.DayUtil;
import eu.ludiq.roostersgn.util.TimeUtil;

/**
 * A View containing the day, the date and the week of the year.
 * 
 * @author Ludo Pulles
 * 
 */
public class DateView extends View implements DateComponent {

	private int mWeek = -1, mDay = -1;
	private float mPercentage = 0;
	private int mSidePressed = 0;
	private String[] mMonths = null;
	private ViewFlipperManager mListener;
	private ContentType mContentType = null;

	private Paint mOrange, mBlue, mPressed;
	private Paint mTextOrange, mTextDate, mTextToday, mTextOtherDay;
	private Text mDate, mToday;
	private Shape mLeft = null, mRight = null;

	public DateView(Context context) {
		super(context);
		init(context);
	}

	public DateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DateView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mOrange = new Paint();
		mBlue = new Paint();
		mPressed = new Paint();

		mTextOrange = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextDate = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextToday = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextOtherDay = new Paint(Paint.ANTI_ALIAS_FLAG);

		Resources res = context.getResources();

		mOrange.setColor(res.getColor(R.color.sgn_orange));
		mBlue.setColor(res.getColor(R.color.sgn_blue));
		mPressed.setColor(res.getColor(R.color.sgn_light_blue));

		mTextOrange.setColor(res.getColor(R.color.sgn_orange));
		mTextDate.setColor(Color.rgb(34, 34, 34));
		mTextToday.setColor(res.getColor(R.color.sgn_orange));
		mTextOtherDay.setColor(Color.WHITE);

		mDate = new Text("", mTextDate);
		mToday = new Text("", mTextToday);
	}

	// ------------------------------------------------------------------------
	// *** ACTIVITY EVENTS ****************************************************
	// ------------------------------------------------------------------------

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		final float pixelTextsizeRatio = 0.6f;
		mTextDate.setTextSize(getDateHeight() * pixelTextsizeRatio);
		mTextToday.setTextSize(getDayHeight() * pixelTextsizeRatio);
		mTextOtherDay.setTextSize(getDayHeight() * pixelTextsizeRatio);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
			int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
			int measuredHeight = (int) (TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_SP, 60, getResources()
							.getDisplayMetrics()));
			setMeasuredDimension(measuredWidth, measuredHeight);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mListener != null && !mListener.canScroll()) {
			return true;
		}
		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			mSidePressed = getSidePressed(event.getX());
			invalidate();
		} else if (action == MotionEvent.ACTION_MOVE) {
			int side = getSidePressed(event.getX());
			if (side != mSidePressed) {
				mSidePressed = 0;
				invalidate();
			}
		} else if (action == MotionEvent.ACTION_UP) {
			if (mSidePressed > 0) {
				mListener.goRight();
			} else if (mSidePressed < 0) {
				mListener.goLeft();
			}
			mSidePressed = 0;
			invalidate();
		} else if (action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_OUTSIDE) {
			mSidePressed = 0;
			invalidate();
		} else {
			return super.onTouchEvent(event);
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int w = getWidth(), h = getHeight();
		float hDate = getDateHeight(), hDay = getDayHeight();

		canvas.drawRect(0, 0, w, hDate, mOrange);
		canvas.drawRect(0, hDate, w, h, mBlue);

		if (mSidePressed < 0) {
			canvas.drawRect(0, hDate, w / 3, h, mPressed);
		} else if (mSidePressed > 0) {
			canvas.drawRect(w * 2 / 3, hDate, w, h, mPressed);
		}

		mDate.draw(canvas, w / 2, hDate / 2);

		// the padding on left and right side where the text will stop
		float padding = (hDay - mToday.getHeight()) * .5f;
		// the minimum position for today so it won't be out of the screen
		float limit = padding + mToday.getWidth() * .5f;

		// positioning of the text for today
		float todayX = w * (0.5f - mPercentage);
		if (todayX < limit) {
			todayX = limit;
		} else if (todayX > w - limit) {
			todayX = w - limit;
		}

		float todayY = hDate + hDay * .5f;
		mToday.draw(canvas, todayX, todayY);

		if (mLeft != null) {
			float min = padding + mLeft.getWidth() / 2;
			float max = todayX - mToday.getWidth() / 2 - min;
			float pos = -w * (0.5f + mPercentage);

			pos = Math.max(pos, min);
			pos = Math.min(pos, max);
			mLeft.draw(canvas, pos, todayY);
		}

		if (mRight != null) {
			float max = w - padding - mRight.getWidth() / 2;
			float min = todayX + mToday.getWidth() / 2 + w - max;
			float pos = w * (1.5f - mPercentage);

			pos = Math.min(pos, max);
			pos = Math.max(pos, min);
			mRight.draw(canvas, pos, todayY);
		}
	}

	// ------------------------------------------------------------------------
	// *** OTHER METHODS ******************************************************
	// ------------------------------------------------------------------------

	private float getDateHeight() {
		return getHeight() * 0.4f;
	}

	private float getDayHeight() {
		return getHeight() - getDateHeight();
	}

	private String[] getMonths() {
		if (this.mMonths == null) {
			this.mMonths = getResources().getStringArray(R.array.months_short);
		}
		return this.mMonths;
	}

	private int getSidePressed(float x) {
		if (x <= getWidth() / 3) {
			return -1;
		}
		return (x >= getWidth() * 2 / 3) ? 1 : 0;
	}

	public void setListener(ViewFlipperManager listener) {
		this.mListener = listener;
	}

	public void setDay(int day) {
		String[] dayNames = getResources().getStringArray(R.array.days_short);
		this.mDay = day;
		updateDate();
		boolean unusual = mContentType == ContentType.NO_TIMETABLE;

		if (day <= DayUtil.MONDAY || unusual) {
			mLeft = new Triangle(true);
		} else {
			mLeft = new Text(dayNames[day - 1], mTextOtherDay);
		}

		if (day >= DayUtil.FRIDAY || unusual) {
			mRight = new Triangle(false);
		} else {
			mRight = new Text(dayNames[day + 1], mTextOtherDay);
		}

		if (mContentType == ContentType.NO_TIMETABLE) {
			mToday.text = "week " + mWeek;
		} else {
			mToday.text = dayNames[day];
		}

		invalidate();
	}

	public void setWeek(int week) {
		this.mWeek = week;
		updateDate();
		invalidate();
	}

	public void setTimetable(Timetable timetable) {
		this.mContentType = timetable.getContentType();
		this.mWeek = timetable.getWeekOfYear();
		updateDate();
		invalidate();
	}

	public void setScrollPosition(ScrollPosition position) {
		this.mPercentage = position.percentage;
		if (position.page != mDay) {
			setDay(position.page);
		}
		invalidate();
	}

	private void updateDate() {
		Calendar date = TimeUtil.currentDate();
		if (TimeUtil.isValidWeek(mWeek)) {
			TimeUtil.setWeekOfYear(date, mWeek);
		}

		String[] months = getMonths();
		if (mContentType == ContentType.NO_TIMETABLE) {
			date.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			int monthFrom = date.get(Calendar.MONTH);
			int dayFrom = date.get(Calendar.DAY_OF_MONTH);

			date.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
			int monthTill = date.get(Calendar.MONTH);
			int dayTill = date.get(Calendar.DAY_OF_MONTH);

			if (monthFrom == monthTill) {
				mDate.text = dayFrom + " - " + dayTill + " "
						+ months[monthFrom];
			} else {
				mDate.text = dayFrom + " " + months[monthFrom] + " - "
						+ dayTill + " " + months[monthTill];
			}
		} else {
			date.set(Calendar.DAY_OF_WEEK, DayUtil.toCalendar(mDay));

			String month = months[date.get(Calendar.MONTH)];
			int day = date.get(Calendar.DAY_OF_MONTH);
			mDate.text = day + " " + month;
		}
	}

	// ------------------------------------------------------------------------
	// *** CLASSES ************************************************************
	// ------------------------------------------------------------------------

	private interface Shape {

		public float getHeight();

		public float getWidth();

		public void draw(Canvas canvas, float cx, float cy);
	}

	private class Triangle implements Shape {

		private final boolean left;

		public Triangle(boolean left) {
			this.left = left;
		}

		public float getHeight() {
			return (getDayHeight() + mToday.getHeight()) * .45f;
		}

		public float getWidth() {
			return getHeight() * 1.1f;
		}

		public void draw(Canvas canvas, float cx, float cy) {
			float width = getWidth() / (left ? -2 : 2);
			float height = getHeight() / (left ? -2 : 2);

			Path triangle = new Path();
			// puntje
			triangle.moveTo(cx + width, cy);
			// onderkant
			triangle.lineTo(cx - width, cy + height);
			// bovenkant
			triangle.lineTo(cx - width, cy - height);
			triangle.close();
			canvas.drawPath(triangle, mOrange);
		}

	}

	private class Text implements Shape {

		private String text;
		private final Paint paint;

		public Text(String text, Paint paint) {
			this.text = text;
			this.paint = paint;
		}

		public void draw(Canvas canvas, float cx, float cy) {
			Align align = paint.getTextAlign();
			if (align == Align.LEFT) {
				cx -= getWidth() / 2f;
			} else if (align == Align.RIGHT) {
				cx += getWidth() / 2f;
			}
			canvas.drawText(text, cx, cy + getHeight() / 2f + 1, paint);
		}

		public float getHeight() {
			return -paint.ascent() - paint.descent();
		}

		public float getWidth() {
			return paint.measureText(text);
		}

	}

}
