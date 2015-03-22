package com.heatclub.moro.action;
import android.animation.*;

public class Morom{
	public static enum Scheme{
		TEL("tel"),
		USSD("ussd"),
		XMPP("xmpp"),
		SMS("sms"),
		ACTIVITY("activity"),
		REPLY("reply");
		
		private String val;
		private String slashes = "://";

		Scheme(String v){
			this.val = v;
		}

		@Override
		public String toString()
		{
			return this.val;
		};
		
		public String addSlash()
		{
			return this.val+slashes;
		};

		public static Scheme getFromString(String sch){
			for(Scheme cm : values()){
				if(cm.toString().equals(sch)){
					return cm;
				}
			}
			return null;
			//throw new IllegalArgumentException("Действие с таким псевдонимом не существует");
		}

	}
	
	
	public static enum Command{
		CALL("call"),
		END_CALL("endcall"),
		CONNECT("connect"),
		DISCONNECT("disconnect"),
		CONFIG("config"),
		SEND("send");

		private String val;
		
		Command(String v){
			this.val = v;
		}
		
		@Override
		public String toString()
		{
			return this.val;
		};
		
		public static Command getFromString(String com){
			for(Command cm : values()){
				if(cm.toString().equals(com)){
					return cm;
				}
			}
			return null;
			//throw new IllegalArgumentException("Действие с таким псевдонимом не существует");
		}
		
	}

	public static enum Action
	{	
		CALL(Morom.Command.CALL, "com.heatclub.moro.action.CALL"),
		CONNECT(Morom.Command.CONNECT, "com.heatclub.moro.action.CONNECT"),
		DISCONNECT(Morom.Command.DISCONNECT, "com.heatclub.moro.action.DISCONNECT"),
		END_CALL(Morom.Command.END_CALL, "com.heatclub.moro.action.END_CALL"),
		SEND(Morom.Command.SEND, "com.heatclub.moro.action.SEND"),
		CONFIG(Morom.Command.CONFIG, "com.heatclub.moro.action.CONFIG");
	
	
		private String action;
		private Command command;
	
		Action(Morom.Command com, String act){
			this.action = act;	
			this.command = com;
		}	
	
		public Command getCommand() {return this.command;}
	
		@Override
		public String toString() {return this.action;}
	
		public static Action getFromCommand(Command com){
			for(Action ma : values()){
				if(ma.getCommand().equals(com)){
					return ma;
				}
			}
			return null;
		//throw new IllegalArgumentException("Действие с таким псевдонимом не существует");
		}
	
		public static Action getFromString(String action){
			for(Action ma : values()){
				if(ma.toString().equals(action)){
					return ma;
				}
			}
			return null;
			//throw new IllegalArgumentException("Действие с таким псевдонимом не существует");
		}
	}
}

