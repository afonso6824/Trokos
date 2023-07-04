package exception;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class RequestNotExistException extends TrokosAppException {
	private static final String MESSAGE_ERROR = "Error, request don't exist";

	public RequestNotExistException() {
	}

	public RequestNotExistException(String message) {
		super(message);
	}

	public void sendMessage(ObjectOutputStream outStream) {
		try {
			outStream.writeObject(MESSAGE_ERROR);
		} catch (IOException ignored) {
		}
	}
}

