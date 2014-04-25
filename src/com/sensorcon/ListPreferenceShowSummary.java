package com.sensorcon;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.AttributeSet;

// AA: Code from stackoverflow: http://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android-preference-in-the-preference-su/8004498#8004498
// 04.2014
public class ListPreferenceShowSummary extends ListPreference {

    private final static String TAG = ListPreferenceShowSummary.class.getName();

    public ListPreferenceShowSummary(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ListPreferenceShowSummary(Context context) {
        super(context);
        init();
    }

    private void init() {

        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference arg0, Object arg1) {
                arg0.setSummary(getEntry());
                return true;
            }
        });
    }

    @Override
    public CharSequence getSummary() {
        return super.getEntry();
    }
}