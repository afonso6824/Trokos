package exception;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class GroupNotExistException extends TrokosAppException {
	private static final String MESSAGE_ERROR = "Error, group don't exist";

	public GroupNotExistException() {
	}

	public GroupNotExistException(String message) {
		super(message);
	}

	public void sendMessage(ObjectOutputStream outStream) {
		try {
			outStream.writeObject(MESSAGE_ERROR);
		} catch (IOException ignored) {
		}
	}
}

