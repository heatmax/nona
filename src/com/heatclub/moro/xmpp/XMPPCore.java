package com.heatclub.moro.xmpp;

import com.heatclub.moro.xmpp.XMPPMessageSender;
import java.io.IOException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.ConnectionConfiguration;
import android.util.Log;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;

import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.*;



public class XMPPCore extends XMPPTCPConnection
{
	private String username;
	private String password;
	
	public boolean isConnects = false;
	private final String TAG = "moro";
	private XMPPStateListener listener;
	private XMPPMessageSender sender;
	private Thread connectThread;
	
	private Runnable rConnect = new Runnable(){

		@Override
		public void run(){
			while(!isConnected()){
			try {
				isConnects = true;					
				superConnect();
				Log.d(TAG, "XMPP CONNECT SUCCESSFUL");
				
				//	superLogin();
				//	sender.start();

				isConnects = false;	
			}
			catch(Exception e){
				isConnects = false;	
				Log.d(TAG, "XMPP CONNECT ERROR");
				
				try{
					Log.d(TAG, "Try reconnect...");					
					Thread.sleep(10000);
				}
				catch(Exception se){
					
				}
				//	System.exit(0);
			}
		}
		}
	};	
	
	XMPPCore(ConnectionConfiguration config){
		super(config);
		init();
	}
	
	XMPPCore(final String domain, final int port, final String server){		
		super(new ConnectionConfiguration(domain, port, server));
		init();		
	}
	
	protected void init(){
	//	this.executor = Executors.newFixedThreadPool(1);
//		this.processSendigThread = new Thread();
		
		SASLAuthentication.supportSASLMechanism("PLAIN", 0);
		setListener(new XMPPStateListener());
		sender = new XMPPMessageSender(this);
		connectThread = new Thread(rConnect);
	
//		addPacketListener(packetListener, filter);			
//		addConnectionListener(connectionListener);
		
	}
/*	
	public void setConnectionListener(ConnectionListener listener){
		
		addConnectionListener(listener);
		
	//	listener = new XMPPStateListener(this);
		
	}
*/
	
	protected void superConnect() throws SmackException, IOException, XMPPException
	{
		// TODO: Implement this method
		super.connect();
	}

	
	protected void superLogin(){
		try{
			super.login(this.username, this.password);
		}
		catch(XMPPException e){
			
		}
		catch(SmackException e){
			
		}
		catch(IOException e){
			
		}
	}
	
	public void reconnect(){
		connectThread.start();
		
	}
	
	public void connect(final String username, final String password)
	{
		this.username = username;
		this.password = password;
		
		connectThread.start();
		
	}

	@Override
	public void disconnect()
	{
		try{
			super.disconnect();
		}
		catch(SmackException.NotConnectedException e){
			Log.d(TAG, "DISCONNECT ERROR: "+e.getMessage());		
		}
	}
	
	
	protected void processSendingOn(){
				sender.start();
	}
	protected void processSendingOff(){
		sender.stop();
	}
	
	
	public void sendMessage(Message msg){
			sender.send(msg);		
	}
	
	public void sendMessage(final String msg, final String recipient){

			sender.send(msg, recipient);		
		
	}

	
	public void addPacketListener(PacketListener p1)
	{
		PacketFilter p2 = new AndFilter(new PacketTypeFilter(Message.class));
		super.addPacketListener(p1, p2);
	}

	public void setListener(XMPPStateListener listener){
		this.listener = listener;
		listener.setConnection(this);
	}
	
	public XMPPStateListener getListener(){
		return this.listener;
	}

}
