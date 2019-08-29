package za.co.wethinkcode;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class App 
{
    public static void main( String[] args ) throws UnknownHostException, IOException {
    	
    	Socket s = new Socket("127.0.0.1", 5001);
    	Scanner in = new Scanner(s.getInputStream());
    	PrintWriter out = new PrintWriter(s.getOutputStream(), true);
    	
    	// Should be 123456
    	System.out.println("Received ID: " + in.nextLine());
    	
    	// this will only process one broker at a time
    	while (in.hasNextLine()) {
    		String brokerId = in.nextLine();
    		System.out.println("Received from router: " + brokerId);
    		
    		out.println(brokerId);
    		System.out.println("Send the message back");
    	}
    }
}
