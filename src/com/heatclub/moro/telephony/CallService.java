package com.heatclub.moro.telephony;

import com.heatclub.moro.action.Morom;
import com.heatclub.moro.action.ActionManager;
import android.net.Uri;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import com.heatclub.moro.R;

import javax.xml.validation.*;
import com.heatclub.moro.xmpp.*;
//import com.heatclub.moro.cmd.CommandGenerator;
//import com.heatclub.moro.cmd.CommandProvider;
//import com.heatclub.moro.cmd.CallCommand;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.xml.sax.*;
//import java.util.Timer;
//import java.util.TimerTask;

public class CallService extends Service {
	
	public static final String COMMAND_SET = "set";
	public static final String COMMAND_ADD = "add";
	public static final String COMMAND_GET = "get";
	public static final String COMMAND_DEL = "del";	
	public static final String COMMAND_SAVE = "save";	
	public static final String COMMAND_LOAD = "load";	
	
	public static final String PARAM_KEY_CALL_NUMBER = "number";
	public static final String PARAM_KEY_OK = "ok";
	public static final String PARAM_KEY_AUTOANSWER_DELAY = "aad";
	public static final String PARAM_KEY_CALL_DURATION = "duration";
	public static final String PARAM_KEY_CALL_DELAY = "delay";
	public static final String PARAM_KEY_CALL_REPEAT_ON = "repeat";
	public static final String PARAM_KEY_CALL_REPEAT_OFF = "repeatoff";
	public static final String PARAM_KEY_CALL_RANDOM_ON = "random";
	public static final String PARAM_KEY_CALL_RANDOM_OFF = "randomoff";	
	public static final String PARAM_KEY_AUTOCALL_ON = "autocall";
	public static final String PARAM_KEY_AUTOCALL_OFF = "autocalloff";
	
	public static final String PARAM_KEY_AUTOSMS_ON = "autosms";
	public static final String PARAM_KEY_AUTOSMS_OFF = "autosmsoff";	
	
	public static final String PARAM_KEY_AUTOANSWER_ON = "autoanswer";
	public static final String PARAM_KEY_AUTOANSWER_OFF = "autoansweroff";	
	public static final String PARAM_KEY_AUTOCONFERENCE_ON = "conference";
	public static final String PARAM_KEY_AUTOCONFERENCE_OFF = "conferenceoff";
	public static final String PARAM_KEY_AUTOCONFERENCE_DELAY = "confdelay";
	
	
	private static CallCore call;

	private static boolean isAutoCall = false;
//	private boolean isRepeatNumber = true;
//	private String delay;
//	private String duration;
//	private String number;
//	private String delayAutoAnswer;
	
	private SharedPreferences prefs;
	private final static String EOL = "\n";
	
	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Morom.Action maction = Morom.Action.getFromString(intent.getAction());
			Uri uri = intent.getData();
		//	Toast.makeText(context, 
	//					   "TEL: action = "+maction+ "-" + uri.getAuthority(), Toast.LENGTH_LONG).show();

