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

    private final int PORT = 5000;
    private final String HOST = "127.0.0.1";
    private Socket socket = new Socket(HOST, PORT);
    
    public Broker() throws UnknownHostException, IOException{
        sendMessage("exit");        
        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String time = reader.readLine();

        System.out.println("Server said: " + time);
        socket.close();
    }

    public void sendMessage(String msg) throws IOException {
        OutputStream os = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(os, true);

        writer.println(msg);
    }

}