package no.uio.tmdetector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

class BatteryMonitor extends BroadcastReceiver {
    private static final String TAG = BatteryMonitor.class.getSimpleName();
    float batteryPct;
    public static Boolean batteryIsLow =false;
    @Override
    public void onReceive(Context context, Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        batteryPct = level * 100 / (float) scale;
        Log.d(TAG, "showBatteryLevel: " + batteryPct);
        if (batteryPct < 20) {
            batteryIsLow = true;
            Log.d(TAG, "onReceive: "+batteryIsLow);
        }
    }
}
