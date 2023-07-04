package trokosClient;

import exception.CommandNotValidException;
import trokosSystem.CommandType;
import trokosSystem.MyQRCode;
import trokosSystem.Transaction;
import trokosSystem.TransactionRequest;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.SignedObject;
import java.security.cert.Certificate;
import java.util.*;
import java.util.stream.Collectors;

import static trokosSystem.CommandType.*;

public class TrokosClientCommunication {

    private static final String PATH_QRCODES = "src/main/resources/qrcodes";
    private static final String TRUSTSTORE_PASSWORD = "keystore";
    private final String ipHost;
    private final int port;
    private boolean authenticated;
    private TrokosClientSecurity security;
    private String userID;
    private Map<String, TransactionRequest> lastViewRequests;

    public TrokosClientCommunication(String ipHost, int port, TrokosClientSecurity security) {
        this.ipHost = ipHost;
        this.port = port;
        this.security = security;
        this.lastViewRequests = new HashMap<>();
    }

    public void connectToServer(String userID) {
        this.userID = userID;

        System.setProperty("javax.net.ssl.trustStore", security.getTruststore());
        System.setProperty("javax.net.ssl.trustStorePassword", TRUSTSTORE_PASSWORD);
        SocketFactory sf = SSLSocketFactory.getDefault();
        // Comunicação
        try (SSLSocket clientSocket = (SSLSocket) sf.createSocket(ipHost, port);
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

        ) {

            authenticated = security.authentication(in, out, userID);

            if (authenticated) {
                while (clientSocket.isConnected()) {
                    try {
                        List<String> commandsList = makeListCommands();
                        userInteraction(commandsList, in, out);
                    } catch (Exception e) {
                        System.out.println("Error, server problem");
                        System.exit(-1);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    private void menuDisplay(List<String> commandsList) {
        StringBuilder menu = new StringBuilder("Available operations menu:\n");
        menu.append("***************************************************************\n");
        commandsList.forEach(command -> menu.append(command).append("\n"));
        menu.append("*****************************************************************\n");
        menu.append("* Write the desired operation or the corresponding first letter *\n");
        menu.append("*****************************************************************\n");
        System.out.print(menu);
    }

    private void userInteraction(List<String> commandsList, ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException, CommandNotValidException {
        Scanner scn = new Scanner(System.in);
        String[] userInput;
        String command;
        menuDisplay(commandsList);

        while (scn.hasNextLine()) {
            userInput = scn.nextLine().split(" ");
            command = userInput[0];

            if (command.length() == 1) {
                userInput[0] = matchLetter(command, commandsList);
            }

            boolean isTransactional = isTransactionalOperation(userInput[0]);


            if (commandsList.contains(userInput[0])) {

                if (isTransactional) {
                    executeTransactionOperation(in, out, userInput);
                } else {
                    executeRegularOperation(in, out, userInput);
                }
            } else {
                System.out.println("Invalid command!");
            }
        }
        scn.close();
    }


    public boolean isTransactionalOperation(String command) {
        boolean isTransaction;
        try {
            isTransaction = isTransactional(command);
        } catch (ArrayIndexOutOfBoundsException e) {
            isTransaction = false;
        }
        return isTransaction;
    }

    private void executeRegularOperation(ObjectInputStream in, ObjectOutputStream out, String[] userInput)
            throws IOException, ClassNotFoundException {

        out.writeObject(String.join(" ", userInput));

        if (userInput[0].equals(obtainQRcode.getType()) || userInput[0].equals("o")) {
            File file = new File(PATH_QRCODES);
            file.mkdir();
            Object o = in.readObject();
            try {
                MyQRCode myQRCode = (MyQRCode) o;
                System.out.println(myQRCode.getQrID());
                myQRCode.printQRCode();
            } catch (ClassCastException e) {
                System.out.println((String) o);
            }
        } else if (userInput[0].equals(viewrequests.getType())) {
            String lastViewRequests = (String) in.readObject();

            if (!lastViewRequests.isEmpty()) {
                saveLastViewRequests(lastViewRequests);
            }

            System.out.println(lastViewRequests);
        } else {
            System.out.println((String) in.readObject());
        }
    }

    private void executeTransactionOperation(ObjectInputStream in, ObjectOutputStream out, String[] userInput)
            throws IOException, ClassNotFoundException {
        TransactionRequest transactionRequest;
        byte[] transactionSigned;

        if (!isParametersTransactionalRequestValid(userInput)) {
            System.out.println("Wrong parameters, try again");
            return;
        }
        switch (CommandType.valueOf(userInput[0])) {
            case makepayment:
                out.writeObject(String.join(" ", userInput));

                transactionRequest = new TransactionRequest(userInput[1], userID, userInput[2]);
                transactionSigned = security.signTransaction(transactionRequest);
                out.writeObject(transactionSigned);

                System.out.println((String) in.readObject());
                break;

            case payrequest:

                if (!this.lastViewRequests.containsKey(userInput[1])) {
                    System.out.println("Please check again the available requests. (viewrequests)");
                    return;
                }

                out.writeObject(String.join(" ", userInput));

                transactionRequest = this.lastViewRequests.get(userInput[1]);
                transactionSigned = security.signTransaction(transactionRequest);
                out.writeObject(transactionSigned);

                System.out.println((String) in.readObject());
                break;


            case confirmQRcode:
                out.writeObject(String.join(" ", userInput));

                String toClient = (String) in.readObject();
                String amount = (String) in.readObject();

                transactionRequest = new TransactionRequest(toClient, userID, amount);
                transactionSigned = security.signTransaction(transactionRequest);
                out.writeObject(transactionSigned);

                System.out.println((String) in.readObject());

            default:

        }
    }

    public boolean isParametersTransactionalRequestValid(String[] request) {

        switch (request[0]) {
            case "makepayment":
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
                return request.length == 2;
        }
        return false;
    }


    private String matchLetter(String command, List<String> commandsList) throws CommandNotValidException {
        Optional<String> response = commandsList.stream()
                .filter(word -> Character.toString(word.charAt(0)).equals(command))
                .findFirst();
        if (response.isPresent()) {
            return response.get();
        } else {
            throw new CommandNotValidException();
        }
    }


    private void saveLastViewRequests(String lastViewRequests) {
        String[] allRequests = lastViewRequests.split("\n");
        for (String request : allRequests) {
            String[] allInfo = request.split(" | ");
            String fromClient = allInfo[1];
            String amount = allInfo[4];
            String idRequest = allInfo[8];
            this.lastViewRequests.put(idRequest, new TransactionRequest(userID, fromClient, amount));
        }

    }


    private List<String> makeListCommands() {
        List<String> commands = new ArrayList<>();
        String file = "src/main/resources/validcommands.txt";
        String command;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while ((command = br.readLine()) != null) {
                commands.add(command);
            }
        } catch (IOException e) {
            System.err.println("Error loading command list.");
            System.exit(-1);
        }
        return commands;
    }
}
