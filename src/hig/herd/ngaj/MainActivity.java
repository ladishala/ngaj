package hig.herd.ngaj;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
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
	ProgressBar mProgress;
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
	
	int TotalScore=0;
	int Level;
	ImageView CurrentLevel;
	ImageView NextLevel;
	Timer timer;


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
		mProgress =(ProgressBar)findViewById(R.id.progressBar1);
		CurrentLevel=(ImageView)findViewById(R.id.CurrentLevel);
		NextLevel=(ImageView)findViewById(R.id.NextLevel);
		serviceIntent = new Intent(this,GPSservice.class);
		
		/**
		 * Get the MapView widget, set the zoom controllers 
		 * and set the initial zoom level of the map
		 * 
		 * 
		 */
		mapView = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapview)).getMap();
        
		getLevel();
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
		savePreferences();
		mapView.setMyLocationEnabled(false);
	}
	public void onDestroy()
	{
		super.onDestroy();
		stopService(serviceIntent);
		mapView.setMyLocationEnabled(false);
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
			mapView.setMyLocationEnabled(true);
	        IntentFilter intentFilter = new IntentFilter("hig.herd.NGAJ.RECEIVEDATA");
	        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(this.ReceiveData ,intentFilter);   
	                
	        if(k==2 && OrientationChange)
	        {
	        	serviceIntent.putExtra("Key", 1);
	        	startService(serviceIntent);
	        }
	        OrientationChange=false;
	        k=1; 	        
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
					mapView.setMyLocationEnabled(true);
					 
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
                    mapView.setMyLocationEnabled(false);
                    stopService(serviceIntent);
					 
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
        serviceChecker();
        latLngList.clear();
        mapView.clear();
        mapView.setMyLocationEnabled(true);
		}
		else if(k==1)
		{
			getSharedPreferences("GPSServiceState",MODE_PRIVATE).edit().putInt("GPSServiceState",2).commit();
			showPauseAlert();	
		}
		
	}
	
	private void serviceChecker()
	{
	 //This method checks if the service is runing while the app is in the record mode if OS killes the service 
     // then this method will restart it.
	 // for performance issues we decided to trigger this checker to check every 5 seconds.
	 timer = new Timer();
	 timer.scheduleAtFixedRate(new TimerTask() {
		 
	    public void run() {
	    	
	       if(k==1 && getSharedPreferences("GPSServiceState",MODE_PRIVATE).getInt("GPSServiceState",0)==0)
	        {
	         serviceIntent.putExtra("Key", 1);
	         startService(serviceIntent);
	        }
	        else if(k==0)
	   	   {
	    	   	timer.cancel();
	   	   }
	    	   }
	      }, 5000, 5000);
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
		extras.putInt("Level", Level);
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
			getSharedPreferences("GPSServiceState",MODE_PRIVATE).edit().putInt("GPSServiceState",1).commit();
			mapView.setMyLocationEnabled(true);
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
					stopService(serviceIntent);
					mapView.setMyLocationEnabled(false);
					
					 
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
		mapView.clear();
		mapView.addPolyline(new PolylineOptions().addAll(latLngList)
                .width(6).color(-16776961));
		}
	}
	private void getLevel()
	{
		TotalScore=getSharedPreferences("TotalScore",MODE_PRIVATE).getInt("TotalScore",0);
		Level = calculateLevel(TotalScore);	
	}
	private int calculateLevel(int score)
	{
		int result=1;
		if(score>=10500)
		{
			result=5;
			mProgress.setMax(score);
			mProgress.setProgress(score);
			CurrentLevel.setImageResource(R.drawable.elite);
			NextLevel.setImageResource(R.drawable.elite);
		}
		else if(score>=5750)
		{
			result=4;
			mProgress.setMax(10500-5750);
			mProgress.setProgress(score-5750);
			CurrentLevel.setImageResource(R.drawable.four);
			NextLevel.setImageResource(R.drawable.five);
		}
		else if(score>=2750)
		{
			result=3;
			mProgress.setMax(5750-2750);
			mProgress.setProgress(score-2750);
			CurrentLevel.setImageResource(R.drawable.three);
			NextLevel.setImageResource(R.drawable.four);
		}
		else if(score>=1000)
		{
			result=2;
			mProgress.setMax(2750-1000);
			mProgress.setProgress(score-1000);
			CurrentLevel.setImageResource(R.drawable.two);
			NextLevel.setImageResource(R.drawable.three);
		}
		else
		{
			result=1;
			mProgress.setMax(2750);
			mProgress.setProgress(score);
			CurrentLevel.setImageResource(R.drawable.one);
			NextLevel.setImageResource(R.drawable.two);
		}
		
		return result;
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
