package hig.herd.ngaj;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
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
	
	private double max=0;
	private double min=100;
	Intent Send;
	public GPSservice() {
		super();
		}

	public void onCreate()
	{
		getApplicationContext();
		this.mContext=this.getApplicationContext();
		declareListeners();
		
	}
	protected void declareListeners() {
		// TODO Auto-generated method stub
		
		mSensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_UI);
		
		
		mLocationManager = (LocationManager)mContext.getSystemService(LOCATION_SERVICE);
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,5,this);
		
		
		Send = new Intent();
		Send.putExtra("Steps", 0);
		Send.setAction("hig.herd.NGAJ.RECEIVEDATA");
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(Send);
	}
	public void onDestroy()
	{
		mSensorManager.unregisterListener(this);
		mLocationManager.removeUpdates(this);
		
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
	       checkforstep();
		}
		
	}
	private void checkforstep()
	{
		if(magnitude>12)
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
		else if(magnitude<1)
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


	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Log.d("Location Changed: ","Latitude: "+Double.toString(location.getLatitude()));
		Send.putExtra("Latitude", location.getLatitude());
		Send.putExtra("Longitude", location.getLongitude());
		Send.setAction("hig.herd.NGAJ.RECEIVEDATA");
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(Send);	
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
}
