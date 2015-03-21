package com.codedemigod.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.os.PatternMatcher;
import android.os.RemoteException;
import android.util.Log;
import com.heatclub.moro.action.ActionManager;
import com.heatclub.moro.action.Morom;
import com.heatclub.moro.telephony.CallService;


import com.android.internal.telephony.IExtendedNetworkService;
//import com..R;

public class CDUSSDService extends Service{

	    private String TAG = "ussdmoro";
	    private boolean mActive = false;  //we will only activate this "USSD listener" when we want it
		
	    BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Morom.Action maction = Morom.Action.getFromString(intent.getAction());
				Uri uri = intent.getData();
					//	Toast.makeText(context, 
					//				   "TEL: action = "+maction+ "-" + uri.getAuthority(), Toast.LENGTH_LONG).show();

				switch(maction){
					case SEND:
						mActive = true;
						break;
					case DISCONNECT:
						mActive = false;
						break;
				}
			}
		};
	    
		private final IExtendedNetworkService.Stub mBinder = new IExtendedNetworkService.Stub () {
			public void clearMmiString() throws RemoteException {
				Log.d(TAG, "called clear");
			}

			public void setMmiString(String number) throws RemoteException {
				Log.d (TAG, "setMmiString:" + number);
			}

			public CharSequence getMmiRunningText() throws RemoteException {
				if(mActive == true){
					return "Пауза...";
				}
				
				return "Выполняется USSD запрос...";
			}

			public CharSequence getUserMessage(CharSequence text)
					throws RemoteException {
				Log.d(TAG, "get user message " + text);
				
				if(mActive == false){
					//listener is still inactive, so return whatever we got
					Log.d(TAG, "inactive " + text);
					return text;
				}
						
				ActionManager.sendReply(getBaseContext(), text.toString()+"?"+CallService.PARAM_KEY_OK);
		//listener is active, so broadcast data and suppress it from default behavior
		/*		
				//build data to send with intent for activity, format URI as per RFC 2396
				Uri ussdDataUri = new Uri.Builder()
				.scheme(getBaseContext().getString(R.string.uri_scheme))
				.authority(getBaseContext().getString(R.string.uri_authority))
				.path(getBaseContext().getString(R.string.uri_path))
				.appendQueryParameter(getBaseContext().getString(R.string.uri_param_name), text.toString())
				.build();
			
				sendBroadcast(new Intent(Intent.ACTION_GET_CONTENT, ussdDataUri));
			*/			
				mActive = false;
				return null;
			}
			
		};

	    @Override
	    public IBinder onBind(Intent intent) {
	    	Log.i(TAG, "called onbind");
	    	
	    	//the insert/delete intents will be fired by activity to activate/deactivate listener since service cannot be stopped
	    	IntentFilter filter = new IntentFilter();
	    	filter.addAction(Morom.Action.SEND.toString());
//	    	filter.addAction(Morom.Action.CONNECT.toString());
//			filter.addAction(Morom.Action.DISCONNECT.toString());		
	    	filter.addDataScheme(Morom.Scheme.USSD.toString());
	//    	filter.addDataAuthority(getBaseContext().getString(R.string.uri_authority), null);
	 //   	filter.addDataPath(getBaseContext().getString(R.string.uri_path), PatternMatcher.PATTERN_LITERAL);
			registerReceiver(receiver, filter);
	    	
	   //     uBind = new USSDBinder();
	//		uBind.activate();
	//		mActive = true;
			return mBinder;
	    }

		@Override
		public boolean onUnbind(Intent intent)
		{
			// TODO: Implement this method
		//	return super.onUnbind(intent);
		//	uBind.deactivate();
		//	mActive = false;			
			return true;
		}
		
}
