package scripts.api.util;

import org.tribot.api2007.types.RSItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Spencer on 8/26/2016.
 */
public class Banking {
    public static List<RSItem> bankCache = new ArrayList<>();

    public static boolean closeBank() {
        if (!org.tribot.api2007.Banking.isBankScreenOpen())
            return true;

        List<RSItem> tempCache = Arrays.asList(org.tribot.api2007.Banking.getAll());
        HashMap<Integer, Integer> delta = new HashMap<>();
        for (RSItem cacheItem : bankCache) {
            if (cacheItem == null) continue;

            int cacheId = cacheItem.getID();
            int cacheAmount = cacheItem.getStack();

            boolean exists = false;
            for (RSItem bankItem : tempCache) {
                if (bankItem == null) continue;

                int bankId = bankItem.getID();
                int bankAmount = bankItem.getStack();
                if (cacheId == bankId) {
                    exists = true;
                    if (cacheAmount - bankAmount != 0)
                        delta.put(bankId, cacheAmount - bankAmount);
                    break;
                }
            }

            if (!exists) {
                delta.put(cacheId, -cacheItem.getStack());
            }
        }

        for (RSItem bankItem : tempCache) {
            if (bankItem == null) continue;

            int bankId = bankItem.getID();
            if (!delta.containsKey(bankId)) {
                delta.put(bankId, bankItem.getStack());
            }
        }

        bankCache = tempCache;

        if (delta.size() > 0) {
            //IO.queueMessage("itemDelta", delta);
        }

        return Banking.closeBank();
    }
}
