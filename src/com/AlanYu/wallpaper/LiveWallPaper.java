package com.AlanYu.wallpaper;

import java.util.Iterator;
import java.util.List;

import com.AlanYu.database.DBHelper;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

@SuppressLint("ShowToast")
public class LiveWallPaper extends WallpaperService {

	@Override
	public void onCreate() {
		DBHelper source = new DBHelper(this);
		SQLiteDatabase db = source.getWritableDatabase();
		super.onCreate();
	}

	private static String touchDownTag = "Touch Event";
	private static String touchMoveTag = "Move Touch";
	private static String touchUpTag = "Up touch";
	private static String outsideTouchTag = "OusideTouch";
	private static String[] protectorAppsName = { "Line", "mail", };
	private int pid = 0;
	private String deleteProcessName = null;

	public class TouchEngine extends Engine {

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			setTouchEventsEnabled(true);

		}

		@Override
		public void onTouchEvent(MotionEvent event) {

			/*
			 * ==================================================================
			 * === detect the touch event position
			 * ==============================
			 * =======================================
			 */

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Log.d(touchDownTag,
						"touch event" + event.getX() + "," + event.getY() + ")");
			}
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				Log.d(touchMoveTag,
						"touch event" + event.getX() + "," + event.getY() + ")");
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				Log.d(touchUpTag,
						"touch event" + event.getX() + "," + event.getY() + ")");
			}
			if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
				Log.d(outsideTouchTag, "touch event" + event.getX() + ","
						+ event.getY() + ")");
			}

			/*
			 * ==================================================================
			 * === Record the touch user made and write into the sqlite database
			 * 
			 * ==================================================================
			 * ===
			 */
			// Check Protector Apps List ok

			// app need to be protected

			// trigger Sesnor to detertmine is user or not
			// is user allow to access and turn off the Sensor
			// execute the machineLearningMethod
			// not user close the apps and turn off the Sensor

			// app is normal apps
			super.onTouchEvent(event);
		}

		/*
		 * ============================================================ if
		 * wallpaperService is forebackground then kill monitorAppsService else
		 * start monitorAppsService
		 * ============================================================
		 */

		@Override
		public void onVisibilityChanged(boolean visible) {
			Intent intent = new Intent(LiveWallPaper.this,
					monitorAppService.class);
			if (visible) {
				Log.d("visible", "true");
				// stopService(intent);
			} else {
				Log.d("visible", "false");
				// startService(intent);

			}
			super.onVisibilityChanged(visible);
		}

	}

	@Override
	public Engine onCreateEngine() {
		// TODO Auto-generated method stub
		return new TouchEngine();
	}

}
