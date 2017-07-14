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
		void onScroll(ResponsiveHorizontalScrollView scrollView);
		void onScrollEnded(ResponsiveHorizontalScrollView scrollView);
	}
	
	private class ScrollChecker implements Runnable {
		// 指が離れてからスクロールが継続されているかチェックするまでの時間(msec)
		// 50msecくらいでいい気もするが少し余裕を持たせる。100だと操作してて違和感があるのでそこまで大きくするべきではない
		private static final int DELAY_SCROLL_CHECK = 60;
		
		private int mLatestX;
		
		void check() {
			mLatestX = getScrollX();
			ResponsiveHorizontalScrollView.this.postDelayed(this, DELAY_SCROLL_CHECK);
		}
		
		@Override
		public void run() {
			int position = getScrollX();
			
			if (mLatestX - position == 0) {
				if (mListener != null) {
					mListener.onScrollEnded(ResponsiveHorizontalScrollView.this);
				}
			} else {
				if (mListener != null) {
					mListener.onScroll(ResponsiveHorizontalScrollView.this);
				}
				checkScroll();
			}
		}
	}
	
	private ScrollChecker mChecker;
	private OnScrollChangedListener mListener;
	
	public ResponsiveHorizontalScrollView(Context context) {
		this(context, null);
	}
	
	public ResponsiveHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mChecker = new ScrollChecker();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			checkScroll();
		} else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			if (mListener != null) {
				mListener.onScroll(this);
			}
		}
		
		return super.dispatchTouchEvent(ev);
	}
	
	public void setListener(OnScrollChangedListener listener) {
		mListener = listener;
	}
	
	private void checkScroll() {
		mChecker.check();
	}
}
