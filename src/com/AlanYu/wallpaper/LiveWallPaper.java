package com.AlanYu.wallpaper;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import com.AlanYu.database.DBHelper;
import com.AlanYu.database.TouchDataNode;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.SensorEvent;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

@SuppressLint("ShowToast")
public class LiveWallPaper extends WallpaperService {

	private static String touchDownTag = "Touch Event";
	private static String touchMoveTag = "Move Touch";
	private static String touchUpTag = "Up touch";
	private static String outsideTouchTag = "OusideTouch";
	private static String[] protectorAppsName = { "Line", "mail", };
	private int pid = 0;
	private String deleteProcessName = null;
	private static boolean isTraining = true;
	protected Vector<TouchDataNode> vc;
	
	/* 
	 *     Parameters for Database Query 
	 */
	
	private static final String TOUCH_TABLE_NAME = "TOUCH";
	private static final String ID = "_ID";
	private static final String ACTION_TYPE = "ACTION";
	private static final String X = "X";
	private static final String Y = "Y";
	private static final String SIZE = "SIZE";
	private static final String PRESSURE = "PRESSURE";
	private static final String LABEL = "LABEL";
	private static final String TIMESTAMP = "TIMESTAMP";
	private static final String OWNERLABEL = "owner";
	private static final String OTHERLABEL = "other";

	public static boolean isTraining() {
		return isTraining;
	}

	public static void setTraining(boolean isTraining) {
		LiveWallPaper.isTraining = isTraining;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

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
			 */
			writeDataBase(event);
			
			try {
				readDatabase();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			/* ============================================================
			 * test weka lib for Android 
			================================================ */
			
			 String[] options = new String[1];
			 options[0] = "-U";            // unpruned tree
			 J48 tree = new J48();         // new instance of tree
			 try {
				tree.setOptions(options);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}     // set the options
//			tree.buildClassifier(data);
			// Check Protector Apps List ok

			// app need to be protected

			// trigger Sensor to determine is user or not
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

				 stopService(intent);
			} else {
				Log.d("visible", "false");

				/*
				 * Make Decision is owner or not
				 */
				if (!isTraining)
					;
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

	private void writeDataBase(MotionEvent event) {

		DBHelper db = new DBHelper(this);
		SQLiteDatabase writeSource = db.getWritableDatabase();

		ContentValues args = new ContentValues();
		args.put(X, String.valueOf(event.getX()));
		args.put(Y, String.valueOf(event.getY()));
		args.put(ACTION_TYPE, event.getAction());
		args.put(PRESSURE, String.valueOf(event.getPressure()));
		args.put(SIZE, String.valueOf(event.getSize()));
		args.put(TIMESTAMP, String.valueOf(event.getEventTime()));

		if (isTraining) {
			args.put(LABEL, OWNERLABEL);
		} else {
			args.put(LABEL, OTHERLABEL);
		}
		
		long rowid = writeSource.insert(TOUCH_TABLE_NAME, null, args);

		Log.d("writeDatabase Event",
				"id =" + rowid + " x:" + event.getX() + " y:" + event.getY()
						+ " Pressure" + event.getPressure() + " Size:"
						+ event.getSize() + " TimeStamp:"
						+ event.getEventTime() + "label:OWNERLABEL");
		writeSource.close();
	}

	private void readDatabase() throws SQLException {
		DBHelper db = new DBHelper(this);
		SQLiteDatabase readSource = db.getReadableDatabase();

		Cursor cursor = readSource.query(TOUCH_TABLE_NAME, new String[] { ID,
				X, Y, PRESSURE, LABEL, SIZE, TIMESTAMP, ACTION_TYPE }, null,
				null, null, null, null);

		try {
			if (cursor.moveToFirst()) {
				do {
					TouchDataNode touchData = new TouchDataNode();
					touchData
							.setId(cursor.getString(cursor.getColumnIndex(ID)));
					touchData.setX(cursor.getString(cursor.getColumnIndex(X)));
					touchData.setY(cursor.getString(cursor.getColumnIndex(Y)));
					touchData.setSize(cursor.getString(cursor
							.getColumnIndex(SIZE)));
					touchData.setPressure(cursor.getString(cursor
							.getColumnIndex(PRESSURE)));
					touchData.setTimestamp(cursor.getString(cursor
							.getColumnIndex(TIMESTAMP)));
					touchData.setLabel(cursor.getString(cursor
							.getColumnIndex(LABEL)));
					touchData.setActionType(cursor.getString(cursor.getColumnIndex(ACTION_TYPE)));
					Log.d("readDatabase event",
							" ID: " + touchData.getId() + " X:"
									+ touchData.getX() + " Y:"
									+ touchData.getY() + " Size:"
									+ touchData.getSize() + " Pressure"
									+ touchData.getPressure() + " Timestamp"
									+ touchData.getTimestamp() + "Action type :"
									+ touchData.getActionType() + "LABEL : "
									+ touchData.getLabel());
					

				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		readSource.close();
	}
}
