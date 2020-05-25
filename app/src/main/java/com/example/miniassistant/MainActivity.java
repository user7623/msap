package com.example.miniassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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

public class MainActivity extends AppCompatActivity {

    TextView tvProgressLabel;
    boolean extra, wifi, connectivity, homework = false;
    Button startButton;
    SeekBar seekBar;
    TextView InformUrl;
    EditText EditUrl;
    Switch UrlSwitch;
    boolean chechOften = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckBox Wifi = (CheckBox) findViewById(R.id.checkbox_wifi);
        CheckBox Connectivity = (CheckBox) findViewById(R.id.checkbox_connectivity);
        CheckBox Homework = (CheckBox) findViewById(R.id.checkbox_homework);
        CheckBox Extra = (CheckBox) findViewById(R.id.checkbox_extraInfo);

        InformUrl = (TextView) findViewById(R.id.url_inform_text);
        EditUrl = (EditText) findViewById(R.id.url_edit_text);
        UrlSwitch = (Switch) findViewById(R.id.switch_url);

        UrlSwitch.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if(chechOften)
                {
                    chechOften = false;
                }
                else
                {
                    chechOften = true;
                }
                return false;
            }
        });

        //TODO: probaj da poednostavis so prevzemanje na vrednostite od Checkbox

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

        // set a change listener on the SeekBar
        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        int progress = seekBar.getProgress();
        tvProgressLabel = findViewById(R.id.textView);
        tvProgressLabel.setText("Charge drops to: " + progress + "%");
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // updated continuously as the user slides the thumb
            tvProgressLabel.setText("Charge drops to: " + progress + "%");
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
        }
    };

    public void startMainService()
    {
        //PreferenceManager.getDefaultSharedPreferences(context).getString("MYLABEL", "defaultStringIfNothingFound");
        int bp = seekBar.getProgress();
        Log.d("MAIN ACTIVITY: ", "battery percentage chosen is " + bp);
        //Log.d("Saved preferences are: " , selectedOptions + bp);
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
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putBoolean("checkOften", chechOften).apply();
        }
        /*editor.putBoolean("extra",extra);
        editor.putBoolean("wifi",wifi);
        editor.putBoolean("connectivity",connectivity);
        editor.putBoolean("homework",homework);
        editor.apply();*/
        Toast.makeText(this,"Saved preferences" + bp, Toast.LENGTH_SHORT).show();
        Intent mainServiceIntent = new Intent(MainActivity.this, MainService.class);
        startService(mainServiceIntent);
        MainActivity.this.finish();
    }

}