			switch(maction){
				case CALL:
					call(uri);				
					break;
				case END_CALL:
					endcall(uri);				
					break;	
				case CONFIG:
					config(uri);				
					break;
				case SEND:
					send(uri);				
					break;

			}

		}
	};
	
	@Override
	public void onCreate() {					  
		super.onCreate();		
		//test9 отключено все
		
		// создаем фильтр для BroadcastReceiver
		//test10 с фильтром
		IntentFilter filter = new IntentFilter();
		filter.addAction(Morom.Action.CALL.toString());
		filter.addAction(Morom.Action.END_CALL.toString());
		filter.addAction(Morom.Action.SEND.toString());
		filter.addAction(Morom.Action.CONFIG.toString());	
		filter.addDataScheme(Morom.Scheme.TEL.toString());
		filter.addDataScheme(Morom.Scheme.USSD.toString());
		filter.addDataScheme(Morom.Scheme.REPLY.toString());
		filter.addDataScheme(Morom.Scheme.SMS.toString());
		
		filter.addCategory("android.intent.category.DEFAULT");
		// регистрируем (включаем) BroadcastReceiver
		//test11
		registerReceiver(receiver, filter);
		//test12
		call = new CallCore(getApplicationContext());
		//test13
		readCallPreference();
		readSmsPreference();
		//test14
		ActionManager.sendReply(getBaseContext(), "Служба телефонии запущенна");
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
/*		
		Morom.Action maction = Morom.Action.getFromString(intent.getAction());
		Uri uri = intent.getData();


		switch(maction){
			case CALL:
				call(uri);				
				break;
			case END_CALL:
				endcall(uri);				
				break;	
			case CONFIG:
				config(uri);				
				break;
			case SEND:
				send(uri);				
				break;
				
				
		}*/
		return START_REDELIVER_INTENT;
	}
	
	private void call(Uri uri){
		if(uri.getScheme().equals(Morom.Scheme.TEL.toString())){
			if((uri.getQueryParameterNames().isEmpty()) &&(uri.getLastPathSegment() == null) && (!uri.getHost().isEmpty())){	
				//Если УРЛ передан без параметров но с номером то совершить обычный звонок
				if(call.isAutoCall()){
					call.autoCallOff();
					ActionManager.sendReply(getBaseContext(), "Автодозвон остановлен");								
					
				}
				call.setCallDuration(0);
				call.setNumber(uri.getHost());
				call.Call();					
				ActionManager.sendReply(getBaseContext(), "Дозвон на номер "+uri.getHost()+" запущен");				
			}			
			else
			if((!uri.getQueryParameterNames().isEmpty()) &&(uri.getLastPathSegment() == null) && (!uri.getHost().isEmpty())){				
				//Если УРЛ передан с параметрами и с номером,
				//то совершить звонок с передаными параметрами		
				if(call.isAutoCall()){
					call.autoCallOff();		
					ActionManager.sendReply(getBaseContext(), "Автодозвон остановлен");				
					
				}
				commandSet(uri);
				call.Call();
				ActionManager.sendReply(getBaseContext(), "Дозвон запущен");				
				
			}		
			else
			if((!uri.getQueryParameterNames().isEmpty()) &&(uri.getLastPathSegment() == null) && (uri.getHost().isEmpty())){				
				//Если УРЛ передан с параметрами но без номера,
				//то применить переданные параметры		
				if(call.isAutoCall()){
					call.autoCallOff();		
					ActionManager.sendReply(getBaseContext(), "Автодозвон остановлен");				
					
				}
					
				commandSet(uri);
				
				if(call.isAutoCall()){				
					if(call.getNumberArray().length <= 0){
						ActionManager.sendReply(getBaseContext(), "ОШИБКА : Пустой список номеров");		
						return;
					}			
				
					call.Call();
					ActionManager.sendReply(getBaseContext(), "Автодозвон запущен");								
				}
			}		
			
			else{
				//если без номера и параметров то запустить
				//автодозвон с применением сохраненных настроек
				readCallPreference();
				if(call.getNumberArray().length <= 0){
					ActionManager.sendReply(getBaseContext(), "ОШИБКА : Пустой список номеров");		
					return;
				}			
				call.autoCallOn();
				call.Call();	
				ActionManager.sendReply(getBaseContext(), "Автодозвон запущен");								
				
			}
				
			
	//		if(!uri.getHost().isEmpty())
				
			
			
			
		//	ActionManager.sendReply(getBaseContext(), "Дозвон запущен");
			
		
			/*
			if(uri.getQueryParameter(PARAM_KEY_AUTOCALL_ON) != null){
					call.autoCallOn();
					ActionManager.sendReply(getBaseContext(), "Автдозвон запущен");			
			}
			else{
				if(uri.getQueryParameter(PARAM_KEY_AUTOCALL_OFF) != null){
					call.autoCallOff();
					ActionManager.sendReply(getBaseContext(), "Автодозвон остановлен");		
					if(!uri.getHost().isEmpty())
						call.Call();
				}
				else{	
					call.Call();		
					ActionManager.sendReply(getBaseContext(), "Дозвон запущен");
				}
				
			}*/
		}
		
	}
	
	private void send(Uri uri){
		if(uri.getScheme().equals(Morom.Scheme.USSD.toString())){
			//отправка ussd запроса
			call.sendUssd(uri.getHost());
			this.isAutoCall = call.isAutoCall();
			call.autoCallOff();
		}else
		if(uri.getScheme().equals(Morom.Scheme.REPLY.toString())){
		//	отправка глобального ответа		
			if((uri.getQueryParameter(PARAM_KEY_OK) != null) && this.isAutoCall){
				call.autoCallOn();
				call.Call();
				ActionManager.sendReply(getBaseContext(), "USSD OK");		
			}
				
		}else
		if(uri.getScheme().equals(Morom.Scheme.SMS.toString())){	
			//отправка смс
			if(call.isAutoCall()){
				call.autoCallOff();
				ActionManager.sendReply(getBaseContext(), "Автодозвон остановлен");								

			}
			
			readSmsPreference();
			
			if(call.getNumberArray().length <= 0){
				ActionManager.sendReply(getBaseContext(), "ОШИБКА : Пустой список номеров");		
				return;
			}			
			
	//		call.setNumber(uri.getHost());
	//		call.setSmsText("test sms");
	//		call.Sms();					
			ActionManager.sendReply(getBaseContext(), "Отправка смс на номер "+uri.getHost()+" запущен");				
			
		}
		
		
	}
	
	
	private void endcall(Uri uri){
	//	if(call.getCallDuration()>=0)
		if(!uri.getAuthority().isEmpty()){
			try{
				call.endCall(Integer.parseInt(uri.getAuthority()));
			}catch(Exception e){
				call.endCall(0);
			}
		
		}else
			call.autoEndCall();		
	}
	
	private void config(Uri uri){
		
	  if(uri.getScheme().equals(Morom.Scheme.TEL.toString()))
		try{
			if(uri.getAuthority().equals(COMMAND_SET)){
				commandSet(uri);
			}else
			if(uri.getAuthority().equals(COMMAND_ADD)){
				commandAdd(uri);
			}else
			if(uri.getAuthority().equals(COMMAND_DEL)){
				commandDel(uri);
			}else
			if(uri.getAuthority().equals(COMMAND_SAVE)){
				writeCallPreference();
			//		reply.append("Настройки телефонии сохранены"+EOL);						
			}else
			if(uri.getAuthority().equals(COMMAND_LOAD)){
				readCallPreference();
			//	reply.append("Настройки телефонии загружены"+EOL);						
			}else	
			//По умолчанию показать конфигурацию
//			if(uri.getAuthority().equals(COMMAND_GET)){
				commandGet(uri);
//			}	
			
		}catch(NullPointerException e){
//			ActionManager.sendReply(getBaseContext(), "config error");					
			//если схема без слешей "tel:"
			commandGet(uri);
		}catch(Exception e){
			
		}
	}
	
	private void commandGet(Uri uri){
		StringBuilder reply = new StringBuilder();				
		boolean getAll;
		if(uri.getQueryParameterNames().isEmpty())
			getAll = true;
		else
			getAll = false;
			
		if((uri.getQueryParameter(PARAM_KEY_CALL_NUMBER) != null) || getAll){
			String[] numList = call.getNumberArray();
			if(numList.length >0){
				reply.append("Список номеров :"+EOL);
				for(String str : numList)
					reply.append(str+EOL);
			}
			else
				reply.append("Список номеров пуст");			
		}
		if((uri.getQueryParameter(PARAM_KEY_CALL_DURATION) != null) || getAll){
			reply.append("Длительность вызова: "+call.getCallDuration()+EOL);
		}
		if((uri.getQueryParameter(PARAM_KEY_CALL_DELAY) != null) || getAll){
			reply.append("Интервал между вызовами: "+call.getAutoCallDelay()+EOL);
		}
		if((uri.getQueryParameter(PARAM_KEY_AUTOANSWER_DELAY) != null) || getAll){
			reply.append("Задержка перед автоприемом: "+call.getAutoAnswerDelay()+EOL);
		}
		if((uri.getQueryParameter(PARAM_KEY_AUTOCONFERENCE_DELAY) != null) || getAll){
			reply.append("Задержка перед автоконференцией: "+call.getConferenceDelay()+EOL);
		}

		if((uri.getQueryParameter(PARAM_KEY_CALL_RANDOM_ON) != null) || getAll){
			reply.append("Номера в случайном порядке: "+call.isRandom()+EOL);
		}


		if((uri.getQueryParameter(PARAM_KEY_CALL_REPEAT_ON) != null) || getAll){
			reply.append("Повторять дозвон: "+call.isRepeat()+EOL);
		}


		if((uri.getQueryParameter(PARAM_KEY_AUTOCALL_ON) != null) || getAll){
			reply.append("Автодозвон: "+call.isAutoCall()+EOL);			
		}		

		if((uri.getQueryParameter(PARAM_KEY_AUTOCONFERENCE_ON) != null) || getAll){
			reply.append("Автоконференция: "+call.isAutoConference()+EOL);		
		}

		if((uri.getQueryParameter(PARAM_KEY_AUTOANSWER_ON) != null) || getAll){
			reply.append("Автоприем входящего вызова: "+call.isAutoAnswer()+EOL);		
		}		
		
		ActionManager.sendReply(getBaseContext(), reply.toString());						
		
	}
	
	private void commandSet(Uri uri){
		StringBuilder reply = new StringBuilder();				
		//Установить длительность разговора
		if(uri.getQueryParameter(PARAM_KEY_CALL_DURATION) != null){
			call.setCallDuration(Integer.parseInt(uri.getQueryParameter(PARAM_KEY_CALL_DURATION)));
			reply.append("Длительность вызова установлена : "+uri.getQueryParameter(PARAM_KEY_CALL_DURATION)+EOL);
		}
		if(uri.getQueryParameter(PARAM_KEY_CALL_DELAY) != null){
			call.setAutoCallDelay(Integer.parseInt(uri.getQueryParameter(PARAM_KEY_CALL_DELAY)));
			reply.append("Интервал между вызовами установлен : "+uri.getQueryParameter(PARAM_KEY_CALL_DELAY)+EOL);
		}
		if(uri.getQueryParameter(PARAM_KEY_CALL_NUMBER) != null){
			call.setNumber(uri.getQueryParameter(PARAM_KEY_CALL_NUMBER));
			reply.append("Номера дозвона установлены : "+uri.getQueryParameter(PARAM_KEY_CALL_NUMBER)+EOL);
		}		
		if(uri.getQueryParameter(PARAM_KEY_AUTOANSWER_DELAY) != null){
			call.setAutoAnswerDelay(Integer.parseInt(uri.getQueryParameter(PARAM_KEY_AUTOANSWER_DELAY)));
			reply.append("Задержка перед автоприемом установленна : "+uri.getQueryParameter(PARAM_KEY_AUTOANSWER_DELAY)+EOL);
		}
		if(uri.getQueryParameter(PARAM_KEY_AUTOCONFERENCE_DELAY) != null){
			call.setConferenceDelay(Integer.parseInt(uri.getQueryParameter(PARAM_KEY_AUTOCONFERENCE_DELAY)));
			reply.append("Задержка перед автоконференцией установленна : "+uri.getQueryParameter(PARAM_KEY_AUTOCONFERENCE_DELAY)+EOL);
		}

		if(uri.getQueryParameter(PARAM_KEY_CALL_RANDOM_ON) != null){
			call.setRandom(true);
			reply.append("Номера в случайном порядке : вкл."+EOL);
		}else
		if(uri.getQueryParameter(PARAM_KEY_CALL_RANDOM_OFF) != null){
			call.setRandom(false);
			reply.append("Номера в случайном порядке : выкл."+EOL);
		}
		

		if(uri.getQueryParameter(PARAM_KEY_CALL_REPEAT_ON) != null){
			call.setRepeat(true);
			reply.append("Повторять дозвон : вкл."+EOL);
		}else
		if(uri.getQueryParameter(PARAM_KEY_CALL_REPEAT_OFF) != null){
			call.setRepeat(false);
			reply.append("Повторять дозвон : вкл."+EOL);
		}
		
		
		if(uri.getQueryParameter(PARAM_KEY_AUTOCALL_ON) != null){
			call.autoCallOn();
			reply.append("Автодозвон : вкл."+EOL);			
		}else
		if(uri.getQueryParameter(PARAM_KEY_AUTOCALL_OFF) != null){
			call.autoCallOff();
			reply.append("Автодозвон : выкл."+EOL);			
		}		

		if(uri.getQueryParameter(PARAM_KEY_AUTOCONFERENCE_ON) != null){
			call.autoConferenceOn();
			reply.append("Автоконференция : вкл."+EOL);		
		}else
		if(uri.getQueryParameter(PARAM_KEY_AUTOCONFERENCE_OFF) != null){		
			call.autoConferenceOff();
			reply.append("Автоконференция : выкл."+EOL);		
		}
					
		if(uri.getQueryParameter(PARAM_KEY_AUTOANSWER_ON) != null){
			call.autoAnswerOn();
			reply.append("Автоприем входящего вызова : вкл."+EOL);		
		}else
		if(uri.getQueryParameter(PARAM_KEY_AUTOANSWER_OFF) != null){	
			call.autoAnswerOff();	
			reply.append("Автоприем входящего вызова : выкл."+EOL);		
		}		
		ActionManager.sendReply(getBaseContext(), reply.toString());									
	}
	
	public void commandAdd(Uri uri){
		String num = uri.getQueryParameter(PARAM_KEY_CALL_NUMBER);
		//Добавить номер телефона в список дозвона
		if(num != null){
			call.addNumber(num);
			ActionManager.sendReply(getBaseContext(), "Добавление номера "+num+" - OK");				
			
			//	sendXmppMessage("ok");
		}
		
	}
	
	public void commandDel(Uri uri){
		String num = uri.getQueryParameter(PARAM_KEY_CALL_NUMBER);
		//Добавить номер телефона в список дозвона
		if(num != null){
			call.delNumber(num);
			ActionManager.sendReply(getBaseContext(), "Удаление номера "+num+" - OK");				
		}

	}
	
