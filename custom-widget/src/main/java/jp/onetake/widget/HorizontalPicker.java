package jp.onetake.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * 横方向へのピッカー<br />
 * ピッカーの各項目には文字列とBitmapをセットすることができる。混在させるのも可
 */
public class HorizontalPicker extends FrameLayout implements ResponsiveHorizontalScrollView.OnScrollChangedListener {
	/**
	 * ピッカーで値を選択したときにそのイベントを通知するリスナ
	 */
	public interface OnSelectListener {
		void onSelect(int position);
	}
	
	private static class PickerItem {
		String text;
		Bitmap bitmap;
	}
	
	/**
	 * ピッカーに表示する選択項目を設定するアダプタ<br />
	 * ListViewと同じロジックではないが、同じような手続きで使えるようにしてみた
	 */
	public static class HorizontalPickerAdapter extends ArrayList<PickerItem> {
		private HorizontalPicker picker;
		
		@SuppressWarnings("unused")
		public void add(String text) {
			PickerItem item = new PickerItem();
			item.text = text;
			add(item);
		}
		
		@SuppressWarnings("unused")
		public void add(Bitmap bitmap) {
			PickerItem item = new PickerItem();
			item.bitmap = bitmap;
			add(item);
		}
		
		@SuppressWarnings("unused")
		public void notifyDataSetChanged() {
			picker.refresh();
		}
	}
	
	private static final int DEFAULT_PICKER_ITEM_WIDTH	= 120;
	
	private HorizontalPickerAdapter mAdapter;
	private OnSelectListener mListener;
	
	private LinearLayout mContentsLayout;
	private View[] mSideViews;
	private int mItemWidth;
	
	public HorizontalPicker(@NonNull Context context) {
		this(context, null);
	}
	
	public HorizontalPicker(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		
		View view = LayoutInflater.from(context).inflate(R.layout.view_horizontal_picker, this, true);
		
		ResponsiveHorizontalScrollView scrollView = (ResponsiveHorizontalScrollView)findViewById(R.id.scroll_area);
		scrollView.setHorizontalScrollBarEnabled(false);
		scrollView.setListener(this);

		int[] viewIds = new int[] { R.id.view_overlay_left, R.id.view_overlay_right, R.id.view_left_space, R.id.view_right_space };
		mSideViews = new View[viewIds.length];
		for (int i = 0 ; i < viewIds.length ; i++) {
			int viewId = viewIds[i];
			
			View v = view.findViewById(viewId);
			v.setTag(viewId == R.id.view_overlay_left || viewId == R.id.view_overlay_right);
			mSideViews[i] = v;
		}
		
		mContentsLayout = (LinearLayout)view.findViewById(R.id.layout_contents);
		
		mItemWidth = DEFAULT_PICKER_ITEM_WIDTH;
		
		if (attrs != null) {
			TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HorizontalPicker);
			
			mItemWidth = array.getDimensionPixelSize(
					R.styleable.HorizontalPicker_item_width, DEFAULT_PICKER_ITEM_WIDTH);
			
			int backgroundColor = array.getColor(R.styleable.HorizontalPicker_background_color, Color.WHITE);
			scrollView.setBackgroundColor(backgroundColor);
			
			if (array.getBoolean(R.styleable.HorizontalPicker_overlay_visible, false)) {
				int color = array.getColor(R.styleable.HorizontalPicker_overlay_color, backgroundColor);
				
				for (View sideView : mSideViews) {
					if ((boolean)sideView.getTag()) {
						sideView.setVisibility(View.VISIBLE);
						sideView.setBackgroundColor(color);
					}
				}
			}
			
			array.recycle();
		}
	}
	
	@SuppressWarnings("unused")
	public void setAdapter(HorizontalPickerAdapter adapter) {
		mAdapter = adapter;
		mAdapter.picker = this;
		
		refresh();
	}
	
	@SuppressWarnings("unused")
	public HorizontalPickerAdapter getAdapter() {
		return mAdapter;
	}
	
	@SuppressWarnings("unused")
	public void setOnSelectListener(OnSelectListener listener) {
		mListener = listener;
	}
	
	private void refresh() {
		for (int i = mContentsLayout.getChildCount() - 2; i > 0; i--) {
			mContentsLayout.removeViewAt(i);
		}
		
		for (PickerItem item : mAdapter) {
			addView(item);
		}
	}
	
	private void addView(PickerItem item) {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.view_picker_item, this, false);
		
		if (item.text != null) {
			TextView textView = (TextView)view.findViewById(R.id.textview_item);
			textView.setText(item.text);
			textView.setVisibility(View.VISIBLE);
		} else {
			ImageView imageView = (ImageView)view.findViewById(R.id.imageview_item);
			imageView.setImageBitmap(item.bitmap);
			imageView.setVisibility(View.VISIBLE);
		}
		
		mContentsLayout.addView(view, mContentsLayout.getChildCount() - 1);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int sideWidth = (MeasureSpec.getSize(widthMeasureSpec) - mItemWidth) / 2;
		int layoutWidth = sideWidth * 2 + mItemWidth * (mContentsLayout.getChildCount() - 2);
		
		mContentsLayout.measure(MeasureSpec.makeMeasureSpec(layoutWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
		
		int sideSpec = MeasureSpec.makeMeasureSpec(sideWidth, MeasureSpec.EXACTLY);
		for (View view : mSideViews) {
			view.measure(sideSpec, heightMeasureSpec);
		}
		
		int itemSpec = MeasureSpec.makeMeasureSpec(mItemWidth, MeasureSpec.EXACTLY);
		for (int i = 1 ; i < mContentsLayout.getChildCount() - 1 ; i++) {
			View item = mContentsLayout.getChildAt(i);
			item.measure(itemSpec, heightMeasureSpec);
		}
	}
	
	@Override
	public void onScroll(ResponsiveHorizontalScrollView scrollView) {
		// Do nothing.
	}
	
	@Override
	public void onScrollEnded(ResponsiveHorizontalScrollView scrollView) {
		int centerX = scrollView.getScrollX() + this.getMeasuredWidth() / 2;
		
		for (int i = 1; i < mContentsLayout.getChildCount() - 1; i++) {
			View view = mContentsLayout.getChildAt(i);
			
			if (centerX >= view.getX() && centerX < view.getX() + view.getWidth()) {
				int viewCenterX = (int)view.getX() + mItemWidth / 2;
				scrollView.smoothScrollBy(viewCenterX - centerX, 0);
				
				if (mListener != null) {
					mListener.onSelect(i - 1);
				}
				
				break;
			}
		}
	}
}
