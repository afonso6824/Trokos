package trokosClient;

public class TrokosClient {
	private static final int PORT_CLIENT = 45678;
	private TrokosClientCommunication trokosClientCommunication;
	private String ipHost;
	private int port;
	private String truststore;
	private String keystore;
	private String passwordKeystore;
	private String userID;

	public TrokosClient(String[] args) {
		try {
			String[] serverAddressArray = args[0].split(":");
			this.truststore = args[1];
			this.keystore = args[2];
			this.passwordKeystore = args[3];
			this.userID = args[4];

			this.ipHost = serverAddressArray[0];

			if (serverAddressArray.length == 1) {
				this.port = PORT_CLIENT;
			} else {
				this.port = Integer.parseInt(serverAddressArray[1]);
			}
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Invalid params to start communication with server");
			System.exit(-1);
		}
	}

	public void run() {

		TrokosClientSecurity security;
		security = new TrokosClientSecurity(keystore, passwordKeystore);
		security.setTruststore(truststore);

		trokosClientCommunication = new TrokosClientCommunication( ipHost, port, security);

		trokosClientCommunication.connectToServer(userID);

	}
}
