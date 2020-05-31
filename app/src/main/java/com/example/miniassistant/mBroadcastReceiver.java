package com.example.miniassistant;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class mBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "MyBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean boot = true;
        SharedPreferences preferences = context.getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("boot", boot).apply();
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG,"boot completed!");
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            Log.d(TAG,"Main activity started!");
            Toast.makeText(context,"Receiver works", Toast.LENGTH_LONG).show();
        }
        if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
            Log.d(TAG,"power connected!");
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            Log.d(TAG,"Main activity started!");
        }
        if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
            Log.d(TAG,"power disconnected!");
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            Log.d(TAG,"Main activity started!");
        }

        if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            Log.d(TAG,"Battery state changed!");
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

            Toast.makeText(context, "Broadcaster received msg", Toast.LENGTH_SHORT).show();
        }
    }


}
