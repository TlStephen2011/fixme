package za.co.wethinkcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import za.co.wethinkcode.broker.Broker;
import za.co.wethinkcode.broker.InvalidInputException;
import za.co.wethinkcode.broker.Transaction;

public class App 
{
    public static void main( String[] args ) throws Exception {    	
    	// Assume interactive mode
    	
    	//Establishes a connection to server
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
    			b.processResponse();
    		} catch (InvalidInputException e) {
    			System.out.println(e.getMessage());
    		}
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
}