package za.co.wethinkcode.fixme.core.exceptions;

public class InputErrorException extends Exception {
	private static final long serialVersionUID = 1L;

	public InputErrorException() {
		super("Input has an error.");
	}
}
