package com.heatclub.moro.xmpp;

import java.lang.reflect.Method;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.net.Uri;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.XMPPConnection;
import com.heatclub.moro.action.Morom;
import com.heatclub.moro.action.ActionManager;
import android.os.Binder;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.heatclub.moro.R;

public class XMPPService extends Service {

	private static XMPPCore xp = null;
//	private final static String ACTION_TYPE = "com.heatclub.moro.INPUT_MESSAGE";
	private static String recipient = "frauder_admin@jabber.ru";
	private static final String CONNECT_MESSAGE = "В сети...";
	
	public static final String NULL_RECIPIENT = "@";
	
	public static final String COMMAND_SET = "set";
	public static final String COMMAND_GET = "get";	
	public static final String PARAM_KEY_RECIPIENT = "recipient";
	public static final String PARAM_KEY_LOGIN = "login";
	public static final String PARAM_KEY_PASS = "pass";
	public static final String PARAM_KEY_PORT = "port";
	public static final String PARAM_KEY_SERVER = "server";	
	public static final String PARAM_KEY_MSG = "msg";
	
	private static String login;
	private static String pass;
	private static String server = "";
	private static Integer port = null;
	private static boolean isUseConnect = false;
//	private static String server = "jabber.ru";
	
	private static final String TAG = "moro";	
	// создаем BroadcastReceiver	
	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Morom.Action maction = Morom.Action.getFromString(intent.getAction());
			Uri uri = intent.getData();
	//		Toast.makeText(context, 
	//				   "XMPP: "+uri.getAuthority(), Toast.LENGTH_LONG).show();
					   
