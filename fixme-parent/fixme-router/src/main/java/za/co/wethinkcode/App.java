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
    	
    	// Spawn a server socket on new thread for market
    	Thread t2 = new Thread(new MarketsHandler());
    	t2.start();
    }
}

class BrokersHandler implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(BrokersHandler.class.getName());
	
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

class MarketsHandler implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(MarketsHandler.class.getName());	
	
	@Override
	public void run() {		
    	try (ServerSocket listener = new ServerSocket(5001)) {
			logger.info("Router is now accepting connections from markets");
			ExecutorService pool = Executors.newFixedThreadPool(20);
			while (true) {
				pool.execute(new MarketHandler(listener.accept()));    			
			}		
		} catch (IOException e) {
			System.out.println(e);
		}
	}	
}

class MarketHandler implements Runnable {
	
	Socket socket = null;
	String marketId = null;
	
	MarketHandler(Socket s) {	
		socket = s;
	}

	@Override
	public void run() {
		System.out.println("Market Connected: " + socket);
		
		try {		
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			Scanner in = new Scanner(socket.getInputStream());	
			// Send market its id TODO fix it
			marketId = "123456";
			out.println("123456");
			System.out.println("Allocating market ID: " + marketId);

			// Add market to active connections
			ActiveConnections.addMarket("123456", socket);
			
			while (in.hasNextLine()) {
				//Forward message to appropraite broker
				
				//Market sending brokerId back for testing
				String brokerId = in.nextLine();
				System.out.println("Market sending to router: " + brokerId);
				Socket broker = ActiveConnections.getBroker(brokerId);
				PrintWriter toBroker = new PrintWriter(broker.getOutputStream(), true);
				
				toBroker.println("Hey I'm the market, I received your message");
			}
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		} finally {
			try {				
				socket.close();
				ActiveConnections.removeMarket(marketId);
			} catch (IOException e) {}

			// Notify brokers that a market has disconnected
			System.out.println("Socket closed");
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
			
			Scanner in = new Scanner(socket.getInputStream());
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
						
			brokerId = IdGenerator.generateId(6);
			while (!ActiveConnections.idIsAvailable(brokerId)) {
				brokerId = IdGenerator.generateId(6);
			}
			
			System.out.println(socket + " issuing ID " + brokerId);
			
			ActiveConnections.addBroker(brokerId, socket);
			out.println(brokerId);
			
			//TODO: Validate broker message
			// TODO: broker message must be forwarded to market
			
			while (in.hasNextLine()) {
				String line = in.nextLine();
				
				// Find market socket, for now assume market ID is 123456
				Socket market = ActiveConnections.getMarket("123456");
				PrintWriter toMarket = new PrintWriter(market.getOutputStream(), true);
				Scanner fromMarket = new Scanner(market.getInputStream());
				
				System.out.println("Broker said: " + line);
				// Sending broker id for testing
				toMarket.println(brokerId);

				// Sending market response to broker WRONG dont send here
				//out.println(fromMarket.nextLine());
			}
			
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		} finally {
			try {				
				socket.close();
				ActiveConnections.removeBroker(brokerId);
			} catch (IOException e) { 
				// Do nothing 				
			}

			// No need to print broker socket closing
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
	
	public static Socket getMarket(String marketId) {
		return markets.get(marketId);
	}
	
	public static Socket getBroker(String brokerId) {
		return brokers.get(brokerId);
	}
	
	//Test method TODO remove
	public static String[] getAvailableBrokers() {
		return brokers.keySet().toArray(new String[0]);
	}
}