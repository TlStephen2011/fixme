package za.co.wethinkcode.fixme.core.exceptions;

public class ChecksumNotEqualException extends Exception {
	private static final long serialVersionUID = 1L;

	public ChecksumNotEqualException() {
		super("Checksum is incorrect.");
	}
}
