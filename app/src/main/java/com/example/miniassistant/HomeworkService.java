package com.example.miniassistant;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class HomeworkService extends Service {
    public HomeworkService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