/*	
	public void sendXmppMessage(String msg){
		String uri = Morom.Scheme.XMPP.plus()+
		"@?"+XMPPService.PARAM_KEY_MSG+
		"="+msg;
		ActionManager.sendCommand(getBaseContext(), Morom.Command.SEND, uri, Morom.Scheme.TEL);
	}
*/	
	private void writeCallPreference(){
		SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();	
		//Сохранить данные в настроики пользователя
		//Время задержки между вызовами
		prefs.putString(getString(R.string.defaultDelayAutoCall), String.valueOf(call.getAutoCallDelay()));
		//Длительность вызова
		prefs.putString(getString(R.string.defaultCallTime), String.valueOf(call.getCallDuration()));
		//Массив номеров дозвона		
      	prefs.putString(getString(R.string.defaultPrefix), call.getNumberString("\n"));
  	 	//Задержка перед автоподьемом
		prefs.putString(getString(R.string.defaultDelayAutoAnswer), String.valueOf(call.getAutoAnswerDelay()));
		prefs.putBoolean(getString(R.string.isRandomManualNumber), call.isRandom());		
		prefs.putBoolean(getString(R.string.isRepeatManualNumber), call.isRepeat());
		prefs.putBoolean(getString(R.string.isAutoConference), call.isAutoConference());
		prefs.putBoolean(getString(R.string.isAutoAnswer), call.isAutoAnswer());
		ActionManager.sendReply(getBaseContext(), "Настройки телефонии сохранены");		

		prefs.apply();
		
	}
	
	private void readCallPreference(){
	
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);	
		//Получить данные из настроек пользователя
		//Время задержки между вызовами
		call.setAutoCallDelay(Integer.parseInt(prefs.getString(getString(R.string.defaultDelayAutoCall), "9")));
		//Длительность вызова
		call.setCallDuration(Integer.parseInt(prefs.getString(getString(R.string.defaultCallTime), "13")));
		//Массив номеров дозвона
		String nums;
		String[] anums;
		call.setNumber(prefs.getString(getString(R.string.defaultPrefix), "").split("\n"));
		
		//test 1
