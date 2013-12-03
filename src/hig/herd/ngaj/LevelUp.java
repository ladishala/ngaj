package hig.herd.ngaj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;
import org.brickred.socialauth.android.SocialAuthAdapter.Provider;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LevelUp extends Activity {

	/**
	 * Declare social networking share components
	 * and the map components
	 */
	
	SocialAuthAdapter adapter;
	LinearLayout bar;
	AlertDialog.Builder Alert;
	
	RelativeLayout Layout1;
	String Filename ="";
	TextView txtLevel;
	
	int Level;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_level_up);
		
		Intent i = getIntent();
		Level = i.getIntExtra("Level", 0);
		
		bar = (LinearLayout) findViewById(R.id.linearbar3);
		bar.setBackgroundResource(R.drawable.bar_gradient);
		Layout1=(RelativeLayout)findViewById(R.id.Layout3);
		txtLevel=(TextView)findViewById(R.id.txtLevelUp);
		
		adapter = new SocialAuthAdapter(new ResponseListener());

		// Add providers
		adapter.addProvider(Provider.FACEBOOK, R.drawable.facebook);
		adapter.addProvider(Provider.TWITTER, R.drawable.twitter);
		adapter.addProvider(Provider.MMS, R.drawable.other);
		adapter.addProvider(Provider.EMAIL, R.drawable.camera);
		adapter.enable(bar);
		
		calculateText();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.level_up, menu);
		return true;
	}
	
	private void calculateText()
	{
		if(Level==5)
		{
			txtLevel.setText("Well Done! \nYou have reached maximum level!\nYour current level is : 5 (Elite)");
		}
		else if(Level==4)
		{
			txtLevel.setText("Well Done! \nYou have raised one level up!\nYour current level is : 4 (Expert)");
		}
		else if(Level==3)
		{
			txtLevel.setText("Well Done! \nYou have raised one level up!\nYour current level is : 3 (Skilled)");
		}
		else if(Level==2)
		{
			txtLevel.setText("Well Done! \nYou have raised one level up!\nYour current level is : 2 (Competent)");
		}
	}
	
	/**
	 * The function for taking a screenshot of the view given as
	 * parameter. It returns the bitmap (screenshot) created. 
	 */
	public Bitmap screenShot(final View view) {
		
		Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        Filename=Environment.getExternalStorageDirectory()+ "/NgajLastScreenShot"+".png";
        FileOutputStream out;
		try {
			out = new FileOutputStream(Filename);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		Alert.setTitle("Confirm Share!");
		Alert.setMessage("Are you sure you want to share your results on "+arg+"?");
		Alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				try {
					int start = txtLevel.getText().toString().indexOf(":");
					String Message = "I have raised one level up on NGAJ.\n"+"My Level is"+txtLevel.getText().toString().substring(start);
					
					adapter.uploadImageAsync(Message, "Name.png",screenShot(Layout1), 0, new MessageListener());
				} catch (Exception ex) {
					// TODO Auto-generated catch block
				Toast.makeText(LevelUp.this, ex.getMessage(),Toast.LENGTH_SHORT).show();
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
				Toast.makeText(LevelUp.this,
						"Message posted on " + provider, Toast.LENGTH_LONG)
						.show();
			else
				Toast.makeText(LevelUp.this,
						"Message not posted on" + provider, Toast.LENGTH_LONG)
						.show();
				}

		@Override
		public void onError(SocialAuthError e) {

		}
	}
	

}