package za.co.wethinkcode.broker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Broker {
	
	private int brokerId;
    private final int PORT = 5000;
    private final String HOST = "127.0.0.1";
    
    public Broker() throws UnknownHostException, IOException{
    }

    public void sendMessage(Transaction t) throws IOException {
    }

    public void processResponse() throws IOException {
    	
    }
    
}