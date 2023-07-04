package trokosClient;

import trokosSystem.Transaction;
import trokosSystem.TransactionRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class TrokosClientSecurity {
    private Certificate certificate;
    private KeyStore keystore;
    private char[] keystorePassword;
    private String truststore;

    public TrokosClientSecurity(String keystore, String keystorePassword) {
        try {
            this.keystore = KeyStore.getInstance("JCEKS");
            this.keystore.load(Files.newInputStream(Paths.get(keystore)), keystorePassword.toCharArray());
            this.keystorePassword = keystorePassword.toCharArray();
            certificate = this.keystore.getCertificate("keyrsa");

        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
            System.err.println("Keystore error detected");
            System.exit(-1);
        } catch (IOException ignored) {
        }
    }

    public void setTruststore(String truststore) {
        this.truststore = truststore;
    }

    public String getTruststore() {
        return truststore;
    }

    public boolean authentication(ObjectInputStream in, ObjectOutputStream out,
                                  String userId) throws IOException, ClassNotFoundException {
        boolean validation;
        out.writeObject(userId);
        String nonce = (String) in.readObject();

        if (isNewClient(nonce)) {
            out.writeObject(nonce);
            out.writeObject(sign(nonce));
            out.writeObject(certificate);
        } else {
            out.writeObject(sign(nonce));
        }

        validation = (boolean) in.readObject();

        if (validation) {
            if (isNewClient(nonce)) {
                System.out.println("Utilizador registado e autenticado.");
            } else {
                System.out.println("Utilizador autenticado.");
            }
        } else {
            System.out.println("Utilizador não autenticado.");
            System.exit(0);
        }
        return validation;
    }

    public boolean isNewClient(String nonce) {
        return nonce.split(" ")[0].equals("-nc");
    }


    public byte[] sign(String nonce) {
        byte[] encryptedNonce = null;
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            PrivateKey privateKey = (PrivateKey) keystore.getKey("keyrsa", keystorePassword);
            signature.initSign(privateKey);
            signature.update(nonce.getBytes());
            encryptedNonce = signature.sign();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedNonce;
    }

    public byte[] signTransaction(TransactionRequest transaction) throws IOException {

        SignedObject signedObject = null;
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            PrivateKey privateKey = (PrivateKey) keystore.getKey("keyrsa", keystorePassword);


            signedObject = new SignedObject(transaction, privateKey, signature);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertToBytes(signedObject);
    }

    private byte[] convertToBytes(Object object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        }
    }

}

