package exception;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class QRCodeNotExistException extends TrokosAppException {
	private static final String MESSAGE_ERROR = "Error, QR code don't exist";

	public QRCodeNotExistException() {
	}

	public QRCodeNotExistException(String message) {
		super(message);
	}

	public void sendMessage(ObjectOutputStream outStream) {
		try {
			outStream.writeObject(MESSAGE_ERROR);
		} catch (IOException ignored) {
		}
	}
}

