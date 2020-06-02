package com.example.miniassistant;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class mBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "MyBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean boot = true;
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (!mobile.isAvailable() || !mobile.isConnected()) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("KN", true).apply();
                Log.d("Network not available ", "kein netz!");
                Intent i = new Intent(context, MainService.class);
                context.startService(i);
                Toast.makeText(context,"no net conn",Toast.LENGTH_SHORT).show();
            }

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG,"boot completed!");
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("boot", boot).apply();
            Intent i = new Intent(context, MainService.class);
            context.startService(i);
            Log.d(TAG,"Main service started!");
            Toast.makeText(context,"Receiver works", Toast.LENGTH_LONG).show();
        }
        if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {
            Log.d(TAG,"time changed!");
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("timeChanged", true).apply();
            Intent i = new Intent(context, MainService.class);
            //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(i);
            Log.d(TAG,"Main service started!");
            Toast.makeText(context, "Broadcaster received msg", Toast.LENGTH_SHORT).show();
        }
        if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
            Log.d(TAG,"power disconnected!");
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("powerDisconnected", true).apply();
            Intent i = new Intent(context, MainService.class);
            context.startService(i);
            Log.d(TAG,"Main service started!");
            Toast.makeText(context, "Broadcaster received msg", Toast.LENGTH_SHORT).show();
        }
        if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
            Log.d(TAG,"power connected!");
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("powerDisconnected", false).apply();
        }
        if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            Log.d(TAG,"Battery state changed!");
            Intent i = new Intent(context, MainService.class);
            context.startService(i);
            Toast.makeText(context, "Broadcaster received msg", Toast.LENGTH_SHORT).show();
        }
    }


}
