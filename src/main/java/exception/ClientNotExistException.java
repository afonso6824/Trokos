package exception;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class ClientNotExistException extends TrokosAppException {
	private static final String MESSAGE_ERROR = "Error, client don't exist";

	public ClientNotExistException() {
	}

	public ClientNotExistException(String message) {
		super(message);
	}

	public void sendMessage(ObjectOutputStream outStream) {
		try {
			outStream.writeObject(MESSAGE_ERROR);
		} catch (IOException ignored) {
		}
	}
}

