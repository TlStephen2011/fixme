package za.co.wethinkcode.broker;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import za.co.wethinkcode.broker.InvalidInputException;
import za.co.wethinkcode.constraints.MarketId;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
public class Transaction {

	@MarketId
	private String marketId;
	private String buyOrSell;
	private String instrument;
	private String orderQty;
	
	
	private Transaction(String marketId, String buyOrSell, String instrument, String orderQty) {
		this.marketId = marketId;
		this.buyOrSell = buyOrSell;
		this.instrument = instrument;
		this.orderQty = orderQty;
	}
	
	public static Transaction buildTransaction(String msg) throws InvalidInputException {
		// Later: regex to split on all whitespace
		String[] args = msg.split("\\s");
		Transaction t;
		if (args.length == 4) {
			t = new Transaction(args[0], args[1], args[2], args[3]);
		} else {
			throw new InvalidInputException("Transaction message must have 4 fields, for example: \n[marketId] [buy|sell] [instrument] [quantity]");
		}
		validateTransaction(t);
		return t;
	}

	private static void validateTransaction(Transaction t) throws InvalidInputException {
		ValidatorFactory validatorFactory = Validation
				.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.getValidator();

		Set<ConstraintViolation<Transaction>> constraintViolations = validator.validate(t);

		if (constraintViolations.size() > 0) {
			System.out.println("Your input is invalid");
			Set<String> violationMessages = new HashSet<String>();

			for (ConstraintViolation<Transaction> constraintViolation : constraintViolations) {
				violationMessages.add(constraintViolation.getPropertyPath() + " : " + constraintViolation.getMessage());
			}
			System.out.println(StringUtils.join(violationMessages, "\n"));
			throw new InvalidInputException("");
		}
	}

	private static boolean isValid(String[] args) {
		int qty;
		
		if (args.length != 4) {
			return false;
		}
		
		if (args[0].length() != 6) {
			return false;
		}
		
		if (!args[1].toLowerCase().equals("buy") && !args[1].toLowerCase().equals("sell")) {
			return false;
		}
		
		if (args[2].length() == 0) {
			return false;
		}
		
		
		try {
			qty = Integer.parseInt(args[3]);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
