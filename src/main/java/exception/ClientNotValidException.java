package exception;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class ClientNotValidException extends TrokosAppException {
	private static final String MESSAGE_ERROR = "Error, client it's not valid";

	public ClientNotValidException() {
	}

	public ClientNotValidException(String message) {
		super(message);
	}

	public void sendMessage(ObjectOutputStream outStream) {
		try {
			outStream.writeObject(MESSAGE_ERROR);
		} catch (IOException ignored) {
		}
	}
}

