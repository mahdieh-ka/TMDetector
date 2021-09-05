package no.uio.tmdetector;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class BackgroundTripStorage extends AsyncTask<Void, Void, Boolean> {
    private static BackgroundTripStorage instance;
    private static final String TAG = BackgroundTripStorage.class.getSimpleName();
    private final static String OS = "Android";
    private String accX, accY, accZ, accMagnitude, magX, magY, magZ, magMagnitude,
            tripStartDate ,legStartDate , eventDate, latitude , longitude, accuracy;
    private long timestamp ,eventTimestamp ,tripStartDateTs ,  legStartDateTs;
    private Location location;
    private int tripId;
    public static int correctedModeOfTransport,previousSelectedMode, legId , previousTripId;
    private AccListener accListener;
    private MagListener magListener;
    private TripManager tripManager;


    //Inserting data to the local database in the background
    @Override
    protected Boolean doInBackground(Void... voids) {

        accX = String.valueOf(accListener.accX);
        accY = String.valueOf(accListener.accY);
        accZ = String.valueOf(accListener.accZ);
        accMagnitude = String.valueOf(accListener.accMagnitude);
        timestamp = accListener.timestamp;

        magX = String.valueOf(magListener.magX);
        magY = String.valueOf(magListener.magY);
        magZ = String.valueOf(magListener.magZ);
        magMagnitude = String.valueOf(magListener.magMagnitude);

        location = LocationListener.location_result;
        if (location != null) {
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            accuracy = String.valueOf(location.getAccuracy());
        }

        DateFormat dateFormatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        tripStartDate = dateFormatter.format(tripManager.currentTrip.getStartDate());
        tripStartDateTs =tripManager.currentTrip.getStartDateTs();
        tripId= tripManager.tripId;
        if (previousTripId != tripId){initializeValues();}
        previousTripId= tripId;



        eventDate = dateFormatter.format(timestamp);
        eventTimestamp = timestamp;

        if (DetectionActivity.selectedMode != null) {
            correctedModeOfTransport = Utility.modeStringToInteger(DetectionActivity.selectedMode);
        }

        //if a new leg started - the user changed the selected mode
        if (previousSelectedMode != correctedModeOfTransport){
            Log.d(TAG, "previousSelectedMode=" + previousSelectedMode + "   correctedModeOfTransport= "+ correctedModeOfTransport);
            previousSelectedMode = correctedModeOfTransport;
            Log.d(TAG, "previousSelectedMode=" + previousSelectedMode);
            legId++;
            legStartDate = dateFormatter.format(timestamp);
            legStartDateTs = timestamp;
            MainActivity.rawDataDB.addData(tripStartDate,legStartDate, String.valueOf(tripStartDateTs), String.valueOf(legStartDateTs), String.valueOf(tripId),
                    String.valueOf(legId), OS, null, String.valueOf(correctedModeOfTransport), eventDate, String.valueOf(eventTimestamp),
                    accX, accY, accZ, latitude, longitude, accuracy, accMagnitude, magX, magY, magZ, magMagnitude, String.valueOf(location));
            Log.d(TAG, "tripStartDate= "+ tripStartDate + "legStartDate= " + legStartDate + " tripId= " + tripId + " legId= " + legId +
                    " OS= " + OS + " correctedModeOfTransport= " + correctedModeOfTransport + " eventDate= " + eventDate + " accMagnitude= " + accMagnitude
                    + " magMagnitude= " + magMagnitude + " location= " + location);
            Log.d(TAG, "---------------------------------------------------- ");
        }
        else{
            MainActivity.rawDataDB.addData("", "", "", "", String.valueOf(tripId), "", "", "",
                    "", eventDate, String.valueOf(eventTimestamp), accX, accY, accZ, latitude, longitude,
                    accuracy, accMagnitude, magX, magY, magZ, magMagnitude, String.valueOf(location));
           /* Log.d(TAG, " eventDate= " + eventDate + " accMagnitude= " + accMagnitude
                    + " magMagnitude= " + magMagnitude + " location= " + location);*/

        }


        return null;
    }
    public void initializeValues(){
        previousSelectedMode = -1;
        correctedModeOfTransport = -2;
        legId = 0;


    }




}
