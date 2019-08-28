package za.co.wethinkcode.fixme.market;

import za.co.wethinkcode.fixme.core.Client;
/**
 * Market Main
 *
 */
public class Main {
    public static void main(String[] args) {
        Client client = new Client("Market");
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
