package za.co.wethinkcode;

public class CheckSum {
    static String generateCheckSum(String message) {
        String hash;
        int checkSum;
        int i;

        for (i = 0, checkSum = 0; i < message.length(); checkSum += (int)message.charAt(i++) );
        hash = String.format("%1$3d", checkSum % 256).replace(' ', '0');
        return hash;
    }

    static boolean validateCheckSum(String message, String checkSum) {
        return checkSum.equals(generateCheckSum(message));
    }
}
