package no.uio.tmdetector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
/**
 * Receives accelerometer readings from the smart phone and send them to the classifier.
 */
class AccListener implements SensorEventListener {
    private static final String TAG = "ACCListener";
    private long lastEventTimestamp = System.nanoTime();
    private static long ONE_SECOND_NANOS = TimeUnit.SECONDS.toNanos(1);
    public static float accX, accY, accZ, accMagnitude;
    public static long timestamp;



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        long timestampDifference = Math.abs(sensorEvent.timestamp - lastEventTimestamp);

        // It might happen to get readings sooner than 1s after the last one.
        // Update lastEventTimestamp with the current event timestamp if the previous one happened more than a second ago.
        if (timestampDifference >= ONE_SECOND_NANOS) {
            lastEventTimestamp = sensorEvent.timestamp;
        } else {
            // Just return if the last event was less than a second ago.
            return;
        }
        //The values stored in local DB
        accX = sensorEvent.values[0];
        accY = sensorEvent.values[1];
        accZ = sensorEvent.values[2];
        accMagnitude = Utility.calculateMagnitude(sensorEvent);
        //timestamp = Instant.now().toEpochMilli();
        timestamp = Utility.correctTimestamp(sensorEvent.timestamp);
        //send sensor readings for segmentation
        TripSegment.getInstance().addAcceleration(timestamp , accMagnitude);

        //Log.d(TAG,"Got acceleration update: "+accMagnitude +" m/s^2" + "   time: " + Utility.getTime(timestamp).substring(11,19));
        //store sensor readings in local database
        new BackgroundTripStorage().execute();
        }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    }


