package com.example.miniassistant;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.example.miniassistant.App.CHANNEL_1_ID;
import static com.example.miniassistant.App.CHANNEL_2_ID;

//import static com.example.miniassistant.App.CHANNEL_ID;

public class MainService extends Service {
    //TODO: popravi formula za kapacitet!
    //pocetni vrednosti
    private int batteryP = 0;
    private boolean batteryPStateChanged = false;
    private boolean wifiSwitch;
    private WifiManager wifiManager;
    private NotificationManagerCompat notificationManager;
    boolean cancelNotification = false;
    PendingIntent pendingIntent;
    int BatteryPercentage = 20;
    String notificationTextString = "";
    //Boolean notify = false;
    Boolean extra, wifi,connection,homework = false;

    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        notificationManager = NotificationManagerCompat.from(this);

        //Toast.makeText(this,"Main service started", Toast.LENGTH_SHORT).show();

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        BatteryPercentage = PreferenceManager.getDefaultSharedPreferences(MainService.this).getInt("batteryPercentage", 20);
       // BatteryPercentage = preferences.getInt("batteryPercentage",15);
        /*
        ovie ne rabotat i zatoa so zastareni
        * connection = preferences.getBoolean("connectivity",false);
        homework = preferences.getBoolean("homework", false);
        extra = preferences.getBoolean("extra", false);
        */
        wifi = PreferenceManager.getDefaultSharedPreferences(MainService.this).getBoolean("wifi", false);
        extra = PreferenceManager.getDefaultSharedPreferences(MainService.this).getBoolean("extra", false);
        connection = PreferenceManager.getDefaultSharedPreferences(MainService.this).getBoolean("connectivity", false);
        homework = PreferenceManager.getDefaultSharedPreferences(MainService.this).getBoolean("homework", false);
        //proverka
        Toast.makeText(this,"Main service started, " + BatteryPercentage, Toast.LENGTH_SHORT).show();
        Log.d("MAIN SERVICE: ", "Main service started, " + BatteryPercentage);

        if(BatteryPercentage < 16)
        {
            Toast.makeText(this,"Default device notification already enabled", Toast.LENGTH_SHORT).show();
        }
        else
        {
            enableBatteryMonitoring();
        }
        if(wifi || connection)
        {
            enableBroadcastReceivers();
        }
        if(homework)
        {
            enableHomeworkService();
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        return START_NOT_STICKY;
    }

    public void enableBatteryMonitoring()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        registerReceiver(batteryInfoReceiver, intentFilter);
    }
    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryData(intent);
        }
    };
    private void updateBatteryData(Intent intent)
    {
        boolean present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT,false);

        //resetiraj za da ne se povtoruva
        notificationTextString = "";
        if(present && extra)
        {
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            notificationTextString = notificationTextString + "Battery health: " + health +"\n";

            if(level != -1 && scale != -1)
            {
                batteryPStateChanged = true;
                batteryP = (int)((level/(float)scale)*100f);
                if(batteryP > BatteryPercentage)
                {
                    //dokolku nema potreba od notifikacijata koga ke se povika App da ja ponisti
                    cancelNotification = true;
                    PreferenceManager.getDefaultSharedPreferences(MainService.this).edit().putBoolean("cancelForeground", cancelNotification).apply();
                }
                notificationTextString = notificationTextString + "Battery percentage: " + batteryP +"\n";
            }
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            if(temperature > 0 )
            {
                notificationTextString = notificationTextString + "Temperature :" + ((float)temperature/10f)+"°C\n";
            }
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
            if(voltage > 0)
            {
                notificationTextString = notificationTextString + "Voltage is: " + voltage + "mV"+"\n";
            }
            long batterCapacity = getBatteryCapacity(this);
            notificationTextString = notificationTextString + "Capacity :" + batterCapacity + "\n";
        }
        else if(present)
        {
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            notificationTextString = notificationTextString + "Battery health: " + health + "\n";
            if(level != -1 && scale != -1)
            {
                batteryP = (int)((level/(float)scale)*100f);
                notificationTextString = notificationTextString + "Battery percentage: " + batteryP + "\n";
            }
        }
        else
        {
            Toast.makeText(MainService.this, "No battery present", Toast.LENGTH_SHORT).show();
        }
        if(batteryP <= BatteryPercentage)
        {
            notifyUser();
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public long getBatteryCapacity(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BatteryManager mBatteryManager = (BatteryManager) ctx.getSystemService(Context.BATTERY_SERVICE);
            Long chargeCounter = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
            Long capacity = mBatteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

            if (chargeCounter != null && capacity != null) {
                long value = (long) (((float) chargeCounter / (float) capacity) * 100f);
                return value;
            }
        }

        return 0;
    }
    private void notifyUser()
    {

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setContentTitle("Battery notification")
                .setContentText(notificationTextString)
                .setSmallIcon(R.drawable.ic_battery_notification)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH) //prioritet moze se do oreo-android 8 a target e 7
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setAutoCancel(true)
                .setColor(Color.GREEN)
                .setOnlyAlertOnce(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notificationTextString))
                .setOngoing(false)
                .build();
        notificationManager.notify(1, notification);
        Log.i("Notification info: ", "starting notification on channel 1");

    }
    public void enableBroadcastReceivers()
    {
        //ako e smeneta sostojbata na promenlivata koja ja cuva vrednosta na polnezot(vo procenti)
        //na baterijata i vrednosta e poniska od taa zadadena kako minimum pred izvestuvanje
        //izgasi wifi
        if(wifi && batteryPStateChanged && batteryP < BatteryPercentage)
        {
            //ova ke raboti samo do API28
            //https://stackoverflow.com/questions/58006340/disable-wifi-on-android-29
             wifiManager.setWifiEnabled(false);
        }
        if(connection)
        {
            boolean connected = false;
            ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            {
                connected = true;
            }
            else
            {
                connected = false;
            }
            if(!connected)
            {
                Log.d("MAIN SERVICE: " , "Connection not detected!!!");
                Notification notification = new NotificationCompat.Builder(this, CHANNEL_2_ID)
                        .setContentTitle("Connection notification")
                        .setContentText("No connection!")
                        .setSmallIcon(R.drawable.ic_no_connection_icon)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT) //prioritet moze se do oreo-android 8 a target e 7
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setAutoCancel(true)
                        .setColor(Color.BLACK)
                        .setOnlyAlertOnce(true)
                        .setOngoing(false)
                        .build();
                notificationManager.notify(2, notification);
                Log.i("Notification info: ", "starting notification on channel 1");
            }
        }
    }
    public void enableHomeworkService()
    {

    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
