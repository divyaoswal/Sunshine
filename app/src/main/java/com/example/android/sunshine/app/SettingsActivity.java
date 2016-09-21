package com.example.android.sunshine.app;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));
    }





    private  void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

       onPreferenceChange(preference,
        PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(),""));

    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int prefindex = listPreference.findIndexOfValue(stringValue);
            if(prefindex >= 0){
                preference.setSummary(listPreference.getEntries()[prefindex]);
            }

            // Set the summary to reflect the new value.
            // preference.setSummary(listPreference.getEntry()[index]);

        } else {
            preference.setSummary(stringValue);
        }
        return true;

    }

}
