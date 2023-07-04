package trokosSystem;

import exception.NotEnoughMoneyException;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.List;
import java.util.Objects;

public class Client implements Serializable {
	public static final double DEFAULT_BALANCE = 100;
	private String username;
	private PublicKey publicKey;
	private Account account;

	public Client(String username) {
		this.username = username;
	}

	public Client(String username, PublicKey publicKey) {
		this.username = username;
		this.publicKey = publicKey;
		account = new Account(username, DEFAULT_BALANCE);
	}

	public String getUsername() {
		return username;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public double getBalance() {
		return account.getBalance();
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public void increaseBalance(double balance) {
		this.account.setBalance(this.account.getBalance() + balance);
	}

	public void decreaseBalance(double balance) {
		this.account.setBalance(this.account.getBalance() - balance);
	}

	protected void setBalance(double balance) {
		this.account.setBalance(balance);
	}

	protected void setAccount(Account account) {
		this.account = account;
	}

	public boolean hasClientEnoughMoney(double amount) {
		return this.account.getBalance() - amount >= 0;
	}

	public Account getAccount() {
		return account;
	}

	synchronized public void makePayment(Transaction transaction) throws NotEnoughMoneyException {
		Client fromClient = transaction.getFromClient();
		Client toClient = transaction.getToClient();
		double amount = transaction.getAmount();

		if (fromClient.hasClientEnoughMoney(amount)) {

			fromClient.decreaseBalance(amount);
			toClient.increaseBalance(amount);

            Backup.getInstance().updateBackup();
			BlockChain.getInstance().addTransaction(transaction);

		} else {
			throw new NotEnoughMoneyException();
		}
	}


	public void requestPayment(Transaction transaction) {
		transaction.getFromClient().addPaymentToAccount(transaction);
	}

	public void payRequest(Transaction transaction) throws NotEnoughMoneyException {
		makePayment(transaction);

		if (transaction.hasGroupAssociated()) {

			GroupCatalog.getInstance()
					.findGroupByGroupID(transaction.getIdGroup())
					.addToPaymentsDone((TransactionGroup) transaction);
		}
		removePaymentToAccount(transaction);
        Backup.getInstance().updateBackup();
		BlockChain.getInstance().addTransaction(transaction);
	}

	public boolean existsRequestWithId(String id) {
		return account.getPendingPayments()
				.stream()
				.anyMatch((transaction -> transaction.getId().equals(id)));
	}

	public void addPaymentToAccount(Transaction transaction) {
		this.account.addPendingPayment(transaction);
        Backup.getInstance().updateBackup();
	}

	public void removePaymentToAccount(Transaction transaction) {
		this.account.removePendingPayment(transaction);
        Backup.getInstance().updateBackup();
	}

	public String getGroupsWhereClientIsOwner() {
		StringBuilder sb = new StringBuilder("Owner in:\n");

		List<Group> myGroups = GroupCatalog
				.getInstance()
				.getGroupsWhoOwnerIs(this);

		if (myGroups.isEmpty()) {
			sb.append("Empty");
			sb.append("\n");
		} else {
			for (Group group : myGroups) {
				sb.append("- ");
				sb.append(group.getID());
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public String getGroupsWhereClientIsMember() {
		StringBuilder sb = new StringBuilder("Member in:\n");

		List<Group> groupsWhereBelong = GroupCatalog
				.getInstance()
				.getGroupsWhereBelongs(this);

		if (groupsWhereBelong.isEmpty()) {
			sb.append("Empty");
		} else {
			for (Group group : groupsWhereBelong) {
				sb.append("- ");
				sb.append(group.getID());
				sb.append("\n");
			}
		}
		return sb.toString();
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Client client = (Client) o;
		return Objects.equals(username, client.username);
	}

	@Override
	public int hashCode() {
		return Objects.hash(username, publicKey, account);
	}
}
