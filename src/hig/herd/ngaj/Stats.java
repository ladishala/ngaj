package hig.herd.ngaj;

import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class Stats extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stats);
		
		double values1 []=new double[]{5,7,13,9,4,10,6};
		double values2 []=new double[]{5,7,13,5,6,10,2};
		
     		LinearLayout layout1 = (LinearLayout) findViewById(R.id.graph2);  
     		LinearLayout layout2=(LinearLayout) findViewById(R.id.graph1); 
     		DrawGraphs(layout1,values1);
     		DrawGraphs(layout2,values2);
		
		
		
	}
	
	public void DrawGraphs(LinearLayout layout,double [] values)
	{
		
		GraphViewData[] data = new GraphViewData[7];  
		
		for (int i=0; i<values.length; i++) {
			data[i] = new GraphViewData(i,values[i]);  
		}  
		// graph with dynamically genereated horizontal and vertical labels  
		GraphViewSeries exampleSeries = new GraphViewSeries(data);
		GraphView graphView = new LineGraphView(  
			      this // context  
			      , "GraphViewDemo" // heading  
			);  
			graphView.addSeries(exampleSeries); // data  
			graphView.setHorizontalLabels(new String[] {"M", "T","W","TH","F","Sa","S"});
		    Arrays.sort(values);
			double min=values[0];
			double max=values[6];
			double middle=(max+min)/2;
			double m1=(middle+max)/2;
			double m2=(middle+min)/2;
			String a []={String.valueOf(max),String.valueOf(m1),String.valueOf(middle),String.valueOf(m2),String.valueOf(min)};
			graphView.setVerticalLabels(a);
			layout.addView(graphView); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stats, menu);
		return true;
	}

}
