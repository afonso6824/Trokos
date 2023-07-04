package exception;

import java.io.ObjectOutputStream;

public abstract class TrokosAppException extends Exception {

	public TrokosAppException() {
	}

	public TrokosAppException(String message) {
		super(message);
	}

	abstract public void sendMessage(ObjectOutputStream outStream);
}

