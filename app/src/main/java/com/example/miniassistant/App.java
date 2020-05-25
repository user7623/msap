package com.example.miniassistant;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

public class App extends Application {
    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";
    public static final String CHANNEL_3_ID = "channel3";
    boolean BatteryPercentage;
    @Override
    public void onCreate() {
        super.onCreate();
        BatteryPercentage = PreferenceManager.getDefaultSharedPreferences(App.this).getBoolean("cancelForeground", false);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try
            {
                NotificationChannel channel1 = new NotificationChannel(
                        CHANNEL_1_ID,
                        "Channel 1",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                channel1.setDescription("This is Channel 1");
                NotificationChannel channel2 = new NotificationChannel(
                        CHANNEL_2_ID,
                        "Channel 2",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                channel2.setDescription("This is Channel 2");
                NotificationChannel channel3 = new NotificationChannel(
                        CHANNEL_3_ID,
                        "Channel 3",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel3.setDescription("This is Channel 3");

                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel1);
                manager.createNotificationChannel(channel2);
                manager.createNotificationChannel(channel3);
                if(BatteryPercentage)
                {
                    manager.cancel(1);
                }
            }catch (Exception e)
            {
                if(e.getMessage() != null)
                {
                    Log.d("Exception in .App:", e.getMessage() );
                }
            }
        }

    }
}

/*public class App extends Application {
    public static final String CHANNEL_ID = "exampleServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}*/