package com.example.sessiontimer;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class PrefsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            final ListPreference calendarsListPreference = (ListPreference) findPreference("calendars");
            final ListPreference themesListPreference = (ListPreference) findPreference("themes");

            setListPreferenceData(calendarsListPreference);
            calendarsListPreference.setOnPreferenceChangeListener(this);
            setListPreferenceData(themesListPreference);
            themesListPreference.setOnPreferenceChangeListener(this);
        }

        protected void setListPreferenceData(ListPreference lp) {
            if ("calendars".equals(lp.getKey())) {
                CharSequence[] entries = { "Kalender Test", "Orga" };
                CharSequence[] entryValues = {"1" , "2"};
                lp.setEntries(entries);
                lp.setEntryValues(entryValues);
                lp.setDefaultValue("1");
            } else if ("themes".equals(lp.getKey())) {
                Log.i("felix", "Nm: "+AppCompatDelegate.getDefaultNightMode());
                switch (AppCompatDelegate.getDefaultNightMode()) {
                    case AppCompatDelegate.MODE_NIGHT_YES:
                        lp.setValue("Dark");
                        break;

                    case AppCompatDelegate.MODE_NIGHT_NO:
                        lp.setValue("Light");
                        break;

                    default: //AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM und AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
                        lp.setValue("SystemDefault");
                        break;
                }
            }
        }


        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Log.i("felix", "changed pref value: " + newValue + " for " + preference);
            if ("themes".equals(preference.getKey())) {
                if ("Light".equals(newValue)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else if ("Dark".equals(newValue)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                }
            }
            return true;
        }
    }
}