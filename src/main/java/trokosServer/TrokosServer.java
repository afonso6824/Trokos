package trokosServer;

import trokosSystem.Backup;
import trokosSystem.BlockChain;
import trokosSystem.ClientCatalog;
import trokosSystem.QRCodeCatalog;
import utils.Utils;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.Socket;

public class TrokosServer {
    private static final int DEFAULT_PORT = 45678;
    private int port = DEFAULT_PORT;
    private String passwordCipher;
    private String keystore;
    private String passwordKeystore;
    private SSLServerSocket serverSocket = null;
    private ClientCatalog clientCatalog;
    private QRCodeCatalog qrCodeCatalog;
    private Backup backup;

    private Utils utils;
    private BlockChain blockChain;

    public TrokosServer(int port, String passwordCipher, String keystore, String passwordKeystore) {
        this(passwordCipher, keystore, passwordKeystore);
        this.port = port;
    }

    public TrokosServer(String passwordCipher, String keystore, String passwordKeystore) {
        this.passwordCipher = passwordCipher;
        this.keystore = keystore;
        this.passwordKeystore = passwordKeystore;
        this.backup = Backup.initialize(passwordCipher);
        this.clientCatalog = ClientCatalog.getInstance();
        this.qrCodeCatalog = QRCodeCatalog.getInstance();
        this.utils = Utils.getInstance();
        this.blockChain = BlockChain.getInstance();

    }

    public void startServer() {
        try {
            System.setProperty("javax.net.ssl.keyStore", keystore);
            System.setProperty("javax.net.ssl.keyStorePassword", passwordKeystore);
            ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
            serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }


        while (true) {
            try {
                Socket inSoc = serverSocket.accept();
                TrokosServerThread newTrokosServerThread = new TrokosServerThread(inSoc, keystore, passwordKeystore);
                newTrokosServerThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) {
        TrokosServer trokosServer = null;
        String passwordCipher = null;
        String keystore = null;
        String passwordKeystore = null;
        try {
            passwordCipher = args[1];
            keystore = args[2];
            passwordKeystore = args[3];
            int port = Integer.parseInt(args[0]);

            trokosServer = new TrokosServer(port, passwordCipher, keystore, passwordKeystore);
            System.out.println("Running in " + args[0]);

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Wrong parameters, try again");
            System.exit(-1);
        } catch (NumberFormatException e) {
            System.out.println("The given port is not valid\nServer running in default port: 45678");
            trokosServer = new TrokosServer(passwordCipher, keystore, passwordKeystore);
        } finally {
            if (trokosServer != null) {
                trokosServer.startServer();
            }
        }
    }
}
