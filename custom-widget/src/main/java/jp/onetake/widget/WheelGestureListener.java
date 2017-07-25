package jp.onetake.widget;

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * ホイールに発生するジェスチャを拾うためのリスナオブジェクト
 */
public class WheelGestureListener extends GestureDetector.SimpleOnGestureListener {
	private static final double DRAGGING_ADJUST_RATIO	= 0.002;
	private static final double FLING_ADJUST_RATIO		= 0.0002f;
	
	private HorizontalWheel mHorizontalWheel;
	
	public WheelGestureListener(HorizontalWheel horizontalWheel) {
		mHorizontalWheel = horizontalWheel;
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		mHorizontalWheel.setState(HorizontalWheel.State.Dragging);
		
		// distanceXに-1.0をかけるのは、distanceXの算出式が「1つ前のX座標 - 現在のX座標」であるため
		// つまり指をX軸の正方向に動かすと、当然1つ前のX座標のが小さいのでdistanceXは負の値になる。それを正に変えるための計算
		mHorizontalWheel.rotateTo(mHorizontalWheel.getRadian() + distanceX * DRAGGING_ADJUST_RATIO * -1.0);
		
		return super.onScroll(e1, e2, distanceX, distanceY);
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		double endRadian = mHorizontalWheel.getRadian() + velocityX * FLING_ADJUST_RATIO ;
		mHorizontalWheel.startInertiaScroll(endRadian);
		mHorizontalWheel.setState(HorizontalWheel.State.Inertia);
		
		return super.onFling(e1, e2, velocityX, velocityY);
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		if (mHorizontalWheel.getState() == HorizontalWheel.State.Inertia) {
			mHorizontalWheel.cancelInertiaScroll();
			mHorizontalWheel.setState(HorizontalWheel.State.Idle);
		}
		
		return super.onDown(e);
	}
}
