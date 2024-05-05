package no.uio.tmdetector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static no.uio.tmdetector.MainActivity.rawDataDB;

/**
 * Part of collection module to visualize the sensor readings and initially learn the thresholds.
 * According to these values we defined the features for the classifier. A part of the feature extraction module.
 */
public class DerivativeGraph extends AppCompatActivity {

    public LineChart chartRawMag, chartDerivMag, chartAvgFilteredMag , chartAvgMag;
    private String TAG = DerivativeGraph.class.getSimpleName();
    String tripId;
    Cursor cursor;
    ProgressDialog progressDialog;
    private BackgroundTripQuery backgroundTripQuery = new BackgroundTripQuery();



    private static final int segmentSize = 90;
    ArrayList<Float> mags = new ArrayList<>();
    ArrayList<String> modes = new ArrayList<>();
    //set x and y axis
    ArrayList<Entry> XvalueYAxis1 = new ArrayList<>();
    ArrayList<Entry> XvalueYAxis2 = new ArrayList<>();
    ArrayList<Entry> XvalueYAxis3 = new ArrayList<>();
    ArrayList<Entry> XvalueYAxis4 = new ArrayList<>();
    ArrayList<Entry> XvalueYAxis5 = new ArrayList<>();
    ArrayList<Entry> XvalueYAxis6 = new ArrayList<>();
    ArrayList<Entry> XvalueYAxis7 = new ArrayList<>();
    ArrayList<Entry> XvalueYAxis8 = new ArrayList<>();
    ArrayList<Entry> XvalueYAxis9 = new ArrayList<>();
    ArrayList<Entry> XvalueYAxis10 = new ArrayList<>();
    ArrayList<Entry> XvalueYAxis11 = new ArrayList<>();
    ArrayList<Entry> XvalueYAxis12 = new ArrayList<>();
    ArrayList<Entry> XvalueYAxis13 = new ArrayList<>();
    ArrayList<Entry> XvalueYAxis14 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_derivative_graph);


        //get intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        tripId = extras.getString("TRIP_ID");

        chartRawMag = (LineChart) findViewById(R.id.Rchart);
        chartDerivMag = (LineChart) findViewById(R.id.Dchart);
        chartAvgFilteredMag = (LineChart) findViewById(R.id.avgFilteredMagChart);
        chartAvgMag = (LineChart) findViewById(R.id.avgMagChart);

        BackgroundTask bt = new BackgroundTask();
        bt.execute();

    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null && progressDialog.isShowing()== true) {
            progressDialog.dismiss();
        }
        super.onDestroy();
    }

    private class BackgroundTask extends AsyncTask<Void, Void, ArrayList<LineData>> {



        @Override
        protected ArrayList<LineData> doInBackground(Void... voids) {
            ArrayList<LineData> data = createSet();
            return data;
        }

        @Override
        protected void onPreExecute() {

            progressDialog = ProgressDialog.show(DerivativeGraph.this , "Drawing the graph","Please wait. It takes time to " +
                    "draw the graph if the data is big ");
            String msg = " Trip: " + tripId;
            Toast.makeText(DerivativeGraph.this , msg , Toast.LENGTH_SHORT ).show();


        }

        @Override
        protected void onPostExecute(ArrayList<LineData> lineData) {

            if (progressDialog != null && progressDialog.isShowing()== true) {
                progressDialog.dismiss();
            }
            //raw magnetometer of magnitude values
            chartRawMag.setData(lineData.get(0));
            chartRawMag.getDescription().setEnabled(true);
            chartRawMag.getDescription().setText("raw magnetic_field");
            chartRawMag.setBackgroundColor(Color.WHITE);
            chartRawMag.setDrawGridBackground(false);
            chartRawMag.getDescription().setEnabled(true);
            chartRawMag.getDescription().setTextSize(10);
            chartRawMag.setPinchZoom(true);
            chartRawMag.setTouchEnabled(true);


            //derivative magnetometer of magnitude values
            chartDerivMag.setData(lineData.get(1));
            chartDerivMag.getDescription().setEnabled(true);
            chartDerivMag.getDescription().setText("derivative of magnetic_field");
            chartDerivMag.setBackgroundColor(Color.WHITE);
            chartDerivMag.setDrawGridBackground(false);
            chartDerivMag.getDescription().setEnabled(true);
            chartDerivMag.getDescription().setTextSize(10);
            chartDerivMag.setPinchZoom(true);
            chartDerivMag.setTouchEnabled(true);


            chartAvgFilteredMag.setData(lineData.get(2));
            chartAvgFilteredMag.getDescription().setEnabled(true);
            chartAvgFilteredMag.getDescription().setText(segmentModes()+ "  postFeaturesChart");
            chartAvgFilteredMag.setBackgroundColor(Color.WHITE);
            chartAvgFilteredMag.setDrawGridBackground(false);
            chartAvgFilteredMag.setPinchZoom(true);
            chartAvgFilteredMag.setTouchEnabled(true);


            chartAvgMag.setData(lineData.get(8));
            chartAvgMag.getDescription().setEnabled(true);
            chartAvgMag.getDescription().setText(segmentModes()  + "  postFeaturesChart");
            chartAvgMag.setBackgroundColor(Color.WHITE);
            chartAvgMag.setDrawGridBackground(false);
            chartAvgMag.setPinchZoom(true);
            chartAvgMag.setTouchEnabled(true);


        }
    }
    public ArrayList<LineData> createSet(){
        ArrayList<LineData> data = new ArrayList<>();

        //create rawData set
        LineDataSet set1 = new LineDataSet(XvalueYAxis1, "raw-Values(μT)");
        set1.setValues(rawMagnetometerData());
        set1.setFillAlpha(110);
        set1.setCircleColor(Color.GRAY);
        set1.setColors(Color.GRAY);
        ArrayList<ILineDataSet> dataSets1 = new ArrayList<>();
        dataSets1.add(set1);
        LineData rawData = new LineData(dataSets1);
        data.add(rawData);



        //create derivativeData set
        for (int i = 0 ; i < derivativeMagnetometerData().size() ; i ++) {
            XvalueYAxis2.add(new Entry(i,derivativeMagnetometerData().get(i) ));
            //Log.d(TAG, "derivativeMagnetometerData: "+derivativeMagnetometerData().get(i));
        }
        LineDataSet set2 = new LineDataSet(XvalueYAxis2 , "derivative-Values(μT)");
        set2.setFillAlpha(110);
        set2.setCircleColor(Color.GREEN);
        set2.setColors(Color.GREEN);
        ArrayList<ILineDataSet> dataSets2 = new ArrayList<>();
        dataSets2.add(set2);
        LineData derivativeData = new LineData(dataSets2);
        data.add(derivativeData);


        //create filteredData set
        for (int i =0 ; i < postFeatureSegmentedData().size() ; i++ ){
            Log.d(TAG, "createSet: postFeatureSegmentedData().size()"+postFeatureSegmentedData().size());
            XvalueYAxis3.add(new Entry(i, postFeatureSegmentedData().get(i).get("avgMag")));
            XvalueYAxis4.add(new Entry(i, postFeatureSegmentedData().get(i).get("minMag")));
            XvalueYAxis5.add(new Entry(i, postFeatureSegmentedData().get(i).get("maxMag")));
            XvalueYAxis6.add(new Entry(i, postFeatureSegmentedData().get(i).get("medianMag")));
            XvalueYAxis7.add(new Entry(i, postFeatureSegmentedData().get(i).get("stdDevMag")));
            XvalueYAxis8.add(new Entry(i , postFeatureSegmentedData().get(i).get("derivativeMag")));

            XvalueYAxis9.add(new Entry(i, postFeatureSegmentedData().get(i).get("avgFilteredMag")));
            XvalueYAxis10.add(new Entry(i, postFeatureSegmentedData().get(i).get("magBelowFilter")));
            XvalueYAxis11.add(new Entry(i, postFeatureSegmentedData().get(i).get("magBetw_20_50")));
            XvalueYAxis12.add(new Entry(i, postFeatureSegmentedData().get(i).get("magBetw_50_70")));
            XvalueYAxis13.add(new Entry(i, postFeatureSegmentedData().get(i).get("magBetw_50_120")));
            XvalueYAxis14.add(new Entry(i, postFeatureSegmentedData().get(i).get("magBetw_120_250")));

        }
        LineDataSet set3 = new LineDataSet(XvalueYAxis3 , "avgMag");
        set3.setCircleColor(Color.DKGRAY);
        set3.setColors(Color.DKGRAY);

        LineDataSet set4 = new LineDataSet(XvalueYAxis4 , "minMag");
        set4.setCircleColor(Color.RED);
        set4.setColors(Color.RED);

        LineDataSet set5 = new LineDataSet(XvalueYAxis5 , "maxMag");
        set5.setCircleColor(Color.CYAN);
        set5.setColors(Color.CYAN);

        LineDataSet set6 = new LineDataSet(XvalueYAxis6 , "medianMag");
        set6.setCircleColor(Color.GREEN);
        set6.setColors(Color.GREEN);

        LineDataSet set7 = new LineDataSet(XvalueYAxis7 , "stdDevMag");
        set7.setCircleColor(Color.MAGENTA);
        set7.setColors(Color.MAGENTA);

        LineDataSet set8 = new LineDataSet(XvalueYAxis8 , "derivativeMag");
        set8.setCircleColor(Color.BLUE);
        set8.setColors(Color.BLUE);




        LineDataSet set9 = new LineDataSet(XvalueYAxis9 , "avgFilteredMag");
        set9.setCircleColor(Color.DKGRAY);
        set9.setColors(Color.DKGRAY);

        LineDataSet set10 = new LineDataSet(XvalueYAxis10 , "magBelowFilter");
        set10.setCircleColor(Color.MAGENTA);
        set10.setColors(Color.MAGENTA);

        LineDataSet set11 = new LineDataSet(XvalueYAxis11 , "magBetw_20_50");
        set11.setCircleColor(Color.CYAN);
        set11.setColors(Color.CYAN);

        LineDataSet set12 = new LineDataSet(XvalueYAxis12 , "magBetw_50_70");
        set12.setCircleColor(Color.GREEN);
        set12.setColors(Color.GREEN);

        LineDataSet set13 = new LineDataSet(XvalueYAxis13 , "magBetw_50_120");
        set13.setCircleColor(Color.RED);
        set13.setColors(Color.RED);

        LineDataSet set14 = new LineDataSet(XvalueYAxis14 , "magBetw_120_250");
        set14.setCircleColor(Color.BLUE);
        set14.setColors(Color.BLUE);


        ArrayList<ILineDataSet> dataSets3 = new ArrayList<>();
        dataSets3.add(set3);
        dataSets3.add(set4);
        dataSets3.add(set5);
        dataSets3.add(set6);
        dataSets3.add(set7);
        dataSets3.add(set8);
        LineData avgMag = new LineData(dataSets3);
        LineData minMag = new LineData(dataSets3);
        LineData maxMag = new LineData(dataSets3);
        LineData medianMag = new LineData(dataSets3);
        LineData stdDevMag = new LineData(dataSets3);
        LineData derivativeMag = new LineData(dataSets3);

        ArrayList<ILineDataSet> dataSets4 = new ArrayList<>();
        dataSets4.add(set9);
        dataSets4.add(set10);
        dataSets4.add(set11);
        dataSets4.add(set12);
        dataSets4.add(set13);
        dataSets4.add(set14);
        LineData avgFilteredMag = new LineData(dataSets4);
        LineData magBelowFilter = new LineData(dataSets4);
        LineData magBetw_20_50 = new LineData(dataSets4);
        LineData magBetw_50_70 = new LineData(dataSets4);
        LineData magBetw_50_120 = new LineData(dataSets4);
        LineData magBetw_120_250 = new LineData(dataSets4);
        data.add(avgMag);
        data.add(minMag);
        data.add(maxMag);
        data.add(medianMag);
        data.add(stdDevMag);
        data.add(derivativeMag);
        data.add(avgFilteredMag);
        data.add(magBelowFilter);
        data.add(magBetw_20_50);
        data.add(magBetw_50_70);
        data.add(magBetw_50_120);
        data.add(magBetw_120_250);


        return data;
    }


    //query magnitude of magnetometer values from the DB based on entered tripId
    public ArrayList<Entry> rawMagnetometerData(){
        ArrayList<Entry> valueTimePairs= new ArrayList<>();
        cursor = rawDataDB.showtripbyid(tripId);
        int i =0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            valueTimePairs.add(new Entry(i , Float.valueOf(cursor.getString(21)))); //magMagnitude column
            mags.add(Float.valueOf(cursor.getString(21)));
            modes.add(cursor.getString(8)); //correctedModeOfTransport column
            i++;


        }
        cursor.close();
        return valueTimePairs;
    }

    public List<Float> derivativeMagnetometerData(){
        List<Float> derivatives = Utility.calculateDerivative(mags);
        return derivatives;
    }

    //segment data and calculate post features for each segment
    public ArrayList< Map<String, Float>> postFeatureSegmentedData() {
        ArrayList<List<Float>> magSegments = new ArrayList<>();
        Map<String, Float> postFeatures= new HashMap<String, Float>();
        ArrayList< Map<String, Float>> allSegmentsPostFeatures = new ArrayList<>();

        for (int j = 0; j < mags.size(); j += segmentSize){
            magSegments.add(mags.subList(j,
                    Math.min(j + segmentSize, mags.size())));
        }

        for (int i = 0; i< magSegments.size() ; i++) {
            postFeatures = Utility.calculatePostFeatures(magSegments.get(i));
            allSegmentsPostFeatures.add(postFeatures);
        }
        return allSegmentsPostFeatures;
    }

    public StringBuffer segmentModes(){
        ArrayList<String> actualModes = new ArrayList<>();
        ArrayList<List<String>> modeSegments = new ArrayList<>();
        for (int j = 0; j < modes.size(); j += segmentSize){
            modeSegments.add(modes.subList(j,
                    Math.min(j + segmentSize, modes.size())));
        }
        for (int i =0 ; i < modeSegments.size(); i++){
            for (int k =0 ; k < modeSegments.get(i).size() ;k++ ){
                if (!modeSegments.get(i).get(k).isEmpty()){

                    actualModes.add(Utility.modeIntegerToString(Integer.valueOf(modeSegments.get(i).get(k))));
                }
            }
        }
        StringBuffer buffer_modes = new StringBuffer();

        for (int x = 0 ; x <actualModes.size() ; x++){
            buffer_modes.append(" ActualModes=" +actualModes.get(x));
        }

        return buffer_modes;
    }


}