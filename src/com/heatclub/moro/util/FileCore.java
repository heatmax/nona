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
import java.io.InputStream;
import java.io.OutputStream;


import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.content.Context;

import com.heatclub.moro.action.ActionManager;
import java.io.*;
import org.jivesoftware.smack.*;
import java.lang.reflect.*;
import java.util.*;


public class FileCore
{
	private Context tContext;
	
	private String filename = "nons.txt";
	
	public FileCore(Context context){
		tContext = context;
	}
	
	public void addNumber(String number){
		String dir = "/sdcard/nona";
		
		List<String> list= openFileAsList(dir+"/"+getFilename());
//		list.
	//	saveFile(dir, number);
	}
	
	// Метод для открытия файла
    private List openFileAsList(String fileName) {
        try {
            FileInputStream inputStream = new FileInputStream(fileName);
			List<String> list = new ArrayList<String>();
			
            if (inputStream != null) {
                InputStreamReader isr = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(isr);
                String line;
           //     StringBuilder builder = new StringBuilder();
			
                while ((line = reader.readLine()) != null) {
                    list.add(line);
                }

                inputStream.close();
				ActionManager.sendReply(tContext, "Содержимое файла : "+list.toString());
				return list;
               // mEditText.setText(builder.toString());
            }
        } catch (Throwable t) {
          	ActionManager.sendReply(tContext,
									"Exception: " + t.toString());
			return null;
        }
		
		return null;
    }

	
	// Метод для открытия файла
    private StringBuilder openFile(String fileName) {
        try {
            FileInputStream inputStream = new FileInputStream(fileName);

            if (inputStream != null) {
                InputStreamReader isr = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(isr);
                String line;
                StringBuilder builder = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }

                inputStream.close();
				ActionManager.sendReply(tContext, "Содержимое файла : "+builder.toString());
				return builder;
				// mEditText.setText(builder.toString());
            }
        } catch (Throwable t) {
          	ActionManager.sendReply(tContext,
									"Exception: " + t.toString());
			return null;
        }

		return null;
    }
	
    // Метод для сохранения файла
    private void saveFile(String dirpath, String text) {
        try {
			
			File dir = new File(dirpath);
			if (!dir.exists())
				dir.mkdirs();
				
			File file = new File(dirpath+"/"+getFilename());
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(text.getBytes());
			fos.close();
			
			ActionManager.sendReply(tContext, "Запись в файл успешна");
			
        } catch (Throwable t) {
          	ActionManager.sendReply(tContext,
				"Exception: " + t.toString());
        }
    }
	
	private String getFilename(){
		return this.filename;
	}	
}


