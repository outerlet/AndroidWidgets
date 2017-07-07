package jp.onetake.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * 横向きピッカー。RecyclerView使用
 */
public class RecyclerHorizontalPicker extends FrameLayout {
	public interface OnSelectListener {
		void onSelect(int position);
	}
	
	private static final int DEFAULT_ITEM_WIDTH = 120;
	
	private RecyclerView mRecyclerView;
	private View[] mOverlays;
	
	private int mWidth;
	private int mHeight;
	private int mItemWidth;
	private int mCurrentPosition;
	private boolean mIsLayouted;
	private OnSelectListener mListener;
	
	public RecyclerHorizontalPicker(Context context) {
		this(context, null);
	}
	
	public RecyclerHorizontalPicker(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		
		mCurrentPosition = 0;
		mIsLayouted = false;
		
		View view = LayoutInflater.from(context).inflate(R.layout.view_recycler_horizontal_picker, this, true);
		mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler_view_picker);
		LinearLayoutManager manager = new LinearLayoutManager(context);
		manager.setOrientation(LinearLayoutManager.HORIZONTAL);
		mRecyclerView.setLayoutManager(manager);
		
		mOverlays = new View[] { view.findViewById(R.id.view_overlay_left), view.findViewById(R.id.view_overlay_right) };
		
		if (attrs != null) {
			TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RecyclerHorizontalPicker);
			
			mItemWidth = array.getDimensionPixelSize(
					R.styleable.RecyclerHorizontalPicker_item_width, DEFAULT_ITEM_WIDTH);
			
			int backgroundColor = array.getColor(R.styleable.RecyclerHorizontalPicker_background_color, Color.WHITE);
			setBackgroundColor(backgroundColor);
			
			boolean overlayVisible = array.getBoolean(R.styleable.RecyclerHorizontalPicker_overlay_visible, false);
			if (overlayVisible) {
				for (View overlay : mOverlays) {
					overlay.setVisibility(View.VISIBLE);
					overlay.setBackgroundColor(array.getColor(R.styleable.RecyclerHorizontalPicker_overlay_color, backgroundColor));
				}
			}
			
			array.recycle();
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// タッチした指が離れたら、RecyclerViewの中央に位置するViewの中央が真ん中になるよう移動する
		if (event.getAction() == MotionEvent.ACTION_UP) {
			float centerX = mWidth / 2.0f;
			
			View view = mRecyclerView.findChildViewUnder(centerX, mHeight / 2.0f);
			float viewCenterX = view.getX() + view.getWidth() / 2.0f;
			
			mRecyclerView.smoothScrollBy((int)(viewCenterX - centerX), 0);
			
			if (view.getTag() != null) {
				int position = (int)view.getTag();
				if (position != mCurrentPosition) {
					if (mListener != null) {
						mListener.onSelect(position);
					}
					
					mCurrentPosition = position;
				}
			}
			
			return true;
		}
		
		return super.dispatchTouchEvent(event);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		mWidth = MeasureSpec.getSize(widthMeasureSpec);
		mHeight = MeasureSpec.getSize(heightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		// RecyclerViewの先頭と末尾にはスクロール阻止のための空白を入れているが、
		// そのサイズを決定するためにはこのView自体のサイズが必要
		if (!mIsLayouted && mWidth > 0) {
			RecyclerHorizontalPickerAdapter adapter = (RecyclerHorizontalPickerAdapter)mRecyclerView.getAdapter();
			adapter.setBlankWidth(mWidth / 2 - mItemWidth / 2);
			adapter.notifyDataSetChanged();
			
			int overlayWidth = (mWidth - mItemWidth) / 2;
			for (View overlay : mOverlays) {
				overlay.getLayoutParams().width = overlayWidth;
			}
			
			mIsLayouted = true;
		}
	}
	
	public void setAdapter(RecyclerHorizontalPickerAdapter adapter) {
		adapter.setItemWidth(mItemWidth);
		adapter.setBlankWidth((mWidth - mItemWidth) / 2);
		
		mRecyclerView.setAdapter(adapter);
	}
	
	public void setListener(OnSelectListener listener) {
		mListener = listener;
	}
}
