package exception;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class GroupAlreadyExistException extends TrokosAppException {
	private static final String MESSAGE_ERROR = "Error, group already exist";

	public GroupAlreadyExistException() {
	}

	public GroupAlreadyExistException(String message) {
		super(message);
	}

	public void sendMessage(ObjectOutputStream outStream) {
		try {
			outStream.writeObject(MESSAGE_ERROR);
		} catch (IOException ignored) {
		}
	}
}

