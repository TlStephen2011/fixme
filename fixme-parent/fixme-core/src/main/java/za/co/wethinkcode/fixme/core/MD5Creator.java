package za.co.wethinkcode.fixme.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class MD5Creator {
	public static String createMD5FromObject(String id) {
		String myHash = "error_checksum";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(id.getBytes());
			byte[] digest = md.digest();
			myHash = DatatypeConverter.printHexBinary(digest).toLowerCase();
		} catch (NoSuchAlgorithmException ex) {
			ex.getLocalizedMessage();
		}
		return myHash;
	}
}
