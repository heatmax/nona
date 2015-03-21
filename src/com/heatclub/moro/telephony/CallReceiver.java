package com.heatclub.moro.telephony;

import com.heatclub.moro.action.Morom;
import com.heatclub.moro.action.ActionManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class CallReceiver extends BroadcastReceiver{	
			
    @Override
    public void onReceive(Context context, Intent intent) {
	
			if (intent.getAction().equals(intent.ACTION_NEW_OUTGOING_CALL)){
				String phoneNum = intent.getExtras().getString(Intent.EXTRA_PHONE_NUMBER);
				char[] phoneArr = phoneNum.toCharArray();
	
				if(phoneArr[0] != '*')			
				if(phoneArr.length >2){
					ActionManager.sendCommand(context, Morom.Command.END_CALL, Morom.Scheme.TEL.plus(), Morom.Scheme.TEL);				
				}
			}			
	}
}
