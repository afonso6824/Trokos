package trokosSystem;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.security.AlgorithmParameters;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;


public class Backup {
    private static final String CLIENTS_BACKUP_PATH = "src/main/resources/backup/clients.cif";
    private static final String ACCOUNTS_BACKUP_PATH = "src/main/resources/backup/accounts.cif";
    private static final String GROUPS_BACKUP_PATH = "src/main/resources/backup/groups.cif";
    private static final String PARAMS_CLIENTS_PATH = "src/main/resources/backup_params/clients.param";
    private static final String PARAMS_ACCOUNTS_PATH = "src/main/resources/backup_params/accounts.param";
    private static final String PARAMS_GROUPS_PATH = "src/main/resources/backup_params/groups.param";

    private static Backup instance;

    private final char[] passwordCipher;
    private final byte[] salt = {(byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea, (byte) 0xf2};


    private Backup(char[] passwordCipher) {
        this.passwordCipher = passwordCipher;
    }

    public static Backup getInstance() {
        return instance;
    }

    public static Backup initialize(String passwordCipher) {
        if (instance == null) {
            instance = new Backup(passwordCipher.toCharArray());
        }
        return instance;
    }


    private void updateClientsBackup() {
        deleteAndCreateBackupFileIfNotExists(CLIENTS_BACKUP_PATH);
        try (FileOutputStream outputFile = new FileOutputStream(CLIENTS_BACKUP_PATH);
             ObjectOutputStream outputStream = new ObjectOutputStream(outputFile)) {

            StringBuilder sb = new StringBuilder();
            for (Client allClient : ClientCatalog.getInstance().getAllClients()) {
                String publicKey = Base64.getEncoder().encodeToString(allClient.getPublicKey().getEncoded());
                sb.append(allClient.getUsername()).append(":").append(publicKey).append("\n");


            }


            outputStream.writeObject(encryptClients(sb.toString()));

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private void updateClientAccountsBackup() {
        deleteAndCreateBackupFileIfNotExists(ACCOUNTS_BACKUP_PATH);
        try (FileOutputStream outputFile = new FileOutputStream(ACCOUNTS_BACKUP_PATH);
             ObjectOutputStream outputStream = new ObjectOutputStream(outputFile)) {

            ArrayList<Account> accountList = new ArrayList<>();
            // List<Clients> =>> account
            ClientCatalog.getInstance()
                    .getAllClients()
                    .forEach(client -> accountList.add(client.getAccount()));

            outputStream.writeObject(encryptAccounts(accountList));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void updateGroupsBackup() {
        deleteAndCreateBackupFileIfNotExists(GROUPS_BACKUP_PATH);
        try (FileOutputStream outputFile = new FileOutputStream(GROUPS_BACKUP_PATH);
             ObjectOutputStream outputStream = new ObjectOutputStream(outputFile)) {

            outputStream.writeObject(encryptGroups(GroupCatalog.getInstance().getGroupsList()));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void updateBackup() {
        updateClientsBackup();
        updateGroupsBackup();
        updateClientAccountsBackup();
    }

    public void createBackupFileIfNotExists(String path) {
        try {
            File myFile = new File(path);
            myFile.createNewFile();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void deleteAndCreateBackupFileIfNotExists(String path) {
        try {
            File myFile = new File(path);
            myFile.delete();
            myFile.createNewFile();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public SealedObject encryptClients(String string) {
        SealedObject sealedObject = null;
        try {

            PBEKeySpec keySpec = new PBEKeySpec(passwordCipher, salt, 20); // pass, salt, iterations
            SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
            SecretKey key = kf.generateSecret(keySpec);

            Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
            c.init(Cipher.ENCRYPT_MODE, key);
            sealedObject = new SealedObject(string, c);
            byte[] params = c.getParameters().getEncoded();

            saveParams(params, PARAMS_CLIENTS_PATH);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sealedObject;
    }

    public SealedObject encryptAccounts(ArrayList<Account> accounts) {
        SealedObject sealedObject = null;
        try {

            PBEKeySpec keySpec = new PBEKeySpec(passwordCipher, salt, 20); // pass, salt, iterations
            SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
            SecretKey key = kf.generateSecret(keySpec);

            Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
            c.init(Cipher.ENCRYPT_MODE, key);
            sealedObject = new SealedObject(accounts, c);
            byte[] params = c.getParameters().getEncoded();

            saveParams(params, PARAMS_ACCOUNTS_PATH);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sealedObject;
    }

    public SealedObject encryptGroups(HashMap<String, Group> map) {
        SealedObject sealedObject = null;
        try {

            PBEKeySpec keySpec = new PBEKeySpec(passwordCipher, salt, 20); // pass, salt, iterations
            SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
            SecretKey key = kf.generateSecret(keySpec);

            Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
            c.init(Cipher.ENCRYPT_MODE, key);
            sealedObject = new SealedObject(map, c);
            byte[] params = c.getParameters().getEncoded();

            saveParams(params, PARAMS_GROUPS_PATH);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sealedObject;
    }


    public void saveParams(byte[] params, String path) {


        try (FileOutputStream outputFile = new FileOutputStream(path);
             ObjectOutputStream outputStream = new ObjectOutputStream(outputFile)) {

            String paramsString = Base64.getEncoder().encodeToString(params);
            outputStream.writeObject(paramsString);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public byte[] getParams(String path) {
        String paramsString = null;
        try (FileInputStream inputFile = new FileInputStream(path);
             ObjectInputStream inputStream = new ObjectInputStream(inputFile)) {

            paramsString = (String) inputStream.readObject();

        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        return Base64.getDecoder().decode(paramsString);
    }

    public String decryptClients(SealedObject sealedObject) {
        String decryptString = null;
        try {

            PBEKeySpec keySpec = new PBEKeySpec(passwordCipher, salt, 20); // pass, salt, iterations
            SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
            SecretKey key = kf.generateSecret(keySpec);


            AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
            p.init(getParams(PARAMS_CLIENTS_PATH));
            Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
            c.init(Cipher.DECRYPT_MODE, key, p);
            decryptString = (String) sealedObject.getObject(c);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptString;
    }

    public ArrayList<Account> decryptAccounts(SealedObject sealedObject) {
        ArrayList<Account> decryptAccounts = null;
        try {

            PBEKeySpec keySpec = new PBEKeySpec(passwordCipher, salt, 20); // pass, salt, iterations
            SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
            SecretKey key = kf.generateSecret(keySpec);


            AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
            p.init(getParams(PARAMS_ACCOUNTS_PATH));
            Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
            c.init(Cipher.DECRYPT_MODE, key, p);
            decryptAccounts = (ArrayList<Account>) sealedObject.getObject(c);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptAccounts;
    }

    public HashMap<String, Group> decryptGroups(SealedObject sealedObject) {
        HashMap<String, Group> decryptAccounts = null;
        try {

            PBEKeySpec keySpec = new PBEKeySpec(passwordCipher, salt, 20); // pass, salt, iterations
            SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
            SecretKey key = kf.generateSecret(keySpec);


            AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
            p.init(getParams(PARAMS_GROUPS_PATH));
            Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
            c.init(Cipher.DECRYPT_MODE, key, p);
            decryptAccounts = (HashMap<String, Group>) sealedObject.getObject(c);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptAccounts;
    }
}
