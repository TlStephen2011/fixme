package za.co.wethinkcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import za.co.wethinkcode.broker.Broker;
import za.co.wethinkcode.broker.InvalidInputException;
import za.co.wethinkcode.broker.Simulation;
import za.co.wethinkcode.broker.Transaction;

public class App
{
	public static void main( String[] args ) throws Exception {
		// Assume interactive mode

		//Establishes a connection to server
		if (args.length == 1 && args[0].equals("-i")) {
			Broker b = new Broker();

			// Loop infinitely for input and process
			while (true) {
				try {
					// Get user input
					String input = getInput();
					// Build new transaction
					Transaction t = Transaction.buildTransaction(input);
					// Send transaction to router
					b.sendMessage(t);
					// Handle server response from "market"
					System.out.println(b.processResponse());
				} catch (InvalidInputException | IOException | NoSuchElementException e) {
					System.out.println(e.getMessage());
				}
			}
		} else if (args.length == 4
				&& args[0].equals("-s")
				&& args[1].matches("\\d+")
				&& args[2].equals("-cycles")
				&& args[3].matches("\\d+")) {
			try {
				int brokers = Integer.parseInt(args[1]);
				ExecutorService pool = Executors.newFixedThreadPool(brokers);
				int i = 0;


				while (i < brokers) {
					pool.execute(new Simulation(Integer.parseInt(args[3]), i + 1));
					i++;
				}
				pool.shutdown();
			} catch (IOException e) {
				System.out.println(e);
			}
		} else {
			System.out.println("Usage:\n\t" +
					"java -jar [*.jar] -i | -s [number of brokers to simulate] -cycles [maximum number of cycles]\n" +
					"Flags:\n\t-i <interactive mode>\n" +
					"\t-s <simulation mode for simulated broker>\n" +
					"\t-cycles <maximum number of cycles>");
		}
	}

	private static String getInput() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Enter transaction message: ");
		try {
			return reader.readLine();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	private static boolean isSimulationMode(String []args){
		if (args.length == 4) {

			return true;
		}
		return false;
	}
}