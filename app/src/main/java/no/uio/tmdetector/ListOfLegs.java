package no.uio.tmdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
/**
 * To show list of legs. Each leg represnt one single mode.
 */
public class ListOfLegs extends AppCompatActivity {

    private static final String TAG = ListOfLegs.class.getSimpleName();
    TextView txtListOfLegs;
    String tripLegs;
    ImageView legIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_legs);

        txtListOfLegs = findViewById(R.id.txtListOfLegs);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        tripLegs = extras.getString("LEGS");

        legIcon = findViewById(R.id.imgViewMode);

        //set Legs info
        //txtListOfLegs.setText(tripLegs);
        ArrayList<String[]> legs = splitLegs(tripLegs);
        for (String [] leg :legs){
            Log.d(TAG, "leg: "+leg[0].split("   "));
            switch (leg[0]) {
                case "0":
                    Drawable stillMode = getResources().getDrawable(R.drawable.ic_still);
                    legIcon.setImageDrawable(stillMode);
                    break;
                case "1":
                    Drawable bikeMode = getResources().getDrawable(R.drawable.ic_bike);
                    legIcon.setImageDrawable(bikeMode);
                    break;
                case "7":
                    Drawable walkMode = getResources().getDrawable(R.drawable.ic_walk);
                    legIcon.setImageDrawable(walkMode);
                    break;
                case "9":
                    Drawable carMode = getResources().getDrawable(R.drawable.ic_car);
                    legIcon.setImageDrawable(carMode);
                    break;
                case "10":
                    //add train
                case "11":
                    //add tram
                case "12":
                    Drawable subwayMode = getResources().getDrawable(R.drawable.ic_subway);
                    legIcon.setImageDrawable(subwayMode);
                    break;
                case "15":
                    Drawable busMode = getResources().getDrawable(R.drawable.ic_bus);
                    legIcon.setImageDrawable(busMode);
                    break;
                default:
                    Drawable unknownMode = getResources().getDrawable(R.drawable.ic_unknown);
                    legIcon.setImageDrawable(unknownMode);
                    break;
            }



        }

    }
    ArrayList<String[]> splitLegs(String tripLegs){
        ArrayList<String[]> legs = new ArrayList<String[]> ();
        legs.add(tripLegs.split("\n\n"));
        return legs;
    }



}