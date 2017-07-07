package jp.onetake.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * HorizontalScrollViewの拡張<br />
 * スクロールが終了したことを検知するためのリスナを設定できる<br />
 * APIレベルが24以上でもいいならRecyclerViewを使ったほうがいい
 */
public class ResponsiveHorizontalScrollView extends HorizontalScrollView {
	public interface OnScrollChangedListener {
		void onScrollEnded(ResponsiveHorizontalScrollView scrollView);
	}
	
	private class ScrollChecker implements Runnable {
		@Override
		public void run() {
			int position = getScrollX();
			
			if (mLatestPosition - position == 0) {
				if (mListener != null) {
					mListener.onScrollEnded(ResponsiveHorizontalScrollView.this);
				}
			} else {
				checkScroll();
			}
		}
	}
	
	// 指が離れてからスクロールが継続されているかチェックするまでの時間(msec)
	// 50msecくらいでいい気もするが少し余裕を持たせる。100だと操作してて違和感があるのでそこまで大きくするべきではない
	private static final int DELAY_SCROLL_CHECK = 60;
	
	private ScrollChecker mScrollChecker;
	private int mLatestPosition;
	private OnScrollChangedListener mListener;
	
	public ResponsiveHorizontalScrollView(Context context) {
		this(context, null);
	}
	
	public ResponsiveHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mScrollChecker = new ScrollChecker();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			checkScroll();
		}
		return super.dispatchTouchEvent(ev);
	}
	
	public void setListener(OnScrollChangedListener listener) {
		mListener = listener;
	}
	
	private void checkScroll() {
		mLatestPosition = getScrollX();
		ResponsiveHorizontalScrollView.this.postDelayed(mScrollChecker, DELAY_SCROLL_CHECK);
	}
}
