package com.heatclub.moro.telephony;

import com.android.internal.telephony.ITelephony;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.net.Uri;
import java.util.Random;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import java.lang.reflect.Method;
import android.view.KeyEvent;

import com.heatclub.moro.db.MoroBase;
import com.heatclub.moro.util.FileCore;

import android.os.Handler;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
//import java.io.*;
//import android.os.IBinder;
//import android.preference.*;


public class CallCore{
	
	private Context tContext;
	private TelephonyManager tm;
	private MoroBase db;
	private FileCore filebase;
	
	private static ITelephony iTel;
	private static Handler h;
	
//	private static final int NUMBER_TYPE_SINGLE = 0;
//	private static final int NUMBER_TYPE_MULTI = 1;
	
	private final int STATUS_NONE = 0; // нет подключения
	private final int STATUS_OUTGOING = 1; // звоним
	private final int STATUS_WAIT = 2; // В ожидании дозвона
	private final int STATUS_ANSWER = 3; // подняли трубку
	private final int STATUS_INCOMING = 4; // входящий вызов
		
//	private static String phoneNumber;
//	private static String[] phoneNumberList = new String[0];
	private ArrayList<String> phoneNumberList = new ArrayList<String>();
//	private static int numberType;
	private static int[] usedNumberIndex = new int[0];
	
	
	private static Integer delayAutoCall = 0;
	private static Integer durationCall = 0;
	private static Integer delayAutoAnswer = 0;	
//	private Integer delayAutoEndCall;	
	private static Integer delayAutoConference = 500;
	
	private static boolean isAutoCall = false;
	private static boolean isAutoEndCall = false;
	private static boolean isAutoAnswer = false;
	private static boolean isAutoConference = true;
	private static boolean isNumberRandom = false;
	private static boolean isNumberRepeat = false;
	private static boolean isRun = false;
	
	
	public static int currentStatus;
	public static int previousStatus;
	
	
	// определяем обработчики событий изменений состояния сети
	private PhoneStateListener listener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(
			final int state, final String incomingNumber) {     
			String msg = null;
			previousStatus = currentStatus;
			
		//	h.removeCallbacks(rAutoCall);
					
			switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:
					msg = "N0NE";
					currentStatus = STATUS_NONE;
				//	go();
				//	put(getPhoneNumber(phoneNumberList));
					if(isAutoCall && isRun)
						Call();
			//		if(isAutoCall && (previousStatus!= STATUS_NONE)){
			//			autoCall();
				//		h.postDelayed(rAutoCall, delayAutoCall*1000);
				//		h.postDelayed(rAutoCall, 1000);
						
				//	}
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					if(previousStatus == STATUS_NONE){
						msg = "OUTGOING";
						currentStatus = STATUS_OUTGOING;
					}
					else
					if(previousStatus == STATUS_INCOMING){
						msg = "ANSWER";
						currentStatus = STATUS_ANSWER;					
					}
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					msg = "INCOMING";
					currentStatus = STATUS_INCOMING;
					autoAnswerCall();
					db.insertNumber(incomingNumber);
					filebase.addNumber(incomingNumber);
					break;
				default:
					msg = "Not defined";
			}
			
