package com.AlanYu.wallpaper;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import com.AlanYu.Filter.DecisionTableFilter;
import com.AlanYu.Filter.J48Classifier;
import com.AlanYu.Filter.KStarClassifier;
import com.AlanYu.Filter.RandomForestClassifier;
import com.AlanYu.Filter.TestFilter;
import com.AlanYu.Filter.kNNClassifier;
import com.AlanYu.database.DBHelper;
import com.AlanYu.database.TouchDataNode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.PowerManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

@SuppressLint("ShowToast")
public class LiveWallPaper extends WallpaperService {

	private boolean TRAINING_MODE = false;
	private String[] PROTECTED_LIST = { "vending", "gm", "mms", "contact",
			"gallery" };
	private int pid = 0;
	private TestFilter testFilter = new TestFilter();
	private String deleteProcessName = null;
	private static boolean isTraining = true;
	protected Vector<TouchDataNode> vc;
	private Instances trainingData;
	private Instances testData;
	private J48Classifier j48;
	private kNNClassifier knn;
	private KStarClassifier kstar;
	private DecisionTableFilter dt;
	private RandomForestClassifier randomF;
	private static final double CONFINDENCE_THRESHOLD = 0.5;
	private double NOW_USER = 1;
	private int ownerLabelNumber = 0;
	private int totalLableNumber = 0;
	private ComponentName devAdminReceiver;
	KeyguardManager keyguardManager;
	KeyguardLock k1;
	private final String TAG = "KeyGuardTest";
	private boolean lock = true;
	KeyguardLock Keylock;
	PowerManager manager;
	DevicePolicyManager mDPM;
	ComponentName mDeviceAdminSample;

	/*
	 * Parameters for Database Query
	 */

	// TODO Refactor code

	private static final String TOUCH_TABLE_NAME = "TOUCH";
	private static final String ID = "_ID";
	private static final String ACTION_TYPE = "ACTION";
	private static final String X = "X";
	private static final String Y = "Y";
	private static final String SIZE = "SIZE";
	private static final String PRESSURE = "PRESSURE";
	private static final String LABEL = "LABEL";
	private static final String TIMESTAMP = "TIMESTAMP";
	private static final String OWNER_LABEL = "owner";
	private static final String OTHER_LABEL = "other";
	private String nowLabel;

	@Override
	public void onCreate() {
		init();
		super.onCreate();
	}

	public static boolean isTraining() {
		return isTraining;
	}

	public static void setTraining(boolean isTraining) {
		LiveWallPaper.isTraining = isTraining;
	}

	public class TouchEngine extends Engine {

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			setTouchEventsEnabled(true);

		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			if (TRAINING_MODE)
				writeDataBase(event);

			
			/* auto Lock screen */ 
//			if (lock) {
//				Log.d("lock screen", "unlockscreen");
//				Keylock.disableKeyguard(); // 自動解鎖
//				lock = false;
//			} else {
//				Log.d("lock screen", "lockscreen");
//				Keylock.reenableKeyguard();
//				lock = true;
//				 mDPM.lockNow();
//			}

			/*
			 * ============================================================ test
			 * weka lib for Android
			 * ================================================
			 */
			FastVector fv = j48.getFvWekaAttributes();
			Instance iExample = new DenseInstance(5);
			Log.d("Prediction phase ", "Predicting the current touch user ");

			iExample.setValue((Attribute) fv.elementAt(0), event.getX());
			iExample.setValue((Attribute) fv.elementAt(1), event.getY());
			iExample.setValue((Attribute) fv.elementAt(2), event.getPressure());
			iExample.setValue((Attribute) fv.elementAt(3), event.getSize());
			j48.predictInstance(iExample);
			// Instances dataUnLabeled;
			// dataUnLabeled = new Instances("TestInstances",
			// knn.getFvWekaAttributes(), 10);
			// dataUnLabeled.add(iExample);
			// dataUnLabeled.setClassIndex(dataUnLabeled.numAttributes() - 1);
			// Vote vote = new Vote();
			// Classifier cls[] = { j48.returnClassifier(),
			// knn.returnClassifier(), kstar.returnClassifier(),
			// dt.returnClassifier(), randomF.returnClassifier() };
			// vote.setClassifiers(cls);
			// double prediction[] = null;
			// try {
			// prediction = vote.distributionForInstance(dataUnLabeled
			// .firstInstance());
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// System.out.println("\n Resuult Vote =========");
			// for (int i = 0; i < prediction.length; i++) {
			// System.out.println("Prediction Class :"
			// + knn.getTrainingData().classAttribute().value(i)
			// + " : " + Double.toString(prediction[i]));
			// }
			//
			// if (prediction[0] > prediction[1]) {
			// ownerLabelNumber++;
			// }
			// totalLableNumber++;
			// System.out.println("\n ownerlabelnumber : " + ownerLabelNumber);
			// double percentange = (double) ownerLabelNumber /
			// totalLableNumber;
			// if (percentange > CONFINDENCE_THRESHOLD) {
			// System.out
			// .println("\n Prediction Result :is owner ============= \n totalLableNumber : "
			// + totalLableNumber
			// + "prediction : "
			// + Double.toString(percentange));
			// } else
			// System.out
			// .println("\n Prediction Result : is other =============\n totallablenumber : "
			// + totalLableNumber
			// + " prediction : "
			// + Double.toString(percentange));
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
				if (name != OWNER_LABEL)
					isTraining = false;
			} else {
				Log.d("invisible", "sdddd");
				if (isInProtectList()) {
					Log.d("invisible",
							"executed process is in the protect list ");
					startService(intent);
				}
			}

