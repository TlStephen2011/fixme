package za.co.wethinkcode.broker;

import za.co.wethinkcode.broker.InvalidInputException;

public class Transaction {
	public String marketId;
	public String buyOrSell;
	public String instrument;
	public String orderQty;
	
	
	private Transaction() {
		
	}
	
	public static Transaction buildTransaction(String msg) throws InvalidInputException {
		// Later: regex to split on all whitespace
		String[] args = msg.split("\\s");
		
		if (!isValid(args)) {
			throw new InvalidInputException("Your format is incorrect.");
		}
		
		
		
		return new Transaction();
	}
	
	private static boolean isValid(String[] args) {
		int qty;
		
		if (args.length != 4) {
			return false;
		}
		
		if (args[0].length() != 6) {
			return false;
		}
		
		if (!args[1].toLowerCase().equals("buy") && !args[1].toLowerCase().equals("sell")) {
			return false;
		}
		
		if (args[2].length() == 0) {
			return false;
		}
		
		
		try {
			qty = Integer.parseInt(args[3]);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
