package za.co.wethinkcode.fixme.router;

import za.co.wethinkcode.fixme.core.Client;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Router Main
 *
 */
public class Main
{

    private static final Logger logger = LogManager.getLogger(Main.class.getName());

    public static void main( String[] args )
    {
        logger.info("Hello from log4j 2");
        logger.info("Starting broker server");
        Server broker = new Server(Server.SVR_BROKER);
        Thread brokerThread = new Thread(broker);
        brokerThread.start();
        logger.info("Starting market server");
        Server market = new Server(Server.SVR_MARKET);
        Thread marketThread = new Thread(market);
        marketThread.start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String command = null;
        while (true) {
            try {
                command = bufferedReader.readLine();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
            if (command != null && command.toLowerCase().equals("exit")) {
                broker.shutThingsDown();
                market.shutThingsDown();
                break;
            }
        }
    }
}
