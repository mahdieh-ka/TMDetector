package no.uio.tmdetector;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
//********** the problem here is when I store the Location in the format of Location
//I can not get them back in the same format....this does not cuase problem for generating another classifier
//but it is problem when I want to fit the values on the classifier
import static no.uio.tmdetector.MainActivity.rawDataDB;

class BackgroundTripQuery extends AsyncTask<String, Void, List<Float> []> {
    private static final String TAG = BackgroundTripStorage.class.getSimpleName();
    private static final int segmentSize = 90;


    @Override
    protected List<Float>[] doInBackground(String... params) {
        // query the whole trip for segmentation

        String tripId= params[0];
        //Log.d(TAG,  "tripid" +tripId);
        Cursor cursor = rawDataDB.showtripbyid(tripId);
        List<Float>[] listOfMagSegments = null;

        if (cursor.getCount() != 0) {
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
        }

        cursor.close();

        return listOfMagSegments;
    }

    @Override
    protected void onPostExecute(List<Float>[] listOfAccSegments) {
        super.onPostExecute(listOfAccSegments);

        
    }



    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

}
