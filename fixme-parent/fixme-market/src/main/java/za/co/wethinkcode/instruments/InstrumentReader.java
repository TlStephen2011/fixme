package za.co.wethinkcode.instruments;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InstrumentReader {
	
	private static String[] readableMarkets = {"Apps", "Cars", "Companies", "Currencies", "Industries", "Retail", "Stocks"};
	private static final int marketChoice = ThreadLocalRandom.current().nextInt(0, readableMarkets.length);
	
	public static List<Instrument> getInstruments() throws Exception {
		
		String pathToJson = getFilePath();
		ObjectMapper mapper = new ObjectMapper();
		
		@SuppressWarnings("resource")
		String jsonInput = new Scanner(new File(pathToJson)).useDelimiter("\\Z").next();
		
		List<Instrument> instruments = mapper.readValue(jsonInput, new TypeReference<List<Instrument>>(){});
						
		return instruments;
	}
	
	private static String getFilePath() {
		StringBuilder b = new StringBuilder();
		
		// Get path to current module and separator
		String modulePath = new File("").getAbsolutePath();		
		String systemFileSeparator = System.getProperty("file.separator");
		
		b.append(modulePath);
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
}
