package scripts.api.util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.*;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSPlayer;
import org.tribot.api2007.types.RSTile;
import scripts.api.misc.BooleanLambda;
import scripts.api.misc.Condition;

/**
 *
 * @author Spencer
 */
public class NPCUtil {
    private static final int DEFAULT_MIN_TIMEOUT = 1250;
    private static final int DEFAULT_MAX_TIMEOUT = 2000;

    public static boolean isTalkingTo(String... name) {
        String chatName = NPCChat.getName();

        if (chatName == null)
            return false;

        RSPlayer player = Player.getRSPlayer();
        if (player != null) {
            String playerName = player.getName();
            if (playerName != null && chatName.equals(playerName))
                return true;
        }

        for (String n : name) {
            if (chatName != null && chatName.equals(n))
                return true;
        }
        return false;
    }

    public static RSNPC[] get(String... names) {
        return NPCs.find(names);
    }

    public static RSNPC[] get(int... ids) {
        return NPCs.find(ids);
    }

    public static RSNPC getNearest(String... names) {
        RSNPC[] npcs = NPCs.findNearest(names);
        if (npcs != null && npcs.length > 0)
            return npcs[0];
        return null;
    }

    public static RSNPC getNearest(int... ids) {
        RSNPC[] npcs = NPCs.findNearest(ids);
        if (npcs != null && npcs.length > 0)
            return npcs[0];
        return null;
    }

    public static boolean interact(ACamera camera, String option, boolean ranged, String... names) {
        RSNPC npc = getNearest(names);
        if (npc != null) {
            return interact(camera, option, ranged, npc);
        }

        return false;
    }

    public static boolean interact(ACamera camera, String option, boolean ranged, RSNPC npc) {
        if (npc != null) {
            if (!ranged && !Movement.canReach(npc)) {
                camera.turnToTile(npc);
                if (!Movement.walkTo(npc)) {
                    WebWalking.walkTo(npc);
                }
            }

            if (!npc.isOnScreen() || !npc.isClickable()) {
                Camera.turnToTile(npc);
            }

            return Clicking.click(option, npc);
        }

        return false;
    }

    public static boolean interact(ACamera camera, String option, RSNPC npc, BooleanLambda successCondition) {
        return interact(camera, option, false, npc, successCondition, DEFAULT_MIN_TIMEOUT, DEFAULT_MAX_TIMEOUT);
    }

    public static boolean interact(ACamera camera, String option, boolean ranged, RSNPC npc, BooleanLambda successCondition) {
        return interact(camera, option, ranged, npc, successCondition, DEFAULT_MIN_TIMEOUT, DEFAULT_MAX_TIMEOUT);
    }

    public static boolean interact(ACamera camera, String option, boolean ranged, RSNPC npc, BooleanLambda successCondition, int t1, int t2) {
        if (interact(camera, option, ranged, npc)) {
            Condition walkingCondition = Movement.getWalkingCondition(successCondition);
            while (!walkingCondition.active())
                General.sleep(100, 250);

            return successCondition.active();
        }

        return false;
    }

    public static boolean interact(ACamera camera, String option, String npc, BooleanLambda successCondition) {
        return interact(camera, option, false, npc, successCondition, DEFAULT_MIN_TIMEOUT, DEFAULT_MAX_TIMEOUT);
    }

    public static boolean interact(ACamera camera, String option, boolean ranged, String npc, BooleanLambda successCondition) {
        return interact(camera, option, ranged, npc, successCondition, DEFAULT_MIN_TIMEOUT, DEFAULT_MAX_TIMEOUT);
    }

    public static boolean interact(ACamera camera, String option, boolean ranged, String npc, BooleanLambda successCondition, int t1, int t2) {
        if (interact(camera, option, ranged, npc)) {
            Condition walkingCondition = Movement.getWalkingCondition(successCondition);
            while (!walkingCondition.active())
                General.sleep(100, 250);

            return successCondition.active();
        }

        return false;
    }
}
