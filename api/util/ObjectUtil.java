/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts.api.util;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.Game;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSObjectDefinition;
import org.tribot.api2007.types.RSTile;
import scripts.api.misc.BooleanLambda;
import scripts.api.misc.Condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class ObjectUtil {
    private static final int DEFAULT_MIN_TIMEOUT = 1250;
    private static final int DEFAULT_MAX_TIMEOUT = 2000;

    public static RSObject[] get() {
        RSObject[] objs = Objects.getAll(50);
        return objs.length > 0 ? objs : null;
    }
    public static RSObject[] get(String... names) {
        RSObject[] objs = Objects.find(50, names);
        return objs.length > 0 ? objs : null;
    }
    
    public static RSObject[] getAt(RSTile tile) {
        RSObject[] objs = Objects.getAt(tile);
        return objs.length > 0 ? objs : null;
    }

    public static RSObject getAt(RSTile tile, String... names) {
        final RSObject[] rsObjects = Arrays.asList(getAt(tile)).stream().filter(o -> hasName(o, names)).toArray(RSObject[]::new);
        return rsObjects != null && rsObjects.length > 0 ? rsObjects[0] : null;
    }
    
    public static boolean hasAction(RSObject obj, String... options) {
        final RSObjectDefinition def = obj.getDefinition();
        if (def != null) {
            final String[] actions = def.getActions();
            if (actions != null && actions.length > 0) {
                for (String action : actions) {
                    if (Util.contains(options, action))
                        return true;
                }
            }
        }
        
        return false;
    }

    public static boolean hasName(RSObject obj, String... names) {
        final RSObjectDefinition def = obj.getDefinition();
        if (def != null) {
            String objectName = def.getName();
            if (objectName != null) {
                return Util.contains(names, objectName);
            }
        }

        return false;
    }

    public static RSObject[] getInArea(RSArea area, String... names) {
        return Objects.findNearest(50, new Filter<RSObject>() {
            @Override
            public boolean accept(RSObject rsObject) {
                return area.contains(rsObject) && hasName(rsObject, names);
            }
        });
    }
    
    public static RSObject getNearestInRange(int range, String... names) {
        final RSObject[] objs = Objects.findNearest(range, names);
        if (objs != null && objs.length > 0) {
            return objs[0];
        }
        return null;
    }
    
    
    public static RSObject getNearest(String... names) {
        return getNearestInRange(50, names);
    }

    public static RSObject[] getWith(String... options) {
        return new ArrayList<>(Arrays.asList(get())).stream().filter((o) -> hasAction(o, options)).toArray(RSObject[]::new);
    }
    
    public static RSObject getNearestByWith(RSTile tile, String... options) {
        if (tile == null) return null;

        final RSObject[] rsObjects = getWith(options);
        Arrays.sort(rsObjects, (o1, o2) -> tile.distanceTo(o1) - tile.distanceTo(o2));

        return rsObjects != null && rsObjects.length > 0 ? rsObjects[0] : null;
    }
    
    public static RSObject getNearestWith(String... options) {
        return getNearestByWith(Player.getPosition(), options);
    }
    
    public static RSObject getNearestByInRange(RSTile tile, int range, String... names) {
        if (tile == null) return null;

        final RSObject[] rsObjects = getByInRange(tile, range, names);
        Arrays.sort(rsObjects, (o1, o2) -> tile.distanceTo(o1) - tile.distanceTo(o2));

        return rsObjects != null && rsObjects.length > 0 ? rsObjects[0] : null;
    }

    public static RSObject[] getByInRange(RSTile tile, int range, String... names) {
        if (tile == null) return null;

        final RSObject[] rsObjects = new ArrayList<>(Arrays.asList(get(names))).stream().filter((o) -> tile.distanceTo(o.getPosition()) <= range).toArray(RSObject[]::new);
        return rsObjects;
    }
    
    public static RSObject getNearestBy(RSTile tile, String... names) {
        return getNearestByInRange(tile, 50, names);
    }
    
    public static boolean canInteract(String... names) {
        final RSObject obj = getNearest(names);
        if (obj != null) {
            return canInteract(obj);
        }
        return false;
    }
    
    public static boolean canInteract(RSObject obj) {
        return isVisable(obj) && Player.getPosition().distanceTo(obj) <= 6;
    }

    public static boolean isVisable(RSObject obj) {
        return obj.isOnScreen() && obj.isClickable();
    }

    public static boolean interact(ACamera camera, String options[], String objName) {
        return interact(camera, options, getNearest(objName));
    }

    public static boolean interact(ACamera camera, String options[], RSObject obj) {
        if (obj != null) {
            if (!canInteract(obj)) {
                camera.turnToTile(obj);
            }

            if (!Movement.canReach(obj) || Player.getPosition().distanceTo(obj) > 6) {
                Movement.walkTo(obj);
            }

            return Clicking.click(options, obj);
        }
        return false;
    }

    public static boolean interact(ACamera camera, String options[], RSObject obj, BooleanLambda successCondition) {
        return interact(camera, options, obj, successCondition, DEFAULT_MIN_TIMEOUT, DEFAULT_MAX_TIMEOUT);
    }

    public static boolean interact(ACamera camera, String options[], RSObject obj, BooleanLambda successCondition, int t1, int t2) {
        if (interact(camera, options, obj)) {
            Condition walkingCondition = Movement.getWalkingCondition(successCondition);
            while (!walkingCondition.active())
                General.sleep(100, 250);

            return successCondition.active();
        }

        return false;
    }

    public static boolean interact(ACamera camera, String options[], String obj, BooleanLambda successCondition) {
        return interact(camera, options, obj, successCondition, DEFAULT_MIN_TIMEOUT, DEFAULT_MAX_TIMEOUT);
    }

    public static boolean interact(ACamera camera, String options[], String obj, BooleanLambda successCondition, int t1, int t2) {
        if (interact(camera, options, obj)) {
            Condition walkingCondition = Movement.getWalkingCondition(successCondition);
            while (!walkingCondition.active())
                General.sleep(100, 250);

            return successCondition.active();
        }

        return false;
    }

    public static boolean interact(ACamera camera, String options[], RSTile overrideTile, RSObject obj) {
        if (obj != null) {
            if (!canInteract(obj)) {
                camera.turnToTile(overrideTile);
            }

            if (!Movement.canReach(overrideTile) || Player.getPosition().distanceTo(overrideTile) > 6) {
                Movement.walkTo(overrideTile);
            }

            return Clicking.click(options, obj);
        }
        return false;
    }

    public static boolean interact(ACamera camera, String options[], RSTile overrideTile, RSObject obj, BooleanLambda successCondition) {
        return interact(camera, options, overrideTile, obj, successCondition, DEFAULT_MIN_TIMEOUT, DEFAULT_MAX_TIMEOUT);
    }

    public static boolean interact(ACamera camera, String options[], RSTile overrideTile, RSObject obj, BooleanLambda successCondition, int t1, int t2) {
        if (interact(camera, options, overrideTile, obj)) {
            Condition walkingCondition = Movement.getWalkingCondition(successCondition);
            while (!walkingCondition.active())
                General.sleep(100, 250);

            return successCondition.active();
        }

        return false;
    }

    public static boolean interact(ACamera camera, String options[], RSTile overrideTile, String obj, BooleanLambda successCondition, int t1, int t2) {
        if (interact(camera, options, overrideTile, getNearest(obj))) {
            Condition walkingCondition = Movement.getWalkingCondition(successCondition);
            while (!walkingCondition.active())
                General.sleep(100, 250);

            return successCondition.active();
        }

        return false;
    }

    public static boolean interact(ACamera camera, String options[], RSTile overrideTile, String obj, BooleanLambda successCondition) {
        return interact(camera, options, overrideTile, obj, successCondition, DEFAULT_MIN_TIMEOUT, DEFAULT_MAX_TIMEOUT);
    }
}
