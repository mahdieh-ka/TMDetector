package no.uio.tmdetector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
/**
 * Receives magnetometer readings from the smart phone.
 */
public class MagListener implements SensorEventListener {
    private static final String TAG= "MagListener";
    private long lastEventTimestamp = System.nanoTime();
    private static long ONE_SECOND_NANOS = TimeUnit.SECONDS.toNanos(1);
    public static float magX, magY, magZ, magMagnitude;
    Queue<Float> magnitudeQueue = new LinkedList<> ();
    public static long timestamp;
    float derivative;

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
        magX = sensorEvent.values[0];
        magY = sensorEvent.values[1];
        magZ = sensorEvent.values[2];
        timestamp = Utility.correctTimestamp(sensorEvent.timestamp);
        magMagnitude = Utility.calculateMagnitude(sensorEvent);
        TripSegment.getInstance().addMagnetic(timestamp , magMagnitude);
        //Log.d(TAG, "Got magnetometer update:" + magMagnitude + " μT" + "   time: " + Utility.getTime(timestamp).substring(11,19));

       if (magnitudeQueue.size() <3) {
           magnitudeQueue.add(magMagnitude);

       }
       else {
           magnitudeQueue.remove();
           magnitudeQueue.add(magMagnitude);
           List<Float> queue = new ArrayList<>();
           for (float value : magnitudeQueue){
               queue.add(value);
           }
           derivative = ((queue.get(0) - queue.get(1) ) + (queue.get(2) - queue.get(1)))/2 ;
           //Log.d(TAG, "Derivative of magnetics: "+derivative);
       }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
