package com.sensorcon;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

        
public class FridgeLockerUserDBHelper extends SQLiteOpenHelper {

	private static final String SQL_CREATE_ENTRIES =
        	    "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
        	    UserEntry._ID + " INTEGER PRIMARY KEY," +
        	    UserEntry.COLUMN_NAME_ENTRY_ID + " INTEGER" + "," +
        	    UserEntry.COLUMN_NAME_USERNAME + " TEXT" + "," +
        	    UserEntry.COLUMN_NAME_DATE + " TEXT" + 
        	    " )";
        
    	private static final String SQL_DELETE_ENTRIES =
        	    "DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME;

    	FridgeLockerUserDBHelper mDbHelper;
    	
    	/* Inner class that defines the table contents */
	    public static abstract class UserEntry implements BaseColumns {
	        public static final String TABLE_NAME = "ViolationsTable";
	        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
	        public static final String COLUMN_NAME_USERNAME = "UserName";
	        public static final String COLUMN_NAME_DATE = "theDate";

	    }

		    // If you change the database schema, you must increment the database version.
		    public static final int DATABASE_VERSION = 1;
		    public static final String DATABASE_NAME = "FridgeLockerDB.db";

		    public FridgeLockerUserDBHelper (Context context) {
		        super(context, DATABASE_NAME, null, DATABASE_VERSION);
		    }
		    public void onCreate(SQLiteDatabase db) {
		        db.execSQL(SQL_CREATE_ENTRIES);
		    }

		    public void addViolation(String username, String strDate) {
		    	SQLiteDatabase db = this.getWritableDatabase();
		    	 
		        ContentValues values = new ContentValues();
		        values.put(UserEntry.COLUMN_NAME_ENTRY_ID, 1); 
		        values.put(UserEntry.COLUMN_NAME_USERNAME, username); 

		        Calendar c = Calendar.getInstance();
		        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
		        String formattedDate = df.format(c.getTime());
		        values.put(UserEntry.COLUMN_NAME_DATE, formattedDate); 
		 
		        // Inserting Row
		        db.insert(UserEntry.TABLE_NAME, null, values);
		        db.close(); // Closing database connection		    	
		    }
		    
		    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		        // This database is only a cache for online data, so its upgrade policy is
		        // to simply to discard the data and start over
		        db.execSQL(SQL_DELETE_ENTRIES);
		        onCreate(db);
		    }
		    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		        onUpgrade(db, oldVersion, newVersion);
		    }
	
}

