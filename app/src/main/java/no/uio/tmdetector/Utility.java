package no.uio.tmdetector;

import android.hardware.SensorEvent;
import android.os.Build;
import android.telephony.CellSignalStrength;
import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Class providing the required utilities
 */
class Utility {

    private static final Integer[] BIKE_MODE_IDS = {1, 16, 17, 35};
    private static final Integer[] CAR_MODE_IDS = {7};
    private static final String TAG = "Utility";


    static boolean isBike(int transportModeId) {
        return Arrays.asList(BIKE_MODE_IDS).contains(transportModeId);
    }

    static boolean isCar(int transportModeId) {
        return Arrays.asList(CAR_MODE_IDS).contains(transportModeId);
    }


    // finds the median value of a list of floats.
    static float getMedian(List values) {
        if (values.isEmpty()) return 0f;
        float median;
        Collections.sort(values);
        if (values.size() % 2 == 0) {
            median = ((float) values.get(values.size() / 2) + (float) values.get(values.size() / 2 - 1)) / 2;
        } else {
            median = (float) values.get(values.size() / 2);
        }

        return median;
    }

    // calculates the mean value of a list of floats.
    static float getMean(List<Float> values) {
        if (values.isEmpty()) return 0f;
        float sum = 0;
        for (float value : values) {
            sum += value;
        }

        return sum / (float) values.size();
    }

    // calculates the standard deviation of a list of floats.
    static float getStandardDeviation(List<Float> values) {
        if (values.isEmpty()) return 0f;
        float mean = getMean(values);
        float squareSum = 0;
        for (float value : values) {
            squareSum += Math.pow(value - mean, 2);
        }

        return (float) Math.sqrt((squareSum / (float) values.size()));
    }

    // calculates the minimum value of a list of floats.
    static float getMin(List<Float> values) {
        if (values.isEmpty()) return 0f;

        float minValue = Float.MAX_VALUE;
        for (float value : values) {
            if (value < minValue) {
                minValue = value;
            }
        }

        return minValue;
    }

    // calculates the maximum value of a list of floats.
    static float getMax(List<Float> values) {
        if (values.isEmpty()) return 0f;

        float maxValue = Float.MIN_VALUE;
        for (float value : values) {
            if (value > maxValue) {
                maxValue = value;
            }
        }

        return maxValue;
    }

    // calculates the maximum value of a list of floats.
    static  Map.Entry<String, Double> getMaxValueEntry(Map<String,Double> values) {
        if (values.isEmpty()){return null;}
        Map.Entry<String, Double> maxValueIndex = null;
        Double maxValueInMap=(Collections.max(values.values()));  // This will return max value in the Hashmap
        for (Map.Entry<String , Double> entry : values.entrySet()) {  // Itrate through hashmap
            if (entry.getValue()==maxValueInMap) {
                maxValueIndex= entry;   // Print the key with max value
            }
        }
        return maxValueIndex;
    }


    // removes values above limit and returns a copy of the values list
    static List<Float> removeValuesAbove(List<Float> values, float limit) {
        List<Float> copy = new ArrayList<>(values);
        for (float value : values) {
            if (value > limit) {
                copy.remove(copy.indexOf(value));
            }
        }

        return copy;
    }

    // removes values below limit and returns a copy of the values list
    static List<Float> removeValuesBelow(List<Float> values, float limit) {
        List<Float> copy = new ArrayList<>(values);
        for (float value : values) {
            if (value < limit) {
                copy.remove(copy.indexOf(value));
            }
        }

        return copy;
    }

    // removes values outside a lower and upper limit nd returns a copy of the values list
    static List<Float> removeValuesNotInInterval(List<Float> values, float lowerLimit, float upperLimit) {
        List<Float> copy = new ArrayList<>(values);
        for (float value : values) {
            if (value < lowerLimit || value > upperLimit) {
                copy.remove(copy.indexOf(value));
            }
        }

        return copy;
    }

    // calculate magnitude from SensorEvent
    public static float calculateMagnitude(SensorEvent sensorEvent) {
        return (float) Math.sqrt(Math.pow(sensorEvent.values[0], 2) +
                Math.pow(sensorEvent.values[1], 2) +
                Math.pow(sensorEvent.values[2], 2));
    }


    //calculate derivative of a list of float values
    public static List<Float> calculateDerivative(List<Float> values) {
        List<Float> result = new ArrayList<>();
        for (int i = 0; i < values.size() - 2; i++) {
            float firstValue = values.get(i);
            float secondValue = values.get(i + 1);
            float thirdValue = values.get(i + 2);
            result.add(((secondValue - firstValue) + (thirdValue - secondValue)) / 2);
        }
        return result;

    }

    //normalize data
    public static ArrayList<Float> normalizeDate(ArrayList<Float> magSegment){
        float maxValue , minValue;
        ArrayList<Float> normValues = new ArrayList<>();
        maxValue = getMax(magSegment);
        minValue = getMin(magSegment);
        for (int i=0 ; i < magSegment.size() ; i++){
            float normValue;
            normValue = ((magSegment.get(i) - minValue) / (maxValue - minValue));
            normValues.add(normValue);
        }
        return normValues;
    }


