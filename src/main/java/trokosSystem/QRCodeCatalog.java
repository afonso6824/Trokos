package trokosSystem;

import exception.QRCodeNotExistException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class QRCodeCatalog {
	private static QRCodeCatalog instance;
	private final Map<String, MyQRCode> myQRCodeList;

	private QRCodeCatalog() {
		myQRCodeList = new HashMap<>();
	}

	public void addQRCode(MyQRCode myQRCode) {
		myQRCodeList.put(myQRCode.getQrID(), myQRCode);
	}

	public void removeQRCode(String qrID) {
		myQRCodeList.remove(qrID);

		try {
			File imageQRCode = new File("src/main/resources/qrcodes/" + qrID + ".png");
			imageQRCode.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean containsQrcode(String qrID) {
		return myQRCodeList.containsKey(qrID);
	}

	public static QRCodeCatalog getInstance() {
		if (instance == null) {
			instance = new QRCodeCatalog();
		}
		return instance;
	}

	public MyQRCode findQRCodeById(String id) throws QRCodeNotExistException {

		Optional<MyQRCode> optionalMyQRCode = myQRCodeList.values()
				.stream()
				.filter((qrCode -> qrCode.getQrID().equals(id)))
				.findFirst();

		if (optionalMyQRCode.isPresent()) {
			return optionalMyQRCode.get();
		} else {
			throw new QRCodeNotExistException();
		}
	}
}
