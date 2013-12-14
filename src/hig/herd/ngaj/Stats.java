package hig.herd.ngaj;


import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class Stats extends Activity {

	TimeZone MyTimezone = TimeZone.getDefault();
	Calendar calendar = Calendar.getInstance();
	SQLiteDatabase db;
	String date;
	String month;
	String year;
	String ValuesX [] = new String[7];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stats);
		
		
		try
		{
		  db=openOrCreateDatabase("NGAJ.db",MODE_PRIVATE,null);
		}
		catch(Exception e)
		{
			
		}
		if(calendar.get(Calendar.DATE)<=9)
		{
			date="0"+Integer.toString(calendar.get(Calendar.DATE));
		}
		else
		{
		date=Integer.toString(calendar.get(Calendar.DATE));
		}
		month=Integer.toString(calendar.get(Calendar.MONTH)+1);
		year=Integer.toString(calendar.get(Calendar.YEAR));

		double values1 []=new double[7]; //{5,7,13,9,4,10,6};
		double values2 []=new double[7]; //{5,7,13,5,6,10,2};
		
		
		for(int i=6;i>=0;i--)
	    {
			double TotalDistance=0;
			Cursor cr1= db.rawQuery("Select SUM(Distance) as TotalDistance From tblTracks Where Date='"+year+"-"+month+"-"+date+"'",null);
	    	if(cr1.moveToFirst())
			{
			TotalDistance = (double)cr1.getFloat(cr1.getColumnIndex("TotalDistance"));
			}
			values1[i]= Double.parseDouble(new DecimalFormat("#").format(TotalDistance)); 
		
			double TotalSteps=0;
			cr1= db.rawQuery("Select SUM(Steps) From tblTracks Where Date='"+year+"-"+month+"-"+date+"'",null);
			if(cr1.moveToFirst())
			{
			TotalSteps = cr1.getInt(0);
			}
			values2[i]=Double.parseDouble(new DecimalFormat("#").format(TotalSteps)); 
			
			ValuesX[i]=date+"-"+month;
			decreaseDate();
			
			
	    }
		
     		LinearLayout layout1 = (LinearLayout) findViewById(R.id.graph2);  
     		LinearLayout layout2=(LinearLayout) findViewById(R.id.graph1); 
     		String strGraph1="Total Discance(Km)";
     		String strGraph2="Total Steps";
     		if (Locale.getDefault().getDisplayName().equals("English (New Zealand)")) 
			{
     			strGraph1="Gjithsejt Distanca(Km)";
     			strGraph2="Gjithsejt Hapa";
			}
     		DrawGraphs(layout1,values1,strGraph1);
     		DrawGraphs(layout2,values2,strGraph2);
	}
	
	private void decreaseDate()
	{
		int intDate = Integer.parseInt(date);
		int intMonth=Integer.parseInt(month);
		int intYear=Integer.parseInt(year);
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR,intYear);
		calendar.set(Calendar.MONTH, (intMonth-1));
		
		if(intDate>1)
		{
			intDate--;
		}
		else
		{
						
			if(intMonth>1)
			{
				intMonth--;
			}
			else
			{
				intMonth=12;
				intYear--;
			}
			calendar.set(Calendar.YEAR,intYear);
			calendar.set(Calendar.MONTH, (intMonth-1));
			intDate=calendar.getActualMaximum(Calendar.DAY_OF_MONTH);	
			
		}
		
		if(intDate<=9)
		{
		date="0"+Integer.toString(intDate);
		}
		else
		{
		date=Integer.toString(intDate);	
		}
		month=Integer.toString(intMonth);
		year=Integer.toString(intYear);
	}

	
	public void DrawGraphs(LinearLayout layout,double [] values,String title)
	{
		
		GraphViewData[] data = new GraphViewData[7];  
		
		for (int i=0; i<values.length; i++) {
			data[i] = new GraphViewData(i,values[i]);  
		}  
		// graph with dynamically genereated horizontal and vertical labels  
		GraphViewSeries exampleSeries = new GraphViewSeries(data);
		GraphView graphView = new LineGraphView(  
			      this // context  
			      , title // heading  
			);  
			graphView.addSeries(exampleSeries); // data  
			graphView.setHorizontalLabels(ValuesX);
		    Arrays.sort(values);
			double min=Double.parseDouble(new DecimalFormat("#").format(values[0])); 
			double max=Double.parseDouble(new DecimalFormat("#").format(values[6]));
			double middle=Double.parseDouble(new DecimalFormat("#").format((max+min)/2));
			double m1=Double.parseDouble(new DecimalFormat("#").format((middle+max)/2));
			double m2=Double.parseDouble(new DecimalFormat("#").format((middle+min)/2));
			String a []={String.valueOf(max),String.valueOf(m1),String.valueOf(middle),String.valueOf(m2),String.valueOf(min)};
			graphView.setVerticalLabels(a);
			layout.addView(graphView); 
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
	    	Intent i = new Intent(Stats.this,CalibratePedometer.class);
			startActivity(i);
			
	    }
	    return true;
	    
	}

}
