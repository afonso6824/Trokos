import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import trokosClient.TrokosClientSecurity;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Connection {

	// TURN ON SERVER FIRST <<---- IMPORTANT
	ObjectInputStream in;
	ObjectOutputStream out;

	@Before
	public void setUp() throws IOException {
		System.setProperty("javax.net.ssl.trustStore", "src/main/resources/security/client/truststore.client");
		System.setProperty("javax.net.ssl.trustStorePassword", "keystore");
		SocketFactory sf = SSLSocketFactory.getDefault();
		SSLSocket clientSocket = (SSLSocket) sf.createSocket("127.0.0.1", 45678);
		in = new ObjectInputStream(clientSocket.getInputStream());
		out = new ObjectOutputStream(clientSocket.getOutputStream());
	}


	@Test
	public void tryToConnectWithCorrectEncryptionNonce() throws IOException, ClassNotFoundException {


		String userId = "maria";
		String keystore = "src/main/resources/security/client/maria/keystore_maria";
		TrokosClientSecurity security = new TrokosClientSecurity(keystore, "keystore");


		out.writeObject(userId);
		String nonce = (String) in.readObject();

		out.writeObject(security.sign(nonce));

		boolean validation = (boolean) in.readObject();

		Assert.assertTrue(validation);

	}

	@Test
	public void tryToConnectWithWrongEncryptionNonce() throws IOException, ClassNotFoundException {
		System.setProperty("javax.net.ssl.trustStore", "src/main/resources/security/client/truststore.client");
		System.setProperty("javax.net.ssl.trustStorePassword", "keystore");
		SocketFactory sf = SSLSocketFactory.getDefault();
		SSLSocket clientSocket = (SSLSocket) sf.createSocket("127.0.0.1", 45678);
		ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

		String userId = "maria";
		// I selected a wrong keystore ( keystore from manel but userID maria )
		String keystore = "src/main/resources/security/client/manel/keystore_manel";
		TrokosClientSecurity security = new TrokosClientSecurity(keystore, "keystore");


		out.writeObject(userId);
		String nonce = (String) in.readObject();

		out.writeObject(security.sign(nonce));

		boolean validation = (boolean) in.readObject();

		Assert.assertFalse(validation);

	}


}
