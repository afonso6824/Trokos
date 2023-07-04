package exception;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class NotEnoughMoneyException extends TrokosAppException {
	private static final String MESSAGE_ERROR = "Error, not enough money";

	public NotEnoughMoneyException() {
	}

	public NotEnoughMoneyException(String message) {
		super(message);
	}

	public void sendMessage(ObjectOutputStream outStream) {
		try {
			outStream.writeObject(MESSAGE_ERROR);
		} catch (IOException ignored) {
		}
	}
}

