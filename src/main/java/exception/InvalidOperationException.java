package exception;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class InvalidOperationException extends TrokosAppException {
	private static final String MESSAGE_ERROR = "Error, the operation is not valid";

	public InvalidOperationException() {
	}

	public InvalidOperationException(String message) {
		super(message);
	}

	public void sendMessage(ObjectOutputStream outStream) {
		try {
			outStream.writeObject(MESSAGE_ERROR);
		} catch (IOException ignored) {
		}
	}
}

