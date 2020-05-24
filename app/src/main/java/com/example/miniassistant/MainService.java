package com.example.miniassistant;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

public class MainService extends Service {
    //pocetni vrednosti
    int BatteryPercentage = 20;

    Boolean extra, wifi,connection,homework = false;

    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this,"Main service started", Toast.LENGTH_SHORT).show();

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);

        BatteryPercentage = preferences.getInt("batteryPercentage",15);
        wifi = preferences.getBoolean("wifi", false);
        connection = preferences.getBoolean("connectivity",false);
        homework = preferences.getBoolean("homework", false);
        extra = preferences.getBoolean("extra", false);
        //proverka
        Toast.makeText(this,"Main service started, " + BatteryPercentage, Toast.LENGTH_SHORT).show();

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
        if(present && extra)
        {
            StringBuilder stringBuilder = new StringBuilder();
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            stringBuilder.append("Battery health: " + health).append("\n");
            if(level != -1 && scale != -1)
            {
                int batteryP = (int)((level/(float)scale)*100f);
                stringBuilder.append("Battery percentage: " + batteryP).append("\n");
            }
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            if(temperature > 0 )
            {
                stringBuilder.append("Temperature :" + ((float)temperature/10f)).append("°C\n");
            }
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
            if(voltage > 0)
            {
                stringBuilder.append("Voltage is: " + voltage + "mV").append("\n");
            }
            long batterCapacity = getBatteryCapacity(this);
            stringBuilder.append("Capacity :" + batterCapacity);
        }
        else if(present)
        {
            StringBuilder stringBuilder = new StringBuilder();
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            stringBuilder.append("Battery health: " + health).append("\n");
            if(level != -1 && scale != -1)
            {
                int batteryP = (int)((level/(float)scale)*100f);
                stringBuilder.append("Battery percentage: " + batteryP).append("\n");
            }
        }
        else
        {
            Toast.makeText(MainService.this, "No battery present", Toast.LENGTH_SHORT).show();
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
    public void enableBroadcastReceivers()
    {

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
