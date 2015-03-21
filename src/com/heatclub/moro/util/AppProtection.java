package com.heatclub.moro.util;

import com.heatclub.moro.db.MoroBase;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.telephony.TelephonyManager;
import android.widget.EditText;
import android.widget.Toast;




public class AppProtection
{
//	private final int IDD_APP_ACTIVE = 0;
	private Context mContext;
	
	public AppProtection(Context context){
		mContext = context;
	}
	
	public Integer getKey(long word){
		Integer result;
		String tm;
		tm = Long.toString(Math.abs((word-1070070000))%15848);
		result = Integer.parseInt(tm);
		return result;
	}
	
  protected void showDialog() {
				boolean result;
       			AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
				// создаем экземпляр TelephonyManager
				TelephonyManager teleManager = (TelephonyManager)mContext.getSystemService(
					mContext.TELEPHONY_SERVICE);
			//	SpannableString devIdV = new SpannableString(teleManager.getDeviceId());

				//	devIdV.setSpan(new UnderlineSpan(), 8, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				//Linkify.addLinks(devIdV, Linkify.ALL);	
				final long devId = Long.parseLong(teleManager.getDeviceId());
				alert.setTitle("Активация");
				alert.setMessage("ID: "+teleManager.getDeviceId());
				// Добавим поле ввода
				final EditText input = new EditText(mContext);
				alert.setView(input);

				alert.setPositiveButton("ОК", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							Integer key = getKey(devId);
							String value = input.getText().toString();
							// Получили значение введенных данных!
							if(key.toString().equals(value))
							{		
								MoroBase db = new MoroBase(mContext);
								db.saveAppKey(value);
								Toast.makeText(mContext, 
											   "Приложение успешно активировано", Toast.LENGTH_SHORT).show();						
							}	
							else
							{
								Toast.makeText(mContext, 
											   "Неверный ключ", Toast.LENGTH_SHORT).show();											
								showDialog();
							}
						}
					});

				alert.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							System.exit(0);
							// Если отменили.
						}
					});


				alert.setCancelable(false);
				alert.show();
	}

	public boolean checkActivation(boolean isShowDialog){
		boolean result = false;
		MoroBase db = new MoroBase(mContext);
		TelephonyManager teleManager = (TelephonyManager)mContext.getSystemService(
			mContext.TELEPHONY_SERVICE);

		Integer real_key = getKey(Long.parseLong(teleManager.getDeviceId()));

		String saved_key = db.readAppKey();

		if(real_key.toString().equals(saved_key))
			return true;	
		if(isShowDialog)
			showDialog();
		return result;
	}
		
}
