package hig.herd.ngaj;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthAdapter.Provider;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;
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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	/**
	 * Declare GUI comonents
	 * and the map components
	 */

	AlertDialog.Builder Alert;
	TextView txtSteps;
	TextView txtTime;
	TextView txtSpeed;
	TextView txtDistance;
	PathOverlay myPath;
	MyLocationOverlay myLocation;
	MapView mapView;
	MapController mapController;
	Intent serviceIntent;
	Button btnStart;
	
	/*
	 * Key For knowing app state
	 * 0 - Not Recording
	 * 1 - Recording
	 */
	int k=0;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		txtSteps = (TextView)findViewById(R.id.steps2);	
		txtSpeed = (TextView)findViewById(R.id.speed2);	
		txtTime = (TextView)findViewById(R.id.time2);	
		txtDistance = (TextView)findViewById(R.id.distance2);	
		btnStart=(Button)findViewById(R.id.btnStart);
		
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
        serviceIntent = new Intent(this,GPSservice.class);
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
			stopService(serviceIntent);
			myLocation.disableMyLocation();
			btnStart.setText("Start");
			k=0;
		}
		
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
			final String Speed = intent.getStringExtra("Time");
			addPoint(Latitude,Longitude);
			txtSteps.setText(Integer.toString(Steps));
			txtTime.setText(Time);
			txtDistance.setText(Distance);
			txtSpeed.setText(Speed);
			Log.d("BroadCast Recieveri","I Got The message From Service: "+Integer.toString(Steps)+" Latitude: "+Double.toString(Latitude)+" Longitude: "+Double.toString(Longitude));
		}
	}; 

}
