package hig.herd.ngaj;

import java.util.ArrayList;
import java.util.Locale;
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
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

	/**
	 * Declare GUI components,map components and other variables.
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
	int TotalScore = 0;
	int Level;
	ImageView imgCurrentLevel;
	ImageView imgNextLevel;
	Timer mTimer;
	/**
	 * Key For knowing app state 0 - Not Recording 1 - Recording 2 - Destroyed
	 * while recording
	 */
	int k = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/**
		 * Intialize UI variables
		 */
		txtSteps = (TextView) findViewById(R.id.steps2);
		txtSpeed = (TextView) findViewById(R.id.speed2);
		txtSpeedExtras = (TextView) findViewById(R.id.speed3);
		txtTime = (TextView) findViewById(R.id.time2);
		txtDistance = (TextView) findViewById(R.id.distance2);
		btnStart = (Button) findViewById(R.id.btnStart);
		mProgress = (ProgressBar) findViewById(R.id.progressBar1);
		imgCurrentLevel = (ImageView) findViewById(R.id.CurrentLevel);
		imgNextLevel = (ImageView) findViewById(R.id.NextLevel);
		serviceIntent = new Intent(this, GPSservice.class);

		/**
		 * Get the GoogleMaps Fragment widget
		 */
		mapView = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.mapview)).getMap();

		/**
		 * Call method getLevel which calculates users level and displays in in
		 * ratingbar together with coresponding imageviews.
		 */
		getLevel();
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
		if (item.getItemId() == R.id.action_settings) {
			Intent i = new Intent(MainActivity.this, CalibratePedometer.class);
			startActivity(i);

		}
		return true;

	}

	public void onPause() {
		super.onPause();
		savePreferences();
		mapView.setMyLocationEnabled(false);
	}

	public void onDestroy() {
		super.onDestroy();
		stopService(serviceIntent);
		mapView.setMyLocationEnabled(false);
		if (k == 1) {
			k = 2;
		}
		savePreferences();
	}

	public void onResume() {
		super.onResume();
		getPreferences();

		/**
		 * If orientation was changed app automaticaly recovers and resumes
		 * previous session If app was previously destroyed while recording user
		 * is offered to continue previous session Else we ensure that the
		 * initial values are set to text boxes and button.
		 */
		if (k == 1 || (k == 2 && OrientationChange)) {

			btnStart.setText("Pause");
			if (Locale.getDefault().getDisplayName()
					.equals("English (New Zealand)")) {
				btnStart.setText("Pauzo");
			}
			mapView.setMyLocationEnabled(true);
			IntentFilter intentFilter = new IntentFilter(
					"hig.herd.NGAJ.RECEIVEDATA");
			LocalBroadcastManager.getInstance(getApplicationContext())
					.registerReceiver(this.ReceiveData, intentFilter);

			if (k == 2 && OrientationChange) {
				serviceIntent.putExtra("Key", 1);
				startService(serviceIntent);
			}
			OrientationChange = false;
			k = 1;
		} else if (k == 2) {
			showResumeAlert();
			btnStart.setText("Pause");
			if (Locale.getDefault().getDisplayName()
					.equals("English (New Zealand)")) {
				btnStart.setText("Pauzo");
			}
		} else {
			btnStart.setText("Start");
			if (Locale.getDefault().getDisplayName()
					.equals("English (New Zealand)")) {
				btnStart.setText("Nisu");
			}
			txtTime.setText("00:00:00");
			txtSpeed.setText("0.00");
			txtDistance.setText("0.00");
			txtSteps.setText("0");
			txtSpeedExtras.setText("0 avg 0 max");
		}

	}

	/**
	 * This method shows the AlertDialog where user is prompted to continue his
	 * previous workout.
	 */
	private void showResumeAlert() {
		Alert = new AlertDialog.Builder(this);
		String strTitle = "Continue Work!";
		String strMessage = "Do you want to continue your previous workout?";
		String strPositive = "Yes";
		String strNegative = "Cancel";
		if (Locale.getDefault().getDisplayName().equals("English(New Zealand)")) {
			strTitle = "Vazhdo punen!";
			strMessage = "A deshironi ta vazhdoni sesionin tuaj te paperfunduar";
			strPositive = "Po";
			strNegative = "Jo";
		}
		Alert.setTitle(strTitle);
		Alert.setMessage(strMessage);
		Alert.setPositiveButton(strPositive,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						/**
						 * If user chooses to continue his previous workout the
						 * GPSservice is started by intent with parameter "Key"
						 * set to 1 which indicates service to continue previous
						 * session by loading shared preferences
						 */
						try {
							serviceIntent.putExtra("Key", 1);
							k = 1;
							startService(serviceIntent);
							IntentFilter intentFilter = new IntentFilter(
									"hig.herd.NGAJ.RECEIVEDATA");
							LocalBroadcastManager.getInstance(
									getApplicationContext()).registerReceiver(
									ReceiveData, intentFilter);
							mapView.setMyLocationEnabled(true);

						} catch (Exception ex) {
							// TODO Auto-generated catch block
							;
						}

					}
				});
		Alert.setNegativeButton(strNegative,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						/**
						 * If user chooses not to continue his previous session
						 * UI textview text and buttons text are set to initial
						 * value. Also we clear the map and the array where
						 * location points reported from GPSservice are saved.
						 */

						k = 0;
						btnStart.setText("Start");
						if (Locale.getDefault().getDisplayName()
								.equals("English (New Zealand)")) {
							btnStart.setText("Nisu");
						}
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

	/**
	 * This method show the AlertDialog where user should confirm discarding
	 * current session.
	 */
	private void showDiscardAlert() {
		Alert = new AlertDialog.Builder(this);
		String strTitle = "Discard Work!";
		String strMessage = "Do you want to discard your current work?\nThis cannot be undone!";
		String strPositive = "Yes";
		String strNegative = "Cancel";
		if (Locale.getDefault().getDisplayName()
				.equals("English (New Zealand)")) {
			strTitle = "Fshije punen!";
			strMessage = "A deshironi ta fshini sesionin aktual?\nKy verpim nuk mund te zhbehet";
			strPositive = "Po";
			strNegative = "Jo";
		}
		Alert.setTitle(strTitle);
		Alert.setMessage(strMessage);
		Alert.setPositiveButton(strPositive,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						try {
							k = 0;
							btnStart.setText("Start");
							if (Locale.getDefault().getDisplayName()
									.equals("English (New Zealand)")) {
								btnStart.setText("Nisu");
							}
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
		Alert.setNegativeButton(strNegative,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						showPauseAlert();
					}
				});
		Alert.show();

	}

	/**
	 * This method is called on click of start/pause button.
	 * 
	 */
	public void startRecording(View v) {
		/**
		 * Start/Stop GPSservice based on value of k.
		 */
		if (k == 0) {

			/**
			 * Declare and initialize the intent for receiving data from the
			 * specific broadcast receiver
			 */
			IntentFilter intentFilter = new IntentFilter(
					"hig.herd.NGAJ.RECEIVEDATA");
			LocalBroadcastManager.getInstance(getApplicationContext())
					.registerReceiver(this.ReceiveData, intentFilter);

			// Check if GPS is enabled
			checkGPS();

			/**
			 * Start Service with Intent and with "Key" parameter set to an
			 * arbitrary value 5 which indicates Service not to load shared
			 * preferences
			 */
			serviceIntent.putExtra("Key", 5);
			startService(serviceIntent);

			// Change button text

			btnStart.setText("Pause");
			if (Locale.getDefault().getDisplayName()
					.equals("English (New Zealand)")) {
				btnStart.setText("Pauzo");
			}
			k = 1;

			/**
			 * Call service checker method which ensures that GPSservice is
			 * running during the record.
			 */

			serviceChecker();

			/**
			 * Clear map and array where location points are saved
			 */
			latLngList.clear();
			mapView.clear();
			mapView.setMyLocationEnabled(true);
		} else if (k == 1) {
			/**
			 * By editing GPSServiceState variable on shared preferences we tell
			 * the GPSservice to go on pause mode and so ignore location updates
			 * and pause timer. *
			 */
			getSharedPreferences("GPSServiceState", MODE_PRIVATE).edit()
					.putInt("GPSServiceState", 2).commit();
			// If the app was recording call showPauseAlert which shows pause
			// alertdialog.
			showPauseAlert();
		}

	}

	/**
	 * This method checks if GPS is enabled if user hasn't previously disabled
	 * this check and GPS is disabled it calls showGPSAlert method which prompts
	 * user that GPS is not enabled and offers him to go to location settings
	 * and enable it.
	 */
	private void checkGPS() {
		if (getPreferences(MODE_PRIVATE).getBoolean("ignoreGPS", false) == false) {
			LocationManager mlocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			if (!mlocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				showGPSAlert();
			}
		}
	}

	/**
	 * This method prompts user that GPS is not enabled and offers him to go to
	 * location settings and enable it.
	 */
	private void showGPSAlert() {
		Alert = new AlertDialog.Builder(this);
		String strTitle = "GPS not enabled!";
		String strMessage = "Your GPS is disabled.\nDo you want to go to location settings and enable it?";
		String strPositive = "Yes";
		String strNegative = "Continue Anyway";
		String strCheckBox = "Do not show this prompt again.";
		if (Locale.getDefault().getDisplayName()
				.equals("English (New Zealand)")) {
			strTitle = "GPS eshte i ndalur!";
			strMessage = "GPS i juaj eshte i ndalur.\nA deshironi ta aktivizoni ate?";
			strPositive = "Po";
			strNegative = "Vazhdo pa e aktivizuar";
			strCheckBox = "Mos e shfaq kete udhezim ne te ardhmen";
		}
		Alert.setTitle(strTitle);
		Alert.setMessage(strMessage);
		final CheckBox chBox = new CheckBox(this);
		Alert.setView(chBox);
		chBox.setText(strCheckBox);

		Alert.setPositiveButton(strPositive,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						/**
						 * If user chooses to go to location settings then he is
						 * redirected to there with intent. Also te value of
						 * checkbox is saved to ingoreGPS variable in shared
						 * preferences which defines if GPS check is disabled or
						 * not.
						 */
						Intent gpsOptionsIntent = new Intent(
								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(gpsOptionsIntent);
						if (chBox.isChecked()) {
							getPreferences(MODE_PRIVATE).edit()
									.putBoolean("ignoreGPS", true).commit();
						}

					}
				});
		Alert.setNegativeButton(strNegative,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (chBox.isChecked()) {
							getPreferences(MODE_PRIVATE).edit()
									.putBoolean("ignoreGPS", true).commit();
						}
					}
				});
		Alert.show();

	}

	private void serviceChecker() {
		/**
		 * This method checks if the service is runing while the app is in the
		 * record mode if OS killes the service then this method will restart
		 * it. for performance issues we decided to trigger this checker to
		 * check every 5 seconds.
		 */
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask() {

			public void run() {

				if (k == 1
						&& getSharedPreferences("GPSServiceState", MODE_PRIVATE)
								.getInt("GPSServiceState", 0) == 0) {
					serviceIntent.putExtra("Key", 1);
					startService(serviceIntent);
				} else if (k == 0) {
					mTimer.cancel();
				}
			}
		}, 5000, 5000);
	}

	/**
	 * This method is called before orientation change and it sets
	 * OrientationChange variable value to true this is used to differ app
	 * destroy and orientation change.
	 */
	public Object onRetainCustomNonConfigurationInstance() {
		OrientationChange = true;
		return OrientationChange;
	}

	/**
	 * This method starts results activity and sends current tracks stats via
	 * intent. It also starts DBservice and sends the same values as results via
	 * intent and then DBservice saves these values to local database.
	 */
	private void startResults(String Name) {
		k = 0;
		savePreferences();
		Intent i = new Intent(MainActivity.this, Results.class);
		Intent s = new Intent(MainActivity.this, DBservice.class);
		Bundle extras = new Bundle();
		extras.putString("Name", Name);
		extras.putString("Time", (String) txtTime.getText());
		extras.putString("Speed", (String) txtSpeed.getText());
		extras.putString("Steps", (String) txtSteps.getText());
		extras.putString("SpeedExtras", (String) txtSpeedExtras.getText());
		extras.putString("Distance", (String) txtDistance.getText());
		extras.putInt("Level", Level);
		extras.putInt("Size", latLngList.size());
		for (int j = 0; j < latLngList.size(); j++) {
			extras.putFloat("Cord_Lat_" + j, (float) latLngList.get(j).latitude);
			extras.putFloat("Cord_Long_" + j,
					(float) latLngList.get(j).longitude);
		}
		i.putExtras(extras);
		s.putExtras(extras);
		startService(s);
		startActivity(i);
		latLngList.clear();
		mapView.clear();
	}

	/**
	 * This Method shows Pause AlertDialog.
	 */
	private void showPauseAlert() {
		Alert = new AlertDialog.Builder(this);
		String strTitle = "Workout Paused!";
		String strMessage = "What you want to do?";
		String strPositive = "Save";
		String strNegative = "Resume";
		String strNeutral = "Discard";
		if (Locale.getDefault().getDisplayName()
				.equals("English (New Zealand)")) {
			strTitle = "Puna u pauzua!";
			strMessage = "Ju lutem zgjedhni nje veprim?";
			strPositive = "Ruaje";
			strNegative = "Vazhdo";
			strNeutral = "Fshije";
		}
		Alert.setTitle(strTitle);
		Alert.setMessage(strMessage);
		Alert.setPositiveButton(strPositive,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						try {
							/**
							 * If users chooses to save the tack the
							 * showNameTrackAlert method is called which offers
							 * user to name the track being saved.
							 */
							showNameTrackAlert();

						} catch (Exception ex) {
							// TODO Auto-generated catch block
							;
						}

					}
				});
		Alert.setNegativeButton(strNegative,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						/**
						 * If user chooses to resume recording the
						 * GPSServiceState variable of shared preferences is set
						 * to 1 which indicates GPSservice to go into recording
						 * state. Also the k variable is set to 1 which tell the
						 * app that it is in recording state and Google Maps
						 * mapview is configured to show current location
						 * pointer on the map.
						 */
						k = 1;
						getSharedPreferences("GPSServiceState", MODE_PRIVATE)
								.edit().putInt("GPSServiceState", 1).commit();
						mapView.setMyLocationEnabled(true);
					}
				});
		Alert.setNeutralButton(strNeutral,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						/**
						 * If user chooses discard option
						 */
						showDiscardAlert();
					}
				});
		Alert.show();

	}

	/**
	 * This method shows NameTrack AlertDialog.
	 */
	private void showNameTrackAlert() {
		Alert = new AlertDialog.Builder(this);
		String strTitle = "Name Track!";
		String strMessage = "Give a name to your track.";
		String strPositive = "Save";
		String strNegative = "Cancel";
		if (Locale.getDefault().getDisplayName()
				.equals("English (New Zealand)")) {
			strTitle = "Emero Shtegun!";
			strMessage = "Ju lutem emeroni shtegun.";
			strPositive = "Ruaje";
			strNegative = "Anulo";
		}
		Alert.setTitle(strTitle);
		Alert.setMessage(strMessage);
		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setSingleLine(true);

		Alert.setView(input);
		Alert.setPositiveButton(strPositive,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						/**
						 * If user chooses to save the track the startResults
						 * method is called, the GPSservice is stoped and the
						 * variable k value is set to 0 this tells the app that
						 * it is now on not recording mode.
						 */
						try {
							k = 0;
							startResults(input.getText().toString());
							stopService(serviceIntent);
							mapView.setMyLocationEnabled(false);

						} catch (Exception ex) {
							// TODO Auto-generated catch block
							;
						}

					}
				});
		Alert.setNegativeButton(strNegative,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						showPauseAlert();
					}
				});
		Alert.show();

	}

	/**
	 * Draws a point into the map using the coordinates given as parameters.
	 * This is done if the Latitude and Longitude are not 0, since this is the
	 * default value. It also moves the camera to center the just drawn point.
	 */
	private void addPoint(double Latitude, double Longitude) {
		if (Latitude != 0 && Longitude != 0) {
			LatLng Point = new LatLng(Latitude, Longitude);
			CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(Point,
					15);
			mapView.animateCamera(camUpdate);
			latLngList.add(Point);
			mapView.clear();
			mapView.addPolyline(new PolylineOptions().addAll(latLngList)
					.width(6).color(-16776961));
		}
	}

	/**
	 * This method gets TotalScore from shared preferences and also calculates
	 * the Level by calling calculatelevel method.
	 */
	private void getLevel() {
		TotalScore = getSharedPreferences("TotalScore", MODE_PRIVATE).getInt(
				"TotalScore", 0);
		Level = calculateLevel(TotalScore);
	}

	/**
	 * This method calculates the level based on parameter score and it also
	 * fills the progressbar and puts the right images on the current and
	 * nextlevel imageviews.
	 */
	private int calculateLevel(int score) {
		int result = 1;
		if (score >= 10500) {
			result = 5;
			mProgress.setMax(score);
			mProgress.setProgress(score);
			imgCurrentLevel.setImageResource(R.drawable.elite);
			imgNextLevel.setImageResource(R.drawable.elite);
		} else if (score >= 5750) {
			result = 4;
			mProgress.setMax(10500 - 5750);
			mProgress.setProgress(score - 5750);
			imgCurrentLevel.setImageResource(R.drawable.four);
			imgNextLevel.setImageResource(R.drawable.five);
		} else if (score >= 2750) {
			result = 3;
			mProgress.setMax(5750 - 2750);
			mProgress.setProgress(score - 2750);
			imgCurrentLevel.setImageResource(R.drawable.three);
			imgNextLevel.setImageResource(R.drawable.four);
		} else if (score >= 1000) {
			result = 2;
			mProgress.setMax(2750 - 1000);
			mProgress.setProgress(score - 1000);
			imgCurrentLevel.setImageResource(R.drawable.two);
			imgNextLevel.setImageResource(R.drawable.three);
		} else {
			result = 1;
			mProgress.setMax(1000);
			mProgress.setProgress(score);
			imgCurrentLevel.setImageResource(R.drawable.one);
			imgNextLevel.setImageResource(R.drawable.two);
		}

		return result;
	}

	/**
	 * This method saves variables k,OrientationChange and array latLngList in
	 * the shared preferences.
	 */
	private void savePreferences() {
		getPreferences(MODE_PRIVATE).edit().putInt("Key", k).commit();
		getPreferences(MODE_PRIVATE).edit()
				.putBoolean("OrientationChange", OrientationChange).commit();
		getPreferences(MODE_PRIVATE).edit().putInt("Size", latLngList.size())
				.commit();

		for (int i = 0; i < latLngList.size(); i++) {
			getPreferences(MODE_PRIVATE).edit().remove("Cord_Lat_" + i)
					.commit();
			getPreferences(MODE_PRIVATE).edit().remove("Cord_Long_" + i)
					.commit();
			getPreferences(MODE_PRIVATE)
					.edit()
					.putFloat("Cord_Lat_" + i,
							(float) latLngList.get(i).latitude).commit();
			getPreferences(MODE_PRIVATE)
					.edit()
					.putFloat("Cord_Long_" + i,
							(float) latLngList.get(i).longitude).commit();
		}

	}

	/**
	 * This method reads variables k,OrientationChange and array latLngList from
	 * shared preferences.
	 */
	private void getPreferences() {
		k = getPreferences(MODE_PRIVATE).getInt("Key", 0);
		OrientationChange = getPreferences(MODE_PRIVATE).getBoolean(
				"OrientationChange", false);
		latLngList.clear();
		int size = getPreferences(MODE_PRIVATE).getInt("Size", 0);
		for (int i = 0; i < size; i++) {
			double lat = (double) getPreferences(MODE_PRIVATE).getFloat(
					"Cord_Lat_" + i, (float) 5.0);
			double lng = (double) getPreferences(MODE_PRIVATE).getFloat(
					"Cord_Long_" + i, (float) 5.0);
			latLngList.add(new LatLng(lat, lng));
		}
	}

	/**
	 * This method is called when camera button is clicked is starts
	 * cameraActivity via intent and offers user to take a photo and save it to
	 * galery.
	 */
	public void cameraClick(View v) {
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivity(cameraIntent);
	}

	/**
	 * This method is called when stats button is clicked it starts Stats
	 * activity via intent
	 */
	public void statsClick(View v) {
		Intent i = new Intent(MainActivity.this, Stats.class);
		startActivity(i);
	}

	/**
	 * This method is called when tracks button is clicked it starts Tracks
	 * activity via intent
	 */
	public void viewTracks(View v) {
		Intent i = new Intent(MainActivity.this, Tracks.class);
		startActivity(i);
	}

	/**
	 * Declaring and implementing a Broadcast Receivers. It will receive data
	 * form and save them in Steps, Latitude and Longitude variables. Also it
	 * calls the addPoint function which draws a point on map. Log.d - used for
	 * testing purpose!!
	 */
	private BroadcastReceiver ReceiveData = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			final int Steps = intent.getIntExtra("Steps", 0);
			final double Latitude = intent.getDoubleExtra("Latitude", 0);
			final double Longitude = intent.getDoubleExtra("Longitude", 0);
			final String Time = intent.getStringExtra("Time");
			final String Distance = intent.getStringExtra("Distance");
			final String Speed = intent.getStringExtra("Speed");
			final String SpeedExtras = intent.getStringExtra("SpeedExtras");
			if (latLngList.isEmpty()) {
				addPoint(Latitude, Longitude);
			} else if (Latitude != latLngList.get(latLngList.size() - 1).latitude
					&& Longitude != latLngList.get(latLngList.size() - 1).longitude) {
				addPoint(Latitude, Longitude);
			}
			txtSteps.setText(Integer.toString(Steps));
			txtTime.setText(Time);
			txtDistance.setText(Distance);
			txtSpeed.setText(Speed);
			txtSpeedExtras.setText(SpeedExtras);
		}
	};

}
