package eu.ludiq.sgn.rooster.scroll;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * A view containing multiple pages. You can scroll from one page to an other by
 * scrolling or by input ({@link #snapToScreen(int)} and
 * {@link #setToScreen(int)}).
 * 
 * @author Ludo Pulles
 * 
 */
public class FlingAndScrollViewer extends ViewGroup {

	/**
	 * if there is no pointer on the screen, this value is used
	 * 
	 * @see mActivePointerId
	 */
	private static final int INVALID_POINTER_ID = -1;
	/**
	 * the velocity needed to go to a next page if it's not the nearest
	 * possibility
	 * 
	 * @see mVelocityTracker
	 */
	protected final static int SNAP_VELOCITY = 250;
	private final static int SCROLL_DURATION = 625;
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;

	/** used for scroll to a specific x (absolute) or move (relative) */
	private Scroller mScroller;
	/** used for calculating the speed of the motion of the user */
	private VelocityTracker mVelocityTracker;

	/** the screen we are showing to the user */
	protected int mCurrentScreen = 0;
	/** the last position on the screen */
	protected float mLastMotionX;
	/** the current pointer whose motion events we handle */
	protected int mActivePointerId = INVALID_POINTER_ID;
	protected int mTouchState = TOUCH_STATE_REST;
	/** Distance for a touch before it is scrolling */
	protected int mTouchSlop = 0;

	public FlingAndScrollViewer(Context context) {
		super(context);
		init(context);
	}

	public FlingAndScrollViewer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FlingAndScrollViewer(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	protected void init(Context context) {
		mScroller = new Scroller(context);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	public void setInitialPosition(int initialPosition) {
		mCurrentScreen = initialPosition;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		/*
		 * This method JUST determines whether we want to intercept the motion.
		 * If we return true, onTouchEvent will be called and we do the actual
		 * scrolling there.
		 */
		/*
		 * Shortcut the most recurring case: the user is in the dragging state
		 * and he is moving his finger. We want to intercept this motion.
		 */
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
		final float x = ev.getX();
		switch (action) {
		case MotionEvent.ACTION_MOVE:
			/*
			 * mIsBeingDragged == false, otherwise the shortcut would have
			 * caught it. Check whether the user has moved far enough from his
			 * original down touch.
			 */
			/*
			 * Locally do absolute value. mLastMotionX is set to the y value of
			 * the down event.
			 */
			final int xDiff = (int) Math.abs(x - mLastMotionX);
			boolean xMoved = xDiff > mTouchSlop;
			if (xMoved) {
				// Scroll if the user moved far enough along the X axis
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			break;
		case MotionEvent.ACTION_DOWN:
			// Remember location of down touch
			mLastMotionX = x;
			/*
			 * If being flinged and user touches the screen, initiate drag;
			 * otherwise don't. mScroller.isFinished should be false when being
			 * flinged.
			 */
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			// Release the drag
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		/*
		 * The only time we want to intercept motion events is if we are in the
		 * drag mode.
		 */
		return mTouchState != TOUCH_STATE_REST;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		// multitouch:
		final int action = event.getAction() & MotionEvent.ACTION_MASK;
		// if one finger starts to touch the screen
		if (action == MotionEvent.ACTION_DOWN) {
			// this view doesn't recieve action_down events because of
			// onInterceptTouchEvent

			// If being flinged and user touches, stop the fling. isFinished
			// will be false if being flinged.
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			// Remember where the motion event started
			mLastMotionX = event.getX();

			// Save the ID of this pointer
			mActivePointerId = event.getPointerId(0);

		} else if (action == MotionEvent.ACTION_MOVE) {
			// if action_down wasn't called so we didn't set an active pointer,
			// we still can do that here:
			if (mActivePointerId == INVALID_POINTER_ID) {
				mActivePointerId = event.getPointerId(0);
			}

			// Scroll to follow the motion event
			int pointerIndex = event.findPointerIndex(mActivePointerId);
			float x = pointerIndex == -1 ? event.getX() : event
					.getX(pointerIndex);
			int deltaX = (int) (mLastMotionX - x);
			mLastMotionX = x;
			onMotionMove(deltaX);

		} else if (action == MotionEvent.ACTION_UP) {
			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000);

			onMotionUp((int) velocityTracker.getXVelocity());

			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			mTouchState = TOUCH_STATE_REST;
			mActivePointerId = INVALID_POINTER_ID;

		} else if (action == MotionEvent.ACTION_CANCEL) {
			mTouchState = TOUCH_STATE_REST;
			mActivePointerId = INVALID_POINTER_ID;

		} else if (action == MotionEvent.ACTION_POINTER_UP) {
			int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			// our active pointer is gone! Quickly, find a new one!
			if (event.getPointerId(pointerIndex) == mActivePointerId) {
				// This was our active pointer going up. Choose a new
				// active pointer and adjust accordingly.
				pointerIndex = pointerIndex == 0 ? 1 : 0;
				mLastMotionX = event.getX(pointerIndex);
				mActivePointerId = event.getPointerId(pointerIndex);
			}
		}
		return true;
	}

	protected void onMotionUp(int velocityX) {
		if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
			// Fling hard enough to move left
			snapToScreen(mCurrentScreen - 1);
		} else if (velocityX < -SNAP_VELOCITY
				&& mCurrentScreen < getChildCount() - 1) {
			// Fling hard enough to move right
			snapToScreen(mCurrentScreen + 1);
		} else {
			snapToDestination();
		}
	}

	protected void onMotionMove(int deltaX) {
		if (deltaX < 0) {
			if (getScrollX() > 0) {
				scrollBy(Math.max(-getScrollX(), deltaX), 0);
			}
		} else if (deltaX > 0) {
			int availableToScroll = getChildAt(getChildCount() - 1).getRight()
					- getScrollX() - getWidth();
			if (availableToScroll > 0) {
				scrollBy(Math.min(availableToScroll, deltaX), 0);
			}
		}
	}

	protected void snapToDestination() {
		snapToScreen(Math.round(((float) getScrollX()) / getWidth()));
	}

	public void snapToScreen(int screen) {
		int previousScreen = mCurrentScreen;
		mCurrentScreen = screen;

		final int distance = screen * getWidth() - getScrollX();
		mScroller.startScroll(getScrollX(), 0, distance, 0,
				Math.min(Math.abs(distance * 2), SCROLL_DURATION));
		invalidate();
		onScreenChanged(previousScreen);
	}

	public void setToScreen(int whichScreen) {
		int previousScreen = mCurrentScreen;
		mCurrentScreen = whichScreen;
		mScroller.startScroll(whichScreen * getWidth(), 0, 0, 0, 10);
		invalidate();
		onScreenChanged(previousScreen);
	}

	protected void onScreenChanged(int previousScreen) {
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth,
						child.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY
				|| heightMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException("error mode.");
		}
		// The children are given the same width and height as the workspace
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
		scrollTo(mCurrentScreen * width, 0);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), 0);
			postInvalidate();
		}
	}
}
