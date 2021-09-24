package no.uio.tmdetector;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static no.uio.tmdetector.MainActivity.rawDataDB;


/**
 * Recycler view adapter. List of trips.
 */
public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private static final String TAG = TripAdapter.class.getSimpleName() ;
    private List<Trip> tripsList;
    private Context context;
    private Cursor cursor;


    TripAdapter(List<Trip> tripsList) {
        this.tripsList = tripsList;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @androidx.annotation.NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.trip_view, viewGroup, false);
        context = viewGroup.getContext();
        return new TripViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull final TripViewHolder viewHolder, int i) {
        final Trip trip = tripsList.get(i);
        DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");



        viewHolder.txtStart.setText(dateFormatter.format(trip.getStartDate()));
        viewHolder.txtEnd.setText(dateFormatter.format(trip.getEndDate()));
        viewHolder.txtTripId.setText(trip.getTripId() + " ");
        viewHolder.txtActualMode.setText(retrieveActualModes(trip));
        viewHolder.txtPredictedMode.setText(convertPredictedModesToString(trip));


        ImageView tripIcon = viewHolder.tripIcon;
        switch (trip.getModeId()) {
            case 0:
                Drawable stillMode = context.getResources().getDrawable(R.drawable.ic_still);
                tripIcon.setImageDrawable(stillMode);
                break;
            case 1:
                Drawable bikeMode = context.getResources().getDrawable(R.drawable.ic_bike);
                tripIcon.setImageDrawable(bikeMode);
                break;
            case 7:
                Drawable walkMode = context.getResources().getDrawable(R.drawable.ic_walk);
                tripIcon.setImageDrawable(walkMode);
                break;
            case 9:
                Drawable carMode = context.getResources().getDrawable(R.drawable.ic_car);
                tripIcon.setImageDrawable(carMode);
                break;
            case 10:
                Drawable trainMode = context.getResources().getDrawable(R.drawable.ic_train);
                tripIcon.setImageDrawable(trainMode);
            case 11:
                Drawable tramMode = context.getResources().getDrawable(R.drawable.ic_tram);
                tripIcon.setImageDrawable(tramMode);
                break;
            case 12:
                Drawable subwayMode = context.getResources().getDrawable(R.drawable.ic_subway);
                tripIcon.setImageDrawable(subwayMode);
                break;
            case 15:
                Drawable busMode = context.getResources().getDrawable(R.drawable.ic_bus);
                tripIcon.setImageDrawable(busMode);
                break;
            case 16:
                Drawable othersMode = context.getResources().getDrawable(R.drawable.ic_unknown);
                tripIcon.setImageDrawable(othersMode);
                break;
            default:
                Drawable unknownMode = context.getResources().getDrawable(R.drawable.ic_unknown);
                tripIcon.setImageDrawable(unknownMode);
                break;
        }

        viewHolder.btnDelete.setOnClickListener(v -> {
            String message = "Trip "+trip.getTripId() + " deleted from repository and local DB!";
            Toast.makeText(context, message,Toast.LENGTH_SHORT).show();
            //delete from repository
            TripRepository.delete(trip.getId());
            //delete from the local database
            rawDataDB.deleteByTripId(String.valueOf(trip.getTripId()));
            TripRepository.fetch(v.getContext());

        });

        viewHolder.btnShowGraphs.setOnClickListener(v -> {
            //create the intent and pass the user input
            Intent intent = new Intent(context, DerivativeGraph.class);
            intent.putExtra("TRIP_ID", String.valueOf(trip.getTripId()));
            context.startActivity(intent);

        });

      /*  viewHolder.btnActualMode.setOnClickListener(v -> {
            selectiveItem = "mode";
            Toast.makeText(context, "TripId"+viewHolder.txtTripId.getText() , Toast.LENGTH_SHORT).show();
            DialogFragment modeSelection = new SingleChoiceDialogFragment(selectiveItem );
            modeSelection.setCancelable(false);
            modeSelection.show(((MainActivity) context).getSupportFragmentManager() , "Mode selection");

        });*/



        viewHolder.btnPredictedMode.setOnClickListener(v -> {
            Intent intent = new Intent(context , ClassifierResults.class);
            intent.putExtra("START" , dateFormatter.format(trip.getStartDate()));
            intent.putExtra("END" , dateFormatter.format(trip.getEndDate()));
            intent.putExtra("DISTANCE" , trip.getDistance() + " m");
            intent.putExtra("TRIP_ID" , trip.getTripId() + " ");

            //sort legs based on their startTime
            Collections.sort(trip.getLegs(), (l1, l2) -> Long.compare(l1.getStartTime() , l2.getStartTime()));


            //convert legs to string
            StringBuffer buffer_legs = new StringBuffer();
            for (Leg tripLeg: trip.getLegs()){
                String legMode = Utility.modeIntegerToString(tripLeg.getModeId());
                buffer_legs.append("Leg_Mode = " + legMode + "    Start= " + sdf.format(tripLeg.getStartTime()) + "    "+"End= " +sdf.format(tripLeg.getEndTime()) +"\n");


            }
            intent.putExtra("LEGS" , buffer_legs.toString());

            //convert the ListOfProbabilities to string
            StringBuffer buffer_segments= new StringBuffer();

            if (trip.getSegments() != null) {
                for (Segment tripSegment: trip.getSegments()) {
                    String segMode = Utility.modeIntegerToString(tripSegment.getModeId());
                    buffer_segments.append("Segment = " + tripSegment.getIndex()+ "   " +"  Tag = " + tripSegment.getTag()+ "  "+"Mode = " + segMode+ "   " +
                            "     Start= " + sdf.format(tripSegment.getStartTime())  + "   End=" + sdf.format(tripSegment.getEndTime()) +
                            "   Distance= " +tripSegment.getDistance()+ "  Probabilities=" +tripSegment.getListOfProbabilities().toString() + "\n\n");
                    Log.d(TAG, " Probabilities in Trip Adapter="+tripSegment.getListOfProbabilities());
                }
                intent.putExtra("PROBABILITIES", buffer_segments.toString());
            }
            context.startActivity(intent);

        });


    }
    @Override
    public int getItemCount() {
        return tripsList.size();
    }

    void setTripsList(List<Trip> tripsList) {
        this.tripsList = tripsList;
    }

    class TripViewHolder extends RecyclerView.ViewHolder {

        TextView txtStart, txtEnd, txtTripId , txtActualMode, txtPredictedMode;
        ImageView tripIcon;
        ImageButton btnDelete;
        Button btnPredictedMode, btnShowGraphs ;

        TripViewHolder(@androidx.annotation.NonNull View itemView ) {
            super(itemView);
            txtStart = itemView.findViewById(R.id.txtStart);
            txtEnd = itemView.findViewById(R.id.txtEnd);
            txtTripId=itemView.findViewById(R.id.txtTripId);
            txtActualMode = itemView.findViewById(R.id.txtActualMode);
            txtPredictedMode = itemView.findViewById(R.id.txtPredictedMode);
            tripIcon = itemView.findViewById(R.id.imgViewMode);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnShowGraphs = itemView.findViewById(R.id.btnShowGraphs);
            btnPredictedMode = itemView.findViewById(R.id.btnPredictedMode);

        }

    }

    //retrive the atual mode of each trip from the local database
    private StringBuffer retrieveActualModes(Trip trip){
        ArrayList<String> modes = new ArrayList<>();
        if (trip != null) {
            //get the actual mode of the current trip from the local DB and set to the viewHolder
            cursor = rawDataDB.showtripbyid(String.valueOf(trip.getTripId()));
            if (cursor.getCount() != 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    //get the list of actual modes of the trip
                    if (!cursor.getString(8).isEmpty()) {
                        modes.add(cursor.getString(8));
                    }
                }
            }
        }
        cursor.close();
        StringBuffer buffer_modes = new StringBuffer();
        for (String mode : modes) {
            String strMode = Utility.modeIntegerToString(Integer.valueOf(mode));
            buffer_modes.append(strMode + " ");
        }


        return buffer_modes;
    }

   private StringBuffer convertPredictedModesToString(Trip trip){
       StringBuffer buffer_predictedModes = new StringBuffer();
       for (Leg tripLeg: trip.getLegs()){
           String legMode = Utility.modeIntegerToString(tripLeg.getModeId());
           buffer_predictedModes.append(legMode + " ");
       }
       return buffer_predictedModes;
   }

}
