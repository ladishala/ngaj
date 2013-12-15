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

	/**
	 * Declare UI components, DB components and activity variables.
	 */
	ListView myList;
	List<String> listItems = new ArrayList<String>();
	List<String> listItemExtras = new ArrayList<String>();
	List<Integer> listIDs = new ArrayList<Integer>();
	ArrayAdapter<String> myAdapter;
	AlertDialog.Builder Alert;
	SQLiteDatabase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracks);

		// Connect to database, Create database if needed.
		db = openOrCreateDatabase("NGAJ.db", MODE_PRIVATE, null);

		// Initialize listview variable
		myList = (ListView) findViewById(R.id.listView1);

		/**
		 * Taken From:
		 * http://stackoverflow.com/questions/11256563/how-to-set-both
		 * -lines-of-a-listview-using-simple-list-item-2
		 * 
		 * This piece of code is used to initialize the ArrayAdapter and
		 * override it's getView method in order to be able to show more than
		 * one line per each listView item in our case two lines. The first line
		 * is read from listItems list and the second from listItemExtras list.
		 */
		myAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_2, android.R.id.text1,
				listItems) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView text1 = (TextView) view
						.findViewById(android.R.id.text1);
				TextView text2 = (TextView) view
						.findViewById(android.R.id.text2);

				text1.setText(listItems.get(position));
				text2.setText(listItemExtras.get(position));
				return view;
			}
		};

		// Set myList adapter to just initialized adapter.
		myList.setAdapter(myAdapter);

		// Set and implement onItemClick and onItemLongClick listeners
		myList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				/**
				 * When an item of listview is clicked the showResults method is
				 * called with clicked item id passed as parameter of method.
				 */
				showResults(arg2);

			}
		});
		myList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				/**
				 * When an item of listview is long clicked the showRemoveAlert
				 * method is called with long clicked item id passed as
				 * parameter of method.
				 */
				showRemoveAlert(arg2);
				return false;
			}

		});
		// Call fillList methos to fill listview with items.
		fillList();
	}

	/**
	 * This method starts Results activity.
	 * 
	 * It also sends to Results activity via starting intent the details of
	 * clicked track which is identified by parameter clickedID.
	 * 
	 * Before sending them via intent it has reads these details from local
	 * database. Each track in local database is identified by it's id. While
	 * filling the listview these id's are read from local db and are saved on
	 * listIDs list. Now this previously saved value is read from listIDs using
	 * clickedID as index. Using ID read from listIDs all the details for the
	 * clicked track are read from local database.
	 */
	private void showResults(int clickedID) {
		Intent i = new Intent(Tracks.this, Results.class);
		Bundle extras = new Bundle();
		extras.putString("Name", listItems.get(clickedID));
		try {
			Cursor cr1 = db.rawQuery("Select * From tblTracks Where ID="
					+ listIDs.get(clickedID), null);
			cr1.moveToFirst();

			String SpeedExtras = cr1.getString(cr1
					.getColumnIndex("SpeedExtras"));
			String Speed = SpeedExtras.substring(0, SpeedExtras.indexOf(" "));

			extras.putString("Name", listItems.get(clickedID));
			extras.putString("Time", cr1.getString(cr1.getColumnIndex("Time")));
			extras.putString("Steps",
					cr1.getString(cr1.getColumnIndex("Steps")));
			extras.putString("Distance",
					cr1.getString(cr1.getColumnIndex("Distance")));
			extras.putString("SpeedExtras", SpeedExtras);
			extras.putString("Speed", Speed);
			Cursor cr2 = db.rawQuery(
					"Select Latitude,Longitude From tblPoints Where TrackID="
							+ listIDs.get(clickedID), null);
			int j = 0;
			while (cr2.moveToNext()) {
				float lat = cr2.getFloat(0);
				float lng = cr2.getFloat(1);
				extras.putFloat("Cord_Lat_" + j, (float) lat);
				extras.putFloat("Cord_Long_" + j, (float) lng);
				j++;
			}
			extras.putInt("Size", j);
		} catch (Exception e) {

		}
		i.putExtras(extras);
		startActivity(i);
	}

	/**
	 * This methos fills the listview with items. It reads the tracks from
	 * database and for each read track it saves it's ID on listIDs list, it
	 * also saves track name on listItems and track's time and distance on
	 * listItemExtras list. Then the method notifyDataSetChanged of array
	 * adapter is called and it fills the listview withh all items of listItems
	 * and listItemExtras.1
	 */
	private void fillList() {
		try {
			Cursor cr1 = db.rawQuery("Select * From tblTracks", null);
			if (cr1 != null) {
				while (cr1.moveToNext()) {
					listIDs.add(cr1.getInt(cr1.getColumnIndex("ID")));
					listItems.add(cr1.getString(cr1.getColumnIndex("Name")));
					String Extras = "Time: ";
					if (Locale.getDefault().getDisplayName()
							.equals("English (New Zealand)")) {
						Extras = "Koha: ";
					}
					Extras += cr1.getString(cr1.getColumnIndex("Time"));
					if (Locale.getDefault().getDisplayName()
							.equals("English (New Zealand)")) {
						Extras += " Distanca: ";
					} else {
						Extras += " Distance: ";
					}
					Extras += cr1.getString(cr1.getColumnIndex("Distance"));
					Extras += " km";
					listItemExtras.add(Extras);
				}
				myAdapter.notifyDataSetChanged();

			}
		} catch (Exception Ex) {

		}
	}

	/**
	 * This method shown the Remove AlertDialog where user has to confirm track
	 * deletion If user confirms track deletion by choosing yes then the track
	 * is deleted from database and track details are deleted from listItems,
	 * listIDs and listItemExtras lists of the activity. Also track is deleted
	 * from listview by calling notifyDataSetChanged method of array adapter.
	 * 
	 * The track on the database is identified by it's ID which is retrieved
	 * from listIDs list by using clickedID parameter as index. While on the
	 * lists track is identified by clickedID which is parameter of this method.
	 */
	private void showRemoveAlert(final int clickedID) {
		Alert = new AlertDialog.Builder(this);
		String strTitle = "Confirm Delete!";
		String strMessage = "Are you sure you want to delete track '"
				+ listItems.get(clickedID) + "'?";
		String strPositive = "Yes";
		String strNegative = "Cancel";
		if (Locale.getDefault().getDisplayName()
				.equals("English (New Zealand)")) {
			strTitle = "Konfirmo fshirjen!";
			strMessage = "A deshironi ta fshini shtegun '"
					+ listItems.get(clickedID) + "'?";
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
							db.execSQL("Delete From tblTracks Where ID="
									+ listIDs.get(clickedID));
							listItems.remove(clickedID);
							listItemExtras.remove(clickedID);
							listIDs.remove(clickedID);
							myAdapter.notifyDataSetChanged();
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

					}
				});
		Alert.show();

	}
}
