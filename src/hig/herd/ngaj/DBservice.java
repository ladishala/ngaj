package hig.herd.ngaj;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

public class DBservice extends IntentService {

	SQLiteDatabase db;
	public DBservice() {
		super("Name");
		// TODO Auto-generated constructor stub
		
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		String Name=intent.getStringExtra("Name");
		String strDistance = intent.getStringExtra("Distance");
		float Distance=Float.parseFloat(strDistance);
		String SpeedExtras=intent.getStringExtra("SpeedExtras");
		String Time=intent.getStringExtra("Time");
		String strSteps = intent.getStringExtra("Steps");
		int Steps=Integer.parseInt(strSteps);
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-mm-dd");
		dateFormatter.setLenient(false);
		Date today = new Date();
		String strDate = dateFormatter.format(today);
		
		db=openOrCreateDatabase("NGAJ.db",MODE_PRIVATE,null);
		db.execSQL("Create Table if not exists tblTracks (ID INTEGER PRIMARY KEY AUTOINCREMENT,Name Varchar,Distance FLOAT,Time Varchar,SpeedExtras Varchar, Steps Integer,Date DateTime)");
		db.execSQL("Insert into tblTracks VALUES ((SELECT max(ID) FROM tblTracks)+1,'"+Name+"',"+Distance+",'"+Time+"','"+SpeedExtras+"',"+Steps+",'"+strDate+"');");
		//db.execSQL("Create Table if not exists tblRelationship (Relation Varchar)");
	}

}
