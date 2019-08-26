package za.co.wethinkcode;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( CheckSum.generateCheckSum(args[0]) );
        System.out.println(CheckSum.validateCheckSum(args[0], "065"));
    }
}
