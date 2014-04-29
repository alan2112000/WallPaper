package com.AlanYu.wallpaper;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import com.AlanYu.Filter.TestFilter;
import com.AlanYu.database.DBHelper;
import com.AlanYu.database.TouchDataNode;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

@SuppressLint("ShowToast")
public class LiveWallPaper extends WallpaperService {

	private boolean COLLECT_DATA = false;
	private String[] PROTECTED_LIST = { "vending", "gm", "mms", "contact",
			"gallery" };
	private int pid = 0;
	private TestFilter testFilter =new TestFilter(); 
	private String deleteProcessName = null;
	private static boolean isTraining = true;
	protected Vector<TouchDataNode> vc;
	private Instances trainingData;
	private Instances testData;

	/*
	 * Parameters for Database Query
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
	private String OWNERLABEL = "owner";
	private String OTHERLABEL = "other";
	private String nowLabel;

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

			if(COLLECT_DATA)
			writeDataBase(event);
			
			
			/*
			 * ============================================================ test
			 * weka lib for Android
			 * ================================================
			 */
			testFilter.setFeature();
			readDatabase();
			testFilter.setTrainingData(trainingData);
			testFilter.setTestData(trainingData);
			testFilter.trainingData();
			testFilter.testData();
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

			/*
			 * ============================================================
			 * 
			 * ============================================================
			 */
			if (visible) {
				stopService(intent);
				SharedPreferences settings = getSharedPreferences("Preference",
						0);
				String name = settings.getString("name", "");
				Log.d("visible", "true now user : " + name);
				nowLabel = name;
				if (name != OWNERLABEL)
					isTraining = false;
			} else {
				Log.d("invisible", "sdddd");
				if (isInProtectList()) {
					Log.d("invisible",
							"executed process is in the protect list ");
					if(COLLECT_DATA)
					 startService(intent);
				}
			}

			super.onVisibilityChanged(visible);
		}

	}

	@Override
	public Engine onCreateEngine() {
		// TODO Auto-generated method stub
		return new TouchEngine();
	}

	private boolean recentlyRunningApps(String processName) {
		ActivityManager service = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RecentTaskInfo> recentTasks = service.getRecentTasks(1,
				ActivityManager.RECENT_WITH_EXCLUDED);
		for (RecentTaskInfo recentTaskInfo : recentTasks) {
			System.out.println(recentTaskInfo.baseIntent);
			if (recentTaskInfo.baseIntent.toString().contains(processName)) {
				SharedPreferences settings = getSharedPreferences("Preference",
						0);
				settings.edit().putString("APP", processName).commit();
				return true;
			}
		}
		return false;
	}

	private boolean isInProtectList() {
		for (String processName : PROTECTED_LIST) {
			if (recentlyRunningApps(processName))
				return true;

		}
		return false;
	}

	private int findMatchProcessByName(String ps) {
		int notFound = 0;
		ActivityManager am = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> l = am.getRunningAppProcesses();
		Iterator<RunningAppProcessInfo> i = l.iterator();
		PackageManager pm = this.getPackageManager();
		while (i.hasNext()) {
			ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i
					.next());
			try {
				CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(
						info.processName, PackageManager.GET_META_DATA));
				String processName = info.processName;
				if (c.toString().equalsIgnoreCase(ps)) {
					return info.pid;
				}
			} catch (Exception e) {
				Log.e("errorTag", e.toString());
			}
		}
		return notFound;
	}

	/*
	 * ===================================================================== Get
	 * Now Running Apps Information
	 * =====================================================================
	 */
	private void getAppsInfo() {
		ActivityManager am = (ActivityManager) this
				.getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> l = am.getRunningAppProcesses();
		Iterator<RunningAppProcessInfo> i = l.iterator();
		PackageManager pm = this.getPackageManager();
		while (i.hasNext()) {
			ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i
					.next());
			try {
				CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(
						info.processName, PackageManager.GET_META_DATA));
				String processName = info.processName;
				Log.d("Process", "Id: " + info.pid + " ProcessName: "
						+ info.processName + "  Label: " + c.toString());
			} catch (Exception e) {
				Log.e("Error tag", e.toString());
			}
		}

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
			args.put(LABEL, nowLabel);
		}

		long rowid = writeSource.insert(TOUCH_TABLE_NAME, null, args);

		Log.d("writeDatabase Event",
				"id =" + rowid + " x:" + event.getX() + " y:" + event.getY()
						+ " Pressure" + event.getPressure() + " Size:"
						+ event.getSize() + " TimeStamp:"
						+ event.getEventTime() + "label:" + nowLabel);
		writeSource.close();
	}

	private void readDatabase()  {
		DBHelper db = new DBHelper(this);
		SQLiteDatabase readSource = db.getReadableDatabase();

		Cursor cursor = readSource.query(TOUCH_TABLE_NAME, new String[] { ID,
				X, Y, PRESSURE, LABEL, SIZE, TIMESTAMP, ACTION_TYPE }, null,
				null, null, null, null);
		Log.d("readDatabase", "reading database");
//		testFilter.setInstances(cursor);
//		testFilter.generateData();
		FastVector fv = testFilter.getFvWekaAttributes();
		trainingData = new Instances("Rel",fv,1000);
//		testData = new Instances("Rel",fv,1000);
		trainingData.setClassIndex(4);
//		testData.setClassIndex(4);;
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
					touchData.setActionType(cursor.getString(cursor
							.getColumnIndex(ACTION_TYPE)));
					Log.d("readDatabase event",
							" ID: " + touchData.getId() + " X:"
									+ touchData.getX() + " Y:"
									+ touchData.getY() + " Size:"
									+ touchData.getSize() + " Pressure"
									+ touchData.getPressure() + " Timestamp"
									+ touchData.getTimestamp()
									+ "Action type :"
									+ touchData.getActionType() + "LABEL : "
									+ touchData.getLabel());
					if(touchData.getLabel().contains("domo") || touchData.getLabel().contains("Jorge") || touchData.getLabel().contains("CY")){
						 Log.d("ReadDatabase ", "skip wrong dataset");
					}
					else {
					Instance iExample = new DenseInstance(5);
					Log.d("readDatabase", "setting instance value ");
					
					iExample.setValue((Attribute)fv.elementAt(0),Double.valueOf(cursor.getString(cursor.getColumnIndex(X))));
					iExample.setValue((Attribute)fv.elementAt(1),Double.valueOf(cursor.getString(cursor.getColumnIndex(Y))));
					iExample.setValue((Attribute)fv.elementAt(2),Double.valueOf(cursor.getString(cursor.getColumnIndex(PRESSURE))));
					iExample.setValue((Attribute)fv.elementAt(3),Double.valueOf(cursor.getString(cursor.getColumnIndex(SIZE))));
					iExample.setValue((Attribute)fv.elementAt(4),cursor.getString(cursor.getColumnIndex(LABEL)));
					 
						
			
					Log.d("readDatabase", "add to training set  ");
					trainingData.add(iExample);
					}
//					testData.add(iExample);

				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		cursor.close();
		readSource.close();
	}
}
