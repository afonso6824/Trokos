package exception;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class CommandNotValidException extends TrokosAppException {
	private static final String MESSAGE_ERROR = "Error, command is not valid";

	public CommandNotValidException() {
	}

	public CommandNotValidException(String message) {
		super(message);
	}

	public void sendMessage(ObjectOutputStream outStream) {
		try {
			outStream.writeObject(MESSAGE_ERROR);
		} catch (IOException ignored) {
		}
	}
}

