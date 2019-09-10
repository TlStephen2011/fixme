package za.co.wethinkcode.market;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

import za.co.wethinkcode.ExecutionReportDecoded;
import za.co.wethinkcode.FixMessage;
import za.co.wethinkcode.SingleOrderDecoded;
import za.co.wethinkcode.exceptions.FixMessageException;
import za.co.wethinkcode.helpers.BroadcastEncoder;
import za.co.wethinkcode.helpers.Instrument;
import za.co.wethinkcode.instruments.InstrumentReader;

public class Market {
	
	private String marketId;
	private String HOST = "127.0.0.1";
	private int PORT = 5001;
	private Socket connection = null;
	List<Instrument> instruments = null;
	
	public Market() throws Exception {
    	connection = new Socket(HOST, PORT);
    	Scanner in = new Scanner(connection.getInputStream());
    	PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
    	
    	marketId = in.nextLine();
    	System.out.println("Market was allocated ID: " + marketId);
    	// Read instruments
    	instruments = InstrumentReader.getInstruments();
    	// Broadcast instruments
    	String encodedBroadcast = BroadcastEncoder.encode(marketId, instruments, true);
    	out.println(encodedBroadcast);
	}
	
	public void sendResponse(String message) throws IOException {
		PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
		
		// handle message and send back FIX to connection
		try {
			SingleOrderDecoded fix = new SingleOrderDecoded(message);
			
			FixMessage executionReport = new FixMessage(
					marketId,
					fix.getSourceID(),
					"2",
					fix.getSymbol(),
					fix.getBuyOrSell(),
					fix.getOrderAmount(),
					fix.getOrderAmount(),
					"42.42"
			);
			
			out.println(executionReport);
			
		} catch (FixMessageException e) {
			e.printStackTrace();
		}
	}
	
	public String getFixMessage() throws IOException {
		Scanner in = new Scanner(connection.getInputStream());			
		return in.nextLine();
	}
	
}
