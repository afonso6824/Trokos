package trokosSystem;

import exception.PendingPaymentNotExistException;
import exception.TrokosAppException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Account implements Serializable {
    public static final double DEFAULT_BALANCE = 100;

    private final String username;
    private double balance;
    private final List<Transaction> pendingPayments;

    public Account(String username, double balance) {
        this.username = username;
        this.balance = balance;
        pendingPayments = new ArrayList<>();

    }

    public Account(String username) {
        super();
        this.username = username;
        this.balance = DEFAULT_BALANCE;
        pendingPayments = new ArrayList<>();

    }

    public List<Transaction> getPendingPayments() {
        return pendingPayments;
    }

    public void addPendingPayment(Transaction transaction) {
        pendingPayments.add(transaction);
        Backup.getInstance().updateBackup();
    }

    public void removePendingPayment(Transaction transaction) {
        pendingPayments.remove(transaction);
        Backup.getInstance().updateBackup();
    }

    public String getUsername() {
        return username;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean containsUser(String username) {
        return username.equals(this.username);
    }

    public Transaction getPendingPayment(String id) throws PendingPaymentNotExistException {
        Optional<Transaction> pendingPayment =
                pendingPayments.stream()
                        .filter((transaction -> transaction.getId().equals(id))).findFirst();

        if (pendingPayment.isPresent()) {
            return pendingPayment.get();
        } else {
            throw new PendingPaymentNotExistException();
        }
    }

    public String getPendingPaymentsToString() {
        StringBuilder sb = new StringBuilder();

        for (Transaction transaction : pendingPayments) {
            sb.append("From: ")
                    .append(transaction.getToClient().getUsername())
                    .append(" | ")
                    .append("Amount: ")
                    .append(transaction.getAmount())
                    .append(" Trokos")
                    .append(" | ")
                    .append("ID: ")
                    .append(transaction.getId())
                    .append("\n");
        }
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(username, account.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, balance);
    }

    @Override
    public String toString() {
        return "Account{" +
                "username='" + username + '\'' +
                ", balance=" + balance +
                '}';
    }
}
