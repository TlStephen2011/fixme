package za.co.wethinkcode.instruments;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InstrumentReader {
	
	private static String[] readableMarkets = {"Market1", "Market2", "Market3", "Market4", "Market5"};
	private static final int marketChoice = ThreadLocalRandom.current().nextInt(0, readableMarkets.length);
	
	public static List<Instrument> getInstruments() throws Exception {
		
		String pathToJson = getFilePath();
		ObjectMapper mapper = new ObjectMapper();
		
		String jsonInput = readFile(pathToJson);
		
		List<Instrument> instruments = mapper.readValue(jsonInput, new TypeReference<List<Instrument>>(){});
						
		return instruments;
	}
	
	private static String getFilePath() {
		StringBuilder b = new StringBuilder();
		
		// Get path to current module and separator
		//String modulePath = new File("").getAbsolutePath();
		// Path improvement
	//	String modulePath = Paths.get("").toAbsolutePath().toString();
	
		String modulePath = System.getProperty("user.dir");
		
		String systemFileSeparator = System.getProperty("file.separator");
		
		b.append(modulePath);
		//b.append(systemFileSeparator);
		//b.append("fixme-market");
		b.append(systemFileSeparator);
		b.append("src");
		b.append(systemFileSeparator);
		b.append("main");
		b.append(systemFileSeparator);
		b.append("resources");
		b.append(systemFileSeparator);
		b.append(readableMarkets[marketChoice] + ".json");
		
		return b.toString();		
	}
	
	private static String readFile(String filePath)
	{
	    StringBuilder contentBuilder = new StringBuilder();
	    try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
	    {
	        stream.forEach(s -> contentBuilder.append(s).append("\n"));
	    }
	    catch (IOException e)
	    {
	    	System.out.println("Unable to read file");
	        e.printStackTrace();
	    }
	    return contentBuilder.toString();
	}
}
