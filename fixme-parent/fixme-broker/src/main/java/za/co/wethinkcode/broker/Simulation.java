package za.co.wethinkcode.broker;

import jdk.jfr.DataAmount;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class Simulation implements  Runnable{

    private Broker broker;
    private int simulationId;
    private static Transaction transaction;
    private static String marketId;
    private static String buyOrSell;
    private static String instrument;
    private static String quantity;
    private int cycles;

    public Simulation(int cycles, int id) throws IOException {
        broker = new Broker();
        this.cycles = cycles;
        simulationId = id;
    }

    public static void generateTransaction() {

        marketId = getRandomMarket();
        instrument = getRandomInstrument(marketId);
        buyOrSell = !brokerHasInstrument(instrument)
                ? "buy"
                : ThreadLocalRandom.current().nextInt(0, 10) > 5
                ? "buy"
                : "sell";
        quantity = getRandomQuantity(buyOrSell);

                try {
                    transaction = Transaction.buildTransaction(marketId + " "
                            + buyOrSell + " "
                            + instrument + " "
                            + quantity);
                } catch (InvalidInputException e) {
                    System.out.println(e.getMessage());
                }
    }

    public static String getRandomMarket() {
        //TODO:
        return "123456";
    }

    public static String getRandomInstrument(String marketId) {
        // TODO:
        return "XAU";
    }

    public static boolean brokerHasInstrument(String instrument) {
        //TODO:
        return true;
    }

    public static String getRandomQuantity(String buyOrSell) {
        //TODO:
        if (buyOrSell.equals("buy")) {
            return "100";
        } else  {
            return "200";
        }
    }

    @Override
    public void run() {

        int i = 0;
        while (i < cycles) {
            System.out.println("Thread - " + simulationId);
           generateTransaction();
           try {
               broker.sendMessage(transaction);
               System.out.println(broker.processResponse());
           } catch (IOException | NoSuchElementException e) {
               System.out.println(e.getMessage());
           }
            i++;
        }
    }



}
