package za.co.wethinkcode.fixme.core.exceptions;

public class InputEmptyException extends Exception {
	private static final long serialVersionUID = 1L;

	public InputEmptyException() {
		super("Input is empty.");
	}
}
