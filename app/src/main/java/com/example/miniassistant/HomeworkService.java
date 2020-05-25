package com.example.miniassistant;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import retrofit2.Callback;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeworkService extends Service {

    private boolean checkOften = false;
    private String url = "";
    private boolean connected = false;
    private static Timer timer;
    private static TimerTask timerTask;
    long oldTime = 0;

    public HomeworkService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);

        /*checkConnection();
        if(connected)
        {
            startTimer();
        }*/
    }

    private void checkConnection() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startTimer() {
        stoptimertask();
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 1000, 600000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                checkForHomework();
            }
        };
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void checkForHomework() {


        //TODO:smeni URL !!!
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        IHomework iHomework = retrofit.create(IHomework.class);

        Call<List<Homework>> call = iHomework.getHomeworks();

        call.enqueue(new Callback<List<Homework>>() {
            @Override
            public void onResponse(Call<List<Homework>> call, Response<List<Homework>> response) {
                //ako e zgresena adresata ili e ne e ulkucen backend
                if (!response.isSuccessful()) {
                    String errorString = "Error" + response.code();
                    Log.d("ERROR: ", errorString);
                }

                List<Homework> homeworks = response.body();

                for (Homework homework : homeworks) {
                    //TODO:izvleci domasni i proveri dali ima novi
                    informUser();
                }

            }

            @Override
            public void onFailure(Call<List<Homework>> call, Throwable t) {
                Log.d("Failure error: ", t.getMessage());
            }
        });

    }

    private void informUser() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
