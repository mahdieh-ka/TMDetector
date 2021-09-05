package no.uio.tmdetector;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class TripRepository {
    static String docPath = Utility.getDeviceName();
    private static final String TAG = "TripRepository";
    private static FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build();

    // fetches trips from firestore and updates UI.
    static void fetch(final Context context) {
        final ArrayList<Trip> tripList = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);

        // gets trips of each user separately(by smartphone model) from firebase ordered descending by startDate
        db
                .collection("users").document(docPath).collection("trips")
                .orderBy("startDate", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Trip trip = document.toObject(Trip.class);
                            trip.setId(document.getId());
                            if (!trip.isInProgress()) {
                                tripList.add(trip);

                            }
                        }
                        ((DetectionActivity) context).refreshTripList(tripList);
                    } else {
                        Toast.makeText(context,
                                "Error getting trips", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Saves a trip on Firebase Firestore
     * @param trip
     */
    static void save(Trip trip) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);
        // storing trip fields in a nested collection of users->trips
        //each user is distinguished by his mobile model
        DocumentReference documentReference = db.collection("users").document(docPath).collection("trips").document();
        documentReference.set(trip);

    }

    /**
     * Deletes a trip from Firebase Firestore
     */
    static void delete(String tripId) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("users").document(docPath).collection("trips").document(tripId).delete();

    }

}
