package com.example.miniassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    TextView tvProgressLabel;
    boolean extra, wifi, connectivity, homework = false;
    Button startButton;
    SeekBar seekBar;
    TextView InformUrl;
    EditText EditUrl;
    Switch UrlSwitch;
    boolean boot = false;
    boolean chechOften = false;
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boot = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("boot", false);
        fab = (FloatingActionButton) findViewById(R.id.floating_a_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cFunction();
            }
        });

        if(boot)
        {
            startMainService();
        }

        CheckBox Wifi = (CheckBox) findViewById(R.id.checkbox_wifi);
        CheckBox Connectivity = (CheckBox) findViewById(R.id.checkbox_connectivity);
        CheckBox Homework = (CheckBox) findViewById(R.id.checkbox_homework);
        CheckBox Extra = (CheckBox) findViewById(R.id.checkbox_extraInfo);

        InformUrl = (TextView) findViewById(R.id.url_inform_text);
        EditUrl = (EditText) findViewById(R.id.url_edit_text);
        UrlSwitch = (Switch) findViewById(R.id.switch_url);

        UrlSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chechOften)
                {
                    chechOften = false;
                }
                else
                {
                    chechOften = true;
                }
            }
        });
        Extra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!extra)
                {
                    extra = true;
                }
                else
                {
                    extra = false;
                }
            }
        });
        Wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!wifi)
                {
                    wifi = true;
                }
                else
                {
                    wifi = false;
                }
            }
        });
        Connectivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!connectivity)
                {
                    connectivity = true;
                }
                else
                {
                    connectivity = false;
                }
            }
        });
        Homework.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!homework)
                {
                    homework = true;
                    InformUrl.setVisibility(View.VISIBLE);
                    EditUrl.setVisibility(View.VISIBLE);
                    UrlSwitch.setVisibility(View.VISIBLE);
                }
                else
                {
                    homework = false;

                    InformUrl.setVisibility(View.GONE);
                    EditUrl.setVisibility(View.GONE);
                    UrlSwitch.setVisibility(View.GONE);
                }
            }
        });

        startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainService();
            }
        });

        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        int progress = seekBar.getProgress();
        tvProgressLabel = findViewById(R.id.textView);
        tvProgressLabel.setText("Charge drops to: " + progress + "%");

    }
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            tvProgressLabel.setText("Charge drops to: " + progress + "%");
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    public void startMainService()
    {
        if(boot)
        {
            Log.d("MainActivity ", "Starting main service after boot");
            Intent mainServiceIntent = new Intent(MainActivity.this, MainService.class);
            startService(mainServiceIntent);
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("boot", false).apply();
            MainActivity.this.finish();
        }
        else {
            int bp = seekBar.getProgress();
            Log.d("MAIN ACTIVITY: ", "battery percentage chosen is " + bp);
            SharedPreferences preferences = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            //editor.putString("selected",selectedOptions);
            //editor.putInt("batteryPercentage",bp);
            //so editor ima problemi i zatoa na zastaren nacin :\
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putInt("batteryPercentage", bp).apply();
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("extra", extra).apply();
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("wifi", wifi).apply();
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("connectivity", connectivity).apply();
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("homework", homework).apply();
            if(!EditUrl.getText().toString().equals(""))
            {
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putString("url", EditUrl.getText().toString()).apply();
            }
            if(chechOften)
            {
                Log.e("MAINACTIVITY" , "checkOften is" + chechOften);
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("checkOften", chechOften).apply();
            }
            Toast.makeText(this,"Saved preferences" + bp, Toast.LENGTH_SHORT).show();
            Intent mainServiceIntent = new Intent(MainActivity.this, MainService.class);
            startService(mainServiceIntent);
            MainActivity.this.finish();
            }
    }
    private void cFunction()
    {
        //ponisti gi site prethodni selekcii i iskluci gi servisite
        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putInt("batteryPercentage", 15).apply();
        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("extra", false).apply();
        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("wifi", false).apply();
        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("connectivity", false).apply();
        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("homework", false).apply();
        Intent mainServiceIntent = new Intent(MainActivity.this, MainService.class);
        startService(mainServiceIntent);

        MainActivity.this.finish();
    }
}