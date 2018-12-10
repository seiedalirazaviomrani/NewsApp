package com.razavy.saro.newsapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    public static class SettingsActivityFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        private int mFlag = -1;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            Preference numberOfNewsTitles = findPreference(getString(R.string.settings_news_numbers_key));
            bindPreferenceSummeryToValue(numberOfNewsTitles, 0);

            Preference orderBy = findPreference(getString(R.string.settings_order_key));
            bindPreferenceSummeryToValue(orderBy, 1);

            Preference fromDate = findPreference(getString(R.string.setting_date_key));
            bindPreferenceSummeryToValue(fromDate, 2);
        }

        private void bindPreferenceSummeryToValue(Preference pref, int flag) {
            pref.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(pref.getContext());
            String preferenceString = preferences.getString(pref.getKey(), "");
            this.mFlag = flag;
            onPreferenceChange(pref, preferenceString);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            if (preference instanceof ListPreference && mFlag == 1) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPreference.getEntries();
                    preference.setSummary(labels[prefIndex]);
                }
            } else if (!stringValue.isEmpty() && mFlag == 0) {
                int value = Integer.valueOf(stringValue);
                if (value < 1 || value > 200)
                    preference.setSummary(getString(R.string.settings_number_of_news_hint));
                else
                    preference.setSummary(stringValue);
            } else if (mFlag == 2)
                preference.setSummary(stringValue);
            return true;
        }
    }
}
