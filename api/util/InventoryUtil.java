/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts.api.util;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.Game;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.types.RSItem;
import scripts.api.misc.BooleanLambda;
import scripts.api.misc.Condition;

/**
 *
 * @author Spencer
 */
public class InventoryUtil {
    public static boolean interact(String item, String... option) {
        if (isUsing(item))
            return true;

        GameTab.open(GameTab.TABS.INVENTORY);

        if (Inventory.getCount(item) > 0) {
            RSItem i = getFirst(item);
            if (i != null) {
                if (i.click(option))
                    return Timing.waitUptext(option + " " + item + "->", 750);
            }
        }

        return false;
    }

    public static boolean interact(String item, BooleanLambda lamba, int t1, int t2, String... option) {
        if (isUsing(item))
            return true;

        GameTab.open(GameTab.TABS.INVENTORY);

        if (Inventory.getCount(item) > 0) {
            RSItem i = getFirst(item);
            if (i != null) {
                if (i.click(option))
                    return Timing.waitCondition(new Condition(lamba), General.random(t1, t2));
            }
        }

        return Timing.waitCondition(new Condition(lamba), General.random(t1, t2));
    }
    
    public static RSItem getFirst(String item) {
        RSItem[] items = Inventory.find(item);
        if (items != null && items.length > 0)
            return items[0];
        return null;
    }
    
    public static boolean isUsing(String item) {
        return (Game.getUptext() != null && Game.getUptext().contains("Use " + item + " ->"));
    }

    public static int getCount(String... items) {
        int count = 0;
        for (String item : items) {
            count += Inventory.getCount(item);
        }

        return count;
    }

    public static int getCount(int... items) {
        int count = 0;
        for (int item : items) {
            count += Inventory.getCount(item);
        }

        return count;
    }
}
