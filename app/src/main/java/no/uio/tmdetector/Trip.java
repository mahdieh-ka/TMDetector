package no.uio.tmdetector;

import android.location.Location;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    class Trip {
    private String id;
    private Date startDate;
    private Date endDate;
    private String TAG = "Trip";
    private boolean inProgress = false;
    private long timeToClassify;
    private float distance;
    private int modeId;
    private int tripId;
    private List<Segment> segments;
    private List<Leg> legs;
    private Long startDateTs;



     void start() {
        this.startDate = new Date();
        this.startDateTs = Instant.now().toEpochMilli();

        inProgress = true;
    }

    void finish() {
        this.endDate = new Date();
        inProgress = false;
    }

    public Date getStartDate() {
        return startDate;
    }
    public Long getStartDateTs(){
         return startDateTs;
    }

    public Date getEndDate() {
        return endDate;
    }
    public boolean isInProgress() {
         return inProgress || endDate == null;
     }
    public void setModeId(int modeId) {

       this.modeId = modeId;
    }


    public int getModeId() {
            return modeId;
        }




     //todo for test purposes
    public long getTimeToClassify() {
        return timeToClassify;
    }

    public void setTimeToClassify(long timeToClassify) {
        this.timeToClassify = timeToClassify;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    //get trip id from the firebase repository
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public List<Segment> getSegments(){
      return segments;
    }

    public void setSegments(List<Segment> segments){
         this.segments = segments;
    }

    public List<Leg> getLegs(){return legs;}

    public void setLegs(List<Leg> legs){
         this.legs = legs;
    }


    }


