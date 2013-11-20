package hig.herd.ngaj;



import java.io.File;
import java.io.FileOutputStream;

import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;
import org.brickred.socialauth.android.SocialAuthAdapter.Provider;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Results extends Activity {

	
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
	TextView txtSteps;
	TextView txtTime;
	TextView txtSpeed;
	TextView txtSpeedExtras;
	TextView txtDistance;
	RelativeLayout Layout1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);
		
		Intent i = getIntent();
		
		helloworld = (TextView)findViewById(R.id.rhelloworld);
		txtSteps = (TextView)findViewById(R.id.rsteps2);	
		txtSpeed = (TextView)findViewById(R.id.rspeed2);
		txtSpeedExtras=(TextView)findViewById(R.id.rspeed3);
		txtTime = (TextView)findViewById(R.id.rtime2);	
		txtDistance = (TextView)findViewById(R.id.rdistance2);
		
		String strSteps=i.getStringExtra("Steps");
		String strSpeed=i.getStringExtra("Speed");
		String strSpeedExtras=i.getStringExtra("SpeedExtras");
		String strTime=i.getStringExtra("Time");
		String strDistance=i.getStringExtra("Distance");
		
		txtSteps.setText(strSteps);
		txtSpeed.setText(strSpeed);
		txtSpeedExtras.setText(strSpeedExtras);
		txtTime.setText(strTime);
		txtDistance.setText(strDistance);
		Layout1=(RelativeLayout)findViewById(R.id.Layout2);
		
		
		
		bar = (LinearLayout) findViewById(R.id.linearbar2);
		bar.setBackgroundResource(R.drawable.bar_gradient);

		adapter = new SocialAuthAdapter(new ResponseListener());

		// Add providers
		adapter.addProvider(Provider.FACEBOOK, R.drawable.facebook);
		adapter.addProvider(Provider.TWITTER, R.drawable.twitter);
		adapter.addProvider(Provider.MMS, R.drawable.other);
		adapter.addProvider(Provider.EMAIL, R.drawable.camera);
		adapter.enable(bar);
		

		
		
		/**
		 * Get the MapView widget, set the zoom controllers 
		 * and set the initial zoom level of the map
		 */
		mapView = (MapView) findViewById(R.id.rmapview);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();
        mapController.setZoom(3);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.results, menu);
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
					String Message = "I have ran "+txtDistance.getText()+" km with NGAJ. \nHere are my stats.";
					adapter.uploadImageAsync(Message, "Name.png",screenShot(Layout1), 0, new MessageListener());
				} catch (Exception ex) {
					// TODO Auto-generated catch block
				Toast.makeText(Results.this, ex.getMessage(),Toast.LENGTH_SHORT).show();
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
			else if(providerName=="share_mail")
			{
				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivity(cameraIntent);
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
				Toast.makeText(Results.this,
						"Message posted on " + provider, Toast.LENGTH_LONG)
						.show();
			else
				Toast.makeText(Results.this,
						"Message not posted on" + provider, Toast.LENGTH_LONG)
						.show();
				}

		@Override
		public void onError(SocialAuthError e) {

		}
	}

}
