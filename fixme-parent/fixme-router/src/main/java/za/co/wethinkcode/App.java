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

import za.co.wethinkcode.helpers.IdGenerator;

public class App 
{
    private static final Logger logger = LogManager.getLogger(App.class.getName());
    
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
	String brokerId = null;
	
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
						
			brokerId = IdGenerator.generateId(6);
			while (!ActiveConnections.idIsAvailable(brokerId)) {
				brokerId = IdGenerator.generateId(6);
			}
			
			System.out.println(socket + " issuing ID " + brokerId);
			
			ActiveConnections.addBroker(brokerId, socket);
			out.println(brokerId);
			
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
				ActiveConnections.removeBroker(brokerId);
			} catch (IOException e) {}

			System.out.println("Socket closed");
		}	
	}
}

abstract class ActiveConnections {
	private static HashMap<String, Socket> brokers = new HashMap<String, Socket>();
	private static HashMap<String, Socket> markets = new HashMap<String, Socket>();
	
	public static boolean idIsAvailable(String id) {		
		if (brokers.get(id) == null && markets.get(id) == null) {
			return true;
		}
		return false;
	}
	
	public static synchronized void addMarket(String marketId, Socket marketSocket) {
		markets.put(marketId, marketSocket);
	}
	
	public static synchronized void addBroker(String brokerId, Socket brokerSocket) {
		brokers.put(brokerId, brokerSocket);
	}
	
	public static synchronized void removeBroker(String brokerId) {
		brokers.remove(brokerId);
	}
	
	public static synchronized void removeMarket(String marketId) {
		markets.remove(marketId);
	}
	
	public static String[] getAvailableMarkets() {
		return markets.keySet().toArray(new String[0]);
	}
	
	//Test method TODO remove
	public static String[] getAvailableBrokers() {
		return brokers.keySet().toArray(new String[0]);
	}
}