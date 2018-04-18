package eu.ludiq.roostersgn.scroll;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.TextView;
import eu.ludiq.roostersgn.exception.NoTimetableException;
import eu.ludiq.roostersgn.rooster.ContentType;
import eu.ludiq.roostersgn.rooster.Timetable;
import eu.ludiq.roostersgn.rooster.Week;
import eu.ludiq.roostersgn.ui.DayAdapter;
import eu.ludiq.roostersgn.ui.ViewFlipperManager;

/**
 * An extended version of {@link FlingAndScrollViewer}
 * 
 * @author Ludo Pulles
 * 
 */
public class ViewFlipperCompat extends FlingAndScrollViewer implements
		ViewFlipperManager.FlipperComponent {

	private Paint mLinePaint;
	private ViewFlipperManager mListener;
	private int mLastScrollX = -1;

	public ViewFlipperCompat(Context context) {
		super(context);
	}

	public ViewFlipperCompat(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ViewFlipperCompat(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void init(Context context) {
		super.init(context);
		setWillNotDraw(false);
		mLinePaint = new Paint();
		mLinePaint.setColor(0x11000000);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (mListener != null && mLastScrollX != getScrollX()) {
			float percentage = (float) getScrollX() / getWidth();
			int page = Math.max(0,
					Math.min(Math.round(percentage), getChildCount() - 1));
			mListener.setScrollPosition(new ScrollPosition(page, percentage
					- page));
			mLastScrollX = getScrollX();
		}
		super.dispatchDraw(canvas);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final int height = getHeight(), childCount = getChildCount();
		if (height > 0 && childCount > 0) {
			canvas.drawLine(-0.5f, 0, -0.5f, height, mLinePaint);
			for (int i = 0; i < childCount; i++) {
				float x = getChildAt(i).getRight() - 0.5f;
				canvas.drawLine(x, 0, x, height, mLinePaint);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mListener == null || mListener.canScroll())
			super.onTouchEvent(event);
		return true;
	}

	@Override
	protected void onMotionUp(int velocityX) {
		// Fling hard enough to move left or right
		if (velocityX > SNAP_VELOCITY && mCurrentScreen >= 0) {
			snapToScreen(mCurrentScreen - 1);
		} else if (velocityX < -SNAP_VELOCITY
				&& mCurrentScreen < getChildCount()) {
			snapToScreen(mCurrentScreen + 1);
		} else {
			snapToDestination();
		}
	}

	@Override
	protected void onMotionMove(int dx) {
		int mScrollX = getScrollX();
		if (dx < 0) {
			int left = -mScrollX - getWidth();
			if (left < 0)
				scrollBy(Math.max(left, dx), 0);
		} else if (dx > 0) {
			int right = getChildAt(getChildCount() - 1).getRight() - mScrollX;
			if (right > 0)
				scrollBy(Math.min(right, dx), 0);
		}
	}

	@Override
	protected void onScreenChanged(int previousScreen) {
		if (mListener != null) {
			if (mCurrentScreen < 0) {
				mListener.previousWeek();
			} else if (mCurrentScreen >= getChildCount()) {
				mListener.nextWeek();
			} else if ((previousScreen == -1 || previousScreen == getChildCount())
					&& 0 <= mCurrentScreen && mCurrentScreen < getChildCount()) {
				mListener.stopLoading();
			}
		}
	}

	public void setListener(ViewFlipperManager listener) {
		this.mListener = listener;
	}

	public void setInitialDay(int day) {
		setInitialPosition(day);
	}

	public void setDay(int day) {
		setToScreen(day);
	}

	public void scrollLeft() {
		snapToScreen(this.mCurrentScreen - 1);
	}

	public void scrollRight() {
		snapToScreen(this.mCurrentScreen + 1);
	}

	public int numberOfPages() {
		return getChildCount();
	}

	@SuppressWarnings("deprecation")
	public void setTimetable(Timetable timetable) {
		ContentType contentType = timetable.getContentType();
		if (contentType == null) {
			return;
		}
		removeAllViews();
		LayoutParams fillParams = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		try {
			Week week = timetable.getWeek();
			for (int i = 0; i < week.length(); i++) {
				ListView lv = new ListView(getContext());
				lv.setAdapter(new DayAdapter(getContext(), week.getDay(i)));
				addView(lv, fillParams);
			}
		} catch (NoTimetableException e) {
			// exception caused by week
			TextView msgView = new TextView(getContext());
			msgView.setGravity(Gravity.CENTER);
			msgView.setText(contentType.getDescription());
			addView(msgView, fillParams);
		}
	}
}
