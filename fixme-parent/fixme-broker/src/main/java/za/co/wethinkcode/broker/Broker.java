package za.co.wethinkcode.broker;

import lombok.Data;
import za.co.wethinkcode.ExecutionReportDecoded;
import za.co.wethinkcode.FixMessage;
import za.co.wethinkcode.exceptions.FixMessageException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

@Data
public class Broker {

    // TODO maybe maintain one instance of the Scanner and PrintWriter

    private String brokerId;
    private final int PORT = 5000;
    private final String HOST = "127.0.0.1";
    private Socket socket;
    private Scanner fromRouter;
    private MarketInstruments marketInstruments;
    private int received = 0;
    public  int sent;

    public Broker() throws UnknownHostException, IOException {
        socket = new Socket(HOST, PORT);
        this.fromRouter = new Scanner(socket.getInputStream());
        brokerId = this.fromRouter.nextLine();
        System.out.println("Connection to router has been established\n" + "Allocated ID: " + brokerId + "\n");
        marketInstruments = new MarketInstruments();
        System.out.println("Awaiting market connections... ");
    }

    public Broker(int simulationId) throws IOException{
        socket = new Socket(HOST, PORT);
        this.fromRouter = new Scanner(socket.getInputStream());
        brokerId = this.fromRouter.nextLine();
        marketInstruments = new MarketInstruments();
        // TOD: Might need to create the scanner object here as well.
        // brokerId = processResponse(simulationId);
        System.out.println("Thread " + simulationId + ":\n" + "Connection to router has been established\n"
                + "Allocated ID: " + brokerId + "\n" + "Awaiting market connections" + "\n");
    }

    public void sendMessage(Transaction t) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        FixMessage message;
        try {
            message = new FixMessage(
                    brokerId,
                    t.getMarketId(),
                    t.getInstrument(),
                    t.getBuyOrSell().equals("buy") ? "1" : "2",
                    t.getOrderQty()
            );
            out.println(message.toString());
        }catch (FixMessageException e) {
            System.out.println(e.getMessage());
        }
    }

    public void processResponse(int simulationId) throws IOException {

        String line;

//        System.out.println("\n+++++Simulation START " + simulationId + "\n");
        line = fromRouter.nextLine();

        try {
            ExecutionReportDecoded executionReport = new ExecutionReportDecoded(line);
            System.out.println(
                    (simulationId > 0 ? String.format("Thread %d Response from Market:\n", simulationId) : "")
                            + "Market "
                            + executionReport.getSourceID()
                            + (executionReport.getOrderStatus().equals("2") ? " ACCEPTED " : " REJECTED ")
                            + "broker "
                            + executionReport.getTargetID()
                            + "'s request to "
                            + (executionReport.getBuyOrSell().equals("1") ? "buy " : "sell ")
                            + executionReport.getSymbol());
            // marketInstruments.updateQuantities(executionReport);
            received++;
        } catch (FixMessageException e) {
            while (line.contains(",")) {
                marketInstruments.updateMarketInstruments(line);
                line = fromRouter.nextLine();
            }
            marketInstruments.printMarketInstruments(simulationId);
        }
//        System.out.println("\n------Simulation END " + simulationId + "\n");
    }

    public boolean sentIsReceived(){
        if (sent == received)
            return    true;
        return false;
    }

    public void killBroker() {
        System.exit(1);
    }

}



