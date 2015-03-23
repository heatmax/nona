package com.heatclub.moro.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.content.Context;

import com.heatclub.moro.action.ActionManager;


public class FileCore
{
	private Context tContext;
	
	private String filename = "nons.txt";
	
	public FileCore(Context context){
		tContext = context;
	}
	
	public void addNumber(String number){
		writeFile();
	}
	
	private void writeFile() {
		String DIR_SD = "nona/";
		String FILENAME_SD = "test.txt";
		// проверяем доступность SD
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			//Log.d(LOG_TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
			return;
		}
		// получаем путь к SD
		File sdPath = Environment.getExternalStorageDirectory();
		// добавляем свой каталог к пути

		//sdPath = new File(DIR_SD);
		// создаем каталог
	//	sdPath.mkdirs();
		// формируем объект File, который содержит путь к файлу
		File sdFile = new File(sdPath, DIR_SD+FILENAME_SD);
		try {
			// открываем поток для записи
			BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
			// пишем данные
			bw.write("Содержимое файла на SD");
			// закрываем поток
			bw.close();
			//Log.d(LOG_TAG, "Файл записан на SD: " + sdFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		ActionManager.sendReply(tContext, "запись в файл ОК "+sdPath.getAbsolutePath());
	}
/*	
	private void writeFile2() {
    try {
	  String FILENAME = "/storage/sdcard0/nona/tm.txt";
      // отрываем поток для записи
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
          openFileOutput(FILENAME, MODE_PRIVATE)));
      // пишем данные
      bw.write("Содержимое файла");
      // закрываем поток
      bw.close();
     // Log.d(LOG_TAG, "Файл записан");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  */
}
