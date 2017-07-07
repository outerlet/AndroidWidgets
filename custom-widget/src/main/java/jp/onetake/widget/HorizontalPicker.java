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
		
		public void add(String text) {
			PickerItem item = new PickerItem();
			item.text = text;
			add(item);
		}
		
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
	private View[] mOverlays;
	private View[] mSpaces;
	private int mWidth;
	private int mItemWidth;
	private int mOverlayWidth;
	private boolean mIsLayouted;
	
	public HorizontalPicker(@NonNull Context context) {
		this(context, null);
	}
	
	public HorizontalPicker(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		
		View view = LayoutInflater.from(context).inflate(R.layout.view_horizontal_picker, this, true);
		
		ResponsiveHorizontalScrollView scrollView = (ResponsiveHorizontalScrollView)findViewById(R.id.scroll_area);
		scrollView.setHorizontalScrollBarEnabled(false);
		scrollView.setListener(this);
		
		mOverlays = new View[] { view.findViewById(R.id.view_overlay_left), view.findViewById(R.id.view_overlay_right) };
		mSpaces = new View[] { view.findViewById(R.id.view_left_space), view.findViewById(R.id.view_right_space) };
		
		mContentsLayout = (LinearLayout)view.findViewById(R.id.layout_contents);
		
		mIsLayouted = false;
		mItemWidth = DEFAULT_PICKER_ITEM_WIDTH;
		
		if (attrs != null) {
			TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HorizontalPicker);
			
			mItemWidth = array.getDimensionPixelSize(
					R.styleable.HorizontalPicker_item_width, DEFAULT_PICKER_ITEM_WIDTH);
			
			int backgroundColor = array.getColor(R.styleable.HorizontalPicker_background_color, Color.WHITE);
			scrollView.setBackgroundColor(backgroundColor);
			
			if (array.getBoolean(R.styleable.HorizontalPicker_overlay_visible, false)) {
				int color = array.getColor(R.styleable.HorizontalPicker_overlay_color, backgroundColor);
				
				for (View overlay : mOverlays) {
					overlay.setVisibility(View.VISIBLE);
					overlay.setBackgroundColor(color);
				}
			}
			
			array.recycle();
		}
	}

	public void setAdapter(HorizontalPickerAdapter adapter) {
		mAdapter = adapter;
		mAdapter.picker = this;
		
		refresh();
	}
	
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
		
		android.util.Log.d(getClass().getSimpleName(), "CHILD-COUNT = " + mContentsLayout.getChildCount());
	}
	
	private void addView(PickerItem item) {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.view_picker_item, this, false);
		
		view.getLayoutParams().width = mItemWidth;
		
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
		
		mWidth = MeasureSpec.getSize(widthMeasureSpec);
		mOverlayWidth = (mWidth - mItemWidth) / 2;
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		
		if (!mIsLayouted) {
			for (View overlay : mOverlays) {
				overlay.getLayoutParams().width = mOverlayWidth;
			}
			
			for (View space : mSpaces) {
				space.getLayoutParams().width = mOverlayWidth;
			}
			
			mIsLayouted = true;
		}
	}
	
	@Override
	public void onScrollEnded(ResponsiveHorizontalScrollView scrollView) {
		int centerX = scrollView.getScrollX() + mWidth / 2;
		
		for (int i = 1; i < mContentsLayout.getChildCount() - 1; i++) {
			View view = mContentsLayout.getChildAt(i);
			
			if (centerX >= view.getX() && centerX < view.getX() + view.getLayoutParams().width) {
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
