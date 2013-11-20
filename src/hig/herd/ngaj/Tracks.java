package hig.herd.ngaj;

import java.util.ArrayList;
import java.util.List;

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
                    		Extras+=cr1.getString(cr1.getColumnIndex("Time"));
                    		Extras+=" Distance: ";
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
		Alert.setTitle("Confirm Delete!");
		Alert.setMessage("Are you sure you want to delete track '"+listItems.get(arg)+"'?");
		Alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
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
		Alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			
			}
		});
		Alert.show();
				
	}

	
	
	
}

