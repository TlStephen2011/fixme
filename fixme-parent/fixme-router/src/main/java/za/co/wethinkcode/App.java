package za.co.wethinkcode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Hello world!
 *
 */
public class App 
{

    private static final Logger logger = LogManager.getLogger(App.class.getName());

    public static void main( String[] args )
    {
        logger.info("Hello from log4j 2");
        System.out.println("Hello from router!");
    }
}
