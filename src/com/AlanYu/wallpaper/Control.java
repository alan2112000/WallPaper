package com.AlanYu.wallpaper;

import com.AlanYu.Filter.TestFilter;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Control extends Activity {

	Button startButton;
	Button stopButton;
	EditText userType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.control);

		startButton = (Button) findViewById(R.id.start);
		stopButton = (Button) findViewById(R.id.stop);
		userType = (EditText) findViewById(R.id.userType);
		startButton.setOnClickListener(start);
		stopButton.setOnClickListener(stop);

	}

	public Control() {
	}

	private OnClickListener start = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// send data to live wallpaper
			Intent intent = new Intent(
					WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);

			intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
					new ComponentName(com.AlanYu.wallpaper.Control.this,
							LiveWallPaper.class));
			SharedPreferences settings = getSharedPreferences("Preference", 0);
			settings.edit().putString("name", userType.getText().toString())
					.commit();
			startActivity(intent);
		}

	};

	private OnClickListener stop = new OnClickListener() {

		@Override
		public void onClick(View v) {
			TestFilter test = new TestFilter() ; 
		}

	};

}
