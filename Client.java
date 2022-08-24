
import java.net.*;
import java.io.*;
import java.util.*;



//The Client that can be run as a console
public class Client  {
	
	// notification
	private String notif = " *** ";

	// for I/O
	private ObjectInputStream sInput;		// to read from the socket
	private ObjectOutputStream sOutput;		// to write on the socket
	private Socket socket;					// socket object
	
	private String server, username;	// server and username
	private int port;					//port

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/*
	 *  Constructor to set below things
	 *  server: the server address
	 *  port: the port number
	 *  username: the username
	 */
	
	Client(String server, int port, String username) {
		this.server = server;
		this.port = port;
		this.username = username;
	}
	
	/*
	 * To start the chat
	 */
	public boolean start() {
		// try to connect to the server
		try {
			socket = new Socket(server, port);
		} 
		// exception handler if it failed
		catch(Exception ec) {
			display("Error connectiong to server:" + ec);
			return false;
		}
		
		String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
		display(msg);
	
		/* Creating 2 way Data Stream */
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}

		// this thread is used to listen from server 
		new ListenFromServer().start();
		try
		{
			sOutput.writeObject(username);
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		// client is informed if successful
		return true;
	}

	/*
	 * send a message to the console
	 */
	private void display(String msg) {

		System.out.println(msg);
		
	}
	
	/*
	 * server receieves a messsage
	 */
	void sendMessage(ChatMessage msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

	/*
	 * Close the Input/Output streams and disconnect
	 */
	private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {}
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {}
        try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {}
			
	}
	/*the client types the username in console*/
	public static void main(String[] args) {
		// default values if not entered
		int portNumber = 1500;
		String serverAddress = "localhost";
		String userName = " ";
		Scanner scan = new Scanner(System.in);
		
		System.out.println("Enter the username: ");
		userName = scan.nextLine();

		// different case according to the length of the arguments.
		switch(args.length) {
			case 3:
				// for > javac Client username portNumber serverAddr
				serverAddress = args[2];
			case 2:
				// for > javac Client username portNumber
				try {
					portNumber = Integer.parseInt(args[1]);
				}
				catch(Exception e) {
					System.out.println("Invalid port number.");
					System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
					return;
				}
			case 1: 
				// for > javac Client username
				userName = args[0];
			case 0:
				// for > java Client
				break;
			// if number of arguments are invalid
			default:
				System.out.println("Usage is: > java Client [username] [portNumber] [serverAddress]");
			return;
		}
		// create the Client object
		Client client = new Client(serverAddress, portNumber, userName);
		// try to connect to the server and return if not connected
		if(!client.start())
			return;
		
		System.out.println("\nHello.! WELCOME !! you are connected!! .\n Start Messaging");
		
		// loops runs infinite times to get user input
		while(true) {
			System.out.print("> ");
			// read message from user
			String msg = scan.nextLine();
			// exit if message is LEAVE
			if(msg.equalsIgnoreCase("LEAVE")) {
				client.sendMessage(new ChatMessage(ChatMessage.LEAVE, ""));
				break;
			}
			// join the chatroom
			else if(msg.equalsIgnoreCase("JOIN")) {
				client.sendMessage(new ChatMessage(ChatMessage.JOIN, ""));				
			}
                       //all the clients connected to the server get disconnected
			else if(msg.equalsIgnoreCase("SHUTDOWN")) {
				client.sendMessage(new ChatMessage(ChatMessage.SHUTDOWN, ""));				

			}
                        //server closes itself and all the clients connected loose connection
			else if(msg.equalsIgnoreCase("SHUTDOWNALL")) {
				client.sendMessage(new ChatMessage(ChatMessage.SHUTDOWNALL, ""));				

			}
			// simple text message
			else {
				client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
			}
		}
		// close resource
		scan.close();
		// client completed its job. disconnect client.
		client.disconnect();	
	}

	/*
	 * listens to any incoming messages from server
	 */
	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					// read the message form the input datastream
					String msg = (String) sInput.readObject();
					// print the message
					System.out.println(msg);
					System.out.print("> ");
				}
				catch(IOException e) {
					display(notif + "Server has closed the connection: " + e + notif);
					break;
				}
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
}