    // Returns the user friendly smartphone name
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }

    //In statistics math, a mode is a value that occurs the highest numbers of time.
    public static int calculateModeValue(List<Integer> values) {
        int maxValue = 0, maxCount = 0, i, j;

        for (i = 0; i < values.size(); ++i) {
            int count = 0;
            for (j = 0; j < values.size(); ++j) {
                if (values.get(i) == values.get(j))
                    ++count;
            }

            if (count > maxCount) {
                maxCount = count;
                maxValue = values.get(i);
            }
        }
        return maxValue;
    }

    //convert timestamp to a readable format date-time for user
    public static String getReadableTime(long time) {
        //long millis = TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS);
        long timeInMillis = (new Date()).getTime()
                + (time - System.nanoTime()) / 1000000L;
        Date date = new Date(timeInMillis);
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String dateFormatted = formatter.format(date);
        return dateFormatted;
    }

    //convert timestamp to a readable format date-time for user
    public static String getTime(long time) {
        LocalDateTime dateFormatted = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        return dateFormatted.toString();
    }


    public static long getTimeinSeconds(long time) {
        LocalDateTime dateFormatted = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());

        long timeinSec = (dateFormatted.getHour() * 3600) + (dateFormatted.getMinute() * 60) + dateFormatted.getSecond();
        return timeinSec;

    }


    //convert raw timestamp of accelerometer sensor to a correct timestamp
    public static Long correctTimestamp(long timestamp) {
        float NS2S = 1.0f / 1000000.0f;
        Float f = new Float(timestamp * NS2S);
        Long correctedTimeStamp = (java.lang.System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime() + f.longValue());
        return correctedTimeStamp;
    }

    public static List<Float> mergeLists(List<Float> firstList, List<Float> secondList) {
        ArrayList<Float> mergedLists = new ArrayList<Float>();
        mergedLists.addAll(firstList);
        mergedLists.addAll(secondList);
        return mergedLists;
    }

    //
    public static Map<String,Float> calculatePostFeatures(List<Float> segmentValues){
        final float magFilter = 50f;
        Map<String, Float> postFeatures= new HashMap<String, Float>();
        for (int i=0 ; i < segmentValues.size() ; i++){

            postFeatures.put("avgMag", getMean(segmentValues));
            postFeatures.put("minMag", getMin(segmentValues));
            postFeatures.put("maxMag", getMax(segmentValues));
            postFeatures.put("medianMag" , getMedian(segmentValues));
            postFeatures.put("stdDevMag", getStandardDeviation(segmentValues));
            postFeatures.put("derivativeMag" , getMean(calculateDerivative(segmentValues)));
            List<Float> filteredMagnetics = removeValuesBelow(segmentValues, magFilter);
            postFeatures.put("avgFilteredMag", getMean(filteredMagnetics));

            // tram < 50
            filteredMagnetics = removeValuesAbove(segmentValues, magFilter);
            postFeatures.put("magBelowFilter", (float) (filteredMagnetics.size() * 100) / (float) segmentValues.size());

            //test
            filteredMagnetics = Utility.removeValuesNotInInterval(segmentValues , 20f , 50f);
            postFeatures.put("magBetw_20_50" , (float) (filteredMagnetics.size() * 100 / (float) segmentValues.size()));


            // 50< bus < 70
            filteredMagnetics = removeValuesNotInInterval(segmentValues, 50f, 70f);
            postFeatures.put("magBetw_50_70", (float) (filteredMagnetics.size() * 100) / (float) segmentValues.size());

            // 50 < subway <120
            filteredMagnetics = removeValuesNotInInterval(segmentValues, 50f, 120f);
            postFeatures.put("magBetw_50_120", (float) (filteredMagnetics.size() * 100) / (float) segmentValues.size());


            // 120 < electric car < 250
            filteredMagnetics = removeValuesNotInInterval(segmentValues, 120f, 250f);
            postFeatures.put("magBetw_120_250", (float) (filteredMagnetics.size() * 100) / (float) segmentValues.size());

        }
        return postFeatures;
    }
    public static int modeStringToInteger(String mode) {
        int modeId = -1;
        switch (mode) {
            case "still":
                modeId = 0;
                break;
            case "bike":
                modeId = 1;
                break;
            case "walk":
                modeId = 7;
                break;
            case "run":
                modeId = 8;
                break;
            case "car":
                modeId = 9;
                break;
            case "train":
                modeId = 10;
                break;
            case "tram":
                modeId = 11;
                break;
            case "subway":
                modeId = 12;
                break;
            case "ferry":
                modeId = 13;
                break;
            case "plain":
                modeId = 14;
                break;
            case "bus":
                modeId = 15;
                break;
            case "other":
                modeId = 16;
                break;
            default:
                break;
        }
        return modeId;
    }

    public static String modeIntegerToString(int modeId) {
        String mode = null;
        switch (modeId) {
            case 0:
                mode = "still";
                break;
            case 1:
                mode = "bike";
                break;
            case 7:
                mode = "walk";
                break;
            case 8:
                mode = "run";
                break;
            case 9:
                mode = "car";
                break;
            case 10:
                mode = "train";
                break;
            case 11:
                mode = "tram";
                break;
            case 12:
                mode = "subway";
                break;
            case 13:
                mode = "ferry";
                break;
            case 14:
                mode = "plain";
                break;
            case 15:
                mode = "bus";
            case 16:
                mode = "other";
                break;
            default:
                break;
        }

        return mode;
    }



}


