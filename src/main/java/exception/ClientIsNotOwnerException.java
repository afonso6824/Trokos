package exception;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class ClientIsNotOwnerException extends TrokosAppException {
	private static final String MESSAGE_ERROR = "Error, you are not the owner";

	public ClientIsNotOwnerException() {
	}

	public ClientIsNotOwnerException(String message) {
		super(message);
	}

	public void sendMessage(ObjectOutputStream outStream) {
		try {
			outStream.writeObject(MESSAGE_ERROR);
		} catch (IOException ignored) {
		}
	}
}

