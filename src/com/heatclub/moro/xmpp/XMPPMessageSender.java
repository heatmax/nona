package com.heatclub.moro.xmpp;

import java.util.LinkedList;
import java.util.List;
import android.util.Log;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.packet.Message;


public class XMPPMessageSender{
	
	private List queue = new LinkedList();
	private final static String TAG = "moro";
	private Thread sendingThread;
	private Chat chat;
	private XMPPCore connection;
	
	Runnable rSendProcess = new Runnable(){

		@Override
		public void run(){
	//		String msg;

			try{
				while(true){
				//	take();
					sendMessage((Message)take());				
				}
			}
			catch(InterruptedException e){
				Log.d(TAG, "Send Message Error ERROR: "+e.getMessage());
			}
			catch(Exception e){
				Log.d(TAG, "Send Message Exception: "+e.getMessage());
			}
			
		}
	};
	
	
	public XMPPMessageSender(XMPPCore connection){
		this.connection = connection;
		sendingThread = new Thread(rSendProcess);	
	}

	protected void sendMessage(final Message msg){
		try{
			if(chat == null){
				chat = ChatManager.getInstanceFor(connection).createChat(msg.getTo(), connection.getListener());
			}
			else{
				if(!chat.getParticipant().equals(msg.getTo()))
					chat = ChatManager.getInstanceFor(connection).createChat(msg.getTo(), connection.getListener());					
			}	
			Log.d(TAG, "SEND MESSAGE");
			
			chat.sendMessage(msg);
		}
		catch(Exception e){
			Log.d(TAG, "ERROR SEND MESSAGE: "+e.getMessage());
		}
	}
	
	private Message createMessage(final String body, final String recipient){
		Message msg = new Message();
		msg.setBody(body);
		msg.setFrom(connection.getUser());
	//	msg.setSubject("тестовое сообщение");
		msg.setTo(recipient);
		
		msg.setType(Message.Type.chat);
		return msg;
	}
	
	public void send(String body, String recipient){
		put(createMessage(body, recipient));
	}
	
	public void send(Message msg){
		put(msg);
	}
	
	
    private synchronized void put(Object item){				
		try{
			Log.d(TAG, "try to put: " + item);
			this.queue.add(item);		
			
			if(sendingThread.getState() == Thread.State.WAITING)
				notifyAll();		
			Log.d(TAG, "put ok: " + item );
		}
		catch(Exception e){
			Log.d(TAG, "Error in take:"+e.getMessage());
		}
		
    }


    private synchronized Object take() throws InterruptedException{
		Log.d(TAG, "try to take");
        while (this.queue.size() == 0){
			Log.d(TAG, "queue is empty, waiting until smth is put");
            wait();
        }
       // notifyAll();
    
		Object item = this.queue.remove(0);
		Log.d(TAG, "take ok: " + item );
        return item;
    }
	
	public void start(){
		Log.d(TAG, "try to start");
		
		if(sendingThread != null)	
			if(!sendingThread.isAlive()){	
				Log.d(TAG, "Process run");			
				sendingThread.start();					
			}
		
	}
	
	public void stop(){
		Log.d(TAG, "Process stop");			
		
		if(sendingThread != null)				
			if(sendingThread.isAlive())			
				sendingThread.interrupt();					
		//		sendingThread.stop();					
	}
}
