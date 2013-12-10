package hig.herd.ngaj;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Tracks extends Activity {

	ListView myList;
	List<String> listItems= new ArrayList<String>();
	List<String> listItemExtras= new ArrayList<String>();
	List<Integer> listIDs= new ArrayList<Integer>();
	ArrayAdapter<String> myAdapter;
	AlertDialog.Builder Alert;
	SQLiteDatabase db;
			
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracks);
		db=openOrCreateDatabase("NGAJ.db",MODE_PRIVATE,null);
		myList=(ListView)findViewById(R.id.listView1);
			
		//Taken From http://stackoverflow.com/questions/11256563/how-to-set-both-lines-of-a-listview-using-simple-list-item-2
		myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, listItems) {
			  @Override
			  public View getView(int position, View convertView, ViewGroup parent) {
			    View view = super.getView(position, convertView, parent);
			    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
			    TextView text2 = (TextView) view.findViewById(android.R.id.text2);

			    text1.setText(listItems.get(position));
			    text2.setText(listItemExtras.get(position));
			    return view;
			  }
			};
		myList.setAdapter(myAdapter);
		myList.setOnItemClickListener(new OnItemClickListener()
		{
		  	@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
		  		showResults(arg2);
		  		
			}
		});
		myList.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				showRemoveAlert(arg2);
				return false;
			}
			
		});
		fillLists();	
	}
	private void showResults(int arg2)
	{
		Intent i = new Intent(Tracks.this,Results.class);
		Bundle extras = new Bundle();
		extras.putString("Name", listItems.get(arg2));
		try{
			Cursor cr1 = db.rawQuery("Select * From tblTracks Where ID="+listIDs.get(arg2),null);
			cr1.moveToFirst();
			
			String SpeedExtras=cr1.getString(cr1.getColumnIndex("SpeedExtras"));
			String Speed = SpeedExtras.substring(0, SpeedExtras.indexOf(" "));
			
			extras.putString("Name", listItems.get(arg2));
			extras.putString("Time", cr1.getString(cr1.getColumnIndex("Time")));		
			extras.putString("Steps", cr1.getString(cr1.getColumnIndex("Steps")));
			extras.putString("Distance", cr1.getString(cr1.getColumnIndex("Distance")));
			extras.putString("SpeedExtras", SpeedExtras);
			extras.putString("Speed", Speed);
			Cursor cr2=db.rawQuery("Select Latitude,Longitude From tblPoints Where TrackID="+listIDs.get(arg2), null);
			int j=0;
			while(cr2.moveToNext())
			{
				float lat = cr2.getFloat(0);
				float lng = cr2.getFloat(1);
				extras.putFloat("Cord_Lat_" + j,(float) lat);
				extras.putFloat("Cord_Long_" + j,(float) lng);
				j++;
			}
			extras.putInt("Size", j);
		}
		catch(Exception e)
		{
			
		}
		
		i.putExtras(extras);
		startActivity(i);		
	}
	private void fillLists()
	{
		try{
            Cursor cr1 = db.rawQuery("Select * From tblTracks",null);
            if(cr1!=null)
            {
                    while(cr1.moveToNext())
                    	{
                    		listIDs.add(cr1.getInt(cr1.getColumnIndex("ID")));
                    		listItems.add(cr1.getString(cr1.getColumnIndex("Name")));
                    		String Extras="Time: ";
                    		if(Locale.getDefault().getDisplayName().equals("English (New Zealand)"))
                    		{
                    			Extras="Koha: ";
                    		}
                    		Extras+=cr1.getString(cr1.getColumnIndex("Time"));
                    		if(Locale.getDefault().getDisplayName().equals("English (New Zealand)"))
                    		{
                    			Extras+=" Distanca: ";
                    		}
                    		else
                    		{
                    			Extras+=" Distance: ";
                    		}
                    		Extras+=cr1.getString(cr1.getColumnIndex("Distance"));
                    		Extras+=" km";
                    		listItemExtras.add(Extras);
                    	}
                    myAdapter.notifyDataSetChanged();
                   
                    
            }
            }
            catch(Exception Ex)
            {
                    
            }
	}
	private void showRemoveAlert(final int arg)
	{
		Alert = new AlertDialog.Builder(this);
		String strTitle="Confirm Delete!";
		String strMessage="Are you sure you want to delete track '"+listItems.get(arg)+"'?";
		String strPositive="Yes";
		String strNegative="Cancel";
		if(Locale.getDefault().getDisplayName().equals("English (New Zealand)"))
		{
			strTitle="Konfirmo fshirjen!";
			strMessage = "A deshironi ta fshini shtegun '"+listItems.get(arg)+"'?";
			strPositive="Po";
			strNegative="Jo";
		}
		Alert.setTitle(strTitle);
		Alert.setMessage(strMessage);
		Alert.setPositiveButton(strPositive, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				try {
					db.execSQL("Delete From tblTracks Where ID="+listIDs.get(arg));
					listItems.remove(arg);
					listItemExtras.remove(arg);
					listIDs.remove(arg);
					myAdapter.notifyDataSetChanged();
				} catch (Exception ex) {
					// TODO Auto-generated catch block
				;
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

	
	
	
}

