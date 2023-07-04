package trokosSystem;

import exception.ClientNotExistException;
import utils.Utils;

import java.io.Serializable;

public class Transaction implements Serializable {
	private final String id;
	private final Client toClient;
	private final Client fromClient;
	private final double amount;
	private String idGroup;


	public Transaction(String toClient, String fromClient, double amount) throws ClientNotExistException {
		this.id = Utils.generateID();
		this.toClient = ClientCatalog.getInstance().findClientByUsername(toClient);
		this.fromClient = ClientCatalog.getInstance().findClientByUsername(fromClient);
		this.amount = amount;
	}

	public Transaction(String toClient, String fromClient, double amount,
					   String idGroup) throws ClientNotExistException {

		this(toClient, fromClient, amount);
		this.idGroup = idGroup;
	}

	public Client getToClient() {
		return toClient;
	}

	public boolean hasGroupAssociated() {
		return idGroup != null;
	}

	public String getIdGroup() {
		return idGroup;
	}

	public Client getFromClient() {
		return fromClient;
	}

	public double getAmount() {
		return amount;
	}

	public String getId() {
		return id;
	}

}