/*		nums = getString(R.string.defaultPrefix);
		//test 2
	//	nums = prefs.getString(nums, null);
		//test 5
		nums = prefs.getString(nums, null);
		//test 3
		anums = nums.split("\n");
		//test 4
        call.setNumber(anums);
	*/
      	//Задержка перед автоподьемом
		call.setAutoAnswerDelay(Integer.parseInt(prefs.getString(getString(R.string.defaultDelayAutoAnswer), "0")));
		call.setConferenceDelay(0);
		//test 18
		if (prefs.getBoolean(getString(R.string.isRandomManualNumber), true)) 
			call.setRandom(true);
		else
			call.setRandom(false);

		if(prefs.getBoolean(getString(R.string.isRepeatManualNumber), true)) 
			call.setRepeat(true);
		else
			call.setRepeat(false);
		//test 19		
		if(prefs.getBoolean(getString(R.string.isAutoConference), true)){
			call.autoConferenceOn();
			ActionManager.sendReply(getBaseContext(), "Автоконференция включена");		
		}
		else{
			call.autoConferenceOff();
			ActionManager.sendReply(getBaseContext(), "Автоконференция выключена");				
		}
		//test 20		
//		if(prefs.getBoolean(getString(R.string.isAutoCall), true))
//			call.autoCallOn();
//		else
//			call.autoCallOff();
	
		if(prefs.getBoolean(getString(R.string.isAutoAnswer), true)){
			call.autoAnswerOn();
			ActionManager.sendReply(getBaseContext(), "Автоприем входящего вызова включен");		
		}
		else{
			call.autoAnswerOff();	
			ActionManager.sendReply(getBaseContext(), "Автоприем входящего вызова выключен");		
			
		}
		
		ActionManager.sendReply(getBaseContext(), "Настройки телефонии загружены");		
	
		
	}
	
	private void readSmsPreference(){

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);	
		//Получить данные из настроек пользователя
		//Время задержки между вызовами
		call.setAutoCallDelay(Integer.parseInt(prefs.getString(getString(R.string.defaultDelayAutoCall), "9")));
		//Длительность вызова
		call.setCallDuration(Integer.parseInt(prefs.getString(getString(R.string.defaultCallTime), "13")));
		//Массив номеров дозвона
		String nums;
		String[] anums;
		call.setNumber(prefs.getString(getString(R.string.defaultPrefix), "").split("\n"));

		//test 1
		/*		nums = getString(R.string.defaultPrefix);
		 //test 2
		 //	nums = prefs.getString(nums, null);
		 //test 5
		 nums = prefs.getString(nums, null);
		 //test 3
		 anums = nums.split("\n");
		 //test 4
		 call.setNumber(anums);
		 */
      	//Задержка перед автоподьемом
		call.setAutoAnswerDelay(Integer.parseInt(prefs.getString(getString(R.string.defaultDelayAutoAnswer), "0")));
		call.setConferenceDelay(0);
		//test 18
		if (prefs.getBoolean(getString(R.string.isRandomManualNumber), true)) 
			call.setRandom(true);
		else
			call.setRandom(false);

		if(prefs.getBoolean(getString(R.string.isRepeatManualNumber), true)) 
			call.setRepeat(true);
		else
			call.setRepeat(false);
		//test 19		
		if(prefs.getBoolean(getString(R.string.isAutoConference), true)){
			call.autoConferenceOn();
			ActionManager.sendReply(getBaseContext(), "Автоконференция включена");		
		}
		else{
			call.autoConferenceOff();
			ActionManager.sendReply(getBaseContext(), "Автоконференция выключена");				
		}
		//test 20		
