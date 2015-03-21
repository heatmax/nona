package com.heatclub.moro;

import com.heatclub.moro.util.AppProtection;
import android.net.Uri;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TabHost;
import android.text.method.ScrollingMovementMethod;
//import android.telephony.TelephonyManager;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.view.Menu;
import android.view.MenuItem;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.text.SimpleDateFormat;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import com.android.internal.telephony.IExtendedNetworkService;

import com.heatclub.moro.action.ActionManager;
import com.heatclub.moro.action.Morom;
import com.heatclub.moro.telephony.*;
import com.heatclub.moro.xmpp.*;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;


public class MainActivity extends Activity implements View.OnClickListener
{
	private static String ACTION = "com.heatclub.moro.ACTION_MESSAGE";
	private final static String ACTION_USSD_START = "com.android.ussd.IExtendedNetworkService";
  /*  private static final String TYPE = "type";
    private static final int ID_ACTION_AUTOCALL_START = 0;
    private static final int ID_ACTION_AUTOCALL_STOP = 1;
	private static final int ID_ACTION_AUTOANSWER_START = 2;
	private static final int ID_ACTION_AUTOANSWER_STOP = 3;
	private static final int ID_ACTION_BROADCAST_STOP = 4;
	private static final int ID_ACTION_BROADCAST_START = 5;	
	private static final int ID_ACTION_SERVICE_STOP = 6;
	private static final int ID_ACTION_SERVICE_START = 7;	
*/	
	private static final int ID_MENU_PREFCALL = 100; //меню настроек хвонков
	private static final int ID_MENU_PREFSMS = 101; //меню настроек СМС
	private static final int ID_MENU_PREF = 102; //меню общих настроек
	private static final int ID_MENU_EXIT = 103;
	private static final int ID_MENU_AUTOCALL = 104;
	private static final int ID_MENU_AUTOANSWER = 105;
	
	private static final int ID_NS_MANUAL = 1;
	private static final int ID_NS_FILE = 2;
	private static final int ID_NS_DB = 3;
	
	private TextView logView;  //Экран журнала
	private TextView callView; //Экран звонков
	private TextView smsView; //Экран Sms
	
	private AppProtection protect;
	
	private static boolean isAutoCall;
	private static boolean isAutoAnswer;
	private boolean bound;
	private ServiceConnection sConn;
	private IExtendedNetworkService mService;
	
	private String delayAutoAnswer;
	private String delay = "0";
	private String callDuration = "0";
	private String number;
	private boolean isRepeatNumber;
	private boolean isRandomNumber;
	private int numberSource;
	
	private WakeLock wakeLock;
	// создаем BroadcastReceiver	
	BroadcastReceiver receiver = new BroadcastReceiver() {
		// действия при получении сообщений
		public void onReceive(Context context, Intent intent) {
			Uri uri = intent.getData();
			Morom.Action action = Morom.Action.getFromString(intent.getAction());
			switch(action){
				case SEND:
					addToLog(uri.getAuthority());
					break;
				
			}
			
		}
	};
	
    
	
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		//Проверить активацию приложения
		protect = new AppProtection(this);		
		protect.checkActivation(true);	
	
		//Отрисовать элементы интерфейса
		setContentView(R.layout.main);		
		
		//Отрисовать закладки
		TabHost tabs=(TabHost)findViewById(android.R.id.tabhost);       
        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("tag1");
		
        spec.setContent(R.id.tabCall);
        spec.setIndicator("Звонки");
        tabs.addTab(spec);

        spec = tabs.newTabSpec("tag2");
        spec.setContent(R.id.tabLog);
        spec.setIndicator("Журнал");
        tabs.addTab(spec);
		
		spec = tabs.newTabSpec("tag3");
        spec.setContent(R.id.tabSms);
        spec.setIndicator("СМС");
        tabs.addTab(spec);

        tabs.setCurrentTab(1);
		
		//загрузить Views
		logView = (TextView)findViewById(R.id.logView);
		logView.setMovementMethod(new ScrollingMovementMethod());
		callView = (TextView)findViewById(R.id.callView);
		callView.setMovementMethod(new ScrollingMovementMethod());		
		smsView = (TextView)findViewById(R.id.smsView);
		smsView.setMovementMethod(new ScrollingMovementMethod());		
		
		// создаем фильтр для BroadcastReceiver
		IntentFilter iniFilter = new IntentFilter();
		iniFilter.addAction(Morom.Action.SEND.toString());
		iniFilter.addDataScheme(Morom.Scheme.REPLY.toString());
		// регистрируем (включаем) BroadcastReceiver
		registerReceiver(receiver, iniFilter);
		
