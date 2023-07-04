package trokosServer;

import exception.*;
import trokosSystem.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import static utils.Utils.generateNonce;

public class TrokosServerSecurity {
	private static final String PARAMS_PATH = "src/main/resources/security/params.param";

	private KeyStore keystore;
	private String keystorePATH;
	private char[] passwordKeystore;

	private String user;

	private static final int COMMAND = 0;

	public TrokosServerSecurity(String keystore, String passwordKeystore) {
		try {
			this.keystorePATH = keystore;
			this.keystore = KeyStore.getInstance("PKCS12");
			this.keystore.load(Files.newInputStream(Paths.get(keystore)), passwordKeystore.toCharArray());
			this.passwordKeystore = passwordKeystore.toCharArray();
			BlockChain.getInstance().initialize(this.keystore, this.passwordKeystore);

		} catch (KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
			System.err.println("Keystore error detected");
			System.exit(-1);
		} catch (IOException ignored) {
		}
	}

	public boolean authentication(String user, ObjectOutputStream outStream, ObjectInputStream inStream)
			throws IOException, ClassNotFoundException {

		boolean authenticated = false;
		boolean isRegister = false;
		String nonceSent;

		try {
			ClientCatalog.getInstance().findClientByUsername(user);
			nonceSent = generateNonce(false);
			outStream.writeObject(nonceSent);

		} catch (ClientNotExistException | IOException e) {
			nonceSent = generateNonce(true);
			outStream.writeObject(nonceSent);

			try {
				isRegister = true;
				String nonceReceived = (String) inStream.readObject();
				byte[] encryptedNonceReceived = (byte[]) inStream.readObject();
				Certificate certificate = (Certificate) inStream.readObject();

				if (isNonceReceivedValid(nonceReceived, nonceSent)
						&& isCertificateValid(nonceSent, encryptedNonceReceived, certificate)) {

					Client newClient = new Client(user, certificate.getPublicKey());
					ClientCatalog.getInstance().addClient(newClient);
					Backup.getInstance().updateBackup();
					this.user = user;
					outStream.writeObject(Boolean.TRUE);
					authenticated = true;

				} else {
					outStream.writeObject(Boolean.FALSE);
				}

			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
		if (!isRegister) {
			byte[] encryptedNonceReceived = (byte[]) inStream.readObject();

			if (isEncryptedNonceValid(user, nonceSent, encryptedNonceReceived)) {
				authenticated = true;
				this.user = user;
				outStream.writeObject(Boolean.TRUE);
			} else {
				outStream.writeObject(Boolean.FALSE);
			}
		}

		return authenticated;
	}

	public boolean isNonceReceivedValid(String nonceReceived, String nonceSent) {
		return nonceReceived.equals(nonceSent);
	}

	public boolean isCertificateValid(String nonceSent, byte[] encryptedNonceReceived,
									  Certificate certificate) {
		boolean response = false;
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			PublicKey key = certificate.getPublicKey();
			signature.initVerify(key);
			signature.update(nonceSent.getBytes());
			response = signature.verify(encryptedNonceReceived);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public boolean isEncryptedNonceValid(String user, String nonceSent, byte[] encryptedNonceReceived) {
		boolean response = false;
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			Client client = ClientCatalog.getInstance().findClientByUsername(user);
			PublicKey key = client.getPublicKey();
			signature.initVerify(key);
			signature.update(nonceSent.getBytes());
			response = signature.verify(encryptedNonceReceived);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public boolean isPaymentValid(String toClient, String fromClient, double amount)
			throws ClientNotExistException, ClientNotValidException {

		if (!ClientCatalog.getInstance().existsClient(toClient)) {
			throw new ClientNotExistException();
		}
		Transaction transaction = new Transaction(toClient, fromClient, amount);

		if (transaction.getFromClient().equals(transaction.getToClient())) {
			throw new ClientNotValidException();
		}

		return true;
	}


	public boolean isPayRequestValid(String requestID, String username)
			throws RequestNotExistException, ClientNotExistException {

		if (!ClientCatalog.getInstance()
				.findClientByUsername(username)
				.existsRequestWithId(requestID)) {
			throw new RequestNotExistException();
		}

		return true;
	}

	public boolean isUserValidToAddToGroup(Client currentClient, String userID, String groupID)
			throws ClientAlreadyInGroupException, ClientNotValidException, ClientIsNotOwnerException,
			GroupNotExistException, ClientNotExistException {

		if (GroupCatalog.getInstance().existsGroup(groupID)) {
			Group group = GroupCatalog.getInstance().findGroupByGroupID(groupID);
			if (group.getOwner().equals(currentClient)) {
				if (ClientCatalog.getInstance().existsClient(userID)) {
					Client clientToBeAdded = ClientCatalog.getInstance().findClientByUsername(userID);
					if (!group.existsClientInGroup(clientToBeAdded.getUsername()) && !group.getOwner().equals(clientToBeAdded)) {
						return true;
					} else {
						throw new ClientAlreadyInGroupException();
					}
				} else {
					throw new ClientNotValidException();
				}
			} else {
				throw new ClientIsNotOwnerException();
			}
		} else {
			throw new GroupNotExistException();
		}
	}

	public boolean isDividePaymentValid(Client currentClient, String groupID)
			throws ClientIsNotOwnerException, GroupNotExistException {

		if (GroupCatalog.getInstance().existsGroup(groupID)) {
			Group group = GroupCatalog.getInstance().findGroupByGroupID(groupID);
			if (group.getOwner().equals(currentClient)) {
				return true;
			} else {
				throw new ClientIsNotOwnerException();
			}
		} else {
			throw new GroupNotExistException();
		}
	}

	public boolean isGroupIDValid(Client currentClient, String groupID)
			throws ClientIsNotOwnerException, GroupNotExistException {

		if (GroupCatalog.getInstance().existsGroup(groupID)) {
			Group group = GroupCatalog.getInstance().findGroupByGroupID(groupID);
			if (group.getOwner().equals(currentClient)) {
				return true;
			} else {
				throw new ClientIsNotOwnerException();
			}

		} else {
			throw new GroupNotExistException();
		}
	}

	public boolean isTransactionSignValid(SignedObject signedObject, Client client) {
		boolean response;
		try {


			PublicKey publicKey = client.getPublicKey();
			Signature signature = Signature.getInstance("SHA256withRSA");

			response = signedObject.verify(publicKey, signature);
		} catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
			return false;
		}
		return response;
	}


	public boolean isParametersTransactionalRequestValid(String[] request, TransactionRequest transactionRequest) {
		if (transactionRequest.getFromClient().isEmpty() || transactionRequest.getToClient().isEmpty()) {
			return false;
		}
		try {
			if (Double.parseDouble(transactionRequest.getAmount()) <= 0) {
				return false;
			}
		} catch (NumberFormatException | NullPointerException e) {
			return false;
		}

		switch (CommandType.valueOf(request[COMMAND])) {
			case confirmQRcode:
			case payrequest:
				return request.length == 2;
		}
		return true;
	}


	public boolean isParametersRegularRequestValid(String[] request) {


		switch (request[COMMAND]) {
			case "viewrequests":
			case "balance":
			case "groups":
				return true;
			case "makepayment":
			case "requestpayment":
				if (request.length != 3) {
					return false;
				}
				try {
					if (Double.parseDouble(request[2]) <= 0) {
						return false;
					}
				} catch (NumberFormatException | NullPointerException e) {
					return false;
				}
				return true;
			case "payrequest":
			case "confirmQRcode":
			case "newgroup":
			case "statuspayments":
			case "history":
				return request.length == 2;
			case "obtainQRcode":
				if (request.length != 2) {
					return false;
				}
				try {
					if (Double.parseDouble(request[1]) <= 0) {
						return false;
					}
				} catch (NumberFormatException | NullPointerException e) {
					return false;
				}
				return true;
			case "addu":
				return request.length == 3;
			case "dividepayment":
				if (request.length != 3) {
					return false;
				}
				try {
					Double.parseDouble(request[2]);
					if (Double.parseDouble(request[2]) <= 0) {
						return false;
					}
				} catch (NumberFormatException | NullPointerException e) {
					return false;
				}
				return true;
		}
		return false;
	}

}
