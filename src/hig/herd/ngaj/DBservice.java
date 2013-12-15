package hig.herd.ngaj;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBservice extends IntentService {

	SQLiteDatabase db;
	public DBservice() {
		super("Name");
		// TODO Auto-generated constructor stub
		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		/**
		 * Read values recieved via intent which started the service.
		 */
		String Name=intent.getStringExtra("Name");
		String strDistance = intent.getStringExtra("Distance");
		float Distance=Float.parseFloat(strDistance);
		String SpeedExtras=intent.getStringExtra("SpeedExtras");
		String Time=intent.getStringExtra("Time");
		String strSteps = intent.getStringExtra("Steps");
		int Steps=Integer.parseInt(strSteps);
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		dateFormatter.setLenient(false);
		Date today = new Date();
		String strDate = dateFormatter.format(today);
		int size = intent.getIntExtra("Size", 0);
		
		/**
		 * Save the just read values into local database if needed create the database and tables.
		 */
		
		db=openOrCreateDatabase("NGAJ.db",MODE_PRIVATE,null);
		db.execSQL("Create Table if not exists tblTracks (ID INTEGER PRIMARY KEY AUTOINCREMENT,Name Varchar,Distance FLOAT,Time Varchar,SpeedExtras Varchar, Steps Integer,Date DateTime)");
		db.execSQL("Insert into tblTracks VALUES ((SELECT max(ID) FROM tblTracks)+1,'"+Name+"',"+Distance+",'"+Time+"','"+SpeedExtras+"',"+Steps+",'"+strDate+"');");
		db.execSQL("Create Table if not exists tblPoints (PointID INTEGER PRIMARY KEY AUTOINCREMENT,TrackID ID INTEGER,Latitude FLOAT,Longitude FLOAT)");
		Cursor cr = db.rawQuery("SELECT max(ID) FROM tblTracks", null);
		int id=0;
		if(cr.moveToFirst())
		{
			id=cr.getInt(0);
		}
		for(int j=0;j<size;j++)
		{
			float lat =  intent.getFloatExtra("Cord_Lat_" + j, (float) 5.0);
            float lng =  intent.getFloatExtra("Cord_Long_" + j, (float) 5.0);
			db.execSQL("Insert into tblPoints Values ((SELECT max(PointID) FROM tblPoints)+1,"+id+","+lat+","+lng+");");
		}
	}

}
