
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

// the server that can be run as a console
public class Server {
	// a id for each connection
	private static int uniqueId;
	// stores clients in a arraylist
	private ArrayList<ClientThread> al;
	// to display time
	private SimpleDateFormat date;
	// active port where clients get connected
	private int port;
	// to check if server is running
	private boolean keepGoing;
	// notification
	private String notif = " *** ";
	
	//constructor that has port number as parameter
	
	public Server(int port) {
		// the port
		this.port = port;
		// to display hh:mm:ss
		date = new SimpleDateFormat("HH:mm:ss");
		// an ArrayList to keep the list of the Client
		al = new ArrayList<ClientThread>();
	}
	
	public void start() {
		keepGoing = true;
		//create socket server and wait for connection requests 
		try 
		{
			// the socket used by the server
			ServerSocket serverSocket = new ServerSocket(port);

			// infinite loop to wait for connections ( till server is active )
			while(keepGoing) 
			{
				display("Clients can get connected on  " + port + ".");
				
				// accept connection if requested from client
				Socket socket = serverSocket.accept();
				// break if server stoped
				if(!keepGoing)
					break;
				// if client is connected, create its thread
				ClientThread t = new ClientThread(socket);
				//add this client to arraylist
				al.add(t);
				
				t.start();
			}
			// try to stop the server
			try {
				serverSocket.close();
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					// close all data streams and socket
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
					}
					catch(IOException ioE) {
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		catch (IOException e) {
            String msg = date.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}
	
// to stop the server
        
	protected void stop(){

		keepGoing = false;
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
		}
	}
	
	
	// Display an event to the console
	private void display(String msg) {
		String time = date.format(new Date()) + " " + msg;
		System.out.println(time);
	}
	
	// to broadcast a message to all Clients
	private synchronized boolean broadcast(String message) {
		// add timestamp to the message
		String time = date.format(new Date());
		
		
		// if message is a broadcast message
		
		
			String messageLf = time + " " + message + "\n";
			// display message
			System.out.print(messageLf);
			
			// we loop in reverse order in case we would have to remove a Client
			// because it has disconnected
			for(int i = al.size(); --i >= 0;) {
				ClientThread ct = al.get(i);
				// try to write to the Client if it fails remove it from the list
				if(!ct.writeMsg(messageLf)) {
					al.remove(i);
					display("Disconnected Client " + ct.username + " removed from list.");
				}
			}
		
		return true;
		
		
	}

	// if client sent LEAVE message to exit
	synchronized  void remove(int id) {
		
		String disconnectedClient = "";
		// scan the array list until we found the client Id
		for(int i = 0; i < al.size(); ++i) {
			ClientThread ct = al.get(i);
			// if found remove it
			if(ct.id == id) {
				disconnectedClient = ct.getUsername();
				al.remove(i);
				break;
			}
		}
		broadcast(notif + disconnectedClient + " has left the chat room." + notif);
	}   
	






	/*
	 *  To run as a console application
	 * > java Server
	 * > java Server portNumber
	 * If the port number is not specified 1500 is used
	 */ 
	public static void main(String[] args) {
		// start server on port 1500 unless a PortNumber is specified 
		int portNumber = 1500;
	System.out.println("Server is Active , waiting for Clients");
		switch(args.length) {
			case 1:
				try {
					portNumber = Integer.parseInt(args[0]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					return;
				}
			case 0:
				break;
			default:
				System.out.println("Usage is: > java Server [portNumber]");
				return;
				
		}
		// create a server object and start it
		Server server = new Server(portNumber);
		server.start();
	}

	// each client gets only one instance of thread
	class ClientThread extends Thread {
		// create a socket to get messages from client
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		int id;
		// the Username of the Client
		String username;
		// chat message object to receive message and its type
		ChatMessage cm;
		// timestamp
		String date;

		// Constructor
		ClientThread(Socket socket) {
			
			id = ++uniqueId;
			this.socket = socket;
			//Creating 2 way Data Stream
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				// read the username
				username = (String) sInput.readObject();
				broadcast(notif + username + " has joined the chat room." + notif);
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}
		
		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		// infinite loop to read and forward message
		public void run() {
			// to loop until LEAVE
			boolean keepGoing = true;
			while(keepGoing) {
				// read a String 
				try {
					cm = (ChatMessage) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				
				String message = cm.getMessage();

				// uses different cases to perform action based of client input
				switch(cm.getType()) {

				case ChatMessage.MESSAGE:
					boolean confirmation =  broadcast(username + ": " + message);
					if(confirmation==false){
						String msg = notif + "User has not joined yet." + notif;
						writeMsg(msg);
					}
					break;
				case ChatMessage.LEAVE:
					display(username + " disconnected with a LEAVE message.");
                                        remove(id);
					break;
				case ChatMessage.SHUTDOWN:
					display("All Clients Left!");
                                       clientClose();
					break;
				case ChatMessage.SHUTDOWNALL:
					display("server inactive. No clients connected!\n Try again when server is active!");
                                       serverClose();                                       
                                                        break;                   
                                                             
                                                
				}
 
                            
			}
			// if out of the loop then disconnected and remove from client list
			 
		}
		


		// close everything
		private void close() {
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

		// write a String to the Client output stream
		private boolean writeMsg(String msg) {
			// send messages to connected clients
			if(!socket.isConnected()) {
				close();
				return false;
			}
			// write the message to the stream
			try {
				sOutput.writeObject(msg);
			}
			// handles errors by sending information to user rather than aborting
			catch(IOException e) {
				display(notif + "Error sending message to " + username + notif);
				display(e.toString());
			}
			return true;
		}
        //closes server connection

        private void serverClose() {
            keepGoing = false;
		try {
			new Socket("localhost", port);
		}
		catch(Exception e) {
		}


 }
        //closes client connection
       private void clientClose() {
      
				for(int i = 0; i < al.size(); ++i) {
					ClientThread tc = al.get(i);
					try {
					// close all data streams and socket
					tc.sInput.close();
					tc.sOutput.close();
					
					}
					catch(IOException ioE) {
					}
				}

 }
}
}

