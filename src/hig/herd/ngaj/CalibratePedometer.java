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

	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
	TextView txtCalibrationResult;
	Button btnCalibration;
	Timer mTimer;
	boolean calibrating;
	int Time=6;
	boolean up =false;
	boolean down =false;
	ArrayList<Double> magnitudes = new ArrayList<Double>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibrate_pedometer);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_UI);
		
		
		btnCalibration = (Button)findViewById(R.id.btnCalibrate);
		txtCalibrationResult = (TextView)findViewById(R.id.txtCalibrationResult);
		calibrating=false;
	}

	public void startCalibration (View v)
	{
		if(!calibrating)
		{
			if(getSharedPreferences("GPSServiceState",MODE_PRIVATE).getInt("GPSServiceState",0)!=1)
				
			{
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
			stopCollecting();
		}
		
		
	}
	public void stopCollecting()
	{
		MediaPlayer mp = MediaPlayer.create(CalibratePedometer.this, R.raw.beep);  
  	    mp.start();
  	    if (Locale.getDefault().getDisplayName().equals("English (New Zealand)")) 
  	    {
  	    	btnCalibration.setText("Starto Kalibrimin");
  	    }
  	    else
  	    {
  	    	btnCalibration.setText("Start Calibration");
  	    }
		mTimer.cancel();
		Time=6;
		calibrating=false;
		if(magnitudes.size()>0)
		{
		calibratePedometer();
		}
		
	}
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
	private void calibratePedometer()
	{
		double downThreshold=minMagnitude();
		double upThreshold=maxMagnitude();
		boolean calibrated=false;
		while(upThreshold>downThreshold)
		{
			int steps=0;
			down=false;
			up=false;
			upThreshold -= 0.05;
			downThreshold +=0.05;
			
			for(int i=0;i<magnitudes.size();i++)
			{
				if(checkforstep(magnitudes.get(i),downThreshold,upThreshold))
				{
					steps++;
				}
			}
			if(steps==10)
			{
				calibrated=true;
				getSharedPreferences("downThreshold",MODE_PRIVATE).edit().putFloat("downThreshold",(float)downThreshold).commit();
				getSharedPreferences("upThreshold",MODE_PRIVATE).edit().putFloat("upThreshold",(float)upThreshold).commit();
				break;
			}
			else if(steps==9 || steps ==11)
			{
				calibrated=true;
				getSharedPreferences("downThreshold",MODE_PRIVATE).edit().putFloat("downThreshold",(float)downThreshold).commit();
				getSharedPreferences("upThreshold",MODE_PRIVATE).edit().putFloat("upThreshold",(float)upThreshold).commit();
			}
		}
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
	private boolean checkforstep(double magnitude,double downthreshold,double upthreshold)
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
		 mTimer = new Timer();
		 mTimer.scheduleAtFixedRate(new TimerTask() {

             public void run() {
                      doTime();
                 }

     }, 0, 1000);
		
	}
	private void doTime()
	{
		runOnUiThread(new Runnable() 
	     {
	    public void run() 
	    {
	            if(Time>0)
	            {
	             	Time--;
	             	txtCalibrationResult.setText(Integer.toString(Time));
	             }
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
			
			double x = event.values[0];
			double y = event.values[1];
			double z = event.values[2];
			double magnitude=Math.sqrt(x*x+y*y+z*z);
			magnitudes.add(magnitude);
		}

	}

}
