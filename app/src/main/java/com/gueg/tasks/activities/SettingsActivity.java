package com.gueg.tasks.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import com.gueg.tasks.R;


public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference prefSwitch = findPreference("prefSwitch");
        if(((SwitchPreference)prefSwitch).isChecked())
            prefSwitch.setSummary("Liste");
        else
            prefSwitch.setSummary("Calendrier");
        prefSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                SharedPreferences.Editor editor = getSharedPreferences("com.gueg.tasks", Context.MODE_PRIVATE).edit();
                editor.putBoolean("prefSwitch", (boolean) o);
                editor.apply();
                if ((boolean) o) {
                    preference.setSummary("Liste");
                }
                else {
                    preference.setSummary("Calendrier");
                }
                return true;
            }
        });
        Preference prefCalendar = findPreference("prefCalendarDay");
        if(((SwitchPreference)prefCalendar).isChecked())
            prefCalendar.setSummary("Petit");
        else
            prefCalendar.setSummary("Normal");
        prefCalendar.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                SharedPreferences.Editor editor = getSharedPreferences("com.gueg.tasks", Context.MODE_PRIVATE).edit();
                editor.putBoolean("prefCalendarDay", (boolean) o);
                editor.apply();
                if ((boolean) o) {
                    preference.setSummary("Petit");
                }
                else {
                    preference.setSummary("Normal");
                }
                return true;
            }
        });


        Preference prefColor = findPreference("prefColor");
        prefColor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = getSharedPreferences("com.gueg.tasks", Context.MODE_PRIVATE).edit();
                editor.putInt("color", (int)newValue);
                editor.apply();
                return true;
            }
        });


    }


}
