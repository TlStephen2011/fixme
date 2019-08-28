package za.co.wethinkcode;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class App 
{
    public static void main( String[] args ) throws Exception {
    	try (Socket s = new Socket("127.0.0.1", 5000)) {
    		System.out.println("Enter lines to capitalize: Ctrl+D to quit");
    		
    		Scanner scanner = new Scanner(System.in);
    		Scanner in = new Scanner(s.getInputStream());
    		PrintWriter out = new PrintWriter(s.getOutputStream(), true);
    		
    		while (scanner.hasNextLine()) {
    			out.println(scanner.nextLine());
    			System.out.println("Server said: " + in.nextLine());
    		}
    		
    	} catch (IOException e) {
    		System.out.println("Error in client: " + e.getMessage());
    	}
    }
}
