package hig.herd.ngaj;




import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Locale;


import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;
import org.brickred.socialauth.android.SocialAuthAdapter.Provider;



import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Results extends FragmentActivity {

	
	/**
	 * Declare social networking share components,
	 * map components, UI components and acitivity variables.
	 */

	SocialAuthAdapter adapter;
	LinearLayout bar;
	AlertDialog.Builder Alert;
	TextView txtRank;
	GoogleMap mapView;
	TextView txtSteps;
	TextView txtTime;
	TextView txtSpeed;
	TextView txtSpeedExtras;
	TextView txtDistance;
	RelativeLayout Layout1;
	ArrayList<LatLng> latLngList = new ArrayList<LatLng>();
	String Filename ="";
	int TotalScore=0;
	int Level;
	ImageView imgCurrentLevel;
	ImageView imgNextLevel;
	ProgressBar mProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);
		
		//Initialize UI component variables.
		txtRank = (TextView)findViewById(R.id.txtRank);
		txtSteps = (TextView)findViewById(R.id.rsteps2);	
		txtSpeed = (TextView)findViewById(R.id.rspeed2);
		txtSpeedExtras=(TextView)findViewById(R.id.rspeed3);
		txtTime = (TextView)findViewById(R.id.rtime2);	
		txtDistance = (TextView)findViewById(R.id.rdistance2);
		imgCurrentLevel=(ImageView)findViewById(R.id.rCurrentLevel);
		imgNextLevel = (ImageView)findViewById(R.id.rNextLevel);
		mProgress =(ProgressBar)findViewById(R.id.rprogressBar1);
		Layout1=(RelativeLayout)findViewById(R.id.Layout2);
		bar = (LinearLayout) findViewById(R.id.linearbar2);
		
		//Read TotalScore from shared preferenes
		TotalScore=getSharedPreferences("TotalScore",MODE_PRIVATE).getInt("TotalScore",0);
		
		//Read values from intent which started the activity
		Intent i = getIntent();
		String strSteps=i.getStringExtra("Steps");
		String strSpeed=i.getStringExtra("Speed");
		String strSpeedExtras=i.getStringExtra("SpeedExtras");
		String strTime=i.getStringExtra("Time");
		String strDistance=i.getStringExtra("Distance");
		Level=i.getIntExtra("Level", 0);
		int size = i.getIntExtra("Size", 0);
		if(size>0)
		{
			for(int j=0;j<size;j++)
			{
				double lat = (double) i.getFloatExtra("Cord_Lat_" + j, (float) 5.0);
            	double lng = (double) i.getFloatExtra("Cord_Long_" + j, (float) 5.0);
            	latLngList.add(new LatLng(lat, lng));
			}
		}
		
		//Set text of UI components with just read values.
		txtSteps.setText(strSteps);
		txtSpeed.setText(strSpeed);
		txtSpeedExtras.setText(strSpeedExtras);
		txtTime.setText(strTime);
		txtDistance.setText(strDistance);
		
		//Initialize social authentication adapter
		adapter = new SocialAuthAdapter(new ResponseListener());

		// Add providers to social authentication adapter.
		adapter.addProvider(Provider.FACEBOOK, R.drawable.facebook);
		adapter.addProvider(Provider.TWITTER, R.drawable.twitter);
		adapter.addProvider(Provider.MMS, R.drawable.other);
		adapter.addProvider(Provider.EMAIL, R.drawable.camera);
		adapter.enable(bar);
		
	
		/**
		 * Get the GoogleMaps Fragment widget
		 */
		mapView = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.rmapview)).getMap();
		
		/**
		 * If size value is greater than 0 it means that some location points are recieved with the intent that
		 * started the activity. If so these points are saved on latLngList and here they are drawn on the map. 
		 */
		if(size>0)
		{
			CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(latLngList.get(latLngList.size()/2),14);
	        mapView.animateCamera(camUpdate);
			mapView.addPolyline(new PolylineOptions().addAll(latLngList).width(6).color(-16776961));
			mapView.addMarker(new MarkerOptions().position(latLngList.get(0)).title("Start Point"));
			mapView.addMarker(new MarkerOptions().position(latLngList.get(latLngList.size()-1)).title("End Point"));
		}
		
		/**
		 * This piece of code is to check if user has just saved the track or he is viewing a previously saved track.
		 * When viewing the previously saved track the Level value is not sent via intent in this case only methods 
		 * calculateMedal and calculateLevel are called otherwise method addScore is called.
		 */
		if(Level!=0)
		{
			addScore();
		}
		else
		{
			calculateLevel(TotalScore);
			calculateMedal();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    if(item.getItemId()==R.id.action_settings)
	    {
	    	Intent i = new Intent(Results.this,CalibratePedometer.class);
			startActivity(i);
			
	    }
	    return true;
	    
	}
	
	/**
	 * This method updates TotalScore by adding current tracks score calculated by calling calculateMedal method 
	 * next it saves the updated TotalScore on shared preferenes.
	 * 
	 * Also this method calculated the diference of user's levels by including the score the score of current track 
	 * and by excluding it. If the difference is 1 it means user has raised one level up and the levelUp activity with
	 * the raised level sent via intent is started.
	 */
	private void addScore()
	{
		int diff = calculateLevel(TotalScore+calculateMedal())-Level;
		TotalScore +=calculateMedal();
		getSharedPreferences("TotalScore",MODE_PRIVATE).edit().putInt("TotalScore",TotalScore).commit();
		if(diff==1)
		{
			Intent i = new Intent(Results.this,LevelUp.class);
			i.putExtra("Level", (Level+1));
			startActivity(i);
		}
	}
	
	/**
	 * This method calculates the level based on parameter score and it also fills the progressbar and 
	 * puts the right images on the current and nextlevel imageviews and returns current level.
	 */
	private int calculateLevel(int score)
	{
		int result=1;
		if(score>=10500)
		{
			result=5;
			mProgress.setMax(score);
			mProgress.setProgress(score);
			imgCurrentLevel.setImageResource(R.drawable.elite);
			imgNextLevel.setImageResource(R.drawable.elite);
		}
		else if(score>=5750)
		{
			result=4;
			mProgress.setMax(10500-5750);
			mProgress.setProgress(score-5750);
			imgCurrentLevel.setImageResource(R.drawable.four);
			imgNextLevel.setImageResource(R.drawable.five);
		}
		else if(score>=2750)
		{
			result=3;
			mProgress.setMax(5750-2750);
			mProgress.setProgress(score-2750);
			imgCurrentLevel.setImageResource(R.drawable.three);
			imgNextLevel.setImageResource(R.drawable.four);
		}
		else if(score>=1000)
		{
			result=2;
			mProgress.setMax(2750-1000);
			mProgress.setProgress(score-1000);
			imgCurrentLevel.setImageResource(R.drawable.two);
			imgNextLevel.setImageResource(R.drawable.three);
		}
		else
		{
			result=1;
			mProgress.setMax(1000);
			mProgress.setProgress(score);
			imgCurrentLevel.setImageResource(R.drawable.one);
			imgNextLevel.setImageResource(R.drawable.two);
		}
		
		return result;
	}
	
	/**
	 * This method calculates the rank of current track based on distance and average speed
	 * and returns achieved points from this track.
	 */
	private int calculateMedal()
	{
		int result =0;
		double Distance= 0;
		double avgSpeed= 0;
		
		try
		{
			int end = txtSpeedExtras.getText().toString().indexOf(" ");
			Distance = Double.parseDouble(txtDistance.getText().toString());
			avgSpeed = Double.parseDouble(txtSpeedExtras.getText().toString().substring(0,end));
		}
		catch(Exception e)
		{
			
		}
		
		if(Distance>=10 && avgSpeed>=15)
		{
			result = 500;
			if (Locale.getDefault().getDisplayName().equals("English (New Zealand)")) 
			{
				txtRank.setText("Vleresimi i shtegut: Gold \n(500 Pike)");
			} 	
			else 
			{   
				txtRank.setText("Track Rank: Gold (500 Points)");
        	}
		}
		else if(Distance>=5 && avgSpeed>=10)
		{
			result = 250;
			if (Locale.getDefault().getDisplayName().equals("English (New Zealand)")) 
			{
				txtRank.setText("Vleresimi i shtegut: Silver \n(250 Pike)");
			} 	
			else 
			{   
				txtRank.setText("Track Rank: Silver (250 Points)");
        	}
			
		}
		else if(Distance>=3 && avgSpeed>=5)
		{
			result = 125;
			if (Locale.getDefault().getDisplayName().equals("English (New Zealand)")) 
			{
				txtRank.setText("Vleresimi i shtegut: Bronze \n(125 Pike)");
			} 	
			else 
			{   
				txtRank.setText("Track Rank: Bronze (125 Points)");
        	}
			
		}
		else
		{
			result = 0;
			if (Locale.getDefault().getDisplayName().equals("English (New Zealand)")) 
			{
				txtRank.setText("Vleresimi i shtegut: Pa Medalje \n(0 Pike)");
			} 	
			else 
			{   
				txtRank.setText("Track Rank: No Medal (0 Points)");
        	}
						
		}
		return result;
		
	}
	
	/**
	 * The function for taking a screenshot of the view given as
	 * parameter. It returns the bitmap (screenshot) created.
	 * Also saves the screnshot to a file in device's storage. 
	 */
	public Bitmap screenShot(final View view) {
		
		/**
		 * Because Google Maps uses openGL ES to take a screenshot of the screen including
		 * Google Maps fragments this method first takes a screenshot of only GoogleMaps fragment this is done
		 * by calling snapshot method of mapView variable and passing callback variable as argument.
		 * Next it takes a regular screenshot of the screen(Layout1) this shows the Google Maps 
		 * all white and finally merges these two screenshots in a single bitmap which is saved to storage
		 * and returned by the method.
		 */
	        SnapshotReadyCallback callback = new SnapshotReadyCallback() {
	        
	            @Override
	            public void onSnapshotReady(Bitmap snapshot) {
	                try {
	                    view.setDrawingCacheEnabled(true);
	                 
	                    //Take screenshot of Layout1
	                    Bitmap backBitmap = view.getDrawingCache();
	                    
	                    //Create final(merged) Bitmap 
	                    Bitmap bmOverlay = Bitmap.createBitmap(backBitmap.getWidth(), backBitmap.getHeight(),backBitmap.getConfig());
	                    Canvas canvas = new Canvas(bmOverlay);
	                    
	                    //Draw taken screnshots of Screen and GoogleMaps to final bitmap
	                    canvas.drawBitmap(backBitmap, new Matrix(), null);
	                    canvas.drawBitmap(snapshot, 0, txtRank.getBottom()+2, null);
	                    
	                    //Save final bitmap(bmpOverlay) to storage
	                    Filename=Environment.getExternalStorageDirectory()+ "/NgajLastScreenShot"+".png";
	                    FileOutputStream out = new FileOutputStream(Filename);
	                    bmOverlay.compress(Bitmap.CompressFormat.PNG, 90, out);
                    
	                } 
	                catch (Exception e) {
	                    
	                }
	            }
	        };

	        /**
	         * Take snapshot of GoogleMaps fragment this calls the code above and passes
	         * the bitmap snapshot of Google Maps fragment as parameter.
	         */
	        mapView.snapshot(callback);
	        return BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()+ "/NgajLastScreenShot"+".png");
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
		String strTitle = "Confirm Share!";
		String strMessage = "Are you sure you want to share your results on "+arg+"?";
		String strPositive = "Yes";
		String strNegative="Cancel";
		if(Locale.getDefault().getDisplayName().equals("English (New Zealand)"))
		{
			strTitle="Konfirmo shperndarjen!";
			strMessage = "A deshironi ta shperndani rezultatin ne "+arg+"?";
			strPositive="Po";
			strNegative="Jo";
		}
		Alert.setTitle(strTitle);
		Alert.setMessage(strMessage);
		Alert.setPositiveButton(strPositive, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				try {
					String Message = "I have ran "+txtDistance.getText()+" km with NGAJ. \nHere are my stats.";
					if(Locale.getDefault().getDisplayName().equals("English (New Zealand)"))
					{
						Message="Une kam vrapuar "+txtDistance.getText()+" km me NGAJ. \nKeto jane rezultatet e mia.";
					}
					
					adapter.uploadImageAsync(Message, "Name.png",screenShot(Layout1), 0, new MessageListener());
				} catch (Exception ex) {
					// TODO Auto-generated catch block
				Toast.makeText(Results.this, ex.getMessage(),Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		Alert.setNegativeButton(strNegative, new DialogInterface.OnClickListener() {
			
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

		File dest = new File(Environment.getExternalStorageDirectory()+ "/NgajLastScreenShot"+".png");
		screenShot(Layout1);
		
		try {
		     		     
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
				screenShot(Layout1);
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
				
			{
				if (Locale.getDefault().getDisplayName().equals("English (New Zealand)")) 
				{
					Toast.makeText(Results.this,
						"Messazhi u postua ne " + provider, Toast.LENGTH_LONG)
						.show();
				} 
				else
				{
					Toast.makeText(Results.this,
						"Message posted on " + provider, Toast.LENGTH_LONG)
						.show();
				}
			}	
			else{
				if (Locale.getDefault().getDisplayName().equals("English (New Zealand)")) 
				{
					Toast.makeText(Results.this,
						"Messazhi nuk u postua ne " + provider, Toast.LENGTH_LONG)
						.show();
				} 
				else
				{
					Toast.makeText(Results.this,
							"Message not posted on" + provider, Toast.LENGTH_LONG)
							.show();
				}
			
				
			}
		}

		@Override
		public void onError(SocialAuthError arg0) {
			// TODO Auto-generated method stub
			
		}

	}
}
