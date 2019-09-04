package za.co.wethinkcode;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import za.co.wethinkcode.exceptions.FixMessageException;
import za.co.wethinkcode.helpers.*;

// Protocol Example:
//
// 1.  Router starts up.
// 2.  Market connects.
// 3.  Router gives market its unique ID.
// 4.  Market gives router the traded list of instruments, router saves that list to
//     HashMap.
// 5.  Market is ready to receive FIX messages.
// 6.  Router attempts to notify connected brokers by sending market HashMap to each connected broker.
// 7.  Broker connects.
// 8.  Router gives broker its unique ID.
// 9.  Router gives broker market HashMap.
// 10. Broker is ready to send FIX messages or receive FIX messages.
// 11. Another market connects.
// 12. Router gives market its unique ID.
// 13. Market gives router the traded list of instruments, router saves that list to
//     HashMap.
// 14. Market is ready to receive FIX messages.
// 15. Router attempts to notify connected brokers by sending market HashMap to each connected broker.
// 16. Broker receives market HashMap.
// 17. Broker sends FIX buy message using market HashMap.
// 18. Router checks FIX buy message checksum.
// 19. Router forwards FIX buy message to related market using market / targetID.
// 20. Market executes FIX buy message.
// 21. Market returns FIX execution report.
// 22. Router forwards FIX execution report to related broker using broker / target ID.
// 23. A market disconnects.
// 24. Router removes market from market HashMap.
// 25. Router attempts to notify connected brokers by sending market HashMap to each connected broker.

// Types of messages that router should handle:
//
// 1.  market list of instruments (EncodedBroadcast upon market connection).
// 2.  FIX messages (checksum validation).

// Types of messages that market should handle:
//
// 1.  Assigned ID.
// 2.  FIX buy / sell message (from broker through router).

// Types of messages that broker should handle:
//
// 1.  Assigned ID.
// 2.  FIX execution report (from market through router).
// 3.  market HashMap (from router).

// Caveats:
//
// 1.  The market should mutate market state synchronously.
// 2.  The broker should receive messages synchronously, otherwise a market might
//     be replying to that broker, i.e. execution report, and at the same time,
//     another market disconnected, forcing the router to notify the brokers by sending
//     the market HashMap, so the broker ends up receiving two messages.

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
	private String encodedBroadcast;
	
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

			// Getting connected market instrument list, this should block.
      if (fromMarket.hasNextLine()) {
        this.encodedBroadcast = fromMarket.nextLine();
      }

      // TODO: Testing if hasNextLine() blocks, remove this.
      if (this.encodedBroadcast.equals(""))
        throw new Exception("You done messed up boy.");

      // Adding market instrument list to marketHashMap.
      ActiveConnections.addEncodedBroadcast(this.encodedBroadcast);

      // Notifying brokers that a new market is available.
      ActiveConnections.notifyBrokersOpenMarket(this.encodedBroadcast);

      // Routes FIX execution reports to related brokers.
			while (fromMarket.hasNextLine()) {

			  String line = fromMarket.nextLine();

        ExecutionReportDecoded decodedExecutionReport = new ExecutionReportDecoded(line);

        FixMessageValidator.validateCheckSum(
          decodedExecutionReport.getMessageWithoutChecksum(),
          decodedExecutionReport.getChecksum()
        );

        String brokerID = decodedExecutionReport.getTargetID();
        Socket broker = ActiveConnections.getBroker(brokerID);

        PrintWriter toBroker = new PrintWriter(broker.getOutputStream(), true);

        // Forwarding message to related broker.
				toBroker.println(line);
			}
		} catch (IOException e) {
      e.printStackTrace(System.out);
    } catch (FixMessageException e) {
		  e.printStackTrace(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		} finally {
			try {
			  ActiveConnections.removeEncodedBroadcast(this.encodedBroadcast);
			  ActiveConnections.notifyBrokersClosedMarket(this.encodedBroadcast);
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

      // Sending broker the current traded market instruments.
      ActiveConnections.notifyBrokerMarketInstruments(this.socket);
			
			while (fromBroker.hasNextLine()) {

				String line = fromBroker.nextLine();

        SingleOrderDecoded decodedBrokerMessage = new SingleOrderDecoded(line);

        // Validating FIX message checksum.
        FixMessageValidator.validateCheckSum(
          decodedBrokerMessage.getMessageWithoutChecksum(),
          decodedBrokerMessage.getChecksum()
        );

        String marketID = decodedBrokerMessage.getTargetID();
				Socket market = ActiveConnections.getMarket(marketID);

				PrintWriter toMarket = new PrintWriter(market.getOutputStream(), true);

				// Forwarding message to related market.
				toMarket.println(line);
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
	private static HashMap<String, String> marketInstruments = new HashMap<>();
	
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

	public static synchronized void addEncodedBroadcast(String encodedMarketBroadcast) {

	  marketInstruments.put(
      BroadcastDecoder.getMarketId(encodedMarketBroadcast),
      encodedMarketBroadcast
    );
  }
	
	public static synchronized void removeBroker(String brokerId) {
		brokers.remove(brokerId);
	}
	
	public static synchronized void removeMarket(String marketId) {
		markets.remove(marketId);
	}

	public static synchronized void removeEncodedBroadcast(String encodedBroadcast) {

	  String marketId = BroadcastDecoder.getMarketId(encodedBroadcast);
	  marketInstruments.remove(marketId);
  }
	
	public static Socket getMarket(String marketId) {
		return markets.get(marketId);
	}
	
	public static Socket getBroker(String brokerId) {
		return brokers.get(brokerId);
	}

	// TODO: Test.
	// Broker gets notified when a market disconnects.
	public static void notifyBrokersClosedMarket(String encodedBroadcast) {

	  String marketId = BroadcastDecoder.getMarketId(encodedBroadcast);
	  String closedMarket = BroadcastEncoder.encode(marketId, null, false);

	  brokers.forEach((brokerId, socket) -> {

	    try {

        PrintWriter toBroker = new PrintWriter(socket.getOutputStream(), true);
        toBroker.println(closedMarket);
      }
	    catch (IOException e) {
	      e.printStackTrace(System.out);
      }
	    catch (Exception e) {
	      e.printStackTrace(System.out);
      }
    });
  }

  // TODO: Test.
  // Broker gets notified when a market connects.
	public static void notifyBrokersOpenMarket(String encodedBroadcast) {

	  brokers.forEach((brokerId, socket) -> {

	    try {

        PrintWriter toBroker = new PrintWriter(socket.getOutputStream(), true);
        toBroker.println(encodedBroadcast);
      }
	    catch (IOException e) {
	      e.printStackTrace(System.out);
      }
	    catch (Exception e) {
	      e.printStackTrace(System.out);
      }
    });
  }

  // TODO: Test.
  // Broker receives market instruments upon connection to router.
  public static void notifyBrokerMarketInstruments(Socket brokerSocket) {

    try {

      PrintWriter toBroker = new PrintWriter(brokerSocket.getOutputStream(), true);
      marketInstruments.forEach((marketId, encodedBroadcast) -> {
        toBroker.println(encodedBroadcast);
      });
    }
    catch (IOException e) {
      e.printStackTrace(System.out);
    }
    catch (Exception e) {
      e.printStackTrace(System.out);
    }
  }
}