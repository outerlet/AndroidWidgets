package jp.onetake.sample.widget;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import jp.onetake.widget.HorizontalPicker;
import jp.onetake.widget.RecyclerHorizontalPicker;
import jp.onetake.widget.RecyclerHorizontalPickerAdapter;

public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		HorizontalPicker.HorizontalPickerAdapter adapter = new HorizontalPicker.HorizontalPickerAdapter();
		for (int i = 0 ; i < 20 ; i++) {
			if (i % 2 == 0) {
				adapter.add("NO." + i);
			} else {
				adapter.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
			}
		}
		
		HorizontalPicker picker = (HorizontalPicker)findViewById(R.id.hpicker);
		picker.setAdapter(adapter);
		
		RecyclerHorizontalPicker rPicker = (RecyclerHorizontalPicker)findViewById(R.id.recycler_hpicker);
		RecyclerHorizontalPickerAdapter rAdapter = new RecyclerHorizontalPickerAdapter(this);
		for (int i = 0 ; i < 20 ; i++) {
			if (i % 2 == 0) {
				rAdapter.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
			} else {
				rAdapter.add("VER." + i);
			}
		}
		rPicker.setAdapter(rAdapter);
	}
}
