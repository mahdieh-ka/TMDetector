package no.uio.tmdetector;

import android.location.Location;
import android.os.SystemClock;
import android.util.Log;
import android.view.autofill.AutofillId;

import org.apache.commons.math3.filter.KalmanFilter;
import org.dmg.pmml.sequence.Time;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class TripSegment {
    private static final String TAG = TripSegment.class.getSimpleName();
    private static TripSegment instance;
    private Map<Long, Location> rawLocations;
    private Map<Long, Float> rawAccelerations;
    private Map<Long, Float> rawMagnetics;
    private static final int segmentSize = 90;
    private List<Segment> listOfSegments;




    public TripSegment(){
        initializeValues();
    }
    static TripSegment getInstance() {
        if (instance == null) {
            instance = new TripSegment();
        }
        return instance;
    }



    void addLocation(Location location) {
       rawLocations.put(location.getTime(), location);
    }

    void addAcceleration(Long time , Float acceleration) {
        rawAccelerations.put(time, acceleration);
    }

    void addMagnetic(Long time , Float magnetic) {
        rawMagnetics.put(time, magnetic);
    }

    public List<Segment> segmentation(long time) {

        long startTime = time;
        int i = 1;

        //A segment including accelerometer values between  90 seconds
         listOfSegments = new LinkedList<>();
        //inner ArrayList
        ArrayList<Float> accValues = new ArrayList<>();
        ArrayList<Long> times = new ArrayList<>();
        Segment segment = new Segment();


        Iterator it = rawAccelerations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            //calculate timeDiff
            long timeDiff = (Math.abs((long) pair.getKey() - startTime) / 1000);
            //Log.d(TAG, "accSegmentation-timeDiff: " + timeDiff );
            it.remove(); // avoids a ConcurrentModificationException
            //segment LinkedHashMap of rawAccelerations
            //todo: add overlap
            if (timeDiff > (i * segmentSize)) {
                listOfSegments.add(segment);
                segment.setAccSegment(accValues);
                segment = new Segment();
                accValues = new ArrayList<>();
                times= new ArrayList<>();
                i++;
                accValues.add((Float) pair.getValue());
                times.add((long) pair.getKey());

            } else {
                accValues.add((Float) pair.getValue());
                times.add((long) pair.getKey());
            }
            if (accValues.size() < segmentSize && it.hasNext() == false) {
                segment.setAccSegment(accValues);
                listOfSegments.add(segment);
            }
            segment.setStartTime(times.get(0));
            segment.setEndTime(times.get(times.size()-1));
        }

            for (int j = 0; j < listOfSegments.size(); j++) {
                listOfSegments.get(j).setIndex(j);
                Log.d(TAG, "accSegment=" + listOfSegments.get(j).getAccSegment());
                //Log.d(TAG, "start of segment=" + Utility.getTime(listOfSegments.get(j).getStartTime()) + "  end of segment=" + Utility.getTime(listOfSegments.get(j).getEndTime()) +
                      //  " length of segment=" + (Utility.getTimeinSeconds(listOfSegments.get(j).getEndTime()) - Utility.getTimeinSeconds(listOfSegments.get(j).getStartTime())));

            }
            locSegmentation();
            magSegmentation(startTime);

            return listOfSegments;
        }


        //segment rawLocations with the segmentSize of 90 seconds
        public void locSegmentation() {

        //inner ArrayList
        ArrayList<Location> locValues = new ArrayList<>();

        for (int i = 0; i < listOfSegments.size(); i++ ) {
            long start = Utility.getTimeinSeconds(listOfSegments.get(i).getStartTime());
            long end = Utility.getTimeinSeconds(listOfSegments.get(i).getEndTime());
            Iterator it = rawLocations.entrySet().iterator();

            CHECK:
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                boolean validLocation = filterAndAddLocation((Location) pair.getValue() );
                it.remove(); // avoids a ConcurrentModificationException
                if (validLocation) {
                    long key = Utility.getTimeinSeconds((long) pair.getKey());
                    if (key >= start && key < end) {
                        locValues.add((Location) pair.getValue());
                        //rawLocations.remove(pair.getKey());
                    }
                    if (!it.hasNext()) {
                        Segment seg = listOfSegments.get(i);
                        seg.setLocSegment(locValues);
                    }

                    if (key >= end) {
                        Segment seg = listOfSegments.get(i);
                        seg.setLocSegment(locValues);
                        locValues = new ArrayList<>();
                        break CHECK;
                    }
                }
            }
        }
            for (int j = 0; j < listOfSegments.size(); j++) {
                Log.d(TAG, "LocSegment=" + listOfSegments.get(j).getLocSegment());
            }


    }

    //todo: add magnetometer
    //segment rawMagnetics with the segmentSize of 90 seconds
    public void magSegmentation(Long startTime) {
        ArrayList<Float> magValues = new ArrayList<>();

        for (int i = 0; i < listOfSegments.size(); i++ ) {
            long start = Utility.getTimeinSeconds(listOfSegments.get(i).getStartTime());
            long end = Utility.getTimeinSeconds(listOfSegments.get(i).getEndTime());
            Iterator it = rawMagnetics.entrySet().iterator();

            CHECK:
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                it.remove(); // avoids a ConcurrentModificationException
                long key = Utility.getTimeinSeconds((long) pair.getKey());
                if (key >= start && key < end) {
                    magValues.add((Float) pair.getValue());
                    rawMagnetics.remove(pair.getKey());
                }
                if (!it.hasNext()) {
                    Segment seg = listOfSegments.get(i);
                    seg.setMagSegment(magValues);
                }

                if (key >= end) {
                    Segment seg = listOfSegments.get(i);
                    seg.setMagSegment(magValues);
                    magValues = new ArrayList<>();
                    break CHECK;
                }
            }

        }
        for (int j = 0; j < listOfSegments.size(); j++) {
            Log.d(TAG, "MagSegment=" + listOfSegments.get(j).getMagSegment());
        }


    }


    private long getLocationAge(Location newLocation ){
        long locationAge;
        if(android.os.Build.VERSION.SDK_INT >= 17) {
            long currentTimeInMilli = (long)(SystemClock.elapsedRealtimeNanos() / 1000000);
            long locationTimeInMilli = (long)(newLocation.getElapsedRealtimeNanos() / 1000000);
            locationAge = currentTimeInMilli - locationTimeInMilli;
        }else{
            locationAge = System.currentTimeMillis() - newLocation.getTime();
        }
        return locationAge;
    }
    //the location info older that 5 minutes and
    private boolean filterAndAddLocation(Location location ){

        long age = getLocationAge(location);


       if(age > 600 * 1000){ //discard location older than 10 minutes
            Log.d(TAG, "Location is old");
            return false;
        }

        if(location.getAccuracy() <= 0){ //discard location which its estimated horizontal accuracy,radial, in meters is 0 or lower
            Log.d(TAG, "Latitude and longitude values are invalid.");
            return false;
        }

        float horizontalAccuracy = location.getAccuracy();

        //when battery level is low, location accuracy is lower, so we have to discard fewer location info
        if (BatteryMonitor.batteryIsLow){
            if (horizontalAccuracy > 2500) { //discard location if horizontal accuracy is too low, the radius(equal to the accuracy) has low probability to be inside the circle
                Log.d(TAG, "Location Accuracy is too low.");
                return false;
            }
        }
        else {
            //getAccuracy() describes the deviation in meters. So, the smaller the number, the better the accuracy.
            if (horizontalAccuracy > 1500) { //discard location if horizontal accuracy is too low, the radius(equal to the accuracy) has low probability to be inside the circle
                Log.d(TAG, "Location Accuracy is too low.");
                return false;
            }
        }
        //todo: find a threshold which the location values is very far from the avg
        //it must be within the avg of the whole--> each point that is 25% above the avg should be removed
        //in addition if the speed is twice(50%) the previous and last points

        return true;
    }



    private void initializeValues() {
        //LinkedHashMap preserves the order of putting elements
        rawAccelerations = new LinkedHashMap<Long, Float>();
        rawLocations = new LinkedHashMap<Long, Location>();
        rawMagnetics = new LinkedHashMap<Long, Float>();

    }


}
