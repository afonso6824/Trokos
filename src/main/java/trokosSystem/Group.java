package trokosSystem;

import exception.ClientNotExistException;
import utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Group implements Serializable {

	private final String groupID;
	private final Client owner;
	private final List<String> allMembers;
	private final List<TransactionGroup> history;
	private final List<TransactionGroup> paymentsDone;

	public Group(String groupID, Client owner) {
		allMembers = new ArrayList<>();
		history = new ArrayList<>();
		paymentsDone = new ArrayList<>();
		this.groupID = groupID;
		this.owner = owner;
	}

	public String getID() {
		return this.groupID;
	}

	public Client getOwner() {
		return this.owner;
	}

	public void addClientToGroup(String userID) {
		allMembers.add(userID);
        Backup.getInstance().updateBackup();
	}

	public void dividePayment(double amount) throws ClientNotExistException {
		double eachShareAmount = amount / allMembers.size();
		String idTransactionGroup = Utils.generateID();
		for (String userID : allMembers) {
			Client member = ClientCatalog.getInstance().findClientByUsername(userID);
			Transaction transaction = new TransactionGroup
					(owner.getUsername(), member.getUsername(),
							eachShareAmount, groupID, idTransactionGroup, allMembers.size());

			owner.requestPayment(transaction);
		}
        Backup.getInstance().updateBackup();
	}

	public void addToPaymentsDone(TransactionGroup transaction) {
		paymentsDone.add(transaction);

		if (isValidToHistory(transaction)) {
			addToHistoryGroup(transaction);
		}
        Backup.getInstance().updateBackup();
	}

	private boolean isValidToHistory(TransactionGroup transaction) {
		int counter = 0;
		for (TransactionGroup t : paymentsDone) {
			if (t.hasGroupAssociated()) {
				if (t.getIdTransactionGroup().equals(transaction.getIdTransactionGroup())) {
					counter += 1;
				}
			}
		}
		return counter == transaction.getMembers();
	}


	private void addToHistoryGroup(TransactionGroup transaction) {
		this.history.add(transaction);
        Backup.getInstance().updateBackup();
	}

	public boolean existsClientInGroup(String client) {
		return allMembers.contains(client);
	}


	public String statusPayments() throws ClientNotExistException {
		StringBuilder sb = new StringBuilder("Status Payments - Group: ").append(groupID).append("\n");

		for (String userID : allMembers) {
			Client member = ClientCatalog.getInstance().findClientByUsername(userID);
			for (Transaction transaction : member.getAccount().getPendingPayments()) {
				if (transaction.hasGroupAssociated() && transaction.getIdGroup().equals(groupID)) {
					TransactionGroup transactionGroup = (TransactionGroup) transaction;
					sb.append(transactionGroup.getFromClient().getUsername())
							.append(" | Transaction: ")
							.append(transactionGroup.getIdTransactionGroup())
							.append(" | Amount: ")
							.append(transactionGroup.getAmount())
							.append(" | Didn't pay yet\n");
				}
			}
		}
		return sb.toString();
	}


	public String getHistory() {
		StringBuilder sb = new StringBuilder("History - Group: ").append(groupID).append("\n");

		for (TransactionGroup transaction : history) {
			sb.append("Transaction: ")
					.append(transaction.getIdTransactionGroup())
					.append(" | Total Amount: ")
					.append(transaction.getAmount() * transaction.getMembers())
					.append(" | Everyone Payed\n");
		}

		return sb.toString();
	}
}