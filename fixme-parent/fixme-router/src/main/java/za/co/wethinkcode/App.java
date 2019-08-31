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

import za.co.wethinkcode.exceptions.FixMessageException;
import za.co.wethinkcode.helpers.FixMessageValidator;
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
	
	private Socket socket;
	private String marketId;
	
	MarketHandler(Socket s) {	
		this.socket = s;
	}

	@Override
	public void run() {

		System.out.println("Market Connected: " + this.socket);
		
		try {

			Scanner fromMarket = new Scanner(this.socket.getInputStream());
			PrintWriter toMarket = new PrintWriter(this.socket.getOutputStream(), true);

      this.marketId = IdGenerator.generateId(6);
			while (!ActiveConnections.idIsAvailable(this.marketId)) {
				this.marketId = IdGenerator.generateId(6);
			}

			ActiveConnections.addMarket(this.marketId, this.socket);
			toMarket.println(this.marketId);
			
			while (fromMarket.hasNextLine()) {

			  String line = fromMarket.nextLine();

        // TODO: Remove this fakeExecutionReport and replace with the real thing
        //       once a proper FIX encoded executionReport is sent from the market.

        FixMessage fakeExecutionReport = new FixMessage(
          "6Q8D7Z",
          "1D5P8X",
          "2",
          "XAU",
          "2",
          "100",
          "100",
          "25.9"
        );

        line = fakeExecutionReport.toString();

        ExecutionReportDecoded decodedExecutionReport = new ExecutionReportDecoded(line);

        FixMessageValidator.validateCheckSum(
          decodedExecutionReport.getMessageWithoutChecksum(),
          decodedExecutionReport.getChecksum()
        );

        String brokerID = decodedExecutionReport.getTargetID();
        Socket broker = ActiveConnections.getBroker(brokerID);

        // TODO: This will not work as targetID is hard coded. Replace with real.
        PrintWriter toBroker = new PrintWriter(broker.getOutputStream(), true);

        // Forwarding message to related broker.
				toBroker.println(decodedExecutionReport.toString());
			}

		} catch (IOException e) {
      e.printStackTrace(System.out);
    } catch (FixMessageException e) {
		  e.printStackTrace(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		} finally {
			try {				
				this.socket.close();
				ActiveConnections.removeMarket(this.marketId);
			} catch (IOException e) {
			  e.printStackTrace(System.out);
      }
		}	
	}
}


class BrokerHandler implements Runnable {
	
	private Socket socket;
	private String brokerId;
	
	BrokerHandler(Socket s) {	
		this.socket = s;
	}

	@Override
	public void run() {

		System.out.println("Broker Connected: " + this.socket);
		
		try {
			
			Scanner fromBroker = new Scanner(this.socket.getInputStream());
			PrintWriter toBroker = new PrintWriter(this.socket.getOutputStream(), true);
						
			this.brokerId = IdGenerator.generateId(6);
			while (!ActiveConnections.idIsAvailable(this.brokerId)) {
				this.brokerId = IdGenerator.generateId(6);
			}
			
			ActiveConnections.addBroker(this.brokerId, this.socket);
			toBroker.println(this.brokerId); // Gives broker it's assigned ID.
			
			while (fromBroker.hasNextLine()) {

				String line = fromBroker.nextLine();

				// TODO: Remove this fakeBuyMessage and replace with the real thing once
        //       a proper FIX encoded buy or sell message is sent from the broker.

        FixMessage fakeBuyMessage = new FixMessage(
          "1D5P8X",
          "6Q8D7Z",
          "XAU",
          "1",
          "100"
        );

        line = fakeBuyMessage.toString();

        SingleOrderDecoded decodedBrokerMessage = new SingleOrderDecoded(line);

        // Validating FIX message checksum.
        FixMessageValidator.validateCheckSum(
          decodedBrokerMessage.getMessageWithoutChecksum(),
          decodedBrokerMessage.getChecksum()
        );

        String marketID = decodedBrokerMessage.getTargetID();
				Socket market = ActiveConnections.getMarket(marketID);

        // TODO: This will not work as targetID is hard coded. Replace with real.
				PrintWriter toMarket = new PrintWriter(market.getOutputStream(), true);

				// Forwarding message to related market.
				toMarket.println(decodedBrokerMessage.toString());
			}

		} catch (IOException e) {
			e.printStackTrace(System.out);
		} catch (FixMessageException e) {
		  e.printStackTrace(System.out);
    } catch (Exception e) {
		  e.printStackTrace(System.out);
		} finally {
			try {				
				this.socket.close();
				ActiveConnections.removeBroker(this.brokerId);
			} catch (IOException e) {
			  e.printStackTrace(System.out);
			}
		}	
	}
}

abstract class ActiveConnections {

	private static HashMap<String, Socket> brokers = new HashMap<>();
	private static HashMap<String, Socket> markets = new HashMap<>();
	
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