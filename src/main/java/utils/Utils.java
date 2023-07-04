package utils;

import java.io.*;
import java.security.SecureRandom;

public class Utils {

	private static final String ALL_IDS_GENERATED_PATH = "src/main/resources/all_ids_generated.txt";
	private static final int INITIAL_ID = 1111;
	private static final int INITIAL_NUM_CURRENT_BLOCK = 1;
	private static Utils instance;
	private static int lastID;
	private static long lastNumCurrentBlock;


	private Utils() {
		initializeUtils();
	}

	public static Utils getInstance() {
		if (instance == null) {
			instance = new Utils();
		}
		return instance;
	}

	synchronized public static String generateID() {
		if (instance == null) {
			instance = new Utils();
		}
		lastID += 1;

		updateUtilsBackup();
		return String.valueOf(lastID);
	}


	public static void saveAndUpdateLastNumCurrentBlock(long id) {
		lastNumCurrentBlock = id;
		updateUtilsBackup();
	}

	public static long getLastNumCurrentBlock() {
		return lastNumCurrentBlock;
	}


	private static void initializeUtils() {
		createBackupFileIfNotExists(ALL_IDS_GENERATED_PATH);
		try (FileInputStream fileIn = new FileInputStream(ALL_IDS_GENERATED_PATH);
			 ObjectInputStream objectIn = new ObjectInputStream(fileIn)) {

			lastID = objectIn.readInt();
			lastNumCurrentBlock = objectIn.readLong();

		} catch (Exception ex) {
			lastID = INITIAL_ID;
			lastNumCurrentBlock = INITIAL_NUM_CURRENT_BLOCK;
		}
	}

	public static void updateUtilsBackup() {
		createBackupFileIfNotExists(ALL_IDS_GENERATED_PATH);
		try (FileOutputStream outputFile = new FileOutputStream(ALL_IDS_GENERATED_PATH);
			 ObjectOutputStream outputStream = new ObjectOutputStream(outputFile)) {

			outputStream.writeInt(lastID);
			outputStream.writeLong(lastNumCurrentBlock);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void createBackupFileIfNotExists(String path) {
		try {
			File myFile = new File(path);
			if (myFile.createNewFile()) {
				System.out.println("New Backup was been initialized");
			}
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}


	public static String generateNonce(boolean isFlagNewClientActivated) {
		SecureRandom secureRandom = new SecureRandom();
		StringBuilder stringBuilder =
				isFlagNewClientActivated ? new StringBuilder("-nc ") : new StringBuilder();

		for (int i = 0; i < 15; i++) {
			stringBuilder.append(secureRandom.nextInt(10));
		}
		return stringBuilder.toString();
	}
}
