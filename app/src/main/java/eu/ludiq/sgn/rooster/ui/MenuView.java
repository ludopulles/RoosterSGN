package eu.ludiq.sgn.rooster.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import eu.ludiq.sgn.rooster.R;

/**
 * A view for menu buttons with a grid layout and a focus color.
 * 
 * @author Ludo Pulles
 * 
 */
public class MenuView extends ViewGroup {

	private Paint mPaintPressed, mPaintBorder2, mPaintBorder1;
	private int mSelectedChild = -1;
	private MenuClickListener menuItemListener;

	public MenuView(Context context) {
		super(context);
		init(context);
	}

	public MenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MenuView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		setWillNotDraw(false);
		Resources res = context.getResources();
		mPaintPressed = new Paint();
		mPaintPressed.setColor(res.getColor(R.color.menu_button_active));
		mPaintBorder2 = new Paint();
		mPaintBorder2.setColor(res.getColor(R.color.menu_border_bottom2));
		mPaintBorder1 = new Paint();
		mPaintBorder1.setColor(res.getColor(R.color.menu_border_bottom1));
	}

	// ------------------------------------------------------------------------
	// *** ACTIVITY EVENTS ****************************************************
	// ------------------------------------------------------------------------

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		float x = event.getX(), y = event.getY();
		if (action == MotionEvent.ACTION_DOWN) {
			int index = getChildAt(x, y);
			if (index != -1) {
				mSelectedChild = index;
				invalidate();
			}
		} else if (action == MotionEvent.ACTION_MOVE) {
			int index = getChildAt(x, y);
			if (mSelectedChild != -1 && index != mSelectedChild) {
				mSelectedChild = -1;
				invalidate();
			}
		} else if (action == MotionEvent.ACTION_UP) {
			if (mSelectedChild != -1) {
				performMenuClick(mSelectedChild);
				mSelectedChild = -1;
				invalidate();
			}
		} else if (action == MotionEvent.ACTION_CANCEL
				|| action == MotionEvent.ACTION_OUTSIDE) {
			if (mSelectedChild != -1) {
				mSelectedChild = -1;
				invalidate();
			}
		} else {
			return super.onTouchEvent(event);
		}
		return true;
	}

	/**
	 * MeasureSpec.EXACTLY: has to be this size; MeasureSpec.AT_MOST: as big as
	 * you want up to a this size; MeasureSpec.UNSPECIFIED: as big as you like
	 * want;
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int totalWidth = 0;
		int biggestHeight = 0;
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			measureChild(child, widthMeasureSpec, heightMeasureSpec);
			totalWidth += child.getMeasuredWidth();
			biggestHeight = Math.max(biggestHeight, child.getMeasuredHeight());
		}
		int width = totalWidth;
		int height = biggestHeight;

		if (widthMode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			width = Math.min(width, widthSize);
		}

		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			height = Math.min(height, heightSize);
		}

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed) {
			int w = r - l, h = b - t;
			for (int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				if (child.getVisibility() != View.GONE) {
					child.layout(i * w / getChildCount(), 0, (i + 1) * w
							/ getChildCount(), h);
				}
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int h = getHeight(), w = getWidth();
		canvas.drawLine(0, h - 2, w, h - 2, mPaintBorder2);
		canvas.drawLine(0, h - 1, w, h - 1, mPaintBorder1);
		if (mSelectedChild != -1) {
			View v = getChildAt(mSelectedChild);
			int l = v.getLeft(), r = v.getRight(), t = v.getTop(), b = v
					.getBottom();
			canvas.drawRect(l, t, r, b, mPaintPressed);
		}
	}

	// ------------------------------------------------------------------------
	// *** OTHER METHODS ******************************************************
	// ------------------------------------------------------------------------

	public ImageView addItem(int resId) {
		ImageView item = new ImageView(getContext());
		item.setImageResource(resId);
		int padding = Math.round(TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 6, getResources()
						.getDisplayMetrics()));
		item.setPadding(padding, padding, padding, padding);
		item.setClickable(false);
		addView(item);
		return item;
	}

	private int getChildAt(float x, float y) {
		for (int i = 0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			if (v.getLeft() <= x && x <= v.getRight() && v.getTop() <= y
					&& y <= v.getBottom()) {
				return i;
			}
		}
		return -1;
	}

	public void setMenuItemListener(MenuClickListener menuItemListener) {
		this.menuItemListener = menuItemListener;
	}

	private void performMenuClick(int index) {
		if (this.menuItemListener != null) {
			this.menuItemListener.onMenuItemClicked(index);
		}
	}

	// ------------------------------------------------------------------------
	// *** CLASSES ************************************************************
	// ------------------------------------------------------------------------

	public static interface MenuClickListener {

		public void onMenuItemClicked(int index);

	}
}
