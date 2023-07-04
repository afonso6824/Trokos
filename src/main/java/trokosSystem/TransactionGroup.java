package trokosSystem;

import exception.ClientNotExistException;

import java.io.Serializable;

public class TransactionGroup extends Transaction implements Serializable {
	private final String idTransactionGroup;
	private final int members;


	public TransactionGroup(String toClient, String fromClient, double amount,
							String idGroup, String idTransactionGroup, int members) throws ClientNotExistException {

		super(toClient, fromClient, amount, idGroup);
		this.idTransactionGroup = idTransactionGroup;
		this.members = members;
	}


	public String getIdTransactionGroup() {
		return idTransactionGroup;
	}

	public int getMembers() {
		return members;
	}
}