//		if(prefs.getBoolean(getString(R.string.isAutoCall), true))
//			call.autoCallOn();
//		else
//			call.autoCallOff();

		if(prefs.getBoolean(getString(R.string.isAutoAnswer), true)){
			call.autoAnswerOn();
			ActionManager.sendReply(getBaseContext(), "Автоприем входящего вызова включен");		
		}
		else{
			call.autoAnswerOff();	
			ActionManager.sendReply(getBaseContext(), "Автоприем входящего вызова выключен");		

		}

		ActionManager.sendReply(getBaseContext(), "Настройки телефонии загружены");		


	}
	
	private void writeSmsPreference(){
		SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();	
		//Сохранить данные в настроики пользователя
		//Время задержки между вызовами
		prefs.putString(getString(R.string.defaultDelayAutoCall), String.valueOf(call.getAutoCallDelay()));
		//Длительность вызова
		prefs.putString(getString(R.string.defaultCallTime), String.valueOf(call.getCallDuration()));
		//Массив номеров дозвона		
      	prefs.putString(getString(R.string.defaultPrefix), call.getNumberString("\n"));
  	 	//Задержка перед автоподьемом
		prefs.putString(getString(R.string.defaultDelayAutoAnswer), String.valueOf(call.getAutoAnswerDelay()));
		prefs.putBoolean(getString(R.string.isRandomManualNumber), call.isRandom());		
		prefs.putBoolean(getString(R.string.isRepeatManualNumber), call.isRepeat());
		prefs.putBoolean(getString(R.string.isAutoConference), call.isAutoConference());
		prefs.putBoolean(getString(R.string.isAutoAnswer), call.isAutoAnswer());
		ActionManager.sendReply(getBaseContext(), "Настройки телефонии сохранены");		

		prefs.apply();

	}

	
