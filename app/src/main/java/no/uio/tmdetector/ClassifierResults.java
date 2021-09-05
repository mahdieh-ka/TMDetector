package no.uio.tmdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ClassifierResults extends AppCompatActivity {


    private static final String TAG = ClassifierResults.class.getSimpleName();
    TextView txtStart, txtEnd, txtTripId, txtDistance, txtLisOfProbabilities , txtListOfLegs , txtActualModes;
    String start, end, distance, tripId, tripProbs, tripLegs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classifier_results);


        txtStart = findViewById(R.id.txtStart);
        txtEnd = findViewById(R.id.txtEnd);
        txtDistance = findViewById(R.id.txtDistance);
        txtTripId = findViewById(R.id.txtTripId);
        txtListOfLegs = findViewById(R.id.txtListOfLegs);
        txtLisOfProbabilities = findViewById(R.id.txtListOfProbs);
        //txtActualModes = findViewById(R.id.txtActualModes);
        //btnShowLegs = findViewById(R.id.btnShowLegs);
        //legIcon = findViewById(R.id.imgViewLegMode);


        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        start = extras.getString("START");
        end = extras.getString("END");
        distance = extras.getString("DISTANCE");
        tripId = extras.getString("TRIP_ID");
        tripLegs = extras.getString("LEGS");
        tripProbs = extras.getString("PROBABILITIES");




        //set Start date and time
        txtStart.setText(start);

        //set End date and time
        txtEnd.setText(end);

        //set the final Distance
        txtDistance.setText(distance);

        //set the TripId
        txtTripId.setText(tripId);

        //set the Actual modes
       /* derivativeGraph = new DerivativeGraph();
        String actualModes =  derivativeGraph.segmentModes().toString();
        txtActualModes.setText(actualModes);*/


        //set the legs info
        //showLegs();
        String[] legs = tripLegs.split("\n");
        ArrayList<Integer> legIds = new ArrayList<>();
        for (int i = 0 ; i < legs.length ; i++) {
            txtListOfLegs.setText(legs[i]);
            //legIds.add(Integer.valueOf(legs[i].substring(13,14)));
            //Log.d(TAG, "legIds: "+legIds);
           /* switch (legIds.get(i)) {
                case 0:
                    Drawable stillMode = getResources().getDrawable(R.drawable.ic_still);
                    legIcon.setImageDrawable(stillMode);
                    break;
                case 1:
                    Drawable bikeMode = getResources().getDrawable(R.drawable.ic_bike);
                    legIcon.setImageDrawable(bikeMode);
                    break;
                case 7:
                    Drawable walkMode = getResources().getDrawable(R.drawable.ic_walk);
                    legIcon.setImageDrawable(walkMode);
                    break;
                case 9:
                    Drawable carMode = getResources().getDrawable(R.drawable.ic_car);
                    legIcon.setImageDrawable(carMode);
                    break;
                case 10:
                    //add train
                case 11:
                    //add tram
                case 12:
                    Drawable subwayMode = getResources().getDrawable(R.drawable.ic_subway);
                    legIcon.setImageDrawable(subwayMode);
                    break;
                case 15:
                    Drawable busMode = getResources().getDrawable(R.drawable.ic_bus);
                    legIcon.setImageDrawable(busMode);
                    break;
                default:
                    Drawable unknownMode = getResources().getDrawable(R.drawable.ic_unknown);
                    legIcon.setImageDrawable(unknownMode);
                    break;
            }*/
        }

        txtListOfLegs.setText(tripLegs);

        //set the ListOfProbabilities
        txtLisOfProbabilities.setText(tripProbs);


    }
   /* public void showLegs(){
        btnShowLegs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create the intent and pass the user input
                Intent intent = new Intent(ClassifierResults.this, ListOfLegs.class);
                intent.putExtra("LEGS", tripLegs);
                startActivity(intent);
            }
        });
    }*/




}