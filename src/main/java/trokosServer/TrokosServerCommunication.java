package trokosServer;

import exception.*;
import trokosSystem.*;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.SignedObject;

import static trokosSystem.CommandType.isTransactional;

public class TrokosServerCommunication {
	private static final int COMMAND = 0;
	private static final int FIRST_ARG = 1;
	private static final int SECOND_ARG = 2;
	Client currentClient;
	String currentUsername;
	ObjectInputStream inStream;
	ObjectOutputStream outStream;
	TrokosServerSecurity trokosServerSecurity;

	public TrokosServerCommunication(String currentUsername, ObjectInputStream inStream, ObjectOutputStream outStream,
									 TrokosServerSecurity trokosServerSecurity) {
		this.inStream = inStream;
		this.outStream = outStream;
		this.currentUsername = currentUsername;
		this.trokosServerSecurity = trokosServerSecurity;
		try {
			currentClient = ClientCatalog.getInstance().findClientByUsername(currentUsername);
		} catch (ClientNotExistException e) {
			e.printStackTrace();
		}
	}


	public void runRequests() {

		boolean connectionAlive = true;

		while (connectionAlive)
			try {


				String request = (String) inStream.readObject();
				String[] requestSplitted = request.split(" ");

				if (isTransactional(requestSplitted[COMMAND])) {
					runRequestTransactional(requestSplitted);
				} else {
					runRequestRegular(requestSplitted);
				}

			} catch (IOException | ClassNotFoundException | TransactionReceivedNotValidException e) {
				connectionAlive = false;
				try {
					outStream.writeObject("Error, something went wrong");
				} catch (IOException ignored) {
				}
			}
	}

	public void runRequestTransactional(String[] request) throws IOException,
			ClassNotFoundException, TransactionReceivedNotValidException {

		byte[] transactionSigned;
		SignedObject signedObject;
		TransactionRequest transactionRequest;


		switch (CommandType.valueOf(request[COMMAND])) {
			case makepayment:

				transactionSigned = (byte[]) inStream.readObject();
				signedObject = (SignedObject) convertToObject(transactionSigned);
				transactionRequest = (TransactionRequest) signedObject.getObject();

				if (!trokosServerSecurity.isTransactionSignValid(signedObject, currentClient)) {
					outStream.writeObject("Something with signature of the transaction went wrong, try again");
					return;
				}

				if (!trokosServerSecurity.isParametersTransactionalRequestValid(request, transactionRequest)) {
					outStream.writeObject("Wrong parameters, try again");
					return;
				}
				responseMakePayment(transactionRequest);
				break;
			case payrequest:

				transactionSigned = (byte[]) inStream.readObject();
				signedObject = (SignedObject) convertToObject(transactionSigned);
				transactionRequest = (TransactionRequest) signedObject.getObject();

				if (!trokosServerSecurity.isTransactionSignValid(signedObject, currentClient)) {
					outStream.writeObject("Something with signature of the transaction went wrong, try again");
					return;
				}

				if (!trokosServerSecurity.isParametersTransactionalRequestValid(request, transactionRequest)) {
					outStream.writeObject("Wrong parameters, try again");
					return;
				}

				responsePayRequest(request[FIRST_ARG], transactionRequest);
				break;
			case confirmQRcode:

				MyQRCode myQRCode;
				try {
					myQRCode = QRCodeCatalog.getInstance().findQRCodeById(request[FIRST_ARG]);
					outStream.writeObject(myQRCode.getFromClientID());
					outStream.writeObject(Double.toString(myQRCode.getAmount()));

				} catch (QRCodeNotExistException e) {
					outStream.writeObject("Not valid");
					outStream.writeObject("Not valid");
				}


				transactionSigned = (byte[]) inStream.readObject();
				signedObject = (SignedObject) convertToObject(transactionSigned);
				transactionRequest = (TransactionRequest) signedObject.getObject();

				if (!trokosServerSecurity.isTransactionSignValid(signedObject, currentClient)) {
					outStream.writeObject("Something with signature of the transaction went wrong, try again");
					return;
				}

				if (!trokosServerSecurity.isParametersTransactionalRequestValid(request, transactionRequest)) {
					outStream.writeObject("Wrong parameters, try again");
					return;
				}
				responseConfirmQRcode(request[FIRST_ARG], transactionRequest);
		}


	}

	private Object convertToObject(byte[] data) throws TransactionReceivedNotValidException {
		try (ByteArrayInputStream in = new ByteArrayInputStream(data);
			 ObjectInputStream is = new ObjectInputStream(in)) {
			return is.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new TransactionReceivedNotValidException();
		}
	}

	private void runRequestRegular(String[] requestSplitted) throws IOException {

		if (!trokosServerSecurity.isParametersRegularRequestValid(requestSplitted)) {
			outStream.writeObject("Wrong parameters, try again");
			return;
		}

		String fromClient;
		String userID;
		String groupID;
		double amount;

		switch (requestSplitted[COMMAND]) {
			case "balance":
				responseBalance();
				break;
			case "requestpayment":
				fromClient = requestSplitted[FIRST_ARG];
				amount = Double.parseDouble(requestSplitted[SECOND_ARG]);
				responseRequestPayment(fromClient, amount);
				break;
			case "viewrequests":
				responseViewRequest();
				break;
			case "obtainQRcode":
				amount = Double.parseDouble(requestSplitted[FIRST_ARG]);
				responseObtainQRcode(amount);
				break;
			case "newgroup":
				groupID = requestSplitted[FIRST_ARG];
				responseNewGroup(groupID);
				break;
			case "addu":
				userID = requestSplitted[FIRST_ARG];
				groupID = requestSplitted[SECOND_ARG];
				responseAddu(userID, groupID);
				break;
			case "groups":
				responseGroups();
				break;
			case "dividepayment":
				groupID = requestSplitted[FIRST_ARG];
				amount = Double.parseDouble(requestSplitted[SECOND_ARG]);
				responseDividePayment(groupID, amount);
				break;
			case "statuspayments":
				groupID = requestSplitted[FIRST_ARG];
				responseStatusPayments(groupID);
				break;
			case "history":
				groupID = requestSplitted[FIRST_ARG];
				responseHistory(groupID);
				break;
		}
	}


