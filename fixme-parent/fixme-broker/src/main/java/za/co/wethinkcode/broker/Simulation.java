package za.co.wethinkcode.broker;

import lombok.Data;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class Simulation implements  Runnable{

    private Broker broker;


    private int simulationId;
    private static Transaction transaction;
    private static String transactionString;
    private static String marketId;
    private static String buyOrSell;
    private static String instrument;
    private static String quantity;

    private int cycles;


    public Simulation(int cycles, int id) throws IOException {
        simulationId = id;
        this.cycles = cycles;
        broker = new Broker(id);
        broker.sent = cycles;
        //marketInstruments = new ThreadLocal<>();//ThreadLocal.withInitial(()->broker.getMarketInstruments());
    }

    public  void generateTransaction() {

        marketId = getRandomMarket();
        instrument = getRandomInstrument(marketId);
        buyOrSell = !brokerHasInstrument(instrument)
                ? "buy"
                : ThreadLocalRandom.current().nextInt(0, 10) > 5
                ? "buy"
                : "sell";
        quantity = getRandomQuantity(buyOrSell);

        try {
            transactionString = marketId + " "
                    + buyOrSell + " "
                    + instrument + " "
                    + quantity;
            transaction = Transaction.buildTransaction(transactionString);
        } catch (InvalidInputException e) {
            System.out.println(e.getMessage());
        }
    }

    public  String getRandomMarket() {
        //TODO:
        int random = ThreadLocalRandom.current().nextInt(0, broker.getMarketInstruments().getInstruments().size());
        return broker.getMarketInstruments().getInstruments().keySet().toArray()[random].toString();
    }

    public  String getRandomInstrument(String marketId) {
        // TODO:
        int random = ThreadLocalRandom.current().nextInt(0, broker.getMarketInstruments().getInstruments().get(marketId).size());
        return broker.getMarketInstruments().getInstruments().get(marketId).get(random).instrument;
    }

    public static boolean brokerHasInstrument(String instrument) {
        //TODO:
        return false;
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


        Thread thread = new Thread(() -> {
           // marketInstruments = new ThreadLocal<>();
            while (true) {

                try {

                    broker.processResponse(simulationId);
                    if (broker.sentIsReceived()) {
                        broker.getSocket().close();
                        System.out.println("DOONNE");
                        break;
                    }
                   // System.out.println("Enter transaction message: ");
                   // if (!dataReceived.get())
                    //flipSwitch();
                   // System.out.println(dataReceived.get());
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                } catch (NoSuchElementException e) {
                 //   if (!dataReceived.get())
                  //  localSwitch = false;
                        broker.killBroker();
                }
            }
        });
        thread.start();

        int i = 0;

        while (i < cycles) {

            if (broker.getMarketInstruments().getInstruments().size() > 0) {
               // marketInstruments.set(broker.getMarketInstruments());
                generateTransaction();
                try {
                    System.out.println("Thread " + simulationId + " Request to Market:\n" + transactionString + "\n");
                    broker.sendMessage(transaction);
                    System.out.println("Thread " + simulationId + " Waiting for Response from Market...\n");
                    // System.out.println("Thread " + simulationId + "Response from Market:\n" + response + "\n");

                } catch (IOException | NoSuchElementException e) {
                    System.out.println("Thread " + simulationId + " Error:\n" + e.getMessage() + "\n");
                }
                i++;
            }
        }
        System.out.println("CYCLES DONE");
    }



}
