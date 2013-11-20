package hig.herd.ngaj;


import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.PathOverlay;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	/**
	 * Declare GUI components
	 * and the map components
	 */

	AlertDialog.Builder Alert;
	TextView txtSteps;
	TextView txtTime;
	TextView txtSpeed;
	TextView txtSpeedExtras;
	TextView txtDistance;
	PathOverlay myPath;
	MyLocationOverlay myLocation;
	MapView mapView;
	MapController mapController;
	Intent serviceIntent;
	Button btnStart;
	Boolean OrientationChange=false;
	
	/*
	 * Key For knowing app state
	 * 0 - Not Recording
	 * 1 - Recording
	 * 2 - Destroyed while recording
	 */
	int k=0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		txtSteps = (TextView)findViewById(R.id.steps2);	
		txtSpeed = (TextView)findViewById(R.id.speed2);
		txtSpeedExtras=(TextView)findViewById(R.id.speed3);
		txtTime = (TextView)findViewById(R.id.time2);	
		txtDistance = (TextView)findViewById(R.id.distance2);	
		btnStart=(Button)findViewById(R.id.btnStart);
		serviceIntent = new Intent(this,GPSservice.class);
		
		/**
		 * Get the MapView widget, set the zoom controllers 
		 * and set the initial zoom level of the map
		 * 
		 * 
		 */
		mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();
        mapController.setZoom(16);
        
        /**
         * Declare myLocationOverlay to show current location pointer on map 
         * also declared path overlay which is used later do draw track
         */
        myLocation = new MyLocationOverlay(this,mapView);
        myPath=new PathOverlay(Color.RED, this);
     }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void onPause()
	{
		super.onPause();
		myLocation.disableMyLocation();
		savePreferences();
		
	}
	public void onDestroy()
	{
		super.onPause();
		stopService(serviceIntent);
		myLocation.disableMyLocation();
		if(k==1)
		{
		k=2;
		}
		savePreferences();
	}
	public void onResume()
	{
		super.onResume();
		getPreferences();
		
		if(k==1 || OrientationChange)
		{
			btnStart.setText("Pause");
			myLocation.enableMyLocation();
			myLocation.enableFollowLocation();
			myLocation.setDrawAccuracyEnabled(false);
	        mapView.getOverlays().add(myLocation);
	        IntentFilter intentFilter = new IntentFilter("hig.herd.NGAJ.RECEIVEDATA");
	        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(this.ReceiveData ,intentFilter);   
	        OrientationChange=false;
		}
		else if(k==2)
		{
			showResumeAlert();
			btnStart.setText("Pause");
		}
		else
		{
			btnStart.setText("Start");
			txtTime.setText("00:00:00");
			txtSpeed.setText("0.00");
			txtDistance.setText("0.00");
			txtSteps.setText("0");
			txtSpeedExtras.setText("0 avg 0 max");
		}
	
	}
   
	private void showResumeAlert()
	{
		Alert = new AlertDialog.Builder(this);
		Alert.setTitle("Continue Work!");
		Alert.setMessage("Do you want to continue your previous workout?");
		Alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				try {
					serviceIntent.putExtra("Key", 1);
					k=1;
					startService(serviceIntent);
					IntentFilter intentFilter = new IntentFilter("hig.herd.NGAJ.RECEIVEDATA");
			        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(ReceiveData ,intentFilter); 
					myLocation.enableMyLocation();
			        myLocation.enableFollowLocation();
			        myLocation.setDrawAccuracyEnabled(false);
			        mapView.getOverlays().add(myLocation);
					 
				} catch (Exception ex) {
					// TODO Auto-generated catch block
				;
				}
				
			}
		});
		Alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			k=0;	
			btnStart.setText("Start");
			txtTime.setText("00:00:00");
			txtSpeed.setText("0.00");
			txtDistance.setText("0.00");
			txtSteps.setText("0");
			txtSpeedExtras.setText("0 avg 0 max");
			myPath.clearPath();
			}
		});
		Alert.show();
				
	}
	
	public void startRecording(View v)
	{ 
		/*
		 * Start/Stop GPSservice based on value of k.
		 */
		if(k==0)
		{
			
		/**
         * Declare and initialize the intent for receiving data from the
         * specific broadcast receiver
         */
        IntentFilter intentFilter = new IntentFilter("hig.herd.NGAJ.RECEIVEDATA");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(this.ReceiveData ,intentFilter);      
        
        //Start Service with Intent
        serviceIntent.putExtra("Key", 5);
        startService(serviceIntent);
        
        //Change button text
        btnStart.setText("Pause");
        k=1;
        
        
        myLocation.enableMyLocation();
        myLocation.enableFollowLocation();
        myLocation.setDrawAccuracyEnabled(false);
        mapView.getOverlays().add(myLocation);
		}
		else if(k==1)
		{
			k=2;
			myLocation.disableMyLocation();
			stopService(serviceIntent);
			showPauseAlert();	
		}
		
	}
	
	public Object onRetainNonConfigurationInstance()
	{
		OrientationChange=true;
		return OrientationChange;
	}
	
	private void startResults()
	{
		k=0;
		savePreferences();
		Intent i = new Intent(MainActivity.this,Results.class);
		i.putExtra("Time", txtTime.getText());
		i.putExtra("Speed",txtSpeed.getText());
		i.putExtra("Steps", txtSteps.getText());
		i.putExtra("SpeedExtras", txtSpeedExtras.getText());
		i.putExtra("Distance", txtDistance.getText());
		startActivity(i);
	}
	
	private void showPauseAlert()
	{
		Alert = new AlertDialog.Builder(this);
		Alert.setTitle("Workout Paused!");
		Alert.setMessage("What you want to do?");
		Alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				try {
					startResults();
										 
				} catch (Exception ex) {
					// TODO Auto-generated catch block
				;
				}
				
			}
		});
		Alert.setNegativeButton("Resume", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			k=1;
			serviceIntent.putExtra("Key", 1);
			startService(serviceIntent);
			myLocation.enableMyLocation();
	        myLocation.enableFollowLocation();
	        myLocation.setDrawAccuracyEnabled(false);
	        mapView.getOverlays().add(myLocation);
			}
		});
		Alert.setNeutralButton("Discard", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			k=0;	
			btnStart.setText("Start");
			txtTime.setText("00:00:00");
			txtSpeed.setText("0.00");
			txtDistance.setText("0.00");
			txtSteps.setText("0");
			txtSpeedExtras.setText("0 avg 0 max");
			myPath.clearPath();
			}
		});
		Alert.show();
				
	}
	
	
	/**
	 * Draws a point into the map using the coordinates given as parameters.
	 * This is done if the Latitude and Longitude are not 0, since this is the default value.
	 */
	private void addPoint(double Latitude,double Longitude)
	{
		GeoPoint Point = new GeoPoint(Latitude,Longitude);
		if(Latitude!=0 && Longitude!=0)
			
			myPath.addPoint(Point);
			mapView.getOverlays().add(myPath);
			mapController.setCenter(Point);
			mapController.setZoom(17);
	}
	
	private void savePreferences()
	{
		getPreferences(MODE_PRIVATE).edit().putInt("Key",k).commit();
		getPreferences(MODE_PRIVATE).edit().putBoolean("OrientationChange", OrientationChange);
		
	}
	private void getPreferences()
	{
		k=getPreferences(MODE_PRIVATE).getInt("Key",0);
		OrientationChange=getPreferences(MODE_PRIVATE).getBoolean("OrientationChange", false);
	}
	
	public void cameraClick(View v)
	{
		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, 1337);
	}
	public void statsClick(View v)
	{
		Intent i = new Intent(MainActivity.this,Stats.class);
		startActivity(i);
	}
	/**
	 * Declaring and implementing a Broadcast Receivers. It will receive data form and save them
	 * in Steps, Latitude and Longitude variables. Also it calls the addPoint function which draws a point on map. 
	 * Log.d - used for  testing purpose!! 
	 */
	private BroadcastReceiver ReceiveData = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			final int Steps=intent.getIntExtra("Steps",0);
			final double Latitude = intent.getDoubleExtra("Latitude", 0);
			final double Longitude = intent.getDoubleExtra("Longitude", 0);
			final String Time = intent.getStringExtra("Time");
			final String Distance = intent.getStringExtra("Distance");
			final String Speed = intent.getStringExtra("Speed");
			final String SpeedExtras = intent.getStringExtra("SpeedExtras");
			addPoint(Latitude,Longitude);
			txtSteps.setText(Integer.toString(Steps));
			txtTime.setText(Time);
			txtDistance.setText(Distance);
			txtSpeed.setText(Speed);
			txtSpeedExtras.setText(SpeedExtras);
			Log.d("BroadCast Recieveri","I Got The message From Service: "+Integer.toString(Steps)+" Latitude: "+Double.toString(Latitude)+" Longitude: "+Double.toString(Longitude));
		}
	}; 

}