		init();
		
		//отключаем режим блокировки
		PowerManager powerManager =
			(PowerManager)getSystemService(Context.POWER_SERVICE);
		wakeLock =
			powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
									 "Full Wake Lock");
		wakeLock.acquire();
	
		String uri;
/*		String command;
		
		uri = Morom.Scheme.XMPP.plus()+"frauder_cl1:mainkey7@jabber.ru:5222"; 
		ActionManager.sendCommand(this, Morom.Command.CONNECT, uri, Morom.Scheme.ACTIVITY);
		
		uri = Morom.Scheme.XMPP.plus()+"set?recipient=frauder_admin@jabber.ru";
		ActionManager.sendCommand(this, Morom.Command.CONFIG, uri, Morom.Scheme.ACTIVITY);

		
		Intent srvIntent = new Intent(this, USSDService.class);
		bindService(srvIntent, sConn, BIND_AUTO_CREATE);
	//	uri = Morom.Scheme.USSD.plus();
	//	ActionManager.sendCommand(this, Morom.Command.CONNECT, uri, Morom.Scheme.ACTIVITY);

	/*	
		uri = Morom.Scheme.TEL.plus()+
			"set?"+
			CallService.PARAM_KEY_CALL_DURATION+"=15&"+
			CallService.PARAM_KEY_CALL_DELAY+"=10&"+
			CallService.PARAM_KEY_AUTOCALL_ON+"=true";
		ActionManager.sendCommand(this, Morom.Command.CONFIG, uri);
*/
	//	ActionManager.sendCommand(this, Morom.Command.SEND, Morom.Scheme.USSD.plus()+"*111#");
