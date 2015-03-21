package com.heatclub.moro;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class prefCallActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
		addPreferencesFromResource(R.xml.prefcall);
		
    }

	@Override
	protected void onDestroy()
	{
		// TODO: Implement this method
		super.onDestroy();		
	}
	 
}
