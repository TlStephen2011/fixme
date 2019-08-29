package za.co.wethinkcode;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App 
{
    private static final Logger logger = LogManager.getLogger(App.class.getName());
    private static HashMap<String, Socket> brokersConnected = new HashMap<String, Socket>();
    private static HashMap<String, Socket> marketsConnected = new HashMap<String, Socket>();
    
    public static void main( String[] args )
    {	
    	// Spawn a server socket on a new thread for broker
    	Thread t1 = new Thread(new BrokersHandler());
    	t1.start();
    }
}

class BrokersHandler implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(BrokersHandler.class.getName());
	
	BrokersHandler() {}
	
	@Override
	public void run() {		
    	try (ServerSocket listener = new ServerSocket(5000)) {
			logger.info("Router is now accepting connections from brokers");
			ExecutorService pool = Executors.newFixedThreadPool(20);
			while (true) {
				pool.execute(new BrokerHandler(listener.accept()));    			
			}		
		} catch (IOException e) {
			System.out.println(e);
		}
	}	
}

class BrokerHandler implements Runnable {
	
	Socket socket = null;
	
	BrokerHandler(Socket s) {
		socket = s;
	}

	@Override
	public void run() {
		System.out.println("Broker Connected: " + socket);
		
		try {
			@SuppressWarnings("resource")
			Scanner in = new Scanner(socket.getInputStream());
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			while (in.hasNextLine()) {
				String line = in.nextLine();
				System.out.println("Broker said: " + line);
				out.println("Cool man");
			}
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException e) {}

			System.out.println("Socket closed");
		}	
	}
}