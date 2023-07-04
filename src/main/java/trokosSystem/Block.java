package trokosSystem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Block implements Serializable {
	private final byte[] hash;
	private final long blockNum;
	private long numTansactions;
	private byte[] signature;
	private final List<Transaction> transactions = new ArrayList<>();

	private static final String PATH_FILE = "src/main/resources/blockchain/";
	private static final String BLOCK_NAME = "block_";
	private static final String EXTENTION = ".blk";

	public Block(byte[] hash, long blockNumber) {
		this.hash = hash;
		this.blockNum = blockNumber;
		this.numTansactions = 0;
	}

	public long getBlockNum() {
		return blockNum;
	}

	public byte[] getHash() {
		return hash;
	}

	public byte[] getSignature() {
		return signature;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public long getNumTansactions() {
		return numTansactions;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	/**
	 * Add's a transaction to the block and saves it
	 *
	 * @param t transaction to be add
	 */
	public void addTransaction(Transaction t) {
		this.transactions.add(t);
		this.numTansactions++;
		saveBlock();
	}

	/**
	 * Save this block in a file
	 */
	public void saveBlock() {

		createBlockChainFileIfNotExists(PATH_FILE + BLOCK_NAME + this.blockNum + EXTENTION);
		try (FileOutputStream fos = new FileOutputStream(PATH_FILE + BLOCK_NAME + this.blockNum + EXTENTION);
			 ObjectOutputStream oos = new ObjectOutputStream(fos)) {

			oos.writeObject(this);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void createBlockChainFileIfNotExists(String path) {
		try {
			File myFile = new File(path);
			if (myFile.exists()) {
				myFile.delete();
			}
			myFile.createNewFile();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
}