			//displayMessage("Изменение состояния вызова - "+msg+"\n  "+currentStatus); 
						 

        }  
	};	
	
	CallCore(Context context){
		tContext = context;
		// создаем экземпляр TelephonyManager
        tm = (TelephonyManager)tContext.getSystemService(
			Context.TELEPHONY_SERVICE);
		tm.listen(listener, 
				   PhoneStateListener.LISTEN_CALL_STATE);   
		db = new MoroBase(tContext);
		filebase = new FileCore(tContext);
		
		h = new Handler();
		h.post(rITelephony);
		filebase.addNumber("111");
		
	}
	
	//создать в отдельном потоке экземпляр класса ITelephone//	public void createTelephonyService(){
	Runnable rITelephony = new Runnable() { 
            @Override 
            public void run() { 
				try{			
					String cName = tm.getClass().getName();
					Class c = Class.forName(cName);
					Method m = c.getDeclaredMethod("getITelephony");
					m.setAccessible(true);
					iTel = (ITelephony) m.invoke(tm);
				}
				catch(Exception e){
				//	Thread.currentThread().interrupt();
					displayMessage("ОШИБКА: in Runnable rITelephony"); 				
					//	Log.e("ERROR", "Thread Interrupted");
				}			
			}
	};
	
	//Runnable автовызова
	Runnable rAutoCall = new Runnable() { 

		@Override 
		public void run() { 	
			try{
			
		//		if((currentStatus == STATUS_NONE) && isAutoCall){
					runCall(getNumber());
				//displayMessage(getNumber());
		//		}
			}
			catch(Exception e){
				//		Thread.currentThread().interrupt();
				displayMessage("ОШИБКА: in Runnable rAutoCall!!!"); 

				//	Log.e("ERROR", "Thread Interrupted");
			}			
		}
	};
	
	//Runnable окончаниия вызова
	Runnable rEndCall = new Runnable() { 
		@Override 
		public void run() { 
			try{
			//	if(currentStatus == STATUS_OUTGOING)
					iTel.endCall();
			}
			catch(Exception e){
			//	Thread.currentThread().interrupt();
				displayMessage("ОШИБКА: in Runnable rEndCall"); 
				//	Log.e("ERROR", "Thread Interrupted");
			}			
		}
	};
/*	
	//Runnable вставки номера БД
	Runnable rDBInsertNumber = new Runnable() { 
		@Override 
		public void run() { 
			try{
				
				db.insertNumber();
			}
			catch(Exception e){
				Thread.currentThread().interrupt();
				//	Log.e("ERROR", "Thread Interrupted");
			}			
		}
	};
*/	
	//Runnable автоматического приема входящего вызова
	Runnable rAutoAnswerCall = new Runnable() { 
		@Override 
		public void run() { 
			try{
					h.removeCallbacks(rEndCall);
					switch(previousStatus){
					case STATUS_OUTGOING:
						// если поступил входящий во время когда
						// осуществляется исходящий вызов то исходящий оборвать
						// а входяший принять
						TimeUnit.MILLISECONDS.sleep(500);
						runCall("1");
				//		h.removeCallbacks(rEndCall);			
					//	displayMessage("ОШИБКА1"); 
						break;
					case STATUS_ANSWER:				
						answerCall(delayAutoAnswer);
						if(isAutoConference){
							TimeUnit.MILLISECONDS.sleep(delayAutoConference);				
							runCall("3");
						}
					//	h.removeCallbacks(rEndCall);
						//displayMessage("ОШИБКА2"); 
						break;
					default:
						answerCall(delayAutoAnswer);
					//	h.removeCallbacks(rEndCall);
						//displayMessage("ОШИБКА3"); 
					}
			}
			catch(Exception e){
			//	Thread.currentThread().interrupt();
				displayMessage("ОШИБКА: in Runnable rAutoAnswerCall"); 
			//	endCall(null);
				//	Log.e("ERROR", "Thread Interrupted");
			}			
		}
	};
	
	//Runneble принятия входящего вызова
	Runnable rAnswerCall = new Runnable() { 		
		@Override 
		public void run() { 
			try{
						
			//	iTel.answerRingingCall();			
				Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
				buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
				tContext.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");
				buttonDown = null;

				Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
				buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
				tContext.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
				buttonUp = null;
			}	
			catch(Exception e){
			//	Thread.currentThread().interrupt();
				displayMessage("ОШИБКА: in Runnable rAnswerCall"); 
				
				//	Log.e("EROR", "Thread Interrupted");
			}		
		}
	};
	
	//Runneble принятия входящего вызова
	Runnable rEndCall2 = new Runnable() { 		
		@Override 
		public void run() { 
			try{
				Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
				buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,
																		   KeyEvent.KEYCODE_CALL));
				tContext.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

// froyo и за ее пределами триггер на buttonUp вместо buttonDown
				Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
				buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,
																		 KeyEvent.KEYCODE_CALL));
				tContext.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
