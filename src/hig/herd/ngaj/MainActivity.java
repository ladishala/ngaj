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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	/**
	 * Declare social networking share components
	 * and the map components
	 */

	SocialAuthAdapter adapter;
	LinearLayout bar;
	AlertDialog.Builder Alert;
	TextView helloworld;
	PathOverlay myPath;
	MapView mapView;
	MapController mapController;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		

		myPath=new PathOverlay(Color.RED, this);
				
		helloworld = (TextView)findViewById(R.id.textView1);
		bar = (LinearLayout) findViewById(R.id.linearbar);
		bar.setBackgroundResource(R.drawable.bar_gradient);

		adapter = new SocialAuthAdapter(new ResponseListener());

		// Add providers
		adapter.addProvider(Provider.FACEBOOK, R.drawable.facebook);
		adapter.addProvider(Provider.TWITTER, R.drawable.twitter);
		adapter.addProvider(Provider.EMAIL, R.drawable.other);
		adapter.enable(bar);
		
		
		/**
		 * Get the MapView widget, set the zoom controllers 
		 * and set the initial zoom level of the map
		 */
		mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();
        mapController.setZoom(3);

        /**
         * Declare and initialize the intent for receiving data from the
         * specific broadcast receiver
         */
        IntentFilter intentFilter = new IntentFilter("hig.herd.NGAJ.RECEIVEDATA");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(this.ReceiveData ,intentFilter);      
        
        //Declare an object of the service we created
        GPSservice gps = new GPSservice(getApplicationContext());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
   
	/**
	 * The function for taking a screenshot of the view given as
	 * parameter. It returns the bitmap (screenshot) created. 
	 */
	public Bitmap screenShot(View view) {
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		return bitmap;
	}


	/**
	 * Defines the dialog that will show up allowing the user to decide whether he/she wants 
	 * to post or not. If the answer is positive the image will be shared using 
	 * a built in method of the SouthAuth object. The parameters required by the method are
	 * the description that will be used when sharing the image, the image's name, the bitmap (actual image 
	 * to be shared) and a SocialAuthListener object.
	 * If the user chooses not to share the image no action will be performed.
	 */
	private void showalert(String arg)
	{
		Alert = new AlertDialog.Builder(this);
		Alert.setTitle("Confirm Share!");
		Alert.setMessage("Are you sure you want to share your results on "+arg+"?");
		Alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				try {
					adapter.uploadImageAsync("Description", "Name.png",screenShot(bar), 0, new MessageListener());
				} catch (Exception ex) {
					// TODO Auto-generated catch block
				Toast.makeText(MainActivity.this, ex.getMessage(),Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		Alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
							
				
			}
		});
		Alert.show();
				
	}
	
	/**
	 * This method allows posting the image on other social networks.
	 * First it determines the directory where the image will be stored in the phone, then puts 
	 * the image into that place. Gets the Uri of the saved image. 
	 * The image will be shared using "share" intent.  
	 * 
	 */
	private void postonother()
	{
				
		String filename = "NGAJ_lastScreenShot.png";
		File sd = Environment.getExternalStorageDirectory();
		File dest = new File(sd, filename);
		Bitmap bitmap = screenShot(bar);
		
		try {
		     FileOutputStream out = new FileOutputStream(dest);
		     bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		     out.flush();
		     out.close();
		     
		     Uri yourUri = Uri.fromFile(dest);
			    Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("image/jpeg");
				share.putExtra(Intent.EXTRA_STREAM, yourUri);
				
				startActivity(Intent.createChooser(share, "Share Image"));
		} catch (Exception e) {
		     
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
	 * Deciding what to do when a certain element of the SocialAuthAdapter
	 * is selected.  
	 */
	private final class ResponseListener implements DialogListener {
		public void onComplete(Bundle values) {

			// Get name of provider after authentication
			final String providerName = values
					.getString(SocialAuthAdapter.PROVIDER);
			if(providerName=="facebook"||providerName=="twitter")
			{	
				showalert(providerName);
			}
			else
			{
				postonother();
			}

		}

		@Override
		public void onBack() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(SocialAuthError arg0) {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * The message to be displayed to the user after posting. Depending on the status 
	 * of the post, if it was successful. 
	 * If an error occurs perform no actions.  
	 */
	private final class MessageListener implements SocialAuthListener<Integer> {
		@Override
		public void onExecute(String provider, Integer t) {
			Integer status = t;
			if (status.intValue() == 200 || status.intValue() == 201
					|| status.intValue() == 204)
				Toast.makeText(MainActivity.this,
						"Message posted on " + provider, Toast.LENGTH_LONG)
						.show();
			else
				Toast.makeText(MainActivity.this,
						"Message not posted on" + provider, Toast.LENGTH_LONG)
						.show();
		}

		@Override
		public void onError(SocialAuthError e) {

		}
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
			addPoint(Latitude,Longitude);
			helloworld.setText("Total Steps: "+Integer.toString(Steps));
			Log.d("BroadCast Recieveri","I Got The message From Service: "+Integer.toString(Steps)+" Latitude: "+Double.toString(Latitude)+" Longitude: "+Double.toString(Longitude));
		}
	}; 

}
