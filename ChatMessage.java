
import java.io.*;


public class ChatMessage implements Serializable {
        //JOIN,LEAVE,SHUTDOWN,SHUTDOWNALL are the different message types

	// MESSAGE an ordinary text message
	// LEAVE to disconnect from the Server
        //SHUTDOWN all the clients get disconnected from server
       //SHUTDOWNALL all the clients get disconnected from server and also the server closes its active connection
	static final int JOIN = 0, MESSAGE = 1, LEAVE = 2,SHUTDOWN=3,SHUTDOWNALL=4;
	private int type;
	private String message;
	
	// constructor
	ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}
	
	int getType() {
		return type;
	}

	String getMessage() {
		return message;
	}
}
