package za.co.wethinkcode;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App 
{
    private static final Logger logger = LogManager.getLogger(App.class.getName());

    public static void main( String[] args )
    {
    	try (ServerSocket listener = new ServerSocket(5000)) {
    		logger.info("The capitalization server is running");
    		ExecutorService pool = Executors.newFixedThreadPool(20);
    		while (true) {
    			pool.execute(new Capitalizer(listener.accept()));    			
    		}
    		
    	} catch (IOException e) {
    		System.out.println(e);
    	}
    }
}

class Capitalizer implements Runnable {

	private Socket socket;
	
	Capitalizer(Socket s) {
		this.socket = s;
	}
	
	@Override
	public void run() {
		System.out.println("Connected: " + socket);
		
		try {
			Scanner in = new Scanner(socket.getInputStream());
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			while (in.hasNextLine()) {
				System.out.println("Client said: " + in.nextLine());
				String line = in.nextLine();
				out.print(line);
			}
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("Error closing socket");				
			}
			System.out.println("Socket closed");
		}
		
	}
	
}