	private void responseBalance() throws IOException {
		outStream.writeObject("Balance: " + currentClient.getBalance() + " Trokos");
	}

	private void responseMakePayment(TransactionRequest transactionRequest) throws IOException {
		try {
			String toClient = transactionRequest.getToClient();
			double amount = Double.parseDouble(transactionRequest.getAmount());

			if (trokosServerSecurity.isPaymentValid(currentUsername, toClient, amount)) {
				Transaction transaction = new Transaction(toClient, currentUsername, amount);
				currentClient.makePayment(transaction);
				outStream.writeObject("Successful Payment");
			}
		} catch (NotEnoughMoneyException | ClientNotExistException | ClientNotValidException e) {
			e.sendMessage(outStream);
		}
	}

	private void responseRequestPayment(String fromClient, double amount) throws IOException {

		try {

			if (trokosServerSecurity.isPaymentValid(currentUsername, fromClient, amount)) {
				Transaction transaction = new Transaction(currentUsername, fromClient, amount);
				currentClient.requestPayment(transaction);
				outStream.writeObject("Successful Request");
			}
		} catch (ClientNotExistException | ClientNotValidException e) {
			e.sendMessage(outStream);
		}
	}

	private void responseViewRequest() throws IOException {

		outStream.writeObject(currentClient.getAccount().getPendingPaymentsToString());

	}

	public void responsePayRequest(String requestID, TransactionRequest transactionRequest) throws IOException {

		try {
			if (trokosServerSecurity.isPayRequestValid(requestID, currentUsername)) {

				Transaction transaction = ClientCatalog
						.getInstance()
						.findClientByUsername(currentUsername)
						.getAccount()
						.getPendingPayment(requestID);

				currentClient.payRequest(transaction);
				outStream.writeObject("Successful Payment");
			}
		} catch (RequestNotExistException | NotEnoughMoneyException | ClientNotExistException |
				 PendingPaymentNotExistException e) {
			e.sendMessage(outStream);
		}
	}


	public void responseObtainQRcode(double amount) throws IOException {

		MyQRCode qrCode = new MyQRCode(currentUsername, amount);
		QRCodeCatalog.getInstance().addQRCode(qrCode);
		outStream.writeObject(qrCode);
	}


	public void responseConfirmQRcode(String qrCodeID, TransactionRequest transactionRequest) throws IOException {

		try {
			MyQRCode myQRCode = QRCodeCatalog.getInstance().findQRCodeById(qrCodeID);
			if (myQRCode.confirmQRcode(currentUsername)) {
				outStream.writeObject("Successful QRCode");
			}

		} catch (QRCodeNotExistException | NotEnoughMoneyException |
				 ClientNotExistException | InvalidOperationException e) {

			e.sendMessage(outStream);
		}
	}

	synchronized public void responseNewGroup(String groupID) throws IOException {

		try {
			Group group = new Group(groupID, currentClient);
			GroupCatalog.getInstance().addGroup(group);
			outStream.writeObject("New group created successfully");
		} catch (GroupAlreadyExistException e) {
			e.sendMessage(outStream);
		}
	}


	synchronized public void responseAddu(String userID, String groupID) throws IOException {

		try {

			if (trokosServerSecurity.isUserValidToAddToGroup(currentClient, userID, groupID)) {
				Group group = GroupCatalog.getInstance().findGroupByGroupID(groupID);
				group.addClientToGroup(userID);
				outStream.writeObject(userID + " added successfully");
			}

		} catch (ClientAlreadyInGroupException | ClientNotValidException | ClientIsNotOwnerException |
				 GroupNotExistException | ClientNotExistException e) {
			e.sendMessage(outStream);
		}
	}

	public void responseGroups() throws IOException {
		String groups = "View Groups\n\n" +
				currentClient.getGroupsWhereClientIsOwner() +
				currentClient.getGroupsWhereClientIsMember();

		outStream.writeObject(groups);
	}


	public void responseDividePayment(String groupID, double amount) throws IOException {

		try {
			if (trokosServerSecurity.isDividePaymentValid(currentClient, groupID)) {
				Group group = GroupCatalog.getInstance().findGroupByGroupID(groupID);
				group.dividePayment(amount);
				outStream.writeObject("Divide payment done successfully");
			}
		} catch (ClientIsNotOwnerException | ClientNotExistException | GroupNotExistException e) {
			e.sendMessage(outStream);
		}
	}

	public void responseStatusPayments(String groupID) throws IOException {
		try {

			if (trokosServerSecurity.isGroupIDValid(currentClient, groupID)) {
				Group group = GroupCatalog.getInstance().findGroupByGroupID(groupID);
				outStream.writeObject(group.statusPayments());
			}
		} catch (ClientIsNotOwnerException | GroupNotExistException | ClientNotExistException e) {
			e.sendMessage(outStream);
		}
	}


	public void responseHistory(String groupID) throws IOException {
		try {

			if (trokosServerSecurity.isGroupIDValid(currentClient, groupID)) {
				Group group = GroupCatalog.getInstance().findGroupByGroupID(groupID);
				outStream.writeObject(group.getHistory());
			}
		} catch (ClientIsNotOwnerException | GroupNotExistException e) {
			e.sendMessage(outStream);
		}
	}
}
