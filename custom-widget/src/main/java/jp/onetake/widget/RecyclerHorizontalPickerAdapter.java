package jp.onetake.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerHorizontalPickerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	class ItemViewHolder extends RecyclerView.ViewHolder {
		View view;
		TextView textView;
		ImageView imageView;
		
		ItemViewHolder(View view) {
			super(view);

			this.view = view;
			this.textView = (TextView)view.findViewById(R.id.textview_item);
			this.imageView = (ImageView)view.findViewById(R.id.imageview_item);
		}
	}
	
	class BlankViewHolder extends RecyclerView.ViewHolder {
		View view;
		
		BlankViewHolder(View view) {
			super(view);
			
			this.view = view;
		}
	}
	
	private class ViewItem {
		String text = null;
		Bitmap bitmap = null;
	}
	
	private static final int ITEM_VIEW_TYPE_BLANK	= 0;
	private static final int ITEM_VIEW_TYPE_NORMAL	= 1;
	
	private Context mContext;
	private List<ViewItem> mItemList;
	private int mItemWidth;
	private int mBlankWidth = 300;
	
	public RecyclerHorizontalPickerAdapter(Context context) {
		mContext = context;

		// 先頭と末尾の空白部分を先に埋める
		mItemList = new ArrayList<>();
		mItemList.add(new ViewItem());
		mItemList.add(new ViewItem());
	}
	
	public void setItemWidth(int itemWidth) {
		mItemWidth = itemWidth;
	}
	
	public void setBlankWidth(int blankWidth) {
		mBlankWidth = blankWidth;
	}
	
	public void add(String text) {
		ViewItem item = new ViewItem();
		item.text = text;
		
		mItemList.add(mItemList.size() - 1, item);
	}
	
	public void add(Bitmap bitmap) {
		ViewItem item = new ViewItem();
		item.bitmap = bitmap;
		
		mItemList.add(mItemList.size() - 1, item);
	}
	
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == ITEM_VIEW_TYPE_BLANK) {
			return new BlankViewHolder(
					LayoutInflater.from(mContext).inflate(R.layout.view_picker_blank_item, parent, false));
		}
		
		return new ItemViewHolder(
				LayoutInflater.from(mContext).inflate(R.layout.view_picker_item, parent, false));
	}
	
	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (getItemViewType(position) == ITEM_VIEW_TYPE_NORMAL) {
			RecyclerHorizontalPickerAdapter.ItemViewHolder viewHolder = (RecyclerHorizontalPickerAdapter.ItemViewHolder)holder;
			
			viewHolder.view.setTag(position - 1);
			viewHolder.view.getLayoutParams().width = mItemWidth;
			
			ViewItem item = mItemList.get(position);
			
			if (item.text != null) {
				viewHolder.textView.setText(item.text);
				viewHolder.textView.setVisibility(View.VISIBLE);
				viewHolder.imageView.setVisibility(View.INVISIBLE);
			} else {
				viewHolder.imageView.setImageBitmap(item.bitmap);
				viewHolder.textView.setVisibility(View.INVISIBLE);
				viewHolder.imageView.setVisibility(View.VISIBLE);
			}
		} else {
			RecyclerHorizontalPickerAdapter.BlankViewHolder viewHolder = (RecyclerHorizontalPickerAdapter.BlankViewHolder)holder;
			viewHolder.view.setTag(null);
			viewHolder.view.getLayoutParams().width = mBlankWidth;
		}
	}
	
	@Override
	public int getItemViewType(int position) {
		if (position == 0 || position == mItemList.size() - 1) {
			return ITEM_VIEW_TYPE_BLANK;
		}
		return ITEM_VIEW_TYPE_NORMAL;
	}
	
	@Override
	public int getItemCount() {
		return mItemList.size();
	}
}