/*	
	private void autoCallStop(){
		if(uri.getScheme().equals(Morom.Scheme.TEL))			
			call.autoCallOff();
	}
	
	private void autoCallStart(){
		call.autoEndCallOn(msg.getDuration());
		call.autoConferenceOn(500);
		
		call.autoCallOn(msg.getNumberList(), msg.getDelay(), msg.isRepeatNumber(), msg.isRandomNumber());
*/
		
	/*
		String[] ar = msg.getNumberList();
		String lst="";
		if(ar != null)
			for(int i=0; i<ar.length; i++)
				lst=lst+ar[i]+"/";
		
		Toast.makeText(this, "Numbers: "+lst+"\n"+
		"Delay :"+msg.getDelay()+"\n"+
		"Duration:"+msg.getDuration()+"\n"+
		"Repeat: "+msg.isRepeatNumber()+"\n"+
		"Random; "+msg.isRandomNumber(), Toast.LENGTH_SHORT).show();
		
			
		}
		else if(args[0].equals("stop")){
			call.autoCallOff();			
		}
	}
	
	public void schedule(int delay, final String[] lst, final boolean random, final boolean repeat) {
		if (tTask != null) tTask.cancel();
		if (delay > 0) {
			tTask = new TimerTask() {
				public void run() {
					if((tm.currentStatus == STATUS_NONE)){
						tm.isNumberRandom = random;
						tm.isNumberRepeat = repeat;
						String num = tm.getPhoneNumber(lst);
						tm.Call(num);
					}	
				}
			};
			timer.schedule(tTask, delay, delay);
		}
	}
*/
	@Override
	public IBinder onBind(Intent intent)
	{
		// Мы не предоставляем возможность привязки, поэтому возвращаем null.
		return null;
	}

	@Override
	public void onDestroy()
	{
		ActionManager.sendReply(getBaseContext(), "Служба XMPP остановлена");		
		//Toast.makeText(this, "Служба остановленна", Toast.LENGTH_SHORT).show();
	}
	
}
