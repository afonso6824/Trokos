package trokosServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TrokosServerThread extends Thread {
	private Socket socket;
	private TrokosServerSecurity security;

	TrokosServerThread(Socket inSoc, String keystore, String keystorePassword) {
		socket = inSoc;
		security = new TrokosServerSecurity(keystore, keystorePassword);
	}

	public void run() {

		try {

			ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

			String user = (String) inStream.readObject();

			if (security.authentication(user, outStream, inStream)) {
				// START COMMUNICATION
				TrokosServerCommunication commands = new TrokosServerCommunication(user, inStream, outStream, security);
				commands.runRequests();

			}

			outStream.close();
			inStream.close();
			socket.close();

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
