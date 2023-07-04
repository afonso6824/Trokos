package trokosSystem;

import utils.Utils;

import java.io.*;
import java.security.*;

public class BlockChain {
	private static final String PATH_FILE = "src/main/resources/blockchain";
	private static final String BLOCK_NAME = "block_";
	private static final String EXTENTION = ".blk";
	private static BlockChain instance;
	private KeyStore keystore;
	private char[] keystorePassword;
	private final long LIST_LIMIT = 5;
	private long blockCounter = 1;
	private Block currentBlock;

	private BlockChain() {
		File file = new File(PATH_FILE);

		if (file.listFiles().length == 0) {
			this.currentBlock = new Block(new byte[32], blockCounter);

		} else {
			blockCounter = Utils.getLastNumCurrentBlock();
			uploadLastBlock();
		}
	}

	public static BlockChain getInstance() {
		if (instance == null) {
			instance = new BlockChain();
		}
		return instance;
	}

	public void initialize(KeyStore keystore, char[] keystorePassword) {
		this.keystore = keystore;
		this.keystorePassword = keystorePassword;
	}

	public void uploadLastBlock() {

		System.out.println(PATH_FILE + "/" + BLOCK_NAME + this.blockCounter + EXTENTION);

		try (FileInputStream fileIn = new FileInputStream(PATH_FILE + "/" + BLOCK_NAME + this.blockCounter + EXTENTION);
			 ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {

			this.currentBlock = (Block) objectIn.readObject();
		} catch (Exception ignored) {
		}
	}

	/**
	 * Add's a transaction to the corrent block
	 */
	public void addTransaction(Transaction transaction) {
		currentBlock.addTransaction(transaction);
		if (currentBlock.getNumTansactions() == LIST_LIMIT) {

			try {

				// Assinar o bloco
				this.currentBlock.setSignature(signBlock(currentBlock));

				// Gerar hash do bloco (inclui a sua assinatura)
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				byte[] hash = md.digest(convertToBytes(this.currentBlock));

				//Guardar o block num ficheiro
				this.currentBlock.saveBlock();

				//Criar um novo block em que o hash do cabeçalho seja a hash gerada anteriormente
				blockCounter += 1;
				Utils.saveAndUpdateLastNumCurrentBlock(blockCounter);

				this.currentBlock = new Block(hash, blockCounter);
				this.currentBlock.saveBlock();


			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public byte[] signBlock(Block block)
			throws IOException {

		SignedObject signedObject = null;
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			PrivateKey privateKey = (PrivateKey) keystore.getKey("myserver", keystorePassword);
			signedObject = new SignedObject(block, privateKey, signature);

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