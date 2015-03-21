package com.heatclub.moro.util;

import java.util.LinkedList;
import java.util.List;
import android.util.Log;

public class BlockingQueue
{
	private List queue = new LinkedList();
    private int  limit = 10;
	
	private final static String TAG = "myQUEUE";

    public BlockingQueue(int limit){
        this.limit = limit;
    }


    public synchronized void put(Object item) throws InterruptedException  {
        Log.d(TAG, " try to put: " + item);
		System.out.println("ok");
		
        while (this.queue.size() == this.limit) {
			Log.d(TAG, " queue is full, waiting until space is free");
            wait();
        }
        if (this.queue.size() == 0) {
			Log.d(TAG, "queue is empty, notify");
            notifyAll();
        }
        this.queue.add(item);
		Log.d(TAG, "put ok: " + item );
    }


    public synchronized Object take() throws InterruptedException{
		Log.d(TAG, " try to take");
        while (this.queue.size() == 0){
			Log.d(TAG, " queue is empty, waiting until smth is put");
            wait();
        }
        if (this.queue.size() == this.limit){
			Log.d(TAG, " queue is full, notify");
            notifyAll();
        }

        Object item = this.queue.remove(0);
		Log.d(TAG, " take ok: " + item );
        return item;
    }
}
