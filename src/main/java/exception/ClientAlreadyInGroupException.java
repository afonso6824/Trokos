package exception;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class ClientAlreadyInGroupException extends TrokosAppException {
	private static final String MESSAGE_ERROR = "Error, client is already in the group";

	public ClientAlreadyInGroupException() {
	}

	public ClientAlreadyInGroupException(String message) {
		super(message);
	}

	public void sendMessage(ObjectOutputStream outStream) {
		try {
			outStream.writeObject(MESSAGE_ERROR);
		} catch (IOException ignored) {
		}
	}
}

