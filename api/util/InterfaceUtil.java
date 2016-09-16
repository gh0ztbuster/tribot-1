package scripts.api.util;

import org.tribot.api.Clicking;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.types.RSInterface;

import java.util.Arrays;

/**
 * Created by Spencer on 8/10/2016.
 */
public class InterfaceUtil {
    public static boolean isOpen(final int... xpath) {
        final RSInterface rsInterface = get(xpath);
        return rsInterface != null && !rsInterface.isHidden();
    }

    public static boolean isOpen(final RSInterface rsInterface) {
        return rsInterface != null && !rsInterface.isHidden();
    }

    public static RSInterface get(final int... xpath) {
        if (xpath.length == 0) return null;

        final RSInterface rsInterface = Interfaces.get(xpath[0]);
        return xpath.length == 1 ? rsInterface : get(rsInterface, Arrays.copyOfRange(xpath, 1, xpath.length));
    }

    public static RSInterface get(final RSInterface rsInterface, final int... xpath) {
        if (rsInterface == null || xpath == null || xpath.length == 0) return null;

        return xpath.length == 1 ? rsInterface.getChild(xpath[0]) : get(rsInterface.getChild(xpath[0]), Arrays.copyOfRange(xpath, 1, xpath.length));
    }

    public static boolean click(final RSInterface rsInterface, String... options) {
        if (rsInterface == null) return false;

        return Clicking.click(options, rsInterface);
    }

    public static boolean isOpen(String text) {
        final RSInterface[] rsInterfaces = Interfaces.getAll();
        for (RSInterface rsInterface : rsInterfaces) {
            if (rsInterface.getText() != null && rsInterface.getText().contains(text))
                return true;
            if (checkChildren(text, rsInterface))
                return true;
        }

        return false;
    }

    public static boolean checkChildren(String text, RSInterface rsInterface) {
        if (rsInterface == null) return false;
        final RSInterface[] rsInterfaces = rsInterface.getChildren();
        if (rsInterfaces == null || rsInterfaces.length == 0) return false;
        for (RSInterface r : rsInterfaces) {
            if (r != null && !r.isHidden()) {
                if (r.getText() != null && r.getText().contains(text))
                    return true;
                if (checkChildren(text, r))
                    return true;
            }
        }

        return false;
    }
}
