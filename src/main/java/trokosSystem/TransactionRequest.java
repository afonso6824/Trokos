package trokosSystem;

import java.io.Serializable;

public class TransactionRequest implements Serializable {

	private final String toClient;
	private final String fromClient;
	private final String amount;

	public TransactionRequest(String toClient, String fromClient, String amount) {
		this.toClient = toClient;
		this.fromClient = fromClient;
		this.amount = amount;
	}


	public String getToClient() {
		return toClient;
	}

	public String getFromClient() {
		return fromClient;
	}

	public String getAmount() {
		return amount;
	}
}
