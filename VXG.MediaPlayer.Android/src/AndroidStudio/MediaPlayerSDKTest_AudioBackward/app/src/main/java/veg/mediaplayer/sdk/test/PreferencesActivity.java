/*
 *
  * Copyright (c) 2011-2017 VXG Inc.
 *
 */


package veg.mediaplayer.sdk.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.CheckBoxPreference;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.Preference;
import android.preference.PreferenceManager;
import java.nio.ByteBuffer;
import java.util.Map;


public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener 
{
    private SharedPreferences settings;
	private SharedPreferences.Editor editor;
    private CheckBoxPreference prefDecodingType;

    private CheckBoxPreference prefSynchroEnable;
    private CheckBoxPreference prefVideoCheckSPS;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());


		prefSynchroEnable = (CheckBoxPreference) findPreference("synchroEnable");
		

		findPreference("synchroNeedDropVideoFrames").setEnabled(prefSynchroEnable.isChecked());



		prefSynchroEnable.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() 
		{
			public boolean onPreferenceChange(Preference pref, Object newValue) 
			{
				findPreference("synchroNeedDropVideoFrames").setEnabled((Boolean)newValue);
				return true;
			}
		});
	}
	
	@Override
	protected void onResume() 
	{
	    super.onResume();
	
	    // Set up initial values for all list preferences
	    Map<String, ?> sharedPreferencesMap = getPreferenceScreen().getSharedPreferences().getAll();
	    Preference pref;
	    EditTextPreference listPref;
	    for (Map.Entry<String, ?> entry : sharedPreferencesMap.entrySet()) 
	    {
	        pref = findPreference(entry.getKey());
	        if (pref instanceof EditTextPreference) 
	        {
	            listPref = (EditTextPreference) pref;
	            String strCurr = pref.getSummary().toString();
	            if (strCurr.indexOf("\nCurrent value: ") >= 0)
	            {
	            	String oldCurr = strCurr.substring(strCurr.indexOf("\nCurrent value: "));
		            strCurr = strCurr.replace(oldCurr, "");
	            }
	            pref.setSummary(strCurr + "\nCurrent value: " + listPref.getText().toString());
	        }
	    }
	
	    // Set up a listener whenever a key changes            
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() 
	{
	    super.onPause();
	
	    // Unregister the listener whenever a key changes            
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
	{
	    Preference pref = findPreference(key);
	
	    if (pref instanceof EditTextPreference) {
	    	EditTextPreference listPref = (EditTextPreference) pref;
	        String strCurr = pref.getSummary().toString(); 
            if (strCurr.indexOf("\nCurrent value: ") >= 0)
            {
            	String oldCurr = strCurr.substring(strCurr.indexOf("\nCurrent value: "));
	            strCurr = strCurr.replace(oldCurr, "");
            }
	        pref.setSummary(strCurr + "\nCurrent value: " + listPref.getText().toString());
	        //pref.setSummary(listPref.getText().toString());
	    }
	}
	
}
