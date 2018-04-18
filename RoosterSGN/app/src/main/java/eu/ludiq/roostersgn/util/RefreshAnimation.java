package eu.ludiq.roostersgn.util;

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class RefreshAnimation extends Animation {

	private float mPivotX = 0.0f;
	private float mPivotY = 0.0f;

	private boolean mStop = false;
	private boolean mStopped = false;
	private boolean mGoBack = false;

	private long mStartTime = -1;
	private long mLastTime = -1;

	private float mAbortTime = -1f;
	private float mLastRotation = -1f;

	public RefreshAnimation() {
		super();
		setRepeatCount(INFINITE);
	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		// relative to self
		mPivotX = 0.5f * width;
		mPivotY = 0.5f * height;
	}

	/**
	 * This will stop the animation, causing it to rotate further to the
	 * beginning position. If it is reached, the animation stops.
	 */
	public void stop() {
		mStop = true;
		mAbortTime = mLastTime;
		mGoBack = false;
	}

	/**
	 * This will abort the animation, causing it to rotate back to the beginning
	 * position. If it is reached, the animation stops.
	 */
	public void abort() {
		mStop = true;
		mAbortTime = mLastTime;
		mGoBack = true;
	}

	public float mod(float a, float m) {
		// a mod m = a - m * (a div m)
		return (float) (a - m * Math.floor(a / m));
	}

	@Override
	public boolean getTransformation(long currentTime,
			Transformation outTransformation) {
		if (mStopped) {
			// don't rotate anymore, because we are done
			outTransformation.getMatrix().setRotate(mLastRotation, mPivotX,
					mPivotY);
			return false;
		}
		if (mLastTime == -1) {
			mStartTime = currentTime;
		}
		long deltaTime = currentTime - mStartTime;
		float rotation;
		if (mGoBack) {
			rotation = (2 * mAbortTime - deltaTime) * 0.5f;
		} else {
			rotation = deltaTime * 0.5f;
		}
		if (mStop) {
			float lastRedRot = mod(mLastRotation, 180);
			float reducedRot = mod(rotation, 180);
			// if passed starting point (0 deg or 180 deg)
			if ((!mGoBack && reducedRot < lastRedRot && rotation > mLastRotation)
					|| (mGoBack && reducedRot > lastRedRot && rotation < mLastRotation)) {
				rotation = 0f;
				mStop = false;
				mStopped = true;
			}
		}
		outTransformation.getMatrix().setRotate(rotation, mPivotX, mPivotY);
		mLastTime = deltaTime;
		mLastRotation = rotation;
		return !mStopped;
	}
}