//				iTel.call("2");
/*
				Intent buttonDown = new Intent(Intent.ACTION_CALL_BUTTON);
				buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, 	KeyEvent.KEYCODE_ENDCALL));
				tContext.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");
				buttonDown = null;

				Intent buttonUp = new Intent(Intent.ACTION_CALL_BUTTON);
				buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENDCALL));
				tContext.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
				buttonUp = null;
				*/
				/*	
				 //	displayMessage(currentStatus);
				 if(previousStatus == STATUS_ANSWER)
				 iTel.call("3");
				 else*/
				//currentStatus = STATUS_ANSWER;

			}	
			catch(Exception e){
			//	Thread.currentThread().interrupt();
				displayMessage("ОШИБКА: in Runnable rAndCall2"); 
				
				//	Log.e("EROR", "Thread Interrupted");
			}		
		}
	};

	public void displayMessage(String info){
		Toast.makeText(tContext, info, 
					   Toast.LENGTH_LONG).show();
	}
	
	//Закончить вызов
	public void endCall(Integer delay){
			
			if(!isAutoCall())
				isRun = false;
		
			if(delay == null){
				h.post(rEndCall);	
				return;
			}
				
			if(delay > 0){
				h.postDelayed(rEndCall, delay*1000);
				return;
			}
			
			h.post(rEndCall);	
	}
	
	//Закончить вызов
	public void autoEndCall(){
		if(getCallDuration() > 0){
			endCall(getCallDuration());
		}
	}
	
	//Принять вызов
	public void answerCall(Integer delay){
		h.removeCallbacks(rAutoCall);
		
		if(delay == null){
			h.post(rAnswerCall);
			return;
		}
		
		if(delay > 0){
			h.postDelayed(rAnswerCall, delay*1000);
			return;
		}
		
		h.post(rAnswerCall);
	}
	
	public void autoAnswerCall(){
		
		if(this.isAutoAnswer){
			h.post(rAutoAnswerCall);
		//	answerCall(delayAutoAnswer);		
		}
	}
	
	public void Call(){
		
		h.removeCallbacks(rAutoCall);
		
		if(!isAutoCall()){
			runCall(getNumber());
			return;
		}
			
		isRun = true;
		
		if((this.delayAutoCall == 0) || (this.delayAutoCall == null)){
			h.post(rAutoCall);	
			return;
		}

		if(this.delayAutoCall > 0){
			h.postDelayed(rAutoCall, this.delayAutoCall*1000);
			return;
		}

		h.post(rAutoCall);	
	}
	
	public void Call(String number){
		setNumber(number);
		Call();
	}
	
	public void Call(String[] numbers){
		setNumber(numbers);
		Call();
	}
	
	//Совершить вызов
	public void runCall(String number){	

		if(number == null)
			return;
			
		Uri uri = Uri.parse("tel:"+number);
		Intent dialogIntent = new Intent(Intent.ACTION_CALL, uri);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		tContext.getApplicationContext().startActivity(dialogIntent);

	}

	public void sendUssd(String number){
	//	autoCallPause();
		String encodedHash = Uri.encode("#");
		runCall(number + encodedHash);
	}	

	//Генератор случайного телефонного номера
	public String numberGenerator(String number){
		Random rand = new Random();
		Integer tm;
		char[] charArr = number.toCharArray();
		int kol = number.length();
		char mask = 'x';
		String result;
		
		for(int i=0; i<kol; i++){
			if(charArr[i] == mask){
				tm = rand.nextInt(9);
				charArr[i]=tm.toString().charAt(0);		
			}
		}
		result = new String(charArr);
		return result;
	}
	
	//Генератор случайного телефонного номера на основе списка
	public String getNumber(){
		Integer i=0;
		String result;		
			
	
		if(isNumberRandom){				
			Random rand = new Random();
			i=rand.nextInt(this.phoneNumberList.size());
	/*		Доработать режим рандом без повторения
			if(!isNumberRepeat){
				while(true){
					if(usedNumberIndex[i])
						i=rand.nextInt(number.length);
					else{
						usedNumberIndex[i]=i;
						break;
					}
				}
			}*/
		}
		else{
			if(usedNumberIndex.length >= this.phoneNumberList.size()){
				if(isNumberRepeat){
					usedNumberIndex = new int[1];
				}
				else{
					autoCallOff();
					return null;
				}
			}
			else{
				i=usedNumberIndex.length;
				usedNumberIndex = new int[i+1];				
			}
				
		}
		
		result=numberGenerator(this.phoneNumberList.get(i));	
		return result;
	
	}
	
	public String[] getNumberArray(){
		return this.phoneNumberList.toArray(new String[this.phoneNumberList.size()]);
	}
	
	public String getNumberString(String spl){
		StringBuffer sb = new StringBuffer();
		for(int i=0; i < this.phoneNumberList.size()-1; i++){
			sb.append(this.phoneNumberList.get(i)).append(spl);
		}
		sb.append(this.phoneNumberList.get(phoneNumberList.size()-1));
		
		return sb.toString();
	//	return "111";
	}
	
	
	public void setNumber(String number){
		if(number == null) return;
		this.phoneNumberList.clear();
		this.phoneNumberList.add(number.trim()); // new String[1];
	//	numbers[0] = number;
	//	setNumber(numbers);
	}
		
	public void setNumber(String[] numbers){
		if((numbers == null) || (numbers.length == 0)) return;
		this.phoneNumberList.clear();
		for(String num : numbers)
			this.phoneNumberList.add(num);
	}
	
	public void addNumber(String number){
	//	setNumber(number);
		this.phoneNumberList.add(number);
	/*	
		int len = this.phoneNumberList.length+1;
		String[] newNumbers = new String[len];

		int i = 0;
		for(String s : this.phoneNumberList){
			newNumbers[i] = s;
			i++;
		}
		newNumbers[i] = number.trim();
		setNumber(newNumbers);
	*/	
	}
	
	public void delNumber(String number){
		this.phoneNumberList.remove(number);
/*
		String[] newNumbers = new String[this.phoneNumberList.length-1];
	
		int i = 0;
		for(String s : this.phoneNumberList){
			if(!s.equals(number.trim())){
				newNumbers[i] = s;
				i++;
			}
		}
	//	newNumbers[i] = number;
		setNumber(newNumbers);
*/
	}
	
	
	public void setAutoCallDelay(int delay){
		this.delayAutoCall = delay;	
	}
	
	public int getAutoCallDelay(){
		return this.delayAutoCall;	
	}
	
	
	public void setCallDuration(int duration){
		this.durationCall = duration;	
	}
	
	public Integer getCallDuration(){
		return this.durationCall;	
	}
	
	
	public void setRepeat(boolean repeat){
		this.isNumberRepeat = repeat;
	}
	
	public boolean isRepeat(){
		return this.isNumberRepeat;
	}
	
	
	public void setRandom(boolean random){
		this.isNumberRandom = random;
	}
	
	public boolean isRandom(){
		return this.isNumberRandom;
	}
	
	//Включить автодозвон
	public void autoCallOn(){
		
		if(this.isNumberRandom)
			usedNumberIndex = new int[this.phoneNumberList.size()];
			
		this.isAutoCall = true;
		
	//	Call();
	}
	
	//Остановить автодозвон
	public void autoCallOff(){		
		isAutoCall = false;
		isRun = false;
		h.removeCallbacks(rAutoCall);
	}
	
	public void autoCallPause(){
		try{
			h.wait();
		}
		catch(Exception e){
			
		}
	}
	
	public void autoCallResume(){
		try{
			h.notify();
		}
		catch(Exception e){

		}
	}
	
	
	
	public void setAutoAnswerDelay(int delay){
		this.delayAutoAnswer = delay;	
	}
	
	public int getAutoAnswerDelay(){
		return this.delayAutoAnswer;	
	}
	
	//Включить автоподьем трубки
	public void autoAnswerOn(){
		isAutoAnswer = true;
	}

	//Остановить автодозвон
	public void autoAnswerOff(){
		isAutoAnswer = false;
	}
	
	public boolean isAutoAnswer(){
		return this.isAutoAnswer;
	}
/*	
	public void setEndCallDelay(int delay){
		this.durationCall = delay;	
	}

	public void autoEndCallOn(){
		this.isAutoEndCall = true;
	}
	
	public void autoEndCallOff(){
		this.isAutoEndCall = false;
	}
*/	
	public int getConferenceDelay(){
		return this.delayAutoConference;	
	}
	
	public void setConferenceDelay(int delay){
		this.delayAutoConference = delay;
	}
	
	public void autoConferenceOn(){
		this.isAutoConference = true;
	}

	public void autoConferenceOff(){
		this.isAutoConference = false;
	}
	
	public boolean isAutoConference(){
		return this.isAutoConference;
	}
	
	
	public boolean isAutoCall(){
		return this.isAutoCall;
	}
	
	public boolean isAutoEndCall(){
		return this.isAutoEndCall;
	}
	
	
	
}

