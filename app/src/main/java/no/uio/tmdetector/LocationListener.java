package no.uio.tmdetector;
import android.location.Location;
import android.util.Log;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Receives location readings from the smartphone and sends them to the classifier.
 */
public class LocationListener extends LocationCallback{

    public static Location location_result;
    private static String TAG = "LocationListener";



    @Override
    public void onLocationResult(LocationResult locationResult) {
        for (Location location : locationResult.getLocations()) {
            Log.d(TAG, "Got location update:" + location + "    time: " +Utility.getTime(location.getTime()));
            //Log.d(TAG, "Got location update:" + location + "    time: " +Utility.getTime(Instant.now().toEpochMilli()));
            location_result = location;
            TripSegment.getInstance().addLocation(location);

        }
    }


}
