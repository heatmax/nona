package com.heatclub.moro.action;

import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.net.Uri;
import android.content.Intent;
import android.content.Context;
import javax.xml.validation.*;
import org.apache.http.conn.scheme.*;



public class ActionManager
{	
	public static final String PARAM_KEY_URI_SENDER = "morosender";
	
	public static boolean connectService(Context context, Morom.Command command, String uri, Morom.Scheme sender){
		sConnection con = new sConnection();

		con.setContext(context);
		try{
			String actionName;
			Morom.Action action = Morom.Action.getFromCommand(command);
			if(action != null)
				actionName = action.toString();
			else
				return false;//actionName = command.toString();
			
			Intent iCommand;
			if(uri != null){
				if(Uri.parse(uri).getQueryParameterNames().isEmpty())
					uri = uri+"?"+PARAM_KEY_URI_SENDER+"="+sender;
				else
					uri = uri+"&"+PARAM_KEY_URI_SENDER+"="+sender;
				
				iCommand = new Intent(actionName, Uri.parse(uri));
			}
			else
				iCommand = new Intent(actionName);
				
		//	context.sendBroadcast(iCommand);
		//	context.startService(iCommand);
			context.bindService(iCommand, con, context.BIND_AUTO_CREATE);
			//context.startActivity(iCommand);		
		}
		catch(IllegalArgumentException e){
		//	addToLog("getAction ERROR: "+e.getMessage()+"\n");
		}
		catch(Exception e){
		//	addToLog("Send Intent ERROR: "+e.getMessage()+"\n");
		}

		return con.bound;
    }
	
	public static Uri parseEncodeUri(String str){
		Uri tm = Uri.parse(str);
		String[] queryArr;
		String[] param = null;
		
		try{
			queryArr = tm.getEncodedQuery().split("&");
		}
		catch(Exception e){
			queryArr = null;		
		}
		
		Uri.Builder uri = new Uri.Builder();
		uri.scheme(tm.getScheme());
		uri.authority(tm.getEncodedAuthority());
		uri.path(tm.getEncodedPath());
		
		if(queryArr != null)
		for(String paramArr : queryArr){
			param = paramArr.split("=");
			if(param.length != 1){
				uri.appendQueryParameter(param[0], param[1]);
			}
			else{
				uri.appendQueryParameter(param[0], "true");		
			}				
			param = null;
		}
		
		return uri.build();
		
/*		
		Uri result = new Uri.Builder()
			.scheme(tm.getScheme())
			.authority(tm.getEncodedAuthority())
			.path(tm.getEncodedPath())
		//	.("?pum=%1000")
		//	.query(query)
		//	.appendQueryParameter(param[0], Uri.encode(param[1]))
		//	.appendQueryParameter(param[0], param[1])
			.build();
		return result;
		*/
	}
	
	public static void sendCommand(Context context, Morom.Command command, String uri, Morom.Scheme sender){
		try{
			String actionName;
			Morom.Action action = Morom.Action.getFromCommand(command);
			if(action != null)
				actionName = action.toString();
			else
				return; //actionName = command.toString();

			Intent iCommand;
			if(uri != null){
				iCommand = new Intent(actionName, parseEncodeUri(uri));
			}
			else
				iCommand = new Intent(actionName);
				
			context.sendBroadcast(iCommand);
		}
		catch(IllegalArgumentException e){
			//	addToLog("getAction ERROR: "+e.getMessage()+"\n");
		}
		catch(Exception e){
			//	addToLog("Send Intent ERROR: "+e.getMessage()+"\n");
		}
		
	}
	
	public static void sendReply(Context context, String msg){
		String uri;
		uri = Morom.Scheme.REPLY.plus()+msg;
		context.sendBroadcast(new Intent(Morom.Action.SEND.toString(), Uri.parse(uri)));
	}
	
	public static class sConnection implements ServiceConnection{
		private boolean bound = false;
		private Context context;
		
		public void setContext(Context cont){
			this.context = cont;
		}
		
		public void onServiceConnected(ComponentName name, IBinder binder) {
			try{
				//	isConnect = true;
				sendReply(this.context, "Приложение подключено к сервису"+name+"\n");
				// addToLog("Приложение подключено к сервису \n");
				 bound = true;
			//	 mService = IExtendedNetworkService.Stub
			//	 .asInterface((IBinder) binder);

			//	 addToLog(mService.getMmiRunningText().toString());
				
			}
			catch(Exception e){

			}

		}

		public void onServiceDisconnected(ComponentName name) {
			//addToLog("Приложение отключено от сервиса \n");
			bound = false;
		}
	}
	
}
