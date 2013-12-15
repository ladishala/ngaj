package hig.herd.ngaj;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CalibratePedometer extends Activity implements
SensorEventListener{

	/**
	 * Declare activity variables and UI components
	 */
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
	TextView txtCalibrationResult;
	Button btnCalibration;
	Timer mTimer;
	int Time=6;
	boolean up =false;
	boolean down =false;
	ArrayList<Double> magnitudes = new ArrayList<Double>();
	/**
	 * Defines the calibration state
	 * When calibrating(Reading values from accelerometer this variable is set to true
	 * otherwise it is set to false
	 */
	boolean calibrating;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibrate_pedometer);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		/**
		 * Initialize sensors.
		 */	
		mSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_UI);
		
		/**
		 * Initialize UI component variables and set calibrating value to false which indicates that the activity is
		 * on not calibrating state.
		 */
		btnCalibration = (Button)findViewById(R.id.btnCalibrate);
		txtCalibrationResult = (TextView)findViewById(R.id.txtCalibrationResult);
		calibrating=false;
	}
	/**
	 * This method is called when button start/stop calibrating is called
	 */
	public void startCalibration (View v)
	{
		/**
		 * Check whether to start or stop calibration
		 */
		if(!calibrating)
		{ 
			/**
			 * Fist check if the GPSservice is running or not if it is running the calibration cannot be done
			 * this is due to the GPSservice reads the thresholds on load and if we calibrate with service recording
			 * then this calibration will not be applied to current session but we decided not to allow calibration when
			 * recording.
			 */
			if(getSharedPreferences("GPSServiceState",MODE_PRIVATE).getInt("GPSServiceState",0)!=1)		
			{
				/**
				 * If GPSservice is not started call startTimer method, clear the array where magnitudes are saved
				 * and change button text.
				 * 
				 * startTimer method starts the countdown for 5 seconds and then starts collecting the magnitudes from
				 * acceleromteter.
				 */
				magnitudes.clear();
				startTimer();
				if (Locale.getDefault().getDisplayName().equals("English (New Zealand)")) 
				{
					btnCalibration.setText("Ndale Kalibrimin");
	  	    	}
				else
				{
					btnCalibration.setText("Stop Calibration");
				}
			}
			else
			{
				/**
				 * Prompt that the app should not be recording a session during the calibration.
				 */
				if (Locale.getDefault().getDisplayName().equals("English (New Zealand)")) 
				{
				   Toast.makeText(this, "Aplikacioni nuk duhet te jete jo aktiv per te kalibruar!", Toast.LENGTH_LONG).show();
				}
				else
				{
					Toast.makeText(this, "App should be in stopped state to calibrate!", Toast.LENGTH_LONG).show();
				}
			}
		}
		else
		{
			/**
			 * Call method stopCollecting to stop storing magnitudes from accelerometer.
			 */
			stopCollecting();
		}
		
		
	}
	/**
	 * This method stops the collection of mangitudes from accelerometer, updates button text,
	 * plays a confirmation beep sound and calls calibratePedometer which tries to calculate thresholds based on collected
	 * magnitudes 
	 */
	public void stopCollecting()
	{
		//Play confirmation beep sound
		MediaPlayer mp = MediaPlayer.create(CalibratePedometer.this, R.raw.beep);  
  	    mp.start();
  	    
  	    //Update button text
  	    if (Locale.getDefault().getDisplayName().equals("English (New Zealand)")) 
  	    {
  	    	btnCalibration.setText("Starto Kalibrimin");
  	    }
  	    else
  	    {
  	    	btnCalibration.setText("Start Calibration");
  	    }
  	    //Stop timer and set Time variable and calibrating to initial value
		mTimer.cancel();
		Time=6;
		calibrating=false;
		//If one or more magnitudes collected call calibratePedometer method.
		if(magnitudes.size()>0)
		{
		calibratePedometer();
		}
		
	}
	/**
	 * This method is called when a key on the device is pressed. We have overriden the volume up
	 * and volume down key press and made them call stopCollecting method.
	 */
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
	    int action = event.getAction();
	    int keyCode = event.getKeyCode();
	    switch (keyCode) {
	        case KeyEvent.KEYCODE_VOLUME_UP:
	            if (action == KeyEvent.ACTION_DOWN) {
	                //TODO
	            	stopCollecting();
	                return true;
	            }
	            return true;
	        case KeyEvent.KEYCODE_VOLUME_DOWN:
	            if (action == KeyEvent.ACTION_DOWN) {
	                //TODO
	            	stopCollecting();
	                return true;
	            }
	            return true;
	        default:
	            return super.dispatchKeyEvent(event);
	    }
	}
	/**
	 * This method tries to calibrate the pedometer (calculate up and down threshold) based on collected magnitudes.
	 * First this method finds the maximum and minimum magnitude values from magnitudes list. Then it calculates number
	 * of steps by checking steps for all mangitudes in magnitudes list. It repeats this process for different values of 
	 * upThreshold and downThreshold until the downThreshold and upThreshold values are equal.
	 * 
	 * In order for the calibration to be sucsessful the number of steps must be 9,10 or 11. If number of steps for given
	 * threshold values is 9 or 11 these threshold values are saved and loop still continues to calculate steps for different
	 * values of threshold with aim to get te combination which gives 10 steps. If this combination is found it is saved and
	 * the loop is broken.
	 * 
	 * We know if calibration was sucssesful by the value of calibrated variable which is set to true if for any combination
	 * of thresholds the number of steps is 9, 10 or 11. If the calibration was succsessful than a message of sucssesful 
	 * calibration is shown in the UI and the values of thresholds are saved to shared preferences.
	 * 
	 * If calibration is unsucssesful a message of unsucssesful calibration is shown in the UI
	 */
	private void calibratePedometer()
	{
		
		// Calculate initial threhsolds from minimum and maximum magnitude value
		double downThreshold=minMagnitude();
		double upThreshold=maxMagnitude();
		boolean calibrated=false;
		
		while(upThreshold>downThreshold)
		{
			//For each cycle set the variable values to their initial value
			int steps=0;
			down=false;
			up=false;
			upThreshold -= 0.05;
			downThreshold +=0.05;
			
			for(int i=0;i<magnitudes.size();i++)
			{
				//Call checkforStep method for all magnitudes in magnitudes list.
				if(checkforStep(magnitudes.get(i),downThreshold,upThreshold))
				{
					steps++;
				}
			}
			//If number of steps for this threshold combination is 10
			if(steps==10)
			{
				calibrated=true;
				getSharedPreferences("downThreshold",MODE_PRIVATE).edit().putFloat("downThreshold",(float)downThreshold).commit();
				getSharedPreferences("upThreshold",MODE_PRIVATE).edit().putFloat("upThreshold",(float)upThreshold).commit();
				break;
			}
			//If number of steps for this threshold combination is 9 or 11
			else if(steps==9 || steps ==11)
			{
				calibrated=true;
				getSharedPreferences("downThreshold",MODE_PRIVATE).edit().putFloat("downThreshold",(float)downThreshold).commit();
				getSharedPreferences("upThreshold",MODE_PRIVATE).edit().putFloat("upThreshold",(float)upThreshold).commit();
			}
		}
		 
		//Show the calibration result message on the UI.
		if(calibrated)
		{
			if (Locale.getDefault().getDisplayName().equals("English (New Zealand)")) 
			{
				txtCalibrationResult.setText("U kalibrua me sukses!");
			}
			else
			{
				txtCalibrationResult.setText("Successufully calibrated!");
			}
			
		}
		else
		{
			if (Locale.getDefault().getDisplayName().equals("English (New Zealand)")) 
			{
				txtCalibrationResult.setText("Kalibrimi Deshtoi.\nProvoni perseri");
			}
			else
			{
				txtCalibrationResult.setText("Calibration failed.\nPlease try again");
			}
		}
	}
	
	//Calculate minimum magnitude value from magnitudes list
	private double minMagnitude()
	{
		double result=magnitudes.get(0);
		for(int i = 1 ; i<magnitudes.size();i++)
		{
			if(magnitudes.get(i)<result)
			{
				result=magnitudes.get(i);
			}
		}
		return result;
	}
	//Calculate maximum magnitude value from magnitudes list
	private double maxMagnitude()
	{
		double result=magnitudes.get(0);
		for(int i = 1 ; i<magnitudes.size();i++)
		{
			if(magnitudes.get(i)>result)
			{
				result=magnitudes.get(i);
			}
		}
		return result;
	}
	/**
	 * This method checks if the current magnitude values given in paramter magnitude indicates a step this method
	 * is called for each magnitude value of magnitudes list and it will indicate a step only for the true sequence
	 * of variables: down,up,down.
	 * 
	 * If it is a new step it returns true.
	 */
	private boolean checkforStep(double magnitude,double downthreshold,double upthreshold)
	{
		boolean result = false;
		if(magnitude>upthreshold)
		{
			if(up && down)
			{
				up=false;
				down=false;
				result=true;
				
			}
			else
			{
			up = true;
			down=false;
			}
			
		}
		else if(magnitude<downthreshold)
		{
			down=true;
		}
		
		return result;
	}
	public void startTimer()
	{
		//This method starts timer, it calls method dotime every second.
		 mTimer = new Timer();
		 mTimer.scheduleAtFixedRate(new TimerTask() {

             public void run() {
                      doTime();
                 }

     }, 0, 1000);
		
	}
	/**
	 * This method is called every second when timer is activated.
	 * It decreases the timer value by one and shows the current value on UI
	 * when the value rechaches 0 it shows a message on UI that calibration started,
	 * plays a confirmation beep sound and changes the calibrating varialbe value to true which indicates the app
	 * to start collecting magnitudes from linear accelerometer into magnitudes list. 
	 */
	private void doTime()
	{
		runOnUiThread(new Runnable() 
	     {
	    public void run() 
	    {
	    		//If time>0 decrease time.
	            if(Time>0)
	            {
	             	Time--;
	             	txtCalibrationResult.setText(Integer.toString(Time));
	            }
	            //Else show calibrating message and play confirmation beep sound once.
	             else
	             {
	            	 if (Locale.getDefault().getDisplayName().equals("English (New Zealand)")) 
	     			 {
	            		 txtCalibrationResult.setText("Duke Kalibruar");
	     			 }
	            	 else
	            	 {
	            		 txtCalibrationResult.setText("Calibrating"); 
	            	 }
	             	
	             	calibrating=true;
	             }
	            if(Time==0)
	            {
	            	Time--;
	            	MediaPlayer mp = MediaPlayer.create(CalibratePedometer.this, R.raw.beep);  
	            	  mp.start();
	            }
	             
	        }
	     });
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.calibrate_pedometer, menu);
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if(event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION && calibrating)
		{
			/**
			 * If app is in calibrating mode(calibrating value is true)
			 * the values of linear acceleration for X,Y and Z are read and from them is calculated the magnitude
			 * then calculated magnitude is saved to magnitudes list. 
			 */
			double x = event.values[0];
			double y = event.values[1];
			double z = event.values[2];
			double magnitude=Math.sqrt(x*x+y*y+z*z);
			magnitudes.add(magnitude);
		}

	}

}
