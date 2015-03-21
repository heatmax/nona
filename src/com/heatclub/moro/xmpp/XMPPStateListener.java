package com.heatclub.moro.xmpp;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.Chat;

import android.util.Log;



public class XMPPStateListener implements ConnectionListener, PacketListener, MessageListener{
	private XMPPCore provider;
	
	public void setConnection(XMPPCore provider){
		this.provider = provider;
		PacketFilter filter = new AndFilter(new PacketTypeFilter(Message.class));
		this.provider.addPacketListener(this, filter);			
		this.provider.addConnectionListener(this);
		
	}

	@Override
	public void processMessage(Chat p1, Message p2)
	{
		// TODO: Implement this method
	//	System.exit(0);
	}	
	
	@Override
	public void processPacket(Packet packet) 
	{		
		
		if (packet instanceof Message) 
		{
			Message message = (Message) packet;
			if(message.getBody() != null){
				try{
					/*
					String msg;
					msg = "Echo: " + message.getBody();// + "' on thread: " + getThreadSignature();
					provider.sendMessage(msg, message.getFrom());
					*/
				}
				catch(Exception e){

				}
			}
		
		}
		
	}	

	
	@Override
	public void connected(XMPPConnection p1)
	{
		provider.superLogin();
	}

	@Override
	public void authenticated(XMPPConnection p1)
	{
		provider.processSendingOn();
//		String msg = "authenticated";// + "' on thread: " + getThreadSignature();
//		provider.sendMessage(msg, "frauder_admin@jabber.ru");
		
	}

	@Override
	public void reconnectionSuccessful() {
		//	logger.info("Successfully reconnected to the XMPP server.");
	}

	@Override
	public void reconnectionFailed(Exception arg0) {
		//	logger.info("Failed to reconnect to the XMPP server.");
	}

	@Override
	public void reconnectingIn(int seconds) {
		//	logger.info("Reconnecting in " + seconds + " seconds.");
	}

	@Override
	public void connectionClosedOnError(Exception arg0) {
		Log.d("moro", "Connection to XMPP server was lost.");
//		System.exit(0);
		
	}

	@Override
	public void connectionClosed() {
		provider.processSendingOff();
		Log.d("moro", "XMPP connection was closed.");
	//	System.exit(0);
		
	}
	
}
