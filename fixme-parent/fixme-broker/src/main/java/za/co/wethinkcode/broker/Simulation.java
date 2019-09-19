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
   // public  static ThreadLocal<Boolean> dataReceived = ThreadLocal.withInitial(() -> false);
    private volatile boolean localSwitch = false;
    public  static ThreadLocal<MarketInstruments> marketInstruments;
    private int cycles;


    public Simulation(int cycles, int id) throws IOException {
        simulationId = id;
        this.cycles = cycles;
        broker = new Broker(id);
        broker.sent = cycles;
        marketInstruments = ThreadLocal.withInitial(()->broker.getMarketInstruments());
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
        //System.out.println("T" + simulationId + "  Random " + random);
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
            while (true) {

                try {

                    broker.processResponse(simulationId);
                    if (broker.sentIsReceived()) {
                        broker.getSocket().close();
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
                    localSwitch = false;
                        broker.killBroker();
                }
            }
        });
        thread.start();

        //String response;


        int i = 0;
        ///awaitBroadCast();
        while (i < cycles) {
            if (broker.getMarketInstruments().getInstruments().size() > 0) {
               // localSwitch = dataReceived.get();
                generateTransaction();
                try {
                    System.out.println("Thread " + simulationId + " Request to Market:\n" + transactionString + "\n");
                    broker.sendMessage(transaction);
                    // response = broker.processResponse();
                    System.out.println("Thread " + simulationId + " Waiting for Response from Market...");
                    // System.out.println("Thread " + simulationId + "Response from Market:\n" + response + "\n");

                } catch (IOException | NoSuchElementException e) {
                    System.out.println("Thread " + simulationId + " Error:\n" + e.getMessage() + "\n");
                }
                i++;
            }
        }
    }



}
