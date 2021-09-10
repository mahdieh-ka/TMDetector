package no.uio.tmdetector;

import android.content.Context;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.joda.time.chrono.LenientChronology;
import org.w3c.dom.ls.LSException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.grpc.okhttp.internal.Util;


/**
 * Class responsible for managing and collecting trip data.
 */
public class TripManager {

    /**
     *
     *
     *  When in trip, there are two possible states: Trip, Waiting Event.
     *
     *  A trip starts if the user commutes to place at least 100 meters away (goes from Still state to
     *  the Trip state)
     *  Throughout a trip, if the user stay within a 100 meters radius for more than 5 minutes, user
     *  go to the WaitingEvent state. If the user leaves a 100 meters radius again,
     *  goes back to Trip state. If the the user stays within a 100 meters radius for another 25
     *  minutes, the trip is ended (goes to still state, the last waiting event is removed - because the
     *  trip has ended and the trip is saved).
     *
     *
     *
     */

    // Constants
    /**
     * minimum radius(meters)  - more than 100 meters to account for the false positives
     */
    final static int tripDistanceLimit = 115;

    /**
     * time constant - 5 minutes in milliseconds - time within a tripDistance limit to go from trip
     * to waiting event
     */
    int tripTimeLimit = 5*60;

    /**
     * time constant - 25 minutes in milliseconds - time within a tripDistance limit to go from
     * waiting event state to still state (end of trip)
     */
    int fullTripTimeLimit = 25*60*1000;

    /**
     * time constant - 30 minutes in milliseconds - time interval which a recovered trip snapshot is
     * considered to be valid.
     */
    final int tripSnapshotFreshTime = 30*60*1000;

    private static String TAG = "TripManager";
    private SensorManager sensorManager;
    private AccListener accelerationListener;
    private Sensor accelerometer,magnetometer;
    private MagListener magnetometerListener;
    private FusedLocationProviderClient locationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    public static Trip currentTrip;
    private static final long ACCEL_SAMPLING_PERIOD = TimeUnit.SECONDS.toMicros(1);
    private static final long GPS_SAMPLING_INTERVAL = TimeUnit.SECONDS.toMillis(10); // 10 seconds in MILIseconds
    private boolean tripInProgress = false;
    public static Integer tripId;
    private long startTime;
    float predictedDistance;
    DetectionActivity detectionActivity = new DetectionActivity();





    TripManager(Context context) {
        // setup accelerometer
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        accelerationListener = new AccListener();

        //setup magnetometer
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magnetometerListener = new MagListener();

        // setup location params
        locationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        locationRequest = new LocationRequest();
        locationRequest.setInterval(GPS_SAMPLING_INTERVAL);
        locationRequest.setMaxWaitTime(GPS_SAMPLING_INTERVAL);
        // callback to receive location updates
        locationCallback = new LocationListener();
    }




    /**
     * Starts recording a trip
     */
    void startTrip() {
        sensorManager.registerListener(accelerationListener,accelerometer, (int) ACCEL_SAMPLING_PERIOD, (int) ACCEL_SAMPLING_PERIOD);
        Log.d(TAG, "Started accelerometer");
        sensorManager.registerListener(magnetometerListener,magnetometer,(int) ACCEL_SAMPLING_PERIOD, (int) ACCEL_SAMPLING_PERIOD);
        Log.d(TAG, "Started magnetometer");
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        Log.d(TAG, "Started GPS");
        tripInProgress = true;
        currentTrip = new Trip();
        currentTrip.start();



        //if there is any previously stored trip in the database get the last tripId
        Cursor cursor = MainActivity.rawDataDB.QueryTripIdAndMode();
        if (cursor.getCount()!= 0) {
            cursor.moveToLast();
            tripId = Integer.valueOf(cursor.getString(0));


        }
        else{ tripId =0; }
        //increase the tripId to store the new data with the new Id
        ++tripId;
        cursor.close();

    }


