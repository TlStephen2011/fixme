package za.co.wethinkcode;

import java.io.IOException;

import za.co.wethinkcode.broker.Broker;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, InterruptedException {
        Broker b = new Broker();
        
       // Thread.sleep(1000);
        
        b.sendMessage("exit");
    }
}