/*		
		uri = "xmpp://@?msg=привет жаба!";
		ActionManager.sendCommand(this, Morom.Command.SEND.toString(), uri);
*/	
	//	uri = "tel:";
		
	//	displayUri(uri);
	//	addToLog(getBaseContext().toString());
	}

	
	@Override
	public void onDestroy(){
		super.onDestroy();
	//	unbindService(sConn);
	/*	cmd.setTo("tel");
		cmd.setCommandName("stop");
		sendBroadcast(cmd.getCommandIntent());
		
		cmd.setTo("xmpp");
		cmd.setCommandName("stop");
		sendBroadcast(cmd.getCommandIntent());
	*/	
		
	//	Intent intent = new Intent(ACTION);
	//	intent.putExtra(TYPE, ID_ACTION_BROADCAST_STOP);
	//	sendBroadcast(intent);
	}
	
	private void init(){
		addToLog("[Инициализация...]");
	
		String action;
	//	action = "android.intent.action.BOOT_COMPLETED";
		action = "com.heatclub.moro.action.BOOT";
		Intent i = new Intent(action);
		sendBroadcast(i);
/*		
	
		try{
			String uri;
			//Запустить службу XMPP
			uri = Morom.Scheme.XMPP.plus();
			startService(new Intent(Morom.Action.CONNECT.toString(), Uri.parse(uri)));
			//	ActionManager.connectService(this, Morom.Command.CONNECT, uri, Morom.Scheme.ACTIVITY);

			//Запустить службу USSD
			uri = Morom.Scheme.USSD.plus(); 
			startService(new Intent(Morom.Action.CONNECT.toString(), Uri.parse(uri)));
	
			//Запустить службу телефонии
			uri = Morom.Scheme.TEL.plus(); 
			startService(new Intent(Morom.Action.CONNECT.toString(), Uri.parse(uri)));
			
			sendBroadcast(new Intent(Morom.Action.CONNECT.toString(), Uri.parse(Morom.Scheme.USSD.plus())));
			
	//		ActionManager.sendCommand(this, Morom.Command.SEND, uri, Morom.Scheme.ACTIVITY);
	//		ActionManager.sendReply(this, "test");
	//		sendBroadcast(new Intent(Morom.Command.SEND.toString(), Uri.parse("xmpp://fuck")));
	
		}

		catch(Exception e){
			ActionManager.sendReply(this, "error");
		}
	*/	
	}
	
	public void displayUri(String msg){
		Uri x;
	//	String uri;
	
		try{
		//	x = Uri.parse(msg);
			x = ActionManager.parseEncodeUri(msg);
	/*		String q="";
			String tm;
			for(String pname : u.getQueryParameterNames()){
				tm = pname+"="+Uri.encode(u.getQueryParameter(pname))+"&";
				q=q+tm;
			}
			
	*/		
			addToLog("======================\n");
			addToLog("getAutority: "+x.getAuthority()+"\n");
			addToLog("getEncodedAuthority: "+x.getEncodedAuthority()+"\n");
			addToLog("getFragment: "+x.getFragment()+"\n");
			addToLog("getHost: "+x.getHost()+"\n");
			addToLog("getLastPathSegment: "+x.getLastPathSegment()+"\n");
			addToLog("getPath: "+x.getPath()+"\n");
			addToLog("getPathSegments: "+x.getPathSegments()+"\n");
			addToLog("getPort: "+x.getPort()+"\n");
			addToLog("getQuery: "+x.getQuery()+"\n");
		//	addToLog("getQueryParameterNames: "+x.getQueryParameterNames()+"\n");
			addToLog("getScheme: "+x.getScheme()+"\n");
			addToLog("getSchemeSpecificPart: "+x.getSchemeSpecificPart()+"\n");
			addToLog("getUserInfo: "+x.getUserInfo()+"\n");

			addToLog("======================\n");

	//		addToLog("Result:"+x.getQueryParameterNames()+" \n");	
			
			String y = x.getEncodedQuery();
			if(!y.isEmpty())
				addToLog("Result:"+y+" \n");	
			
		}
		catch(Exception e){
			addToLog("Ошибка при парсинге URI: "+e.getMessage()+"\n");
			return;
		}
		
	}
	
	//сохранить состояние приложения
	@Override
    protected void onSaveInstanceState(Bundle outState) {/*
		 outState.putString("logCall", logCall.getText().toString());
		 */  
	}
	
	//Загрузить состояние приложения
	protected void onLoadInstanceState(Bundle inState) {/*
        if (inState != null) {
            logCall.setText(inState.getString("logCall"));
	    }*/
    }


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		}
	}
	
	
	//запустить цикл автодозвона
	private void autoCallStart(){		
	
		if(protect.checkActivation(true)) {	
			if(!isAutoCall){
				ActionManager.sendCommand(this, 
										  Morom.Command.CALL, 
										  Morom.Scheme.TEL.plus(),
										  Morom.Scheme.ACTIVITY);
				/*
				String uri;// tel://num?dur&delay&rep&ran
				String repeat;
				String random;
				if(isRepeatNumber)
					repeat = "true";
				else
					repeat = "false";

				if(isRandomNumber)
					random = "true";
				else
					random = "false";					
				
				uri = Morom.Scheme.TEL.plus()+
				CallService.COMMAND_SET+
				"?"+CallService.PARAM_KEY_CALL_DURATION+"="+callDuration+
				"&"+CallService.PARAM_KEY_CALL_DELAY+"="+delay+
				"&"+CallService.PARAM_KEY_CALL_REPEAT+"="+repeat+
				"&"+CallService.PARAM_KEY_CALL_RANDOM+"="+random+
				"&"+CallService.PARAM_KEY_AUTOCALL_ON+"=true";
					
				ActionManager.sendCommand(this, Morom.Command.CONFIG, uri, Morom.Scheme.ACTIVITY);
				ActionManager.sendCommand(this, Morom.Command.CALL, Morom.Scheme.TEL.plus()+number, Morom.Scheme.ACTIVITY);
				*/			
			//	addToLog("Дозвон запущен...\n");
				isAutoCall = true;
			}
		}
		else
			Toast.makeText(getApplicationContext(), 
						   "Нужно активировать приложение", Toast.LENGTH_SHORT).show();											
		
	}
	
	
	//остановить выполнение цикла автодозвона
	private void autoCallStop(){
	//	if(isAutoCall){
			ActionManager.sendCommand(this, Morom.Command.CALL, 
					Morom.Scheme.TEL.plus()+
					"?"+CallService.PARAM_KEY_AUTOCALL_OFF,
					Morom.Scheme.ACTIVITY);
			
	//		addToLog("Дозвон остановлен...\n");
			isAutoCall = false;
		
	}
	
	
	//Включить выполнение автоприема вызова
	private void autoAnswerStart(){
		if(!isAutoAnswer){
			String uri = Morom.Scheme.TEL.plus()+CallService.COMMAND_LOAD;
	/*		String uri = Morom.Scheme.TEL.plus()+
				CallService.COMMAND_SET+
				"?"+CallService.PARAM_KEY_AUTOANSWER_DELAY+
				"="+delayAutoAnswer+
				"&"+CallService.PARAM_KEY_AUTOANSWER_ON+"=true"+
				"&"+CallService.PARAM_KEY_AUTOCONFERENCE_ON+"=true";	
		*/
			ActionManager.sendCommand(this, Morom.Command.CONFIG, uri, Morom.Scheme.ACTIVITY);			
		//	addToLog("Автоответчик активирован...\n");
			isAutoAnswer = true;
		}
	}
	
	//остановить работу автоответчика
	private void autoAnswerStop(){
		if(isAutoAnswer){
			String uri = Morom.Scheme.TEL.plus()+CallService.COMMAND_LOAD;
			
/*			String uri = Morom.Scheme.TEL.plus()+
				CallService.COMMAND_SET+
				"?"+CallService.PARAM_KEY_AUTOANSWER_ON+
				"=false";				
*/
			ActionManager.sendCommand(this, Morom.Command.CONFIG, uri, Morom.Scheme.ACTIVITY);			
			
	//		addToLog("Автоответчик деактивирован...\n");
			isAutoAnswer = false;
		}
	}
	
	
	//============
	private void readPreference(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);	

		//info.setTextColor(9);
		
		readCallPreference(prefs);
	//	readSmsPreference(prefs);
	}
	//загрузить настройки смс
	private void readSmsPreference(SharedPreferences prefs){

		StringBuilder sb = new StringBuilder();		
	}
	
	//загрузить настройки смс
	private void readCallPreference(SharedPreferences prefs){

		StringBuilder sb = new StringBuilder();		
		String EOL = "\n";
		//Получить данные из настроек пользователя
		//Время задержки между вызовами
		delay =prefs.getString(getString(R.string.defaultDelayAutoCall), "9");
		//Длительность вызова
		callDuration = prefs.getString(getString(R.string.defaultCallTime), "13");
		//Массив номеров дозвона
        number = prefs.getString(getString(R.string.defaultPrefix), "+38067xxxxxxx");
		//	prefix = nums.split("\n");
      	//Задержка перед автоподьемом
		delayAutoAnswer = prefs.getString(getString(R.string.defaultDelayAutoAnswer), "-1");

		//String listValue = prefs.getString("list", "не выбрано");
		switch(Integer.parseInt(prefs.getString(getString(R.string.methodList), "1"))){
//		switch(prefs.getInt(getString(R.string.methodList), 1)){				
			case ID_NS_MANUAL:
				numberSource = ID_NS_MANUAL;
				if (prefs.getBoolean(getString(R.string.isRandomManualNumber), true)) 
					isRandomNumber = true;
				else
					isRandomNumber = false;

				if(prefs.getBoolean(getString(R.string.isRepeatManualNumber), true)) 
					isRepeatNumber = true;
				else
					isRepeatNumber = false;

				sb.append("Повторять: "+isRepeatNumber).append(EOL);
				sb.append("Случайно: "+isRandomNumber).append(EOL);			
				sb.append("Источник номеров: Ручной ввод").append(EOL);
				break;

			case ID_NS_FILE:
				sb.append("Источник номеров: Из файла (не реализован)").append(EOL);
				break;

			case ID_NS_DB:
				sb.append("Источник номеров: Из БД (не реализован)").append(EOL);
				break;

			default:
				sb.append("НЕ ВЫБРАН ИСТОЧНИК НОМЕРОВ!").append(EOL);

		}



		/*	isRandomManualNumber = Boolean.parseBoolean(
		 prefs.getString(getString(R.string), "9"));
		 */

		sb.append("Повтор дозвона через:    ").append(delay).append(" с.").append(EOL);
		sb.append("Длительность вызова:    ").append(callDuration).append(" с.").append(EOL);
		sb.append("Поднимать трубку через:    ").append(delayAutoAnswer).append(" с.").append(EOL);

		if (prefs.getBoolean(getString(R.string.isAutoAnswer), true)){
			autoAnswerStart();
			sb.append("Автоответчик:  Активирован").append(EOL);			
		}
		else{
			autoAnswerStop();
			sb.append("Автоответчик: Деактивирован").append(EOL);		
		}

		//infoMsg+="Источник номеров:    "+msgNS+"\n";
		sb.append("Номера дозвона:").append(EOL+EOL).append(number).append(EOL);	

		callView.setText(sb);


		/*info.append("Текущий интервал дозвона: "+delay+"\n");
		 info.append("Текущий код оператора: "+prefix+"\n");
		 info.append("Текущий интервал сброса: "+callDuration+"\n");
		 info.append("Текущий интервал автоприема: "+delayAutoAnswer+"\n");
		 */	
		/*
		 if(bAutoCall.isChecked()){
		 autoCallStop();
		 autoCallStart();
		 }
		 */
	}

    @Override
    public void onResume() {
        super.onResume();			
//		wakeLock.acquire();
		readPreference();
	//	sendBroadcast(new Intent(Morom.Action.CONNECT.toString(), Uri.parse(Morom.Scheme.USSD.plus())));
    }

	@Override
	protected void onPause() {
		super.onPause();
	//	wakeLock.release();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		String acMsg = getString(R.string.infoAutoCallOff);
		int acIcon = R.drawable.play;
		
		if(isAutoCall){
			acMsg = getString(R.string.infoAutoCallOn);
			acIcon = R.drawable.stop;		
		}
		menu.add(Menu.NONE, ID_MENU_AUTOCALL, 1, acMsg)
            .setIcon(acIcon)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | 
							MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add(Menu.NONE, ID_MENU_PREFSMS, 2, "Параметры СМС")
            .setIcon(R.drawable.sms)
          	.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | 
							 MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(Menu.NONE, ID_MENU_PREFCALL, 3, "Параметры Звонков")
            .setIcon(R.drawable.call)
          	.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | 
							 MenuItem.SHOW_AS_ACTION_WITH_TEXT);
     	menu.add(Menu.NONE, ID_MENU_PREF, 4, "Общие параметры")
            .setIcon(R.drawable.preference)
          	.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | 
							 MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add(Menu.NONE, ID_MENU_EXIT, 5, "Выход")
            .setIcon(R.drawable.ic_menu_exit)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | 
							 MenuItem.SHOW_AS_ACTION_WITH_TEXT);				 
		
    
        return(super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
     /*       case IDM_OPEN:
                openFile(FILENAME);
                break;
            case IDM_SAVE:
                saveFile(FILENAME);
                break;*/
			case ID_MENU_AUTOCALL:
				if(isAutoCall){
					item.setTitle(getString(R.string.infoAutoCallOff));
					item.setIcon(R.drawable.ic_menu_autocall_off);				
					autoCallStop();
				}
				else{
					item.setTitle(getString(R.string.infoAutoCallOn));
					item.setIcon(R.drawable.ic_menu_autocall_on);				
					autoCallStart();
				}
				break;
			case ID_MENU_PREFSMS:
           //     Intent isms = new Intent();
        	//	isms.setClass(this,prefSmsActivity.class);
              //  startActivity(isms);
			  	startActivity(new Intent().setClass(this,prefSmsActivity.class));
                break;
            case ID_MENU_PREFCALL:
                Intent icall = new Intent();
        		icall.setClass(this,prefCallActivity.class);
                startActivity(icall);
                break;
			case ID_MENU_PREF:
                Intent ipref = new Intent();
        		ipref.setClass(this,prefCallActivity.class);
                startActivity(ipref);
                break;
				
            case ID_MENU_EXIT:
                appExit();
                break;
            default:
                return false;
        }
        return true;
    }
	
	public void addToLog(String msg){

		long curTime = System.currentTimeMillis(); 
	//	String curStringDate = new SimpleDateFormat("dd.MM HH:mm:ss").format(curTime); 
		String curStringDate = new SimpleDateFormat("HH:mm:ss").format(curTime); 	
		logView.append(curStringDate+" > "+msg+"\n");
	}
	
	public void appExit(){
	//	autoCallStop();
	//	autoAnswerStop();
	//	unbindService(sConn);
		finish();
	}
