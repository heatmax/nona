package com.heatclub.moro.telephony;

import com.heatclub.moro.action.ActionManager;
import com.heatclub.moro.action.Morom;
import android.content.Context;
import android.app.Activity;
import android.telephony.gsm.SmsManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.widget.Toast;

public class SmsCore
{
	Context tContext;
	String Numbers;
	
	BroadcastReceiver receiverSending = new BroadcastReceiver(){
	//	BroadcastReceiver receiver = new BroadcastReceiver(){
	@Override
	public void onReceive(Context arg0, Intent arg1) {
	//	Toast toast = Toast.makeText(tContext, 
		//							 "Пора покормить кота!", Toast.LENGTH_SHORT); 
	//	toast.show(); 
		
		switch (getResultCode())
		{
			case Activity.RESULT_OK: // сообщение отправлено
				// выполняем необходимые действия
				ActionManager.sendReply(arg0, "Отправка СМС : OK");
				break;
			case SmsManager.RESULT_ERROR_GENERIC_FAILURE: // неопределённая ошибка
				// выполняем необходимые действия
				ActionManager.sendReply(tContext, "Отправка СМС : Неизвестная ошибка");
				break;
			case SmsManager.RESULT_ERROR_NO_SERVICE: // если отключен сервис
				// выполняем необходимые действия
				ActionManager.sendReply(tContext, "Отправка СМС : ОШИБКА! Сервис отключен.");
				break;
			case SmsManager.RESULT_ERROR_NULL_PDU: // нулевое значение формы PDU
				// выполняем необходимые действия
				ActionManager.sendReply(tContext, "Отправка СМС : ОШИБКА! Нулевой PDU.");
				break;
			case SmsManager.RESULT_ERROR_RADIO_OFF: // если отключена сеть
				// выполняем необходимые действия
				ActionManager.sendReply(tContext, "Отправка СМС : ОШИБКА! Отсутствует сеть.");
				
				break;
		}
	}
	};
	
	BroadcastReceiver receiverDeliver = new BroadcastReceiver(){
		//	BroadcastReceiver receiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			ActionManager.sendReply(arg0, "Доставка СМС : OK");
		//		Toast toast = Toast.makeText(tContext, 
		//								 "Сообщение доставленно!", Toast.LENGTH_SHORT); 
		//		toast.show(); 
		}
	};
	
	SmsCore(Context cntx){
		this.tContext = cntx;
	}
	
	public Boolean autoOn(){
		return true;
	}
	
	public Boolean send(String num, String text){
		
		String SENT = "SEND_SMS"; //Morom.Command.SEND.toString(); // ключ отправки сообщения
		String DELIVER = "DELIVER_SMS";
		
        PendingIntent sentPI = PendingIntent.getBroadcast(tContext, 0, new Intent(SENT), 0);
		PendingIntent deliverPI = PendingIntent.getBroadcast(tContext, 0, new Intent(DELIVER), 0);
		IntentFilter filterSend = new IntentFilter(SENT);
		IntentFilter filterDeliver = new IntentFilter(DELIVER);
		tContext.registerReceiver(receiverSending, filterSend);
		tContext.registerReceiver(receiverDeliver, filterDeliver);
	
		SmsManager sms = SmsManager.getDefault(); // задаём стандартный мэнеджер
		sms.sendTextMessage(num, null, text, sentPI, deliverPI);
		
		
		ActionManager.sendReply(tContext, "Отправка смс на номер "+num+" ...");				
	
	//	ActionManager.sendReply(tContext, "Смс - ок ");
		
		return true;
	}
}
