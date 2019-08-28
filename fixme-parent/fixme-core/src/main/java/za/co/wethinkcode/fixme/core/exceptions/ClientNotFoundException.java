package za.co.wethinkcode.fixme.core.exceptions;

public class ClientNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	public ClientNotFoundException() {
		super("This client was not found in the routing table.");
	}
}
