package no.uio.tmdetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Detection activity is an important part of detection module. It starts running the service in the background
 * and stop the background service with start and stop toggle button.
 */
public class DetectionActivity extends AppCompatActivity implements SingleChoiceDialogFragment.SingleChoiceListener  {
    private static final String TAG = DetectionActivity.class.getSimpleName() ;
    private Intent backgroundServiceIntent;
    public static final String CHANNEL_ID = "DetectionActivityNotificationChannel";
    public ToggleButton toggleBtnTrip;
    private Button btnActualMode;
    private static Bundle bundle = new Bundle();
    public static String selectedMode ;
    String selectiveItem;
    private List<Trip> tripsList = new ArrayList<>();
    private TripAdapter mAdapter;
    public RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);



        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        mAdapter = new TripAdapter(tripsList);

        recyclerView.setAdapter(mAdapter);

        createNotificationChannel();


        backgroundServiceIntent = new Intent(this, TripService.class);
        TripRepository.fetch(this);



        Classifier.loadModelAsync();
        Classifier.setContext(this);
        toggleBtnTrip = (ToggleButton) findViewById(R.id.tglBtnTrip);
        btnActualMode = (Button) findViewById(R.id.btnActualMode);
        selectTripMode();


        // Get required permissions
        try {
            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            ArrayList<String> permissionsToRequest = new ArrayList<>();
            if (info.requestedPermissions != null) {
                for (String permission : info.requestedPermissions) {
                    // check if we need to ask for these permissions
                    if (ContextCompat.checkSelfPermission(this, permission)
                            != PackageManager.PERMISSION_GRANTED) {
                        permissionsToRequest.add(permission);
                    }
                }
            }

            // request permissions
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(new String[0]), 32);
            } else {
                // all permissions have been already granted.
                startService();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        bundle.putBoolean("ToggleButtonState", toggleBtnTrip.isChecked());
    }

    @Override
    public void onResume() {
        super.onResume();
        toggleBtnTrip.setChecked(bundle.getBoolean("ToggleButtonState", false));
    }

    /**
     * Updates the recycler view with a list of Trips
     *
     * @param newTripsList list of Trip objects
     */
    public void refreshTripList(List<Trip> newTripsList) {
        tripsList = newTripsList;
        mAdapter.setTripsList(tripsList);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Click handler for "Start trip" toggle button
     * @param view
     */
    public void toggleStartTrip(View view) {
        if (((ToggleButton) view).isChecked()) {
            showNotification("Trip started!");
            sendBroadcast(new Intent("TRIP_STARTED"));
            updateServiceNotification("A trip is being recorded");
        } else {
            sendBroadcast(new Intent("TRIP_ENDED"));
            showNotification("Trip ended!");
            updateServiceNotification("Service running");
            TripRepository.fetch(this);
        }
    }

    /**
     * Shows a notification
     */
    private void showNotification(String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_subway)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    /**
     * Updates an existing notification
     * @param content
     */
    private void updateServiceNotification(String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, TripService.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_subway)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(TripService.NOTIFICATION_ID, builder.build());
    }

    /**
     * Creates the notification channel used by the app
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "TMDetectorNotificationChannel", importance);
            channel.setDescription("TMDetector Notification Channel");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int grantResult : grantResults) {
            if (grantResult != PermissionChecker.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.d(TAG, "Missing permission grant");
                finishAndRemoveTask();
                return;
            }
        }

        startService();
    }


    private void startService() {
        Log.d(TAG, "Starting trip service...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(backgroundServiceIntent);
        } else {
            startService(backgroundServiceIntent);
        }
    }


    // Select mode of the current trip
    public void selectTripMode(){
        btnActualMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectiveItem = "mode";
                DialogFragment modeSelection = new SingleChoiceDialogFragment(selectiveItem);
                modeSelection.setCancelable(false);
                modeSelection.show(getSupportFragmentManager(), "Mode selection");


            }
        });
    }

    @Override
    public void onPositiveButtonClicked(String[] list, int position, String selectiveItem , String id) {
        //if choosing between modes
        if (selectiveItem == "mode") {
            selectedMode = list[position];
            Toast.makeText(getApplicationContext() , selectedMode , Toast.LENGTH_SHORT).show();

            boolean isUpdate = MainActivity.rawDataDB.updateData(selectedMode, id);
            if (isUpdate == true && selectedMode != null) {
                Log.d(TAG, "Data updated!");
            }
        }


    }

    @Override
    public void onNegativeButtonClicked(String selectiveItem) {
        if (selectiveItem == "mode"){
            Toast toast = Toast.makeText(DetectionActivity.this , "you should select your current mode!", Toast.LENGTH_SHORT);
            toast.show();
            selectedMode = null;
        }

    }


}