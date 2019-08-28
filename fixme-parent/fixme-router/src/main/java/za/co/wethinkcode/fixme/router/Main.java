package za.co.wethinkcode.fixme.router;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        System.out.println("Hello from router!");
    }
}
