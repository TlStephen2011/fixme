package za.co.wethinkcode.fixme.broker;

import za.co.wethinkcode.fixme.core.Client;
/**
 * Broker Main
 *
 */
public class Main
{
    public static void main( String[] args )
    {
        Client client = new Client("Broker");
        Thread clientThread = new Thread(client);
        clientThread.start();
        try {
            clientThread.join();
        } catch (InterruptedException ex) {
            ex.getLocalizedMessage();
        }
        Client.inputHandler(client);
    }
}
