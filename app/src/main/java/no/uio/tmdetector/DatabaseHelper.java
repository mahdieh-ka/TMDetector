package no.uio.tmdetector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import androidx.annotation.Nullable;
/**
 * DatabaseHelper for storing sensors readings locally on the smartphone
 */
class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sensorReadings.db";
    public static final String TABLE_NAME = "rawdata_table";
    public static final String COL1 = "tripStartDate";
    public static final String COL2 = "legStartDate";
    public static final String COL3 = "tripStartDateTs";
    public static final String COL4 = "legStartDateTs";
    public static final String COL5 = "tripId";
    public static final String COL6 = "legId";
    public static final String COL7 = "OS";
    public static final String COL8 = "activityDetected";
    public static final String COL9 = "correctedModeOfTransport";
    public static final String COL10 = "eventDate";
    public static final String COL11 = "eventTimestamp";
    public static final String COL12 = "x"; //accelX
    public static final String COL13 = "y"; //accelY
    public static final String COL14 = "z"; //accelZ
    public static final String COL15 = "lat"; //latitude
    public static final String COL16 = "lon"; //longitude
    public static final String COL17 = "acc"; //location accuracy
    public static final String COL18 = "accMagnitude";
    public static final String COL19 = "magX";
    public static final String COL20 = "magY";
    public static final String COL21 = "magZ";
    public static final String COL22 = "magMagnitude";
    public static final String COL23 = "location";
    public static final String COL24 = "city";


    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable= "CREATE TABLE " + TABLE_NAME+ "(tripStartDate TEXT,legStartDate TEXT, tripStartDateTs TEXT, legStartDateTs TEXT, tripId TEXT , legId TEXT , OS TEXT, " +
                "activityDetected TEXT ,correctedModeOfTransport TEXT, eventDate TEXT, eventTimestamp TEXT, x TEXT ,y TEXT, z TEXT, lat TEXT , lon TEXT , acc TEXT ,accMagnitude TEXT, magX TEXT, " +
                "magY TEXT,magZ TEXT, magMagnitude TEXT, location TEXT, CITY TEXT )" ;
        db.execSQL(createTable);

    }

    // upgrade table (when the schema changes)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);

    }

    //insert data to the database
    public boolean addData(String tripStartDate , String legStartDate, String tripStartDateTs , String legStartDateTs , String tripId , String legId,
                           String OS , String activityDetected , String correctedModeOfTransport , String eventDate , String eventTimestamp , String x, String y ,
                           String z , String lat , String lon, String acc , String accMagnitude , String magX , String magY , String magZ , String magMagnitude , String location , String city ){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, tripStartDate);
        contentValues.put(COL2, legStartDate);
        contentValues.put(COL3, tripStartDateTs);
        contentValues.put(COL4, legStartDateTs);
        contentValues.put(COL5, tripId);
        contentValues.put(COL6, legId);
        contentValues.put(COL7, OS);
        contentValues.put(COL8, activityDetected);
        contentValues.put(COL9, correctedModeOfTransport);
        contentValues.put(COL10, eventDate);
        contentValues.put(COL11, eventTimestamp);
        contentValues.put(COL12, x);
        contentValues.put(COL13, y);
        contentValues.put(COL14 , z);
        contentValues.put(COL15 ,lat);
        contentValues.put(COL16, lon );
        contentValues.put(COL17, acc);
        contentValues.put(COL18, accMagnitude);
        contentValues.put(COL19, magX);
        contentValues.put(COL20, magY);
        contentValues.put(COL21, magZ);
        contentValues.put(COL22, magMagnitude);
        contentValues.put(COL23, location);
        contentValues.put(COL24, city);


        long result = db.insert(TABLE_NAME , null , contentValues);
        if (result == -1){
            return false;
        }else
        {
            return true;}

    }
    // update MODE column
    public boolean updateData(String correctedModeOfTransport , String tripId){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL9, correctedModeOfTransport);
        db.update(TABLE_NAME,contentValues,"TRIPID=?", new String[] {tripId});
        return true;
    }
    //query tripIDs and timestamps
    public Cursor QueryTripIdAndMode(){
        SQLiteDatabase db = getWritableDatabase();
        String[] queryCols = {COL5,COL9 };  // TripId, correctedModeOfTransport
        Cursor data = db.query(TABLE_NAME, // the table to query
                queryCols,                // the columns to return
                null,            // the columns for the WHERE clause
                null,         // the values for the WHERE clause
                null,             // don't group the rows
                null,               // don't filter by row groups
                null
        );

        return data;
    }


    //Show all the mag records based on the the selcted trip id
    public Cursor showtripbyid (String tripId ){
        SQLiteDatabase db = getWritableDatabase();
        Cursor data = db.rawQuery(" SELECT * FROM " + TABLE_NAME + " WHERE TRIPID = ?", new String[]{tripId} );
        return data;
    }
    // delete selected trip
    public Integer deleteByTripId (String tripId){
        SQLiteDatabase db = getWritableDatabase();
        Integer result= db.delete(TABLE_NAME,"TRIPID=?",new String[] {tripId});
        return result;
    }

    //upgrade the database
    public void upgrade(){
        SQLiteDatabase db = getWritableDatabase();
        onUpgrade(db, 3,4);
    }

    //delete rows (test purposes)
    public void deleterows(){
        SQLiteDatabase db= getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
    }
}
