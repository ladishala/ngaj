package hig.herd.ngaj;


import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;



import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class GPSservice extends Service implements 
SensorEventListener,
LocationListener
{

	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
	private double magnitude=0;
	private boolean up=false;
	private boolean down=false;
	private int steps=0;
	private LocationManager mLocationManager; 
	private Context mContext;
	private int NOTIFICATION_ID = 1984;
    private Timer timer;
    private int time;
    private float speed;
    private float maxspeed=0;
    float avgSpeed=0;
    private float totaldistance=0;
    private double lastLat=0;
    private double lastLng=0;
    
    /**Defines the state of GPSService this value is stored on sharedpreferences
     * and is shared with all other app components.
     * 
     * This Service can have 3 states:
     * 		0 - Stopped
     *      1 - Started
     *      2 - Paused
     */
    
    
    
	int state =0;
	private double downThreshold;
	private double upThreshold;
	Intent Send;
	public GPSservice() {
		super();
		}

	public void onCreate()
	{
		getApplicationContext();
		this.mContext=this.getApplicationContext();
		declareListeners();
		startServiceInForeground();
		startTimer();
		state=1;
		getSharedPreferences("GPSServiceState",MODE_PRIVATE).edit().putInt("GPSServiceState",state).commit();
		downThreshold=getSharedPreferences("downThreshold",MODE_PRIVATE).getFloat("downThreshold",2);
		upThreshold = getSharedPreferences("upThreshold",MODE_PRIVATE).getFloat("upThreshold",8);
	}
	public void onStart()
	{
		
	}
	protected void declareListeners() {
		// TODO Auto-generated method stub
		
		mSensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_UI);
		
		
		mLocationManager = (LocationManager)mContext.getSystemService(LOCATION_SERVICE);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10,this);
		
		
		Send = new Intent();
		Send.putExtra("Steps", 0);
		Send.putExtra("Speed", "0");
		Send.putExtra("Distance", "0.00");
		Send.setAction("hig.herd.NGAJ.RECEIVEDATA");
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(Send);
	}
	public void onDestroy()
	{
		mSensorManager.unregisterListener(this);
		mLocationManager.removeUpdates(this);
		timer.cancel();
		savePreferences();
		state=0;
		getSharedPreferences("GPSServiceState",MODE_PRIVATE).edit().putInt("GPSServiceState",state).commit();
		
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if(event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION)
		{
			
			double x = event.values[0];
			double y = event.values[1];
			double z = event.values[2];
			magnitude=Math.sqrt(x*x+y*y+z*z);

			
			/*if(magnitude<=min)
			{
				min=magnitude;
				//Log.d("Last min=",Double.toString(min));
			}
			if(magnitude>=max)
			{
				max=magnitude;
				//Log.d("Last max=",Double.toString(max));
			}*/
			state = getSharedPreferences("GPSServiceState",MODE_PRIVATE).getInt("GPSServiceState",0);
			if(state!=2)
			{
	       checkforstep();
			}
		}
		
	}
	private void checkforstep()
	{
		if(magnitude>upThreshold)
		{
			if(up && down)
			{
				up=false;
				down=false;
				steps++;
				sendsteps();
			}
			else
			{
			up = true;
			down=false;
			}
		}
		else if(magnitude<downThreshold)
		{
			down=true;
		}
		

	}
	private void sendsteps()
	{
		Send.putExtra("Steps", steps);
		Send.setAction("hig.herd.NGAJ.RECEIVEDATA");
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(Send);
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		
		
		return null;
	}

	@SuppressWarnings("deprecation")
	public int onStartCommand(Intent intent, int flags, int startId) {
		      super.onStart(intent, startId);
		      state=1;
		      getSharedPreferences("GPSServiceState",MODE_PRIVATE).edit().putInt("GPSServiceState",state).commit();
		       int k = intent.getIntExtra("Key", 0);
				if(k==1)
				{
					getPreferences();
				
				}
		       return  startId;
	}
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if(state!=2)
		{
		state = getSharedPreferences("GPSServiceState",MODE_PRIVATE).getInt("GPSServiceState",0);
		Log.d("Location Changed: ","Latitude: "+Double.toString(location.getLatitude()));
		Send.putExtra("Latitude", location.getLatitude());
		Send.putExtra("Longitude", location.getLongitude());
		
		if(lastLat!=0 && lastLng!=0)
		{
			totaldistance += Distance(lastLat,lastLng,location.getLatitude(),location.getLongitude());
		}
		lastLat=location.getLatitude();
		lastLng=location.getLongitude();
		String strDistance = new DecimalFormat("#.##").format(
                (double) (totaldistance)).toString();
		Send.putExtra("Distance", strDistance);
		
		
		speed=location.getSpeed();
		if(speed>maxspeed)
		{
			maxspeed=speed;
		}
		
		avgSpeed = totaldistance*3600/Float.parseFloat(Integer.toString(time));
		String strSpeed = new DecimalFormat("#.##").format(
                (double) (speed)).toString();
		String strAvgSpeed = new DecimalFormat("#.##").format(
                (double) (avgSpeed)).toString();
		String strMaxSpeed = new DecimalFormat("#.##").format(
                (double) (maxspeed)).toString();
		String strSpeedExtras = strAvgSpeed+" avg "+strMaxSpeed+" max";
		Send.putExtra("Speed", strSpeed);
		Send.putExtra("SpeedExtras", strSpeedExtras);
		Send.setAction("hig.herd.NGAJ.RECEIVEDATA");
		
				
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(Send);	
	}
	}

	private void doTime() {
         //This method is called from timer when activated and it deals with timing, increases the value and shows on UI a proper format of time.
		state = getSharedPreferences("GPSServiceState",MODE_PRIVATE).getInt("GPSServiceState",0);
		if(state!=2)
		{
		 time += 1;
         
         String result = "";
         int hh=time / 3600;
         int mm=time / 60;
         int ss=time % 60;
         if(hh<10)
         {
        	 result +="0";
         }
         result +=Integer.toString(hh)+":";
         if(mm<10)
         {
        	 result +="0";
         }
         result +=Integer.toString(mm)+":";
         if(ss<10)
         {
        	 result +="0";
         }
         result +=Integer.toString(ss);
         if (time > 3600 * 24) {
                 time = 0;
         }
         Send.putExtra("Time", result);
         LocalBroadcastManager.getInstance(mContext).sendBroadcast(Send);
		}
 }

	private void startTimer() {
        //This method starts timer when in record mode this timer calls method dotime every second.
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

                public void run() {
                         doTime();
                    }

        }, 0, 1000);
	}
	private void savePreferences()
	{
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("steps", steps).commit();
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("time", time).commit();
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putFloat("speed", speed).commit();
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putFloat("maxspeed", maxspeed).commit();
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putFloat("totalDistance", totaldistance).commit();
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putFloat("lastLat", (float)lastLat).commit();
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putFloat("lastLng", (float)lastLng).commit();
	}
	private void getPreferences()
	{
		steps=PreferenceManager.getDefaultSharedPreferences(mContext).getInt("steps", 0);
		time=PreferenceManager.getDefaultSharedPreferences(mContext).getInt("time", 0);
		speed=PreferenceManager.getDefaultSharedPreferences(mContext).getFloat("speed", 0);
		maxspeed=PreferenceManager.getDefaultSharedPreferences(mContext).getFloat("maxspeed", 0);
		totaldistance=PreferenceManager.getDefaultSharedPreferences(mContext).getFloat("totalDistance", 0);
		lastLat=(double)PreferenceManager.getDefaultSharedPreferences(mContext).getFloat("lastLat", 0);
		lastLng=(double)PreferenceManager.getDefaultSharedPreferences(mContext).getFloat("lastLng", 0);
	}
	private float Distance(double nLat1, double nLon1, double nLat2,
            double nLon2) {
    /*
     * Taken From Jaimerios.com it uses Haversine formula to calculate
     * distance.
     */

    double nRadius = 6371; // Earth's radius in Kilometers
    /*
     * Get the difference between our two points then convert the difference
     * into radians
     */

    double nDLat = Math.toRadians(nLat2 - nLat1);
    double nDLon = Math.toRadians(nLon2 - nLon1);

    // Here is the new line
    nLat1 = Math.toRadians(nLat1);
    nLat2 = Math.toRadians(nLat2);

    double nA = Math.pow(Math.sin(nDLat / 2), 2) + Math.cos(nLat1)
                    * Math.cos(nLat2) * Math.pow(Math.sin(nDLon / 2), 2);
    double nC = 2 * Math.atan2(Math.sqrt(nA), Math.sqrt(1 - nA));
    double nD = nRadius * nC;

    return (float) nD; // Return our calculated distance
}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	 private void startServiceInForeground() {
         Bitmap img1=BitmapFactory.decodeResource(getResources(), R.drawable.runn);
         	// What we do here is:
         	// 1. setup what Intent should be invoked when we select a notification
         final Intent i = new Intent(this, MainActivity.class);
         i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
         PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
         // 2. setup the notification (in the notification bar up on the top of the screen)
         final Notification note = new NotificationCompat.Builder(this)
         .setContentTitle("NGAJ")
         .setContentText("Tracking Location and Counting Steps.")
         //.setSmallIcon(android.R.drawable.arrow_down_float)
         .setLargeIcon(img1)
         .setSmallIcon(R.drawable.runn)
         .setContentIntent(pi)
         .build();
         note.icon=R.drawable.runn;
         note.flags |= Notification.FLAG_NO_CLEAR; // the notification cannot be cleared
         // 3. run our service in the foreground, with the notification attached
         startForeground(NOTIFICATION_ID, note);
	 }
}
