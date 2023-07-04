package trokosSystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum CommandType {

    makepayment("makepayment"), payrequest("payrequest"), confirmQRcode("confirmQRcode"), obtainQRcode("obtainQRcode"), viewrequests("viewrequests");


    private final String type;


    CommandType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    private static final List<String> listCommandsTransactionals =
            new ArrayList<>(Arrays.asList(makepayment.getType(), payrequest.getType(), confirmQRcode.getType()));

    public static boolean isTransactional(String commandType) {
        return listCommandsTransactionals.stream()
                .anyMatch(commandType::equals);
    }

    public static boolean isRegular(String commandType) {
        return listCommandsTransactionals.stream()
                .noneMatch(commandType::equals);
    }

}
