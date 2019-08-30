package za.co.wethinkcode.broker;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Broker {
	
	// TODO maybe maintain one instance of the Scanner and PrintWriter
	
	private String brokerId;
    private final int PORT = 5000;
    private final String HOST = "127.0.0.1";
    Socket socket;
    
    public Broker() throws UnknownHostException, IOException {
    	socket = new Socket(HOST, PORT);
    	System.out.println("Connection to router has been established");
    	brokerId = processResponse();
    	System.out.println("Allocated ID: " + brokerId);    	
    }

    public void sendMessage(Transaction t) throws IOException {
    	PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
    	out.println("Yo watsup");
    }

    public String processResponse() throws IOException {
    	Scanner in = new Scanner(socket.getInputStream());
    	return in.nextLine();
    }
    
}