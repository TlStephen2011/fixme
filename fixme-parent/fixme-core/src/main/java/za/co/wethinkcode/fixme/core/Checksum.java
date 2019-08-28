package za.co.wethinkcode.fixme.core;

public class Checksum {
	static String generateChecksum(String message) {
		String hash;
		int checkSum;
		int i;

		for (i = 0, checkSum = 0; i < message.length(); checkSum += (int) message.charAt(i++))
			;
		hash = String.format("%1$3d", checkSum % 256).replace(' ', '0');
		return hash;
	}

	static boolean validateChecksum(String message, String checkSum) {
		return checkSum.equals(generateChecksum(message));
	}
}