			switch(maction){
				case CONNECT:
					connect(uri);
					break;
				case DISCONNECT:
					disconnect();
					break;
				case SEND:
					send(uri);
					break;
				case CONFIG:
					config(uri);
					break;
			}
			
		}
	};
	
	
	@Override
	public IBinder onBind(final Intent intent) {	
		//ActionManager.sendReply(getBaseContext(), "Служба XMPP запущенна через bind");
		return null; // new LocalBinder<XMPPService>(this);
	}

	@Override
	public void onCreate() {		
		super.onCreate();		
		// создаем фильтр для BroadcastReceiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(Morom.Action.CONNECT.toString());
		filter.addAction(Morom.Action.DISCONNECT.toString());
		filter.addAction(Morom.Action.SEND.toString());
		filter.addAction(Morom.Action.CONFIG.toString());	
		filter.addDataScheme(Morom.Scheme.XMPP.toString());
		filter.addDataScheme(Morom.Scheme.REPLY.toString());
		filter.addCategory("android.intent.category.DEFAULT");
		// регистрируем (включаем) BroadcastReceiver
		registerReceiver(receiver, filter);
		readPreference();
	
		if(init()){
			ActionManager.sendReply(getBaseContext(), "Инициализация службы XMPP - успешно");
			if(this.isUseConnect){
				login();
				ActionManager.sendReply(getBaseContext(), "Служба XMPP подключается к сети...");
			}
		}
		else
			ActionManager.sendReply(getBaseContext(), "Ошибка при инициализации службы XMPP");
		
	
//		cmd = new CommandGenerator();		
	}
		
	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {		
/*		Morom.Action maction = Morom.Action.getFromString(intent.getAction());
		Uri uri = intent.getData();
		Toast.makeText(getBaseContext(), 
					   "PARAMetr: " + uri.getQueryParameterNames(), Toast.LENGTH_LONG).show();
*/
		return Service.START_NOT_STICKY;
	}

	@Override
	public boolean onUnbind(final Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		xp.disconnect();
	}
	
	private void readPreference(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);	
		//Получить данные из настроек пользователя
		//Логин пользователя
		setLogin(prefs.getString(getString(R.string.xmppLogin), ""));
		//Пароль
		setPass(prefs.getString(getString(R.string.xmppPass), ""));
		//Номер порта
        setPort(Integer.parseInt(prefs.getString(getString(R.string.xmppPort), "5222")));
      	//Имя сервера
		setServer(prefs.getString(getString(R.string.xmppServer), ""));
		//Автоподключение
		if (prefs.getBoolean(getString(R.string.isXmppUseConnect), true)) 
			isUseConnect = true;
		else
			isUseConnect = false;
		
	}
	
	public void config(Uri uri){
		StringBuilder sb = new StringBuilder();
		String eol = "\n";
		
		if(uri.getHost().equals(COMMAND_SET)){
			//Установить получателя сообщений
			if(uri.getQueryParameter(PARAM_KEY_RECIPIENT) != null){
				setRecipient(uri.getQueryParameter(PARAM_KEY_RECIPIENT));
				sb.append("Set Recipient - OK"+eol);
			}
			//Установить логин пользователя		
			if(uri.getQueryParameter(PARAM_KEY_LOGIN) != null){
				setLogin(uri.getQueryParameter(PARAM_KEY_LOGIN));
				sb.append("Set Login - OK"+eol);
			}
			//Установить пароль пользователя		
			if(uri.getQueryParameter(PARAM_KEY_PASS) != null){
				setPass(uri.getQueryParameter(PARAM_KEY_PASS));
				sb.append("Set Pass - OK"+eol);
			}
			
			//Установить номер порта
			if(uri.getQueryParameter(PARAM_KEY_PORT) != null){
				setPort(Integer.parseInt(uri.getQueryParameter(PARAM_KEY_PORT)));
				sb.append("Set Port - OK"+eol);
			}
			
			//Установить адрес сервера
			if(uri.getQueryParameter(PARAM_KEY_SERVER) != null){
				setServer(uri.getQueryParameter(PARAM_KEY_SERVER));
				sb.append("Set Server - OK"+eol);
			}
		} 
		else 
		if(uri.getHost().equals(COMMAND_GET)){
			sb.append("Login: "+getLogin()+eol);
			sb.append("Pass: "+getPass()+eol);
			sb.append("Server: "+getServer()+eol);
			sb.append("Port: "+getPort()+eol);
			sb.append("Recipient: "+getRecipient()+eol);
		//	sendMessage(sb.toString(), getRecipient());
		}
		
		ActionManager.sendReply(getBaseContext(), sb.toString());
	}
	
	public boolean init(String host, Integer port, String server){
		
		if((!host.equals("")) && (port != null) && (!server.equals(""))){
			xp = new XMPPCore(host, port, server);		
			xp.setListener(new stateListener());
			return true;
		}
		return false;
		
	}
		
	public void send(Uri uri){
		if(xp == null) return;
	//	if(!xp.isConnected()) return;
		
		String msg = null;			
		String recip = getRecipient();
		
		Morom.Scheme scheme = Morom.Scheme.getFromString(uri.getScheme());
		
		switch(scheme){
			case REPLY:
				msg = uri.getAuthority();
				break;
				
			case XMPP:			
				if(!uri.getAuthority().equals(NULL_RECIPIENT))
					recip = uri.getAuthority();
					
				msg = uri.getQueryParameter(PARAM_KEY_MSG);
				if(msg == null)
					msg = uri.getLastPathSegment();
				break;
				
		}
		
		if((recip != null) && (msg != null ))	
			sendMessage(msg, recip);
		
	
	}
	
	private void login(String user, String pass){
		if(xp == null) return;
		if(xp.isConnected())
			xp.disconnect();
		xp.connect(user, pass);
	}
	
	private void login(){
		login(getLogin(), getPass());
	}
	
	private boolean init(){
		return init(getServer(), getPort(), getServer());
	}
	
	private void connect(){
		if(init(getServer(), getPort(), getServer()))
			login(this.login, this.pass);
	}
	
	private void connect(Uri uri){
		if(uri.getHost() != null)
			setServer(uri.getHost());
		if(uri.getPort() != -1)
			setPort(uri.getPort());
		if(uri.getUserInfo() != null){
			String[] userInfo = uri.getUserInfo().split(":");
			setLogin(userInfo[0]);
			setPass(this.pass = userInfo[1]);
		}
		
		connect();
	}
	
	private void disconnect(){
		if(xp == null) return;
		if(xp.isConnected())
			xp.disconnect();
		this.stopSelf();	
	}
	
	private void sendMessage(String msg, String rec){
		if(xp == null) return;
	//	if(xp.isConnected())
			xp.sendMessage(msg, rec);
	}
	
	private void setRecipient(String rec){
		this.recipient = rec;
	}

	private void setLogin(String login){
		this.login = login;
	}
	
	private void setPass(String pass){
		this.pass = pass;
	}
	
	private void setPort(int port){
		this.port = port;
	}
	
	private void setServer(String server){
		this.server = server;
	}
	
	private String getRecipient(){
		return this.recipient;
	}

	private String getLogin(){
		return this.login;
	}

	private String getPass(){
		return this.pass;
	}

	private Integer getPort(){
		return this.port;
	}

	private String getServer(){
		return this.server;
	}
	
	class stateListener extends XMPPStateListener{
		
		@Override
		public void authenticated(XMPPConnection p1)
		{
			ActionManager.sendReply(getBaseContext(), CONNECT_MESSAGE);				
		//	xp.sendMessage(CONNECT_MESSAGE, recipient);	
			
		}
		
		@Override
		public void processPacket(Packet packet) 
		{
			if (packet instanceof Message) 
			{
				Message message = (Message) packet;
				if(message.getBody() != null){
					try{ 
				/*		String body = message.getBody();
						String[] arr = body.split(" ");
						String cmd = arr[0];
						String msg = "body.substring(cmd.length(), body.length())";
						ActionManager.sendCommand(getBaseContext(), Morom.Command.getFromString(cmd), msg);
				*/		
						String body = message.getBody().trim();
				//		String cmd = body.split(" ")[0];
				//		String msg = body.substring(cmd.length(), body.length()).trim();
					//	ActionManager.sendCommand(getBaseContext(), Morom.Command.getFromString(cmd), msg);
						
						String[] arr = body.split(" ", 2);
						ActionManager.sendCommand(getBaseContext(), Morom.Command.getFromString(arr[0]), arr[1], Morom.Scheme.XMPP);
						
					//	Uri parse = Uri.parse(arr[1]);
					//	String msg = Uri.encode(parse.getPath());
						
					//	sendMessage(msg, recipient);
					}
					catch(Exception e){

					}
				}	
			}
		}
	}		
	
}
