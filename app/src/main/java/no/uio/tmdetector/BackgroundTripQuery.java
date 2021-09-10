package no.uio.tmdetector;

import android.database.Cursor;
import android.os.AsyncTask;

import java.util.ArrayList;
//********** the problem here is when I store the Location in the format of Location
//I can not get them back in the same format....this does not cuase problem for generating another classifier
//but it is problem when I want to fit the values on the classifier
import static no.uio.tmdetector.MainActivity.rawDataDB;

/**
 * This class do a background query based on the tripId and returns all the required columns of a trip
 */
class BackgroundTripQuery extends AsyncTask<String, Void, ArrayList<ArrayList<String>>> {
    private static final String TAG = BackgroundTripStorage.class.getSimpleName();
    private static final int segmentSize = 90;
    private Cursor cursor;
    ArrayList<ArrayList<String>> allValues = new ArrayList<>(); // the required columns
    ArrayList<String> correctedModeOfTransports = new ArrayList<>(); //column 8
    ArrayList<String> magMagnitudes = new ArrayList<>(); //column 21


    @Override
    protected ArrayList<ArrayList<String>> doInBackground(String... params) {
        String tripId= params[0];
        cursor = rawDataDB.showtripbyid(tripId);
        if (cursor.getCount() != 0) {
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                //get magnitude of magnetometer readings
                magMagnitudes.add(cursor.getString(21));
                //get the list of actual modes of the trip
                    if (!cursor.getString(8).isEmpty()){
                        correctedModeOfTransports.add(cursor.getString(8));
                    }
                }
            allValues.add(magMagnitudes);
            allValues.add(correctedModeOfTransports);
            }

        cursor.close();

        return allValues;
    }

    @Override
    protected void onPostExecute(ArrayList<ArrayList<String>> allValues) {
        super.onPostExecute(allValues);

        
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

}
 /*if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            long firstTimeStamp = cursor.getLong(10); //eventTimestamp column
            cursor.moveToLast();
            long lastTimeStamp = cursor.getLong(10);
            int numberOfSegments = (int) ((lastTimeStamp - firstTimeStamp) / segmentSize* 1000)+1;
            //Log.d(TAG, "numberOfSegments: "+numberOfSegments);
            listOfMagSegments = new List[numberOfSegments];

            cursor.moveToFirst();
            int i=0;
            ArrayList<Float> magSegment = new ArrayList<>();; //A segment including magnetometer values with 90 seconds duration
            while (!cursor.isAfterLast()) {
                if (i < numberOfSegments) {
                    long timeDifference = (cursor.getLong(10) - firstTimeStamp) / 1000;
                    //Log.d(TAG, "timeDifference: " + timeDifference);
                    if (timeDifference > ((i + 1) * 10) ) {
                        listOfMagSegments[i] = magSegment;
                        magSegment = new ArrayList<>();
                        magSegment.add(cursor.getFloat(21));
                        i++;
                    } else {
                        magSegment.add(cursor.getFloat(21));
                    }
                }
                cursor.moveToNext();
               // Log.d(TAG, "accSegment: "+accSegment);
                if (i+1 == numberOfSegments){
                    listOfMagSegments[i] = magSegment;

                }
            }

            //Log.d(TAG, "list of segments size: " + listOfAccSegments.length);
            for (int j=0 ; j < numberOfSegments ; j++){
                //Log.d(TAG, "listOfAccSegments: "+listOfAccSegments[j]);
                //Classifier.getInstance().addAcceleration(listOfAccSegments[j]);
                //Log.d(TAG, "---------------------------- ");
                //Log.d(TAG, "listOfLocSegments: "+listOfLocSegments[j]);
              //  Classifier.getInstance().addLocation(listOfLocSegments[j]);

            }
        }*/

