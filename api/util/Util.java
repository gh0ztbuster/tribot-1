/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts.api.util;

import org.tribot.api.General;
import org.tribot.api2007.Login;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSTile;
import scripts.api.misc.BooleanLambda;
import scripts.api.misc.Condition;
import scripts.api.patterns.BaseScript;
import scripts.deluxehub.Main;

import java.util.Arrays;

/**
 *
 * @author Spencer
 */
public class Util {

    public static String formatTime(final long time) {
        final StringBuilder t = new StringBuilder();
        final long total_secs = time / 1000;
        final long total_mins = total_secs / 60;
        final long total_hrs = total_mins / 60;
        final long total_days = total_hrs / 24;
        final int secs = (int) total_secs % 60;
        final int mins = (int) total_mins % 60;
        final int hrs = (int) total_hrs % 24;
        final int days = (int) total_days;
        if (days > 0) {
            if (days < 10)
                t.append("0");

            t.append(days);
            t.append(":");
        }
        if (hrs < 10)
            t.append("0");

        t.append(hrs);
        t.append(":");
        if (mins < 10)
            t.append("0");

        t.append(mins);
        t.append(":");
        if (secs < 10)
            t.append("0");

        t.append(secs);
        return t.toString();
    }
    
    public static String[] array(String... t) {
        return t;
    }

    public static boolean contains(String[] arr, String val) {
        return Arrays.asList(arr).contains(val);
    }

    public static RSTile getCenterTile(RSArea area) {
        return area.polygon.npoints > 0 ? new RSTile((int) Math.round(avg(area.polygon.xpoints)), (int) Math.round(avg(area.polygon.ypoints))) : null;
    }

    private static double avg(final int... nums) {
        long total = 0;
        for (int i : nums) {
            total += (long) i;
        }
        return (double) total / (double) nums.length;
    }

    public static String encode(String... args) {
        String str = "{";
        for (int i = 0; i < args.length - 1; i += 2) {
            if (i != 0)
                str += ", ";
            str += "\"" + args[i] + "\": \"" + args[i+1] + "\"";
        }
        str += "}";
        return str;
    }

    public static boolean react(BaseScript script, int shortTime, int longTime, BooleanLambda lambda) {
        int shortReaction = script.getBag().get("reaction_short", 1000);
        int longReaction = script.getBag().get("reaction_long", 1000);

        Condition condition = new Condition(lambda);
        condition.getBag().addOrUpdate("reaction_short", shortReaction);
        condition.getBag().addOrUpdate("reaction_long", longReaction);

        return condition.execute(shortTime * (shortReaction < 1000 ? 1000 : (shortReaction / 1000)), longTime * (longReaction < 1000 ? 1000 : (longReaction / 1000)));
    }

    public static void react(BaseScript script) {
        int shortReaction = script.getBag().get("reaction_short", 1000);
        int longReaction = script.getBag().get("reaction_long", 1000);
        General.sleep(shortReaction, longReaction);
    }

    public static boolean isBanned(BaseScript script) {
        if (Login.getLoginState() != Login.STATE.INGAME) {
            if (Login.getLoginState() == Login.STATE.WELCOMESCREEN) {
                Login.login();
            } else {
                if (script.getBag().get("email", "").equals("")) {
                    Login.login();
                } else {
                    Login.login(script.getBag().get("email"), script.getBag().get("password"));
                }
            }
        }

        if (Login.getLoginResponse().contains("Your account has been dis") || Login.getLoginResponse().contains("Account locked as")) {
            return true;
        }

        return false;
    }
}
