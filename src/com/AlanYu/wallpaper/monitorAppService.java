package com.AlanYu.wallpaper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.AlanYu.database.DBHelper;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.IntentService;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class monitorAppService extends IntentService implements SensorEventListener {

	private String[] protectAppsName={};
	private String errorTag="Exception";
	private String sensorTag = "Sensor data";
	private SensorManager sensorManager ; 
	private static final String tableName = "SENSOR_MAIN";
	private static final String xColumn = "X";
	private static final String yColumn = "Y";
	private static final String zColumn = "Z";

	public monitorAppService() {
		super("monitorAppService");
		// TODO Auto-generated constructor stub
	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	


	@Override
	public void onCreate() {
		Log.d("monitorAppService","onCreate");
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		
		setSensor();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
     	String protectorApp ="氣象";
		if(intent != null){
			Log.d("monitorAppService","onStartCommand");
			
			if(deleteProcessByName(protectorApp))
				Log.d("Delete Process",protectorApp);
			else 
				Log.d("Delete Process","Apps not found");
			// check apps is there any apps is executing in protector list 
				//if(yes) trigger Sensor 
					// trigger userJudgement()
						//if(not user) 
								//verify user password 
								//if(isUser) 
								//else kill process
						//else   let it go 
			    //else   let it go 
		}
		
		else 
			Log.d("monitorAppService","intent is null");
		    return START_STICKY;
	}

	/* =====================================================================
	 * Get Now Running Apps Information 
	===================================================================== */
	private void getAppsInfo(){
		ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> l = am.getRunningAppProcesses();
		Iterator<RunningAppProcessInfo> i = l.iterator();
		PackageManager pm = this.getPackageManager();
		while(i.hasNext()){
			ActivityManager.RunningAppProcessInfo info=(ActivityManager.RunningAppProcessInfo)(i.next());
				try{
				CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
				String processName = info.processName;
				 Log.d("Process", "Id: "+ info.pid +" ProcessName: "+ info.processName +"  Label: "+c.toString());
				}
				catch(Exception e){
					Log.e(errorTag, e.toString());
				}
			 }
	
	}
	
	/*  =====================================================================
	 * Delete the Process by name  
	 * 
	 =====================================================================*/
	 private int findMatchProcessByName(String ps){
		int notFound=0;
		ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> l = am.getRunningAppProcesses();
		Iterator<RunningAppProcessInfo> i = l.iterator();
		PackageManager pm = this.getPackageManager();
		while(i.hasNext()){
			ActivityManager.RunningAppProcessInfo info=(ActivityManager.RunningAppProcessInfo)(i.next());
			try{
				CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
				String processName = info.processName;
				if(c.toString().equalsIgnoreCase(ps)){
					return info.pid;
				 	}
			    }
				catch(Exception e){
				Log.e(errorTag, e.toString());
				}
			 }
		return notFound;
	}
	 
	private boolean deleteProcessByName(String ps)
	{
		int pid=findMatchProcessByName(ps);
	   if(pid!=0){
		   android.os.Process.killProcess(pid);
		   return true;
	   }
	   else
	    return false;
	}
	
	private void setSensor(){
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if(sensors.size()>0){
			sensorManager.registerListener(this, sensors.get(0),SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("intentService","in onHandleIntent");
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}


	@Override
	public void onDestroy() {
		sensorManager.unregisterListener(this);
		super.onDestroy();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
	
			float[] values = event.values; 
			int  timeStamp = (int) (event.timestamp / 1000000000) ; 
//			Log.d(sensorTag, "X :"+String.valueOf(values[0])+ " Y: "+String.valueOf(values[1]) + "z :" + String.valueOf(values[2]) + "TimeStamp :"+String.valueOf(timeStamp));
//			writeDataBase(event);
/*		try {
				readDatabase();
			} catch (SQLException e) {
				e.printStackTrace();
			}*/
	}


	private void writeDataBase(SensorEvent event) {
		
		DBHelper db = new DBHelper(this);
	    SQLiteDatabase writeSource = db.getWritableDatabase();
			float[] values = event.values; 
			ContentValues args = new ContentValues();
			int  timeStamp = (int) (event.timestamp / 1000000000) ; 
			args.put("SENSOR_TYPE","AC");
			args.put("X", String.valueOf(values[0]));
			args.put("Y", String.valueOf(values[1]));
			args.put("Z",String.valueOf(values[2]));
			args.put("TIME_STAMP",String.valueOf(timeStamp));
			long rowid=writeSource.insert("SENSOR_MAIN", null,args);
			Log.d("writeDatabase Event", "id ="+ rowid);
			writeSource.close();
	}

	private void readDatabase() throws SQLException{
		DBHelper db = new DBHelper(this);
		 SQLiteDatabase readSource = db.getReadableDatabase(); 
		 Cursor cursor = readSource.query(tableName,new String[] {xColumn,yColumn,zColumn}, null,null,null,null,null); 
		 double x=0,y=0,z=0;
		 if(cursor !=null){
			 cursor.moveToFirst();
			 while(cursor.isAfterLast() == false ){
				 
				x = Double.valueOf(cursor.getString(cursor.getColumnIndex(xColumn)));
				y = Double.valueOf(cursor.getString(cursor.getColumnIndex(yColumn)));
				z = Double.valueOf(cursor.getString(cursor.getColumnIndex(zColumn)));
				Log.d("Read Database", "x ="+x+"y="+y+"z="+z);
				cursor.moveToNext();
			 }
			 cursor.close();
		 }
	}
}
