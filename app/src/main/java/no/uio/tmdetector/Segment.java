package no.uio.tmdetector;

import android.location.Location;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
/**
 * Segment class encapsulate the segment features and methods. A segment is list of sensor readings
 * with fixed size.
 */
class Segment {
    private int index;
    private long startTime;
    private long endTime;
    private int modeId;
    private float distance;

    private Map<String,Double> listOfProbabilities;
    private String tag;
    private ArrayList<Float> accSegment;
    private ArrayList<Float> magSegment;
    private ArrayList<Location> locSegment;
    private Map<String,Float> postMagFeatures;




    public void setIndex(int index){
        this.index = index;
    }
    public int getIndex() { return index;}
    public void setStartTime(long startTime){
        this.startTime = startTime;
    }

    public long getStartTime(){
        return startTime;
    }

    public void setEndTime(long endTime){
        this.endTime = endTime;
    }

    public long getEndTime(){
        return endTime;
    }
    public void setModeId(int modeId){
        this.modeId = modeId;
    }

    public int getModeId(){
        return modeId;
    }

    public float getDistance(){return distance;}
    public void setDistance(float distance){
        this.distance = distance;
    }

    public Map<String,Double> getListOfProbabilities(){
        return listOfProbabilities;
    }

    public void setListOfProbabilities(Map<String,Double> listOfProbabilities){
        this.listOfProbabilities = listOfProbabilities;
    }

    public void setTag(String tag){
        this.tag = tag;
    }

    public String getTag(){
        return tag;
    }

    public void setAccSegment(ArrayList<Float> accSegment){
        this.accSegment = accSegment;
    }
    public ArrayList<Float> getAccSegment(){
        return accSegment;
    }

    public void setMagSegment(ArrayList<Float> magSegment){
        this.magSegment = magSegment;
    }
    public ArrayList<Float> getMagSegment(){
        return magSegment;
    }

    public void setLocSegment(ArrayList<Location> locSegment){
        this.locSegment = locSegment;
    }
    public ArrayList<Location> getLocSegment(){
        return locSegment;
    }

    public void setPostMagFeatures(Map<String,Float> postMagFeatures) {
        this.postMagFeatures = postMagFeatures;
    }
    public Map<String,Float> getPostMagFeatures(){
        return postMagFeatures;
    }

}
