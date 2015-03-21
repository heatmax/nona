package com.heatclub.moro;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class prefSmsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefsms);
    }

	@Override
	protected void onDestroy()
	{
		// TODO: Implement this method
		super.onDestroy();		
	}

}