    /**
     * Stops recording a trip
     */
    void stopTrip() {
        // stop sensors
        sensorManager.unregisterListener(accelerationListener, accelerometer);
        Log.d(TAG, "Stopped accelerometer");
        sensorManager.unregisterListener(magnetometerListener, magnetometer);
        Log.d(TAG, "Stopped magnetometer ");
        locationProviderClient.removeLocationUpdates(locationCallback);
        Log.d(TAG, "Stopped GPS");

        // stop trip
        if (tripInProgress && currentTrip != null) {
            tripInProgress = false;
            currentTrip.finish();
            startTime = System.nanoTime();


            List<Segment> tripSegments = TripSegment.getInstance().segmentation(currentTrip.getStartDate().getTime());
            //insert segments with no location in a waiting list and correct their location based on last and previous segments with available location info
            locationCorrection(tripSegments);
            //call the classifier for each segment separately
            for (int i = 0; i < tripSegments.size(); i++) {
                    Classifier classifier = new Classifier();
                    classifier.addLocation(tripSegments.get(i).getLocSegment());
                    classifier.addAcceleration(tripSegments.get(i).getAccSegment());
                    segmentAssignments(classifier,i,tripSegments);
            }
            //post-process Trip
            //merge segments with the same mode to leg
            List<Leg> tripLegs = mergingSegments(tripSegments);
            Log.d(TAG, "---------------------------------------- ");
            long endTime = System.nanoTime();
            long classificationTime = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
            Log.d(TAG, "Classification took " + classificationTime + " ms");


            long tripTime = (currentTrip.getEndDate().getTime() - currentTrip.getStartDate().getTime());
            long tripTimeinSeconds = (tripTime/1000) ;
            Log.d(TAG, "What is trip time?: "+tripTimeinSeconds);

            /*//add all the actual modes
            Log.d(TAG, "print actual modes: "+actualMode);
            int i=0;
            while (actualMode != null){
                tripActualModes[i] =actualMode;
                i++;
            }*/

            //todo: introduce an arrayList of modeIds which reflects the different legs in a trip
            currentTrip.setTimeToClassify(classificationTime);
            currentTrip.setDistance(predictedDistance);
            currentTrip.setTripId(tripId);
            currentTrip.setSegments(tripSegments);
            currentTrip.setLegs(tripLegs);
            currentTrip.setModeId(tripLegs.get(0).getModeId());


            // save trip
            TripRepository.save(currentTrip);
            currentTrip = null;

        }
    }



    /**
     * @param tripSegments
     * @return correctedSegments
     *
     */
    void locationCorrection(List<Segment> tripSegments){
        List<Integer> innerWaitingList = new ArrayList<>();
        ArrayList<List<Integer>> outerWaitingList = new ArrayList<List<Integer>>();
        //put the segments without location info in a waiting list
        for (int i = 0; i < tripSegments.size(); i++) {
            if (tripSegments.get(i).getLocSegment() == null) {
                Log.d(TAG, "There is no location info for segment: " + i);
                //put the segments without location in a waiting list until the location information is available
                innerWaitingList.add(i);

                //if it is not the last segment, end the wainting list if the next segment location in not null
                if (i != tripSegments.size() -1){
                    if (tripSegments.get(i+1).getLocSegment()!= null){
                        outerWaitingList.add(innerWaitingList);
                        innerWaitingList= new ArrayList<>();
                    }
                }

                // if it is the last segment of the trip
                if (i == tripSegments.size()-1){
                    outerWaitingList.add(innerWaitingList);
                    innerWaitingList= new ArrayList<>();
                }
            }
        }
        if (!outerWaitingList.isEmpty()) {
            for (int i = 0; i < outerWaitingList.size(); i++) {
                List<Integer> waitingList = outerWaitingList.get(i);
                //if the whole trip does not contain any location info
                if (waitingList.size() == tripSegments.size()) {
                    //do nothing
                } else {
                    //---------------------last segments with null location----------------------------
                    // get the location info from the last previous segment with available loc info
                    if (waitingList.get(waitingList.size() - 1) == tripSegments.size() - 1) {
                        Log.d(TAG, "last trip segment with null location info: ");
                        int previousSegIndex = waitingList.get(0) - 1;
                        ArrayList<Location> prevSegLocValues = tripSegments.get(previousSegIndex).getLocSegment();
                        for (int k = waitingList.get(0); k <= waitingList.get(waitingList.size() - 1); k++) {
                            tripSegments.get(k).setLocSegment(prevSegLocValues);
                        }
                    }

                    //--------------------first segments with null location-----------------------
                    // get the location info from the next segment with available loc info
                    else if (waitingList.get(0) == 0) {
                        Log.d(TAG, "first trip segment with null location info: ");
                        int nextSegIndex = waitingList.get(waitingList.size() - 1) + 1;
                        ArrayList<Location> nextSegLocValues = tripSegments.get(nextSegIndex).getLocSegment();
                        for (int k = waitingList.get(0); k <= waitingList.get(waitingList.size() - 1); k++) {
                            tripSegments.get(k).setLocSegment(nextSegLocValues);
                        }
                    }
                    //--------------------middle segments with null location-----------------------
                    //get the location form the next and previous segments with available loc info
                    else {
                        int previousSegIndex = waitingList.get(0) - 1;
                        ArrayList<Location> prevSegLocValues = tripSegments.get(previousSegIndex).getLocSegment();
                        int nextSegIndex = waitingList.get(waitingList.size() - 1) + 1;
                        ArrayList<Location> nextSegLocValues = tripSegments.get(nextSegIndex).getLocSegment();
                        // merge the two previous and next segments with available location info and calculate avg speed based on both available segments
                        prevSegLocValues.addAll(nextSegLocValues);
                        for (int k = waitingList.get(0); k <= waitingList.get(waitingList.size() - 1); k++) {
                            tripSegments.get(k).setLocSegment(prevSegLocValues);
                        }
                    }
                }
            }
        }
    }

