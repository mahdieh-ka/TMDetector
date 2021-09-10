package no.uio.tmdetector;


import java.util.List;

/**
 * A leg consists of the merged segments with the same modeId
 * So a leg is the duration that the user had the same mode of transportation
 */
public class Leg implements Comparable<Leg>{
    private long startTime;
    private long endTime;
    private int modeId;


    private List<Segment> legSegments;


    public int getModeId(){
        return modeId;
    }
    public void setModeId(int modeId){
        this.modeId = modeId;
    }

    public long getStartTime(){
        return startTime;
    }
    public void setStartTime(long startTime){
        this.startTime = startTime;
    }

    public long getEndTime(){
        return endTime;
    }
    public void setEndTime(long endTime){
        this.endTime = endTime;
    }

    public void setLegSegments(List<Segment> segments){
        this.legSegments = segments;
    }
    public List<Segment> getLegSegments(){
        return legSegments;
    }


    // overriding the compareTo method of Comparable class
    public int compareTo(Leg compareLeg) {
        long compareage
                = ((Leg)compareLeg).getStartTime();

        //  For Ascending order
        return (int) (this.startTime - compareage);

    }



}
