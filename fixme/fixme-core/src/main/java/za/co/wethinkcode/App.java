package za.co.wethinkcode;

import za.co.wethinkcode.exceptions.FixMessageException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        // System.out.println( CheckSum.generateCheckSum(args[0]) );
        // System.out.println(CheckSum.validateCheckSum(args[0], "065"));

        try {

          FixMessage testBuy = new FixMessage(
            "1D5P8X",
            "6Q8D7Z",
            "XAU",
            "1",
            "100"
          );

          FixMessage testSell = new FixMessage(
            "1D5P8X",
            "6Q8D7Z",
            "XAU",
            "2",
            "100"
          );

          FixMessage testExecutionReport = new FixMessage(
            "1D5P8X",
            "6Q8D7Z",
            "2",
            "XAU",
            "2",
            "1000",
            "1000",
            "25.9"
          );

          System.out.println(testBuy.getFixMessage());
          System.out.println(testSell.getFixMessage());
          System.out.println(testExecutionReport.getFixMessage());
        }
        catch (FixMessageException e) {

          e.printStackTrace(System.out);
        }
    }
}