    /**
     * Tagging segments of a Trip to Strong, Candidate or Separation
     * if probability of walk > 0.8 or the segment is classified as still--> the segment is strong
     * if the probability of walk is the highest or the segment is classified as still--> the segment is Candidate
     * other segments--> separation segments
     * @param segment
     * @return
     */
    String taggingSegment( Segment segment ){
        String segtag = null;

        //
        for (Map.Entry<String, Double> entry : segment.getListOfProbabilities().entrySet()) {
            Integer key = Integer.valueOf(entry.getKey());
            Double value = entry.getValue();
            if ((key == 7 && value >= 0.8) || segment.getModeId()==0) {
                segtag = "Strong";
            } else if ((segment.getModeId() == 7 && value < 0.8) || segment.getModeId() ==0) {
                segtag = "Candidate";
            } else {
                segtag = "Separation";
            }
        }

        //Log.d(TAG, " transportModeId= " + segmentModeId+ "    and tag= "+segtag);
        return segtag;
    }

    /**
     * @param classifier
     * @param i
     * @param tripSegments
     * Assign each segment modeId, tag and probabilities
     */
    void segmentAssignments(Classifier classifier , int i , List<Segment> tripSegments){
        
        //set the segment transport mode Id
        int segModeId = classifier.classify();
        tripSegments.get(i).setModeId(segModeId);

        //set the segment list of probabilities
        Map<String, Double> listOfProbabilities;
        listOfProbabilities = classifier.predictsProbabilities();
        tripSegments.get(i).setListOfProbabilities(listOfProbabilities);
        Log.d(TAG, "segment" + i +"segmentAssignments: "+tripSegments.get(i).getListOfProbabilities());

        
        //set the segment tag
        String segmentTag = taggingSegment(tripSegments.get(i));
        tripSegments.get(i).setTag(segmentTag);

        //set the segment distance
        float distance = classifier.getFeature("distance");
        tripSegments.get(i).setDistance(distance);
        //the whole trip distance
        predictedDistance += classifier.getFeature("distance");

    }



