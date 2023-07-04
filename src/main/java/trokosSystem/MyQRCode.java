package trokosSystem;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import exception.ClientNotExistException;
import exception.InvalidOperationException;
import exception.NotEnoughMoneyException;
import utils.Utils;

import javax.imageio.ImageIO;

public class MyQRCode implements Serializable {
	private final String qrID;
	private final String fromClient;
	private final double amount;
	transient private BitMatrix matrix;
	private static final String PATH_QRCODE = "src/main/resources/qrcodes/";


	public MyQRCode(String fromClient, double amount) {
		this.qrID = Utils.generateID();
		this.fromClient = fromClient;
		this.amount = amount;
		obtainQrcode(amount);
	}

	public String getQrID() {
		return qrID;
	}

	private void obtainQrcode(double amount) {
		String data = "Created by " + fromClient + " | " + "Amount: " + amount + " Trokos" + " | " +
				"ID: " + qrID;
		String charset = "UTF-8";

		createQR(data, charset, 1, 1);
	}

	public void createQR(String data, String charset, int height, int width) {

		try {
			this.matrix = new MultiFormatWriter().encode(
					new String(data.getBytes(charset), charset),
					BarcodeFormat.QR_CODE, width, height);
		} catch (IOException | WriterException e) {
			e.printStackTrace();
		}
	}

	public void printQRCode() {

		try {
			obtainQrcode(amount);
			String path = PATH_QRCODE + qrID + ".png";
			BufferedImage bi = new BufferedImage(200, 200, 1);
			File outputfile = new File(path);
			ImageIO.write(bi, "png", outputfile);

			MatrixToImageWriter.writeToPath(
					matrix,
					path.substring(path.lastIndexOf('.') + 1),
					Paths.get(path));
		} catch (IOException ignored) {
		}

	}

	public String getFromClientID() {
		return this.fromClient;
	}

	public double getAmount() {
		return this.amount;
	}

	public BitMatrix getMatrix() {
		return this.matrix;
	}

	private boolean isValidClientToConfirmQRcode(String toClient) throws ClientNotExistException {
		if (!QRCodeCatalog.getInstance().containsQrcode(qrID)) {
			return false;
		}

		Client client = ClientCatalog.getInstance().findClientByUsername(toClient);

		return client.hasClientEnoughMoney(amount);
	}


	public boolean confirmQRcode(String toClient) throws
			NotEnoughMoneyException, ClientNotExistException, InvalidOperationException {

		if (!isValidClientToConfirmQRcode(toClient)) {
			QRCodeCatalog.getInstance().removeQRCode(qrID);
			throw new NotEnoughMoneyException();
		}

		if (fromClient.equals(toClient)) {
			throw new InvalidOperationException();
		}

		Transaction transaction = new Transaction(fromClient, toClient, amount);

		transaction.getFromClient().makePayment(transaction);
		QRCodeCatalog.getInstance().removeQRCode(qrID);
		BlockChain.getInstance().addTransaction(transaction);
		return true;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
		ImageIO.write(image, "png", out);

	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		ImageIO.read(in);
	}
}