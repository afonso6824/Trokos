package trokosSystem;

import exception.ClientNotExistException;

import javax.crypto.SealedObject;
import java.io.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class ClientCatalog {
    private static ClientCatalog instance;
    private final List<Client> allClients;
    private static final String CLIENTS_BACKUP_PATH = "src/main/resources/backup/clients.cif";
    private static final String ACCOUNTS_BACKUP_PATH = "src/main/resources/backup/accounts.cif";


    private ClientCatalog() {
        allClients = new ArrayList<>();
        initializeCatalog();
    }

    public void addClient(Client client) {
        allClients.add(client);
    }

    public List<Client> getAllClients() {
        return allClients;
    }

    public boolean isUsernameAndPasswordValid(Client c2) {
        return allClients.stream()
                .anyMatch((client -> client.equals(c2)));
    }

    public boolean isClientAlreadyRegistered(Client c2) {
        return allClients.stream()
                .anyMatch(client -> client.getUsername().equals(c2.getUsername()));
    }

    public static ClientCatalog getInstance() {
        if (instance == null) {
            instance = new ClientCatalog();
        }
        return instance;
    }

    public Client findClientByUsername(String username) throws ClientNotExistException {
        Optional<Client> optionalClient =
                allClients.stream()
                        .filter((client -> client.getUsername().equals(username)))
                        .findFirst();

        if (optionalClient.isPresent()) {
            return optionalClient.get();
        } else {
            throw new ClientNotExistException();
        }
    }

    public boolean existsClient(String username) {
        return allClients.stream()
                .anyMatch((client -> client.getUsername().equals(username)));
    }


    private void initializeCatalog() {
        initializeClientsWithBackup();
        initializeAccountsWithBackup();
    }

    private void initializeClientsWithBackup() {
        Backup.getInstance().createBackupFileIfNotExists(CLIENTS_BACKUP_PATH);
        try (FileInputStream inputFile = new FileInputStream(CLIENTS_BACKUP_PATH);
             ObjectInputStream inputStream = new ObjectInputStream(inputFile)) {

            String allData = Backup.getInstance().decryptClients((SealedObject) inputStream.readObject());

            String[] allDataSplitted = allData.split("\n");
            for (String data : allDataSplitted) {
                String[] line = data.split(":");
                byte[] publicBytes = Base64.getDecoder().decode(line[1]);

                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey pubKey = keyFactory.generatePublic(keySpec);
                Client client = new Client(line[0], pubKey);
                addClient(client);
            }

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | ClassNotFoundException ignored) {
        }
    }

    private void initializeAccountsWithBackup() {
        List<Account> accounts;
        if (allClients.isEmpty()) {
            return;
        }
        try (FileInputStream fileIn = new FileInputStream(ACCOUNTS_BACKUP_PATH);
             ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {

            accounts = Backup.getInstance().decryptAccounts((SealedObject) objectIn.readObject());

            for (Client client : allClients) {
                for (Account account : accounts) {
                    if (client.getUsername().equals(account.getUsername()))
                        client.setAccount(account);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