    /**
     *  Post-processing Trip (Merging Strong and Candidate segments of a Trip)
     *  if there is a sequence of segments with strong tag and still or walk mode, merge them to one segment the same mode
     *  if there is a sequence of segments with strong, separation and then candidate tag, in case of
     *  |S| <= (|C|+2)/2 which S is the number of Strong segments and C is the number of Candidate segments
     */
    List<Leg> mergingSegments(List<Segment> segments) {
        ArrayList<Segment> walk = new ArrayList<>();
        ArrayList<Segment> still = new ArrayList<>();
        ArrayList<Leg> legs = new ArrayList<>();
        ArrayList< Map <String , Float>> segmentPostFeatures = new ArrayList();
        Leg leg = new Leg();
        int C= 0; //number of candidate segments
        int S= 0; //number of strong segments
        int numofSegments= segments.size();



        for (int i = 0; i < numofSegments; i++) {
            Log.d(TAG, "segment-index: " + segments.get(i).getIndex() + "  tag = " + segments.get(i).getTag() +
                    "   probabilities = " + segments.get(i).getListOfProbabilities() + " modeId = " + segments.get(i).getModeId());
            boolean skipThisSegment = false;
            calculateMagFeatures(segments.get(i));
            //---------phase one(still mode)---------------
            //if it is a strong segment with still mode
            if (segments.get(i).getModeId() == 0) {
                if (segments.get(i).getTag() == "Strong") {
                    still.add(segments.get(i));
                    Log.d(TAG, "add segment: " + i + " to still");
                }
                //current segment is candidate
                if (segments.get(i).getTag() == "Candidate") {
                    C++;
                    //--------phase two(merge candidate and strong segments)
                    if (S <= (C + 2) / 2) {
                        still.add(segments.get(i));
                    }
                }
                //if it is the last segment of the trip with strong tag
                if (i == segments.size() - 1 && !still.isEmpty()) {
                    legs.add(leg);
                    leg.setLegSegments(still);
                    leg.setModeId(0);
                    leg.setStartTime(still.get(0).getStartTime());
                    leg.setEndTime(still.get(still.size() - 1).getEndTime());
                    leg = new Leg(); S =0 ; C= 0;
                }
            } else {
                if (!still.isEmpty()) {
                    //current segment is separation
                    if (segments.get(i).getTag() == "Separation") {
                        S++;
                        //if the condition is met, this separation segment is added to the current leg
                        if (S <= (C + 2) / 2) {
                            still.add(segments.get(i));
                            skipThisSegment = true;
                        }
                        for (int j = i + 1; j < numofSegments; j++) {
                            //if there is no candidate after this separation then this leg is finished
                            if (segments.get(j).getTag() != "Candidate") {
                                legs.add(leg);
                                leg.setLegSegments(still);
                                leg.setModeId(0);
                                leg.setStartTime(still.get(0).getStartTime());
                                leg.setEndTime(still.get(still.size() - 1).getEndTime());
                                Log.d(TAG, "last segment of still leg: "+still.get(still.size()-1).getIndex());
                                leg = new Leg(); S =0 ; C= 0;
                                still = new ArrayList<>();
                                break;
                            }
                        }
                    }
                    // current segment is strong but is walk, so the still leg is not yet added
                    if ((segments.get(i).getTag() == "Strong" || segments.get(i).getTag() == "Candidate") && segments.get(i).getModeId() == 7){
                        Log.d(TAG, "still arrayList include: " + still);
                        legs.add(leg);
                        leg.setLegSegments(still);
                        leg.setModeId(0);
                        leg.setStartTime(still.get(0).getStartTime());
                        leg.setEndTime(still.get(still.size() - 1).getEndTime());
                        leg = new Leg(); S =0 ; C= 0;
                        still = new ArrayList<>();
                    }

                }
            } //end else still

            //----------phase one(walk mode) --------------
            //todo: there is a bug with the walk mode
            //if it is a strong segment with walk mode
            if (segments.get(i).getModeId() == 7) {
                if (segments.get(i).getTag() == "Strong") {
                    walk.add(segments.get(i));
                    Log.d(TAG, "add segment: " + i + " to walk");

                }
                //current segment is candidate
                if (segments.get(i).getTag() == "Candidate") {
                    C++;
                    if (S <= (C + 2) / 2) {
                        walk.add(segments.get(i));
                    }
                }
                //if it is the last segment with strong tag
                if (i == segments.size()-1 && !walk.isEmpty()){
                    legs.add(leg);
                    leg.setLegSegments(walk);
                    leg.setModeId(7);
                    Log.d(TAG, "why????????: "+walk.get(0).getStartTime());
                    leg.setStartTime(walk.get(0).getStartTime());
                    leg.setEndTime(walk.get(walk.size()-1).getEndTime());
                    leg = new Leg(); S =0 ; C= 0;
                }
            } else {
                if (!walk.isEmpty()) {
                    //current segment is separation
                    if (segments.get(i).getTag() == "Separation") {
                        S++;
                        //--------phase two(merge candidate and strong segments)
                        if (S <= (C + 2) / 2) {
                            walk.add(segments.get(i));
                            skipThisSegment = true;
                        }

                        /*for (int j = i + 1; j < numofSegments; j++) {
                            //if there is no candidate after this separation then this leg is probably finished
                            if (segments.get(j).getTag() != "Candidate") {
                                legs.add(leg);
                                leg.setLegSegments(walk);
                                leg.setModeId(7);
                                leg.setStartTime(walk.get(0).getStartTime());
                                leg.setEndTime(walk.get(walk.size() - 1).getEndTime());
                                Log.d(TAG, "last segment of walk leg: "+walk.get(walk.size()-1).getIndex());
                                leg = new Leg(); S =0 ; C= 0;
                                walk = new ArrayList<>();
                                break;
                            }

                        }*/

                    }

                    if (segments.get(i).getTag() == "Strong" && segments.get(i).getModeId() == 0){
                        Log.d(TAG, "walk arrayList include: " + walk);
                        legs.add(leg);
                        leg.setLegSegments(walk);
                        leg.setModeId(7);
                        leg.setStartTime(walk.get(0).getStartTime());
                        leg.setEndTime(walk.get(walk.size() - 1).getEndTime());
                        leg = new Leg(); S =0 ; C= 0;
                        walk = new ArrayList<>();
                    }
                }
            }//end else walk

            // --------------phase three(merge separation segments with the same modeId)------------------
            if (segments.get(i).getTag() == "Separation") {
                // if the separation segment is not already merged with a walk or still
                if (skipThisSegment == false) {
                int firstIndex = i;
                List<Segment> separationSegments = new ArrayList<>();
                double carProbs = 0, bikeProbs = 0, busProbs = 0, trainProbs = 0;
                //loop over list of probabilities for each segment
                Iterator it = segments.get(i).getListOfProbabilities().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    it.remove(); // avoids a ConcurrentModificationException
                    //sum up the probabilities of segments
                    switch (pair.getKey().toString()) {
                        case "1":
                            bikeProbs = (double) pair.getValue();
                            break;
                        case "9":
                            carProbs = (double) pair.getValue();
                            break;
                        case "10":
                            trainProbs = (double) pair.getValue();
                            break;
                        case "15":
                            busProbs = (double) pair.getValue();
                            break;
                        default:
                            break;
                    }
                }
                separationSegments.add(segments.get(i));
                //add the segments to a single leg as long as they are separation
                for (int j = i + 1; j < numofSegments; j++) {
                    if (segments.get(j).getTag() == "Separation") {
                        separationSegments.add(segments.get(j));
                        i = j;
                        //loop over list of probabilities for each segment
                        Iterator it2 = segments.get(j).getListOfProbabilities().entrySet().iterator();
                        while (it2.hasNext()) {
                            Map.Entry pair = (Map.Entry) it2.next();
                            it2.remove(); // avoids a ConcurrentModificationException
                            //sum up the probabilities of segments
                            switch (pair.getKey().toString()) {
                                case "1":
                                    bikeProbs += (double) pair.getValue();
                                    break;
                                case "9":
                                    carProbs += (double) pair.getValue();
                                    break;
                                case "10":
                                    trainProbs += (double) pair.getValue();
                                    break;
                                case "15":
                                    busProbs += (double) pair.getValue();
                                    break;
                                default:
                                    break;
                            }
                        }
                    } else {
                        break;
                    }
                }
                //pick the mode with the highest probability among the separation segments
                Map<String, Double> values = new HashMap<>();
                //List<Double> values = new ArrayList<>();
                values.put("bikeProbs", bikeProbs);
                values.put("carProbs", carProbs);
                values.put("trainProbs", trainProbs);
                values.put("busProbs", busProbs);
                Log.d(TAG, "what is the probability of each separation segment: " + values);

                //get the index of the max value (mode with the highest probability)
                String maxValueIndex = Utility.getMaxValueEntry(values).getKey();
                double maxValue = Utility.getMaxValueEntry(values).getValue();
                Log.d(TAG, "maxValueIndex: " + maxValueIndex);
                switch (maxValueIndex) {
                    case "bikeProbs":
                        //bike
                        leg.setModeId(1);
                        break;
                    case "carProbs":
                        //car
                        leg.setModeId(9);
                        break;
                    case "trainProbs":
                        //train
                        leg.setModeId(10);
                        break;
                    case "busProbs":
                        //bus
                        leg.setModeId(15);
                        break;
                    default:
                        break;
                }
                //remove the value with the highest probability
                values.remove(maxValueIndex);
                //get the index of the second value with the highest probability

                String secMaxValueIndex = Utility.getMaxValueEntry(values).getKey();
                double secMaxValue = Utility.getMaxValueEntry(values).getValue();
                Log.d(TAG, "secMaxValueIndex: " + secMaxValueIndex);
                //compare detected modes probabilities. if abs(secMaxValueIndex - maxVlaueIndex) <= 0.30 --> apply phase 4
                if (Math.abs(maxValue - secMaxValue) <= 0.80 ) {
                    //--------------phase four(correct miss-classifications after merge with the help of magnetometer)
                    //get the postFeaturesMag for each segment in the separationSegment List
                    for (int k = 0; k < separationSegments.size(); k++) {
                        int index = separationSegments.get(k).getIndex();
                        Map<String, Float> features = calculateMagFeatures(segments.get(index));
                        segmentPostFeatures.add(features);
                    }

                    //calculate avg of each feature for all the segments in the list of separationSegments
                    float avgDerivativeMags = 0;
                    float avgFilteredMags = 0;
                    float avgMagBetw_20_50s = 0;
                    float avgMagBetw_50_120s = 0;
                    float avgMagBetw_120_250s = 0;
                    float avgMagBetw_50_70s = 0;
                    float avgMagBelowFilters = 0;
                    List<Float> derivativeMags = new ArrayList<>();
                    List<Float> filteredMags = new ArrayList<>();
                    List<Float> magBetw_20_50s = new ArrayList<>();
                    List<Float> magBetw_50_70s = new ArrayList<>();
                    List<Float> magBetw_50_120s = new ArrayList<>();
                    List<Float> magBetw_120_250s = new ArrayList<>();
                    List<Float> magBelowFilters = new ArrayList<>();
                    for (Map<String, Float> features : segmentPostFeatures) {

                        Iterator it3 = features.entrySet().iterator();
                        while (it3.hasNext()) {
                            Map.Entry pair = (Map.Entry) it3.next();
                            it3.remove(); // avoids a ConcurrentModificationException
                            //calculate average of each postFeature separately
                            switch (pair.getKey().toString()) {
                                case "derivativeMags":
                                    derivativeMags.add((Float) pair.getValue());
                                    avgDerivativeMags = Utility.getMean(derivativeMags);
                                    break;
                                case "avgFilteredMag":
                                    filteredMags.add((Float) pair.getValue());
                                    avgFilteredMags = Utility.getMean(filteredMags);
                                    break;
                                case "magBetw_20_50s":
                                    magBetw_20_50s.add((Float) pair.getValue());
                                    avgMagBetw_20_50s = Utility.getMean(magBetw_20_50s);
                                    break;
                                case "avgMagBetw_50_70s":
                                    magBetw_50_70s.add((Float) pair.getValue());
                                    avgMagBetw_50_70s = Utility.getMean(magBetw_50_70s);
                                    break;
                                case "magBetw_50_120":
                                    magBetw_50_120s.add((Float) pair.getValue());
                                    avgMagBetw_50_120s = Utility.getMean(magBetw_50_120s);
                                    break;
                                case "magBetw_120_250":
                                    magBetw_120_250s.add((Float) pair.getValue());
                                    avgMagBetw_120_250s = Utility.getMean(magBetw_120_250s);
                                    break;
                                case "magBelowFilters":
                                    magBelowFilters.add((Float) pair.getValue());
                                    avgMagBelowFilters = Utility.getMean(magBelowFilters);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }


                    //if the next mode with the highest probability is metro and it's magnetic fields is within the range
                    if (maxValueIndex == "carProbs" && secMaxValueIndex == "trainProbs" && avgMagBetw_50_120s > 50 || avgFilteredMags > 80){
                        //subway
                        leg.setModeId(12);
                    } else if (secMaxValueIndex == "carProbs" && avgMagBetw_120_250s > 90) {
                        // electric car
                        leg.setModeId(9);
                    } else if (secMaxValueIndex == "busProbs" && (avgMagBetw_50_70s > 60 )) {
                        // bus
                        leg.setModeId(15);
                    } else if ((secMaxValueIndex== "busProbs") && (avgFilteredMags > 60  || avgMagBetw_50_70s > 40)) {
                        // tram
                        leg.setModeId(11);
                    }
                }
                legs.add(leg);
                leg.setStartTime(segments.get(firstIndex).getStartTime());
                leg.setEndTime(segments.get(i).getEndTime());
                leg.setLegSegments(separationSegments);
                leg = new Leg();
                S = 0;
                C = 0;
                }
            }
            Log.d(TAG, "why only last leg?: "+legs);

        }//end original for
        return legs;
    }


    //  50 < metro < 100    tram < 50

    public Map<String,Float> calculateMagFeatures(Segment segment){
        final float magFilter = 50f;
        Map<String, Float> postFeatures= new HashMap<String, Float>();
        postFeatures.put("avgMag", Utility.getMean(segment.getMagSegment()));
        postFeatures.put("minMag", Utility.getMin(segment.getMagSegment()));
        postFeatures.put("maxMag", Utility.getMax(segment.getMagSegment()));
        postFeatures.put("medianMag" , Utility.getMedian(segment.getMagSegment()));
        postFeatures.put("stdDevMag", Utility.getStandardDeviation(segment.getMagSegment()));
        postFeatures.put("derivativeMag" , Utility.getMean(Utility.calculateDerivative(segment.getMagSegment())));
        List<Float> filteredMagnetics = Utility.removeValuesBelow(segment.getMagSegment(), magFilter);
        postFeatures.put("avgFilteredMag", Utility.getMean(filteredMagnetics));

        // tram < 50
        filteredMagnetics = Utility.removeValuesAbove(segment.getMagSegment(), magFilter);
        postFeatures.put("magBelowFilter", (float) (filteredMagnetics.size() * 100) / (float) segment.getMagSegment().size());

        //test
        filteredMagnetics = Utility.removeValuesNotInInterval(segment.getMagSegment() , 20f , 50f);
        postFeatures.put("magBetw_20_50" , (float) (filteredMagnetics.size() * 100 / (float) segment.getMagSegment().size()));

        // <50 bus < 70
        filteredMagnetics = Utility.removeValuesNotInInterval(segment.getMagSegment(), 50f, 70f);
        postFeatures.put("magBetw_50_70", (float) (filteredMagnetics.size() * 100) / (float) segment.getMagSegment().size());

        // 50 < subway <120
        filteredMagnetics = Utility.removeValuesNotInInterval(segment.getMagSegment(), 50f, 120f);
        postFeatures.put("magBetw_50_120", (float) (filteredMagnetics.size() * 100) / (float) segment.getMagSegment().size());


        // 120 < electric car < 250
        filteredMagnetics = Utility.removeValuesNotInInterval(segment.getMagSegment(), 120f, 250f);
        postFeatures.put("magBetw_120_250", (float) (filteredMagnetics.size() * 100) / (float) segment.getMagSegment().size());
        segment.setPostMagFeatures(postFeatures);

        Log.d(TAG, "calculateMagFeatures: avgMag="+postFeatures.get("avgMag") + "   minMag="+postFeatures.get("minMag") + " maxMag=" +postFeatures.get("maxMag")+"  medianMag=" + postFeatures.get("medianMag")+
                "   stdDevMag="+postFeatures.get("stdDevMag") +  "  derivativeMag=" + postFeatures.get("derivativeMag") + "   avgFilteredMag="+postFeatures.get("avgFilteredMag") + " magBelowFilter=" + postFeatures.get("magBelowFilter") +
                "   magBetw_50_70=" +postFeatures.get("magBetw_50_70") + "  magBetw_50_120=" +postFeatures.get("magBetw_50_120") + " magBetw_120_250=" +postFeatures.get("magBetw_120_250") + "\n \n");

     return postFeatures;
    }


}
