package com.example.sessiontimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final static int MSG_START_TIMER = 0;
    final static int MSG_STOP_TIMER = 1;
    final static int MSG_UPDATE_TIMER = 2;

    
    final static int REFRESH_RATE = 100;

    static NotificationMaster nMaster;

    static Chronometer tvTextView;
    static Button btnStart,btnStop, btnSettings, btnStats;
    static CheckBox chBoxAdd;
    static ChipGroup chipGroup;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //Set Theme
        String selectedTheme = sharedPref.getString("themes", "na");
        if (selectedTheme.equals("Light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (selectedTheme.equals("Dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        switch (AppCompatDelegate.getDefaultNightMode()) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                setTheme(R.style.Theme_FiBuSteuernNight);
                break;

            case AppCompatDelegate.MODE_NIGHT_NO:
                setTheme(R.style.Theme_FiBuSteuernDay);
                break;

            default: //AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM und AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
                int nightModeFlags =
                        getResources().getConfiguration().uiMode &
                                Configuration.UI_MODE_NIGHT_MASK;
                switch (nightModeFlags) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        Log.i("felix", "night yes");
                        setTheme(R.style.Theme_FiBuSteuernNight);
                        break;

                    case Configuration.UI_MODE_NIGHT_NO:
                        Log.i("felix", "night no");
                        setTheme(R.style.Theme_FiBuSteuernDay);
                        break;

                    case Configuration.UI_MODE_NIGHT_UNDEFINED:
                        Log.i("felix", "night undef");
                        setTheme(R.style.Theme_FiBuSteuernDay);
                        break;
                }
                break;
        }

        setContentView(R.layout.activity_main);

        nMaster = new NotificationMaster(this);
        handler = new Handler();
        editor = sharedPref.edit();

        sharedPref.getAll().forEach((key, value) -> Log.i("felix", "Pref - " + key + " : " + value));

        tvTextView = (Chronometer) findViewById(R.id.TextView01);

        btnStart = (Button)findViewById(R.id.Button01);
        btnStop = (Button)findViewById(R.id.Button02);
        btnSettings = (Button)findViewById(R.id.button);
        btnStats = (Button)findViewById(R.id.button3);
        chipGroup = findViewById(R.id.chipgroup01);
        chBoxAdd = (CheckBox)findViewById(R.id.checkBox01);

        checkNgrandPermissions();

        btnStop.setEnabled(false);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnStats.setOnClickListener(this);
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
               if(checkedId != 0 && checkedId != -1 && checkedId != group.getCheckedChipId()) {
                   //Log.i("felix", ""+checkedId+"chip group: "+ getResources().getResourceEntryName(checkedId)+ " value: "+((Chip)findViewById(chipGroup.getCheckedChipId())).getText().toString());
                   editor.putInt("chipSelected", checkedId);
                   editor.apply();
               }
            }
        });
        //Load preferred Event-Title
        if (sharedPref.getInt("chipSelected", 0) != 0) {
            chipGroup.clearCheck();
            chipGroup.check(sharedPref.getInt("chipSelected", 0));
        }
        chBoxAdd.setChecked(sharedPref.getBoolean("addToCal", false));
        chBoxAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("addToCal", isChecked);
                editor.apply();
            }
        });

        //State: timer is still running
        if (sharedPref.getLong("current_base", 0) != 0) {
            tvTextView.setBase(sharedPref.getLong("current_base", 0));
            tvTextView.start();
            btnStop.setEnabled(true);
        }

        tvTextView.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                Log.i("felix", "current: "+(SystemClock.elapsedRealtime() - chronometer.getBase()));
            }
        });

        //Check if coming from notification
        if (getIntent().getAction() == "fromNotificationMaster") {
            Log.i("felix","Intent coming from notification");
        }



    }

    private void checkForUserAccount() {
        //Check if we have an user account
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        if (sharedPref.getString("useraccount", "").isEmpty()) {
            Log.i("felix", "building account chooser dialog...");
            EventGoogleCal googleCalInterf = new EventGoogleCal(this);
            CharSequence[] accountList = googleCalInterf.listAllAccounts(this);

            builder.setTitle(R.string.title_chooseCalAcc);
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finishAffinity();
                }
            });
            builder.setItems(accountList, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i("felix", "account chooser dialog: "+accountList[which]);
                }
            });

        } else {
            builder.setTitle(R.string.title_noGoogleAccount);
            builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finishAffinity();
                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void checkNgrandPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available
            Log.i("felix", "permission already granted");
        } else {
            // Permission is missing and must be requested.
            requestPermissions();
        }
    }

    private void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CALENDAR)) {

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR, Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS},
                            0);

        } else {
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR, Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS}, 0);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == 0) {
            // Request for permission.
            if (grantResults.length == 4 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                Log.i("felix", "PERMISSION GRANTED!!!");
                checkForUserAccount();
            } else {
                // Permission request was denied.
                Log.i("felix", "PERMISSION Denied!!!");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.title_noPermissionGiven);
                builder.setCancelable(false);
                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finishAffinity();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }

    public void timerCycle() {
        Log.i("felix", "timer up!!");
        nMaster.setupGenericNotification("Timer up", "Make a break");
    }

    @Override
    public void onClick(View v) {
        if (btnStart == v) {
            long now = SystemClock.elapsedRealtime();
            tvTextView.setBase(now);
            tvTextView.start();

            editor.putLong("current_base", now);
            editor.apply();

            //Setup Notification:
            Intent notifyIntent = new Intent(this, MainActivity.class);
            notifyIntent.setAction("fromNotificationMaster");
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notifyIntent.setAction("fromNotificationClockOut");
            PendingIntent pendingIntentClOut = PendingIntent.getActivity(this, 2, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            nMaster.setupNotification(pendingIntent, pendingIntentClOut);
            //Setup Timer:
            Runnable timerTask = new Runnable() {
                @Override
                public void run() {
                    timerCycle();
                    //handler.postDelayed(this, 10000);
                }
            };
            int delayTimer = Integer.parseInt(((EditText)findViewById(R.id.editTextNumber)).getText().toString());
            //handler.removeCallbacks(timerTask);
            //handler.postDelayed(timerTask, delayTimer*60000);

            btnStop.setEnabled(true);
        } else if (btnStop == v){
            editor.putLong("current_base", 0);
            editor.apply();
            nMaster.dismissNotification();
            if (sharedPref.getBoolean("addToCal", false)) {
                EventGoogleCal event = new EventGoogleCal(this);
                long startMills = new Date().getTime();
                startMills -= (SystemClock.elapsedRealtime() - tvTextView.getBase());
                long endMills = new Date().getTime();
                String eventTitle = ((Chip)findViewById(chipGroup.getCheckedChipId())).getText().toString();
                event.createEvent(12, startMills, endMills, eventTitle);
            }
            tvTextView.stop();
            btnStop.setEnabled(false);
        } else if (btnSettings == v) {
            Intent intent = new Intent(this, PrefsActivity.class);
            EventGoogleCal dummyEvent = new EventGoogleCal(this);
            //intent.putExtra("cal array",dummyEvent.getCalendarsArr());
            startActivity(intent);
        } else if (btnStats == v) {
            Intent intent = new Intent(this, StatsActivity.class);
            //Check if timer is currently running and if so, attach current start-time:
            if (btnStop.isEnabled()) {
                Log.i("felix","MainActivity: BttnStats->OnClick: timer_running = TRUE");
                intent.putExtra("currentTimer", tvTextView.getBase());
            }
            startActivity(intent);
        }
    }
}