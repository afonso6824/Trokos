package exception;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class TransactionReceivedNotValidException extends TrokosAppException {
	private static final String MESSAGE_ERROR = "Error, transaction received not valid";

	public TransactionReceivedNotValidException() {
	}

	public TransactionReceivedNotValidException(String message) {
		super(message);
	}

	public void sendMessage(ObjectOutputStream outStream) {
		try {
			outStream.writeObject(MESSAGE_ERROR);
		} catch (IOException ignored) {
		}
	}
}

