package com.example.miniassistant;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import retrofit2.Callback;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static com.example.miniassistant.App.CHANNEL_2_ID;
import static com.example.miniassistant.App.CHANNEL_3_ID;

public class HomeworkService extends Service {
//pomosno promenlivi za domasni
    private String title;
    private boolean completed;
    private boolean newHomework = false;
    private boolean first = true;
    private boolean homeworkOn = false;
//---------------------------------------
    private boolean checkOften = false;
    private String url = "";
    private boolean connected = false;
    private static Timer timer;
    private static TimerTask timerTask;
    private NotificationManagerCompat notificationManager;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("HomeworkService","started!!!!");
        notificationManager = NotificationManagerCompat.from(this);
        checkOften = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("checkOften", false);
        url = PreferenceManager.getDefaultSharedPreferences(this).getString("url", "");
        homeworkOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("homework", false);
        Log.e("HS on start cmd", "-------------" + homeworkOn);
        if(!homeworkOn){
            Log.d("Homework service" , "attempting to stop homework service!");
            stopSelf();
        }
        if(url.equals(""))
        {
            //ako nema vneseno nova adresa korisi default
            url = "https://jsonplaceholder.typicode.com/";
        }
        checkConnection();
        checkForHomework();
        startTimer();
        return START_NOT_STICKY;
    }

    private void checkConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null) {
            connected = activeNetwork.isConnectedOrConnecting();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public void startSecondTimer()
    {
        stoptimertask();
        timer = new Timer();
        initializeTimerTask();
        Log.e("STIMER", "second timer !!");
            timer.schedule(timerTask, 1000, 1200000); //proveri povtorno za 20min
    }
    public void startTimer() {
        Log.d("TimerTask" , "entered timer task!");
        Log.d("TimerTask" , "parameters:homeworkOn,checkOften" + first + homeworkOn + checkOften);
        homeworkOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("homework", false);
        if(!homeworkOn){stopSelf();}
        stoptimertask();
        timer = new Timer();
        initializeTimerTask();
        //TODO: 3600000 i 86400000 ms
      if(checkOften && homeworkOn && !first) {
            Log.d("TIMER***" , "SET EVERY HOUR");
            timer.schedule(timerTask, 1000, 3600000); //sekoj cas
        }
        else
        {
            Log.d("TIMER***" , "SET EVERY DAY");
            timer.schedule(timerTask, 1000, 86400000);//sekoj den
        }
    }
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                if(newHomework){informUser();}
                Log.e("HS", "initialize timer task");
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
        homeworkOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("homework", false);

        if(!connected)
        {
            //ako nema net iskluci primaren timer i proveri povtorno za 20min
            stoptimertask();
            startSecondTimer();
//            Toast.makeText(this,"No net will check again later",Toast.LENGTH_SHORT).show();
            Log.d("Line ~153 in homeworkS", "no connection!");
        }
        else
        {
            //ako prethodno nemalo net sega vrati go primarniot timer
            Log.d("URL is: " , url);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://jsonplaceholder.typicode.com")
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
                    if(response.body() == null){Log.d("ERR" , "null response from rest api");}
                    List<Homework> homeworks = response.body();
                    title = "";
                    completed = false;
                    //pocni da gi izminuvas i ako najdes nekompletirana informiraj go korisnikot
                    for (Homework homework : homeworks) {

                        title = homework.getTitle();
                        Log.d("title: " , title);
                        completed = homework.getCompleted();
                        if(!completed && !title.equals(""))
                        {
                            newHomework = true;
                            try{
                                return;
                            }
                            catch (Exception e)
                            {
                                Log.d("Exception is: " , e.getMessage());
                            }
                        }
                    }
                }
                @Override
                public void onFailure(Call<List<Homework>> call, Throwable t) {
                    Log.d("Failure error: ", t.getMessage());
                }
            });
        }
    }

    private void informUser() {
        if(homeworkOn)
        {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            PendingIntent actionIntent = PendingIntent.getActivity(this, 0, browserIntent, 0);

            Notification notification = new NotificationCompat.Builder(HomeworkService.this, CHANNEL_3_ID)
                    .setContentTitle("Unfinished homework's!")
                    .setContentText("Homework title :\n" + title)
                    .setSmallIcon(R.drawable.ic_book)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setAutoCancel(true)
                    .setColor(Color.WHITE)
                    .setOnlyAlertOnce(true)
                    .setOngoing(false)
                    .addAction(R.drawable.ic_open_school_site_icon, "Go to site", actionIntent)
                    .build();
            notificationManager.notify(3, notification);
            Log.i("Notification info: ", "starting homework notification on channel 3");
            //vrati gi pocetnite vrednosti
            title = "";
            completed = false;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
