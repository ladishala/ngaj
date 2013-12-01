package hig.herd.ngaj;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.SupportMapFragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

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
	GoogleMap mapView;
	ArrayList<LatLng> latLngList = new ArrayList<LatLng>();

	Intent serviceIntent;
	Button btnStart;
	Boolean OrientationChange;
	
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
		mapView = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapview)).getMap();
		mapView.setMyLocationEnabled(true);
		//mapView.setMyLocationEnabled(true);
        /*mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();
        mapController.setZoom(16);*/
        
        /**
         * Declare myLocationOverlay to show current location pointer on map 
         * also declared path overlay which is used later do draw track
         */
       /* myLocation = new MyLocationOverlay(this,mapView);
        myPath=new PathOverlay(Color.RED, this);*/
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
		//myLocation.disableMyLocation();
		savePreferences();
		
	}
	public void onDestroy()
	{
		super.onPause();
		stopService(serviceIntent);
		//myLocation.disableMyLocation();
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
		
		if(k==1 || (k==2 && OrientationChange))
		{
			btnStart.setText("Pause");
			//myLocation.enableMyLocation();
			//myLocation.enableFollowLocation();
			//myLocation.setDrawAccuracyEnabled(false);
	        //mapView.getOverlays().add(myLocation);
	        IntentFilter intentFilter = new IntentFilter("hig.herd.NGAJ.RECEIVEDATA");
	        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(this.ReceiveData ,intentFilter);   
	        OrientationChange=false;
	        k=1;
	        serviceIntent.putExtra("Key", k);
	        startService(serviceIntent);
	        
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
					//myLocation.enableMyLocation();
			        //myLocation.enableFollowLocation();
			        //myLocation.setDrawAccuracyEnabled(false);
			        //mapView.getOverlays().add(myLocation);
					 
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
			latLngList.clear();
			mapView.clear();
			}
		});
		Alert.show();
				
	}
	private void showDiscardAlert()
	{
		Alert = new AlertDialog.Builder(this);
		Alert.setTitle("Discard Work!");
		Alert.setMessage("Do you want to discard your current work?\nThis cannot be undone!");
		Alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				try {
					k=0;	
					btnStart.setText("Start");
					txtTime.setText("00:00:00");
					txtSpeed.setText("0.00");
					txtDistance.setText("0.00");
					txtSteps.setText("0");
					txtSpeedExtras.setText("0 avg 0 max");
					latLngList.clear();
                    mapView.clear();
					 
				} catch (Exception ex) {
					// TODO Auto-generated catch block
				;
				}
				
			}
		});
		Alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showPauseAlert();
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
        
        latLngList.clear();
        mapView.clear();
        
		}
		else if(k==1)
		{
			k=2;
			stopService(serviceIntent);
			showPauseAlert();	
		}
		
	}
	
	
	public Object onRetainCustomNonConfigurationInstance()
	{
		OrientationChange=true;
		return OrientationChange;
	}
	
	private void startResults(String Name)
	{
		k=0;
		savePreferences();
		Intent i = new Intent(MainActivity.this,Results.class);
		Intent s = new Intent(MainActivity.this,DBservice.class);
		Bundle extras = new Bundle();
		extras.putString("Name", Name);
		extras.putString("Time", (String) txtTime.getText());
		extras.putString("Speed",(String) txtSpeed.getText());
		extras.putString("Steps", (String) txtSteps.getText());
		extras.putString("SpeedExtras", (String) txtSpeedExtras.getText());
		extras.putString("Distance", (String) txtDistance.getText());
		extras.putInt("Size", latLngList.size());		 
		for (int j = 0; j < latLngList.size(); j++) {
			extras.putFloat("Cord_Lat_" + j,(float) latLngList.get(j).latitude);
			extras.putFloat("Cord_Long_" + j,(float) latLngList.get(j).longitude);
			}
		i.putExtras(extras);
		s.putExtras(extras);
		startService(s);				
		startActivity(i);	
		latLngList.clear();
		mapView.clear();
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
					showNameTrackAlert();
															 
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
			}
		});
		Alert.setNeutralButton("Discard", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			showDiscardAlert();
			}
		});
		Alert.show();
				
	}
	
	
	private void showNameTrackAlert()
	{
		Alert = new AlertDialog.Builder(this);
		Alert.setTitle("Name Track");
		Alert.setMessage("Give a name to your track.");
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setSingleLine(true);
	
		Alert.setView(input);
		Alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				try {
					
					startResults(input.getText().toString());
					
					 
				} catch (Exception ex) {
					// TODO Auto-generated catch block
				;
				}
				
			}
		});
		Alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			showPauseAlert();
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
		if(Latitude!=0 &&Longitude!=0)
		{
		LatLng Point = new LatLng(Latitude,Longitude);
		CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(Point,15);
        mapView.animateCamera(camUpdate);
		latLngList.add(Point);
		mapView.addPolyline(new PolylineOptions().addAll(latLngList)
                .width(6).color(-16776961));
		}
	}
	
	private void savePreferences()
	{
		getPreferences(MODE_PRIVATE).edit().putInt("Key",k).commit();
		getPreferences(MODE_PRIVATE).edit().putBoolean("OrientationChange", OrientationChange).commit();
		getPreferences(MODE_PRIVATE).edit().putInt("Size", latLngList.size()).commit();
		 
		for (int i = 0; i < latLngList.size(); i++) {
            getPreferences(MODE_PRIVATE).edit().remove("Cord_Lat_" + i).commit();
            getPreferences(MODE_PRIVATE).edit().remove("Cord_Long_" + i).commit();
            getPreferences(MODE_PRIVATE).edit().putFloat("Cord_Lat_" + i,(float) latLngList.get(i).latitude).commit();
            getPreferences(MODE_PRIVATE).edit().putFloat("Cord_Long_" + i,(float) latLngList.get(i).longitude).commit();
			}
		
	}
	private void getPreferences()
	{
		k=getPreferences(MODE_PRIVATE).getInt("Key",0);
		OrientationChange=getPreferences(MODE_PRIVATE).getBoolean("OrientationChange", false);
		latLngList.clear();
        int size = getPreferences(MODE_PRIVATE).getInt("Size", 0);
        for (int i = 0; i < size; i++) {
            double lat = (double) getPreferences(MODE_PRIVATE).getFloat("Cord_Lat_" + i, (float) 5.0);
            double lng = (double) getPreferences(MODE_PRIVATE).getFloat("Cord_Long_" + i, (float) 5.0);
            latLngList.add(new LatLng(lat, lng));
        }
	}
	
	public void cameraClick(View v)
	{
		Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivity(cameraIntent);
	}
	public void statsClick(View v)
	{
		Intent i = new Intent(MainActivity.this,Stats.class);
		startActivity(i);
	}
	public void viewTracks(View v)
	{
		Intent i = new Intent(MainActivity.this,Tracks.class);
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
			if(latLngList.isEmpty())
			{
				addPoint(Latitude,Longitude);
			}
			else if(Latitude!=latLngList.get(latLngList.size()-1).latitude && Longitude!=latLngList.get(latLngList.size()-1).longitude)
			{
			addPoint(Latitude,Longitude);
			}
			txtSteps.setText(Integer.toString(Steps));
			txtTime.setText(Time);
			txtDistance.setText(Distance);
			txtSpeed.setText(Speed);
			txtSpeedExtras.setText(SpeedExtras);
			Log.d("BroadCast Recieveri","I Got The message From Service: "+Integer.toString(Steps)+" Latitude: "+Double.toString(Latitude)+" Longitude: "+Double.toString(Longitude));
		}
	};

}
