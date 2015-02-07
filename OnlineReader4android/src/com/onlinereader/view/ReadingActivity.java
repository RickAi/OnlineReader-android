package com.onlinereader.view;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class ReadingActivity extends Activity {
	private TextView tvReadingArea;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reading);
		
		tvReadingArea = (TextView) findViewById(R.id.tv_reading_area);
		tvReadingArea.setText(getIntent().getStringExtra("content"));
	}
}
