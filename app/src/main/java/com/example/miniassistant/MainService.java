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
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.miniassistant.App.CHANNEL_1_ID;
import static com.example.miniassistant.App.CHANNEL_2_ID;

public class MainService extends Service {
    //TODO: popravi formula za kapacitet!
    //pocetni vrednosti
    private int batteryP = 0;
    private WifiManager wifiManager;
    private NotificationManagerCompat notificationManager;
    boolean cancelNotification = false;
    PendingIntent pendingIntent;
    int BatteryPercentage = 20;
    String notificationTextString = "";
    Boolean extra, wifi,connection,homework = false;
    boolean notified = false;
    boolean registeredR = false;
    boolean powerDisconnected, timeChanged = false;
    int counter = 0;
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
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        BatteryPercentage = PreferenceManager.getDefaultSharedPreferences(MainService.this).getInt("batteryPercentage", 20);
        wifi = PreferenceManager.getDefaultSharedPreferences(MainService.this).getBoolean("wifi", false);
        extra = PreferenceManager.getDefaultSharedPreferences(MainService.this).getBoolean("extra", false);
        connection = PreferenceManager.getDefaultSharedPreferences(MainService.this).getBoolean("connectivity", false);
        homework = PreferenceManager.getDefaultSharedPreferences(MainService.this).getBoolean("homework", false);
        powerDisconnected = PreferenceManager.getDefaultSharedPreferences(MainService.this).getBoolean("powerDisconnected", false);
        timeChanged = PreferenceManager.getDefaultSharedPreferences(MainService.this).getBoolean("timeChanged", false);

        //proverka
        Log.d("MAIN SERVICE: ", "Main service started, " + BatteryPercentage);
        if(PreferenceManager.getDefaultSharedPreferences(MainService.this).getBoolean("timeChanged", false))
        {
            enableBroadcastReceivers();
            return START_NOT_STICKY;
        }
        if(!wifi && !extra && !connection && !homework && BatteryPercentage == 15)
        {
            if(registeredR){unregisterReceiver(batteryInfoReceiver);}
            //ne go prekinuva homeworks servisot zasto?
            Intent stopIntent = new Intent(MainService.this, HomeworkService.class);
            stopService(stopIntent);
            stopSelf();
            Log.d("MAIN SERVICE", "Services stopped!");
            return START_NOT_STICKY;
        }
        if(powerDisconnected && BatteryPercentage < 16)
        {
            enableBatteryMonitoring();
            PreferenceManager.getDefaultSharedPreferences(MainService.this).edit().putBoolean("powerDisconnected", false).apply();
            return START_NOT_STICKY;
        }
        if(BatteryPercentage < 16)
        {
            Toast.makeText(this,"Default device notification already enabled", Toast.LENGTH_SHORT).show();
        }
        else
        {
            enableBatteryMonitoring();
        }
        if(powerDisconnected)
        {
            enableBatteryMonitoring();
            return START_NOT_STICKY;
        }
        if(timeChanged)
        {
            rFunk();
            PreferenceManager.getDefaultSharedPreferences(MainService.this).edit().putBoolean("timeChanged", false).apply();
            return START_NOT_STICKY;
        }
        if(wifi || connection)
        {
            enableBroadcastReceivers();
        }
        if(homework)
        {
            enableHomeworkService();
        }
        else
        {
            Intent stopIntent = new Intent(MainService.this, HomeworkService.class);
            stopService(stopIntent);
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        startTimer();
        return START_NOT_STICKY;
    }
    public void enableBatteryMonitoring()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        registerReceiver(batteryInfoReceiver, intentFilter);
        registeredR = true;
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
        if(!notified || powerDisconnected) {
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
                    .setVibrate(new long[]{250, 250, 250, 250, 250})
                    .setLights(Color.GREEN, 1000, 1000)

                    .build();
            notificationManager.notify(1, notification);
            Log.i("Notification info: ", "starting notification on channel 1");
            notified = true;
        }
    }
    public void enableBroadcastReceivers()
    {
        //ako e smeneta sostojbata na promenlivata koja ja cuva vrednosta na polnezot(vo procenti)
        //na baterijata i vrednosta e poniska od taa zadadena kako minimum pred izvestuvanje
        //izgasi wifi
        if(wifi && batteryP < BatteryPercentage)
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
                        .setVibrate(new long[]{750,750})
                        .setOngoing(false)
                        .build();
                notificationManager.notify(2, notification);
                Log.i("Notification info: ", "starting notification on channel 2");
            }
        }
    }
    public void enableHomeworkService()
    {
        Log.d("MAIN SERVICE", "Homework service enabled");
        Intent homework = new Intent(MainService.this, HomeworkService.class);
        startService(homework);
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    private static Timer timer;
    private static TimerTask timerTask;
    long oldTime = 0;
    public void startTimer() {
        stoptimertask();
        timer = new Timer();
        initializeTimerTask();
        //TODO: 1800000 za 30min
        timer.schedule(timerTask, 1000, 3000); //
    }
    public void initializeTimerTask() {
        Log.e("TIMER", "TIMER");
        timerTask = new TimerTask() {
            public void run() {
                rFunk();
                //enableBroadcastReceivers();
            }
        };
    }
    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    private void rFunk()
    {
        wifi = PreferenceManager.getDefaultSharedPreferences(MainService.this).getBoolean("wifi", false);
        connection = PreferenceManager.getDefaultSharedPreferences(MainService.this).getBoolean("connectivity", false);
        homework = PreferenceManager.getDefaultSharedPreferences(MainService.this).getBoolean("homework", false);
        if(wifi || connection)
        {
            enableBroadcastReceivers();
        }
        if(counter == 450)//30min - na sekoi 4sec se inkrementira
        {
            //pokazuvaj notifikacija na sekoi 30min i proveruvaj za wifi i net
            notified = false;
            enableHomeworkService();
            enableBatteryMonitoring();
            counter = 0;//resetiraj
        }
        counter++;
        startTimer();
    }
}