			super.onVisibilityChanged(visible);
		}

	}

	@Override
	public Engine onCreateEngine() {
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
			args.put(LABEL, OWNER_LABEL);
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

	private void readDatabase() {
		DBHelper db = new DBHelper(this);
		SQLiteDatabase readSource = db.getReadableDatabase();

		Cursor cursor = readSource.query(TOUCH_TABLE_NAME, new String[] { ID,
				X, Y, PRESSURE, LABEL, SIZE, TIMESTAMP, ACTION_TYPE }, null,
				null, null, null, null);
		Log.d("readDatabase", "reading database");
		FastVector fv = j48.getFvWekaAttributes();
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
					// Log.d("readDatabase event",
					// " ID: " + touchData.getId() + " X:"
					// + touchData.getX() + " Y:"
					// + touchData.getY() + " Size:"
					// + touchData.getSize() + " Pressure"
					// + touchData.getPressure() + " Timestamp"
					// + touchData.getTimestamp()
					// + "Action type :"
					// + touchData.getActionType() + "LABEL : "
					// + touchData.getLabel());
					if (touchData.getLabel().contains("domo")
							|| touchData.getLabel().contains("Jorge")
							|| touchData.getLabel().contains("CY")) {
						// Log.d("ReadDatabase ", "skip wrong dataset");
					} else {
						Instance iExample = new DenseInstance(5);
						// Log.d("readDatabase", "setting instance value ");

						iExample.setValue((Attribute) fv.elementAt(0), Double
								.valueOf(cursor.getString(cursor
										.getColumnIndex(X))));
						iExample.setValue((Attribute) fv.elementAt(1), Double
								.valueOf(cursor.getString(cursor
										.getColumnIndex(Y))));
						iExample.setValue((Attribute) fv.elementAt(2), Double
								.valueOf(cursor.getString(cursor
										.getColumnIndex(PRESSURE))));
						iExample.setValue((Attribute) fv.elementAt(3), Double
								.valueOf(cursor.getString(cursor
										.getColumnIndex(SIZE))));
						if (touchData.getLabel().contains("owner"))
							iExample.setValue((Attribute) fv.elementAt(4),
									cursor.getString(cursor
											.getColumnIndex(LABEL)));
						else
							iExample.setValue((Attribute) fv.elementAt(4),
									OTHER_LABEL);
						// Log.d("readDatabase", "add to training set  ");
						j48.addInstanceToTrainingData(iExample);
						knn.addInstanceToTrainingData(iExample);
						kstar.addInstanceToTrainingData(iExample);
						dt.addInstanceToTrainingData(iExample);
						randomF.addInstanceToTrainingData(iExample);
					}

				} while (cursor.moveToNext());
			}
		} finally {
			cursor.close();
		}
		cursor.close();
		readSource.close();
	}

	private void init() {

		keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
		Keylock = keyguardManager.newKeyguardLock(Activity.KEYGUARD_SERVICE);
		manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdminSample = new ComponentName(LiveWallPaper.this,
				deviceAdminReceiver.class);
		
		// Build classifier model`
		// TODO put the build model in asynTask
		j48 = new J48Classifier();
		knn = new kNNClassifier();
		kstar = new KStarClassifier();
		dt = new DecisionTableFilter();
		randomF = new RandomForestClassifier();
		readDatabase();
		j48.trainingData();
		knn.trainingData();
		kstar.trainingData();
		dt.trainingData();
		randomF.trainingData();
	}
}
