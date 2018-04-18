package eu.ludiq.roostersgn.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * A Spinner like widget used with calendars.
 * 
 * @author Ludo Pulles
 * 
 */
public class AbstractSpinner extends ViewGroup {

	protected boolean mIncreasePressed = false, mDecreasePressed = false;
	protected SpinnerListener mListener;

	private RectF mTop, mBottom;
	private Paint mFill, mFillPressed, mText, mStroke;

	public AbstractSpinner(Context context) {
		super(context);
		init();
	}

	public AbstractSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setWillNotDraw(false);

		mFill = new Paint();
		mFillPressed = new Paint();
		mStroke = new Paint();

		mFill.setColor(0xffeeeeee);
		mFillPressed.setColor(0xffdddddd);
		mStroke.setColor(0xcccccc);
		mStroke.setStyle(Style.STROKE);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int width = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 96, metrics);
		int maxWidth = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 128, metrics);
		int height = width;
		int maxHeight = maxWidth;

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		if (widthMode == MeasureSpec.EXACTLY) {
			// Must be this size
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			// Can't be bigger than...
			width = Math.min(Math.min(width, widthSize), maxWidth);
		}

		if (heightMode == MeasureSpec.EXACTLY) {
			// Must be this size
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			// Can't be bigger than...
			height = Math.min(Math.min(width, heightSize), maxHeight);
		} else {
			// Be whatever you want
			height = Math.min(width, maxHeight);
		}

		setMeasuredDimension(width, height);

		int widthChildSpec = MeasureSpec.makeMeasureSpec(width,
				MeasureSpec.EXACTLY);
		int heightChildSpec = MeasureSpec.makeMeasureSpec(height,
				MeasureSpec.AT_MOST);
		for (int i = 0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			v.measure(widthChildSpec, heightChildSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int w = r - l, h = b - t;
		for (int i = 0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			int mh = v.getMeasuredHeight();
			if (mh > 0) {
				int padding = h / 4 - Math.min(h / 4, mh / 2);
				v.setPadding(0, padding, 0, padding);
			}
			v.layout(0, h / 4, w, h * 3 / 4);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mTop = new RectF(0, 0, w - 1, h / 4 - 1);
		mBottom = new RectF(0, h * 3 / 4, w - 1, h - 1);
		mText = new Paint(Paint.ANTI_ALIAS_FLAG);
		mText.setColor(0xFF151515);
		mText.setTextAlign(Align.CENTER);
		mText.setTextSize(h / 8);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		float y = event.getY();
		float yBorderTop = getHeight() / 4;
		float yBorderBottom = getHeight() * 3 / 4;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (y < yBorderTop) {
				mIncreasePressed = true;
				invalidate();
			} else if (y > yBorderBottom) {
				mDecreasePressed = true;
				invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mIncreasePressed && y >= yBorderTop) {
				mIncreasePressed = false;
				invalidate();
			} else if (mDecreasePressed && y >= yBorderBottom) {
				mDecreasePressed = false;
				invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			if (mListener != null) {
				if (mIncreasePressed && y < yBorderTop) {
					mListener.onPressed(true);
				} else if (mDecreasePressed && y > yBorderBottom) {
					mListener.onPressed(false);
				}
			}
			mDecreasePressed = mIncreasePressed = false;
			invalidate();
			break;
		}
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(mTop, mIncreasePressed ? mFillPressed : mFill);
		canvas.drawRect(mTop, mStroke);
		canvas.drawRect(mBottom, mDecreasePressed ? mFillPressed : mFill);
		canvas.drawRect(mBottom, mStroke);

		drawText(canvas, mTop, "+", mText);
		drawText(canvas, mBottom, "-", mText);
	}

	public void setListener(SpinnerListener listener) {
		this.mListener = listener;
	}

	private void drawText(Canvas canvas, RectF area, String text, Paint paint) {
		float cx = area.centerX(), cy = area.centerY();
		float baseline = cy - paint.getFontMetrics().ascent * 0.45f;
		canvas.drawText(text, cx, baseline, paint);
	}

	public interface SpinnerListener {

		public void onPressed(boolean increase);

	}

}