/*
    private void openFile(String fileName) {
        try {
            InputStream inStream = openFileInput(FILENAME);

            if (inStream != null) {
                InputStreamReader tmp = 
                    new InputStreamReader(inStream);
                BufferedReader reader = new BufferedReader(tmp);
                String str;
                StringBuffer buffer = new StringBuffer();

                while ((str = reader.readLine()) != null) {
                    buffer.append(str + "\n");
                }

                inStream.close();
                edit.setText(buffer.toString());
            }
        }
        catch (Throwable t) {
            Toast.makeText(getApplicationContext(), 
						   "Exception: " + t.toString(), Toast.LENGTH_LONG)
                .show();
        }     
    }

    private void saveFile(String FileName) {
        try {
            OutputStreamWriter outStream = 
				new OutputStreamWriter(openFileOutput(FILENAME, 0));

            outStream.write(edit.getText().toString());
            outStream.close();      
        }
        catch (Throwable t) {
            Toast.makeText(getApplicationContext(), 
						   "Exception: " + t.toString(), Toast.LENGTH_LONG)
                .show();
        }
    }

    class FloatKeyListener extends NumberKeyListener {
        private static final String CHARS="0123456789-.";

        protected char[] getAcceptedChars() {
            return(CHARS.toCharArray()); 
        }

		@Override
		public int getInputType() {
			// TODO Auto-generated method stub
			return 0;
		}
    }*/
}
