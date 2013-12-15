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

public class GPSservice extends Service implements 
SensorEventListener,
LocationListener
{
    /**
     * Declare service variables
     */
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
	private double magnitude=0;
	private boolean up=false;
	private boolean down=false;
	private int Steps=0;
	private LocationManager mLocationManager; 
	private Context mContext;
	private int NOTIFICATION_ID = 1984;
    private Timer mTimer;
    private int Time=0;
    private float Speed=0;
    private float maxSpeed=0;
    float avgSpeed=0;
    private float totalDistance=0;
    private double lastLat=0;
    private double lastLng=0;
    private double downThreshold;
	private double upThreshold;
	Intent Send;
    
    /**Defines the state of GPSService this value is stored on sharedpreferences
     * and is shared with all other app components.
     * 
     * This Service can have 3 states:
     * 		0 - Stopped
     *      1 - Started
     *      2 - Paused
     */   
	int state =0;
	
	
	public GPSservice() {
		super();
		}


	public void onCreate()
	{
		  getApplicationContext();
          this.mContext=this.getApplicationContext();

          //Initialize listeners by calling declareListeners method
          declareListeners();
          
          //Start service in foreground
          startServiceInForeground();
          
          //Start timer to count time
          startTimer();
          
          /**
           * Change service state to 1(recording) and save this value to sharedpreferences with key GPSServiceState
           * in order to tell other app components service's state
           */         
          state=1;
          getSharedPreferences("GPSServiceState",MODE_PRIVATE).edit().putInt("GPSServiceState",state).commit();
          
          /**
           * Read up and downThreshold from sharedpreferences these are used for the pedometer to count steps.
           */
          downThreshold = getSharedPreferences("downThreshold",MODE_PRIVATE).getFloat("downThreshold",2);
          upThreshold = getSharedPreferences("upThreshold",MODE_PRIVATE).getFloat("upThreshold",8);
		
	}
	/**
	 * This method initializes the sensors and the Send intent which is used to send broadcasts from service
	 * to mainactivity
	 */
	protected void declareListeners() {
		// TODO Auto-generated method stub
			
		mSensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_UI);
        
        
        mLocationManager = (LocationManager)mContext.getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10,this);
        
        
        Send = new Intent();
        Send.setAction("hig.herd.NGAJ.RECEIVEDATA");
		
	}
	public void onDestroy()
	{
		mSensorManager.unregisterListener(this);
		mLocationManager.removeUpdates(this);
		mTimer.cancel();
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
			/**
			 * Here the values of linear acceleration for X,Y and Z are read and from them is calculated the magnitude
			 * then the method checkforstep is called.
			 */
			double x = event.values[0];
			double y = event.values[1];
			double z = event.values[2];
			magnitude=Math.sqrt(x*x+y*y+z*z);

			
			state = getSharedPreferences("GPSServiceState",MODE_PRIVATE).getInt("GPSServiceState",0);
			if(state!=2)
			{
				checkforStep();
			}
		}
		
	}
	/**
	 * This method checks if the current magnitude values indicates a step this method is called for each
	 * magnitude value read from accelerometer and it will indicate a step only for the true sequence of variables:
	 * down,up,down.
	 */
	private void checkforStep()
	{
		if(magnitude>upThreshold)
		{
			if(up && down)
			{
				up=false;
				down=false;
				Steps++;
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
	/**
	 * This method broadcasts the number of steps via Send intent. By using LocalBroadcastManager we prevent other activities
	 * to view this broadcast.
	 */
	private void sendsteps()
	{
		Send.putExtra("Steps", Steps);
		Send.setAction("hig.herd.NGAJ.RECEIVEDATA");
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(Send);
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * This method is called when service is loaded it reads the KEY variable recieved via intent that started the 
	 * service and based on it's value decides to load preferences(continue previous session) or not.
	 * It also sends a broadcast message with current steps,speed,time and distance values.
	 */
	@SuppressWarnings("deprecation")
	public int onStartCommand(Intent intent, int flags, int startId) {
		      super.onStart(intent, startId);
		      
		      /**
		       * Change state variable value to 1 to indicate that service is in recording mode save this variable to
		       * shared preferenes with Key GPSServiceState to tell other components the service state.
		       */
		      state=1;
		      getSharedPreferences("GPSServiceState",MODE_PRIVATE).edit().putInt("GPSServiceState",state).commit();
		      /**
		       * Read the Key variable from intent and decide to load preferences or not based on k value.
		       */
		      int k = intent.getIntExtra("Key", 0);
				if(k==1)
				{
					getPreferences();
				
				}
				
				/**
				 * Send a broadcast message with steps,speed,time and distance initial values.
				 */
				Send.putExtra("Steps", Steps);
				Send.putExtra("Speed", new DecimalFormat("#.##").format((double) (Speed)).toString());
				avgSpeed = totalDistance*3600/Float.parseFloat(Integer.toString(Time));
				String strAvgSpeed = new DecimalFormat("#.##").format(
		                (double) (avgSpeed)).toString();
				if(strAvgSpeed=="NaN")
				{
					strAvgSpeed="0.00";
				}
				String strMaxSpeed = new DecimalFormat("#.##").format(
		                (double) (maxSpeed)).toString();
				String strSpeedExtras = strAvgSpeed+" avg "+strMaxSpeed+" max";
				Send.putExtra("SpeedExtras",strSpeedExtras);
				Send.putExtra("Distance", new DecimalFormat("#.##").format((double) (totalDistance)).toString());
				LocalBroadcastManager.getInstance(mContext).sendBroadcast(Send);
				
		       return  startId;
	}
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		/**
		 * When new location is provided by location provider this method updates the distance, reads the 
		 * current speed from location provider, calculates maxSpeed and avgSpeed and sends the values of
		 * Current Speed, Current Latitude, Current Longitude,totalDistance,avgSpeed,maxSpeed to MainActivity via
		 * local broadcast message.
		 */
		state = getSharedPreferences("GPSServiceState",MODE_PRIVATE).getInt("GPSServiceState",0);
		if(state!=2)
		{

		//Send current latitude and longitude to MainActivity
		Send.putExtra("Latitude", location.getLatitude());
		Send.putExtra("Longitude", location.getLongitude());
		
		//If this is not the first read point calculate distance between this point and previous one.
		if(lastLat!=0 && lastLng!=0)
		{
			totalDistance += Distance(lastLat,lastLng,location.getLatitude(),location.getLongitude());
		}
		
		//Update last point to current point
		lastLat=location.getLatitude();
		lastLng=location.getLongitude();
		
		//Format distance
		String strDistance = new DecimalFormat("#.##").format((double) (totalDistance)).toString();
		Send.putExtra("Distance", strDistance);
		
		//Read speed from current location provided by location provider.
		Speed=location.getSpeed();
		
		//Calculate maxSpeed
		if(Speed>maxSpeed)
		{
			maxSpeed=Speed;
		}
		
		//Calculate avgSpeed
		avgSpeed = totalDistance*3600/Float.parseFloat(Integer.toString(Time));
		String strSpeed = new DecimalFormat("#.##").format(
                (double) (Speed)).toString();
		String strAvgSpeed = new DecimalFormat("#.##").format(
                (double) (avgSpeed)).toString();
		String strMaxSpeed = new DecimalFormat("#.##").format(
                (double) (maxSpeed)).toString();
		String strSpeedExtras = strAvgSpeed+" avg "+strMaxSpeed+" max";
		
		//Add the just calculated variables to intent extras
		Send.putExtra("Speed", strSpeed);
		Send.putExtra("SpeedExtras", strSpeedExtras);
		Send.setAction("hig.herd.NGAJ.RECEIVEDATA");
		
		//Send local broadcast message via intent
	    LocalBroadcastManager.getInstance(mContext).sendBroadcast(Send);	
	}
	}

	private void doTime() {
		/**
		 *  This method is called from timer when activated and it deals with timing, 
		 *  increases the value and formates the time and send it to main activity via broadcast message with intent.
		 */
        
		state = getSharedPreferences("GPSServiceState",MODE_PRIVATE).getInt("GPSServiceState",0);
		if(state!=2)
		{
		 Time += 1;
         
         String result = "";
         int hh=Time / 3600;
         int mm=Time / 60;
         int ss=Time % 60;
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
         if (Time > 3600 * 24) {
                 Time = 0;
         }
         Send.putExtra("Time", result);
         LocalBroadcastManager.getInstance(mContext).sendBroadcast(Send);
		}
 }

	private void startTimer() {
        //This method starts timer, it calls method dotime every second.
		mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {

                public void run() {
                         doTime();
                    }

        }, 0, 1000);
	}
	/**
	 * Saves the value of variables Steps,Time,Speed,maxSpeed,totalDistance,lastLat,lastLng to shared preferences
	 */
	private void savePreferences()
	{
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("steps", Steps).commit();
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putInt("time", Time).commit();
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putFloat("speed", Speed).commit();
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putFloat("maxspeed", maxSpeed).commit();
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putFloat("totalDistance", totalDistance).commit();
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putFloat("lastLat", (float)lastLat).commit();
		PreferenceManager.getDefaultSharedPreferences(mContext).edit().putFloat("lastLng", (float)lastLng).commit();
	}
	/**
	 * Reads the value of variables Steps,Time,Speed,maxSpeed,totalDistance,lastLat,lastLng from shared preferences
	 */
	private void getPreferences()
	{
		Steps=PreferenceManager.getDefaultSharedPreferences(mContext).getInt("steps", 0);
		Time=PreferenceManager.getDefaultSharedPreferences(mContext).getInt("time", 0);
		Speed=PreferenceManager.getDefaultSharedPreferences(mContext).getFloat("speed", 0);
		maxSpeed=PreferenceManager.getDefaultSharedPreferences(mContext).getFloat("maxspeed", 0);
		totalDistance=PreferenceManager.getDefaultSharedPreferences(mContext).getFloat("totalDistance", 0);
		lastLat=(double)PreferenceManager.getDefaultSharedPreferences(mContext).getFloat("lastLat", 0);
		lastLng=(double)PreferenceManager.getDefaultSharedPreferences(mContext).getFloat("lastLng", 0);
	}
	private float Distance(double nLat1, double nLon1, double nLat2,
            double nLon2) {
    /**
     * Taken From Jaimerios.com it uses Haversine formula to calculate
     * distance.
     */

    double nRadius = 6371; // Earth's radius in Kilometers
    /**
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
		 
		 /**
		  * Taken from gtl-hig bitbucket IMT_3662 service example repo.
		  */
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
