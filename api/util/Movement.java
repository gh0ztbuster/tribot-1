package scripts.api.util;

import org.tribot.api.General;
import org.tribot.api.interfaces.Positionable;
import org.tribot.api.util.Sorting;
import org.tribot.api.util.abc.preferences.WalkingPreference;
import org.tribot.api2007.*;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.api2007.util.DPathNavigator;
import scripts.api.antiban.Antiban;
import scripts.api.data.Bag;
import scripts.api.misc.BooleanLambda;
import scripts.api.misc.Condition;

import java.util.ArrayList;

/**
 * Helper class that manages movement logic.
 *
 * @author Laniax
 */
public class Movement {

    private static DPathNavigator nav = new DPathNavigator();

    private static final int NOT_MOVING_TIMEOUT = 2000;

    static {
        nav.setStoppingConditionCheckDelay(50);
    }

    public static DPathNavigator getNavigator() {
        return nav;
    }

    /**
     * Checks if we can reach the specified Positionable (RSTile/RSObject/RSPlayer/RSGroundItem etc)
     *
     * @param toReach
     * @return if we can reach the positionable or not.
     */
    public static boolean canReach(final Positionable toReach) {

        return PathFinding.canReach(toReach, toReach instanceof RSObject);

    }

    /**
     * Check if a position is in the currently loaded region.
     *
     * @param pos to check
     * @return true if in the region, false otherwise.
     */
    public static boolean isInLoadedRegion(Positionable pos) {

        final RSTile base = new RSTile(Game.getBaseX(), Game.getBaseY());
        final RSArea chunk = new RSArea(base, new RSTile(base.getX() + 103, base.getY() + 103));

        return chunk.contains(pos) && pos.getPosition().getPlane() == Game.getPlane();
    }

    /**
     * Gets all the walkable tiles in the {@link RSArea}.
     *
     * @param area
     * @return array of walkable tiles, empty if none are found.
     */
    public static RSTile[] getAllWalkableTiles(RSArea area) {

        ArrayList<RSTile> walkables = new ArrayList<RSTile>();

        for (RSTile tile : area.getAllTiles()) {
            if (PathFinding.isTileWalkable(tile))
                walkables.add(tile);
        }

        return walkables.toArray(new RSTile[walkables.size()]);
    }

    /**
     * Walks to the destination using DPathNavigator
     *
     * @param posToWalk
     * @return true if reached the destination, false otherwise.
     */
    public static boolean walkToPrecise(Positionable posToWalk) {

        // DPathNavigator cannot traverse outside the currently loaded region
        if (!isInLoadedRegion(posToWalk))
            return false;

        return nav.traverse(posToWalk);
    }

    /**
     * Walks to the destination using WebWalking
     *
     * @param posToWalk
     * @return true if reached the destination, false otherwise.
     */
    public static boolean webWalkTo(final Positionable posToWalk) {

        Antiban.activateRun();

        if (Antiban.getWalkingPreference(Player.getPosition().distanceTo(posToWalk)) == WalkingPreference.SCREEN) {

            RSTile[] path = Walking.generateStraightScreenPath(posToWalk);

            if (Walking.walkScreenPath(path, getWalkingCondition(posToWalk), General.random(10000, 12500)))
                return true;
        }


        return WebWalking.walkTo(posToWalk, getWalkingCondition(posToWalk), 100);
    }

    public static void setUseCustomDoors(RSObject[] doors) {
        nav.overrideDoorCache(true, doors);
    }

    public static void setUseDefaultDoors() {
        nav.overrideDoorCache(false, null);
    }

    public static void setExcludeTiles(final Positionable[] tiles) {

        nav.setExcludeTiles(tiles);
    }

    public static Positionable[] getExcludeTiles() {

        return nav.getExcludeTiles();
    }

    /**
     * Walks to the position using either DPathNavigator for close by precision or WebWalking for greater lengths.
     * <p>
     * Checks if run can be toggled.
     *
     * @param posToWalk
     * @return if successfully reached destination or not.
     */

    public static boolean walkTo(Positionable posToWalk, Condition stoppingCondition) {

        //if (posToWalk instanceof RSTile)
        //    PaintHelper.destination_tile = (RSTile) posToWalk;

        final RSTile tile = posToWalk.getPosition();

        if (tile == null)
            return false;

        Antiban.activateRun();

        nav.setStoppingCondition(stoppingCondition);

        if (isInLoadedRegion(posToWalk)) {

            if (Antiban.getWalkingPreference(Player.getPosition().distanceTo(posToWalk)) == WalkingPreference.SCREEN) {

                RSTile[] path = Walking.generateStraightScreenPath(posToWalk);

                System.out.println("ScreenWalking to tile: " + posToWalk.getPosition().getX() + ", " + posToWalk.getPosition().getY());
                return Walking.walkScreenPath(path, new Condition(Combat::isUnderAttack), 100);
            }

            // Check if the tile we want to walk to is actually walkable.
            // DPathNavigator will fail if it isnt, so select the nearest best tile in a 5 tile radius around the destination.
            if (!PathFinding.isTileWalkable(posToWalk)) {
                RSArea area = new RSArea(posToWalk, 5);
                RSTile[] walkables = getAllWalkableTiles(area);

                if (walkables.length == 0)
                    return false;

                Sorting.sortByDistance(walkables, posToWalk, true);
                posToWalk = walkables[0];
            }

            System.out.println("NavWalking to tile: " + posToWalk.getPosition().getX() + ", " + posToWalk.getPosition().getY());
            nav.overrideStartPosition(Player.getPosition());
            return nav.traverse(posToWalk);

        } else {

            final Positionable finalPosToWalk = posToWalk;

            System.out.println("WebWalking to tile: " + posToWalk.getPosition().getX() + ", " + posToWalk.getPosition().getY());
            if (!WebWalking.walkTo(posToWalk, new Condition() {
                @Override
                public boolean active() {
                    return isInLoadedRegion(finalPosToWalk);
                }
            }, 1000)) {
                if (!PathFinding.isTileWalkable(posToWalk)) {
                    RSArea area = new RSArea(posToWalk, 5);
                    RSTile[] walkables = getAllWalkableTiles(area);

                    if (walkables.length == 0)
                        return false;

                    Sorting.sortByDistance(walkables, posToWalk, true);
                    posToWalk = walkables[0];
                }

                System.out.println("NavWalking to tile: " + posToWalk.getPosition().getX() + ", " + posToWalk.getPosition().getY());
                nav.overrideStartPosition(Player.getPosition());
                return nav.traverse(posToWalk);
            }
        }

        return false;
    }

    public static boolean walkTo(Positionable posToWalk, BooleanLambda stoppingCondition) {
        return walkTo(posToWalk, new Condition(stoppingCondition));
    }

    public static boolean walkTo(Positionable posToWalk) {
        return walkTo(posToWalk, getWalkingCondition(posToWalk));
    }

    public static Condition getWalkingCondition(BooleanLambda lambda) {

        return new Condition() {

            long notMovingSince = 0;

            @Override
            public boolean active() {

                if (!Player.isMoving()) {
                    if (notMovingSince == 0) {
                        notMovingSince = System.currentTimeMillis();
                    } else if (System.currentTimeMillis() - notMovingSince > NOT_MOVING_TIMEOUT) {
                        return true;
                    }
                } else {
                    notMovingSince = 0;
                }

                return lambda.active();

            }
        };
    }

    public static Condition animationWait(BooleanLambda lambda, int animationLength) {
        return new Condition() {

            long notAnimatingSince = 0;

            @Override
            public boolean active() {

                if (!Player.isMoving() && Player.getAnimation() == -1) {
                    if (notAnimatingSince == 0) {
                        notAnimatingSince = System.currentTimeMillis();
                    } else if (System.currentTimeMillis() - notAnimatingSince > animationLength) {
                        return true;
                    }
                } else {
                    notAnimatingSince = 0;
                }

                return lambda.active();
            }
        };
    }

    /**
     * Gets a stopping condition that keeps track for when you reach the target, or if you stood still for to long.
     *
     * @param destination
     * @return
     */
    public static Condition getWalkingCondition(final Positionable destination) {

        Condition condition = new Condition();

        Bag bag = condition.getBag();

        condition.setLambda(() -> {

            long not_moving_since = bag.get("not_moving_since", 0L);

            if (!Player.isMoving()) {
                if (not_moving_since == 0L) {
                    bag.addOrUpdate("not_moving_since", System.currentTimeMillis());
                } else if (System.currentTimeMillis() - not_moving_since > NOT_MOVING_TIMEOUT) {
                    return true;
                }
            } else {
                bag.addOrUpdate("not_moving_since", 0L);
            }

            return Player.getPosition() == destination;
        });

        return condition;
    }


    /**
     * Walks to the position by straight user-made paths. Will try to handle doors.
     * This is mostly used in areas which WebWalking has not mapped.
     * <p>
     * Checks if run can be toggled.
     *
     * @param path
     * @return if succesfully reached destination or not.
     */
    public static boolean walkPath(final RSTile[] path) {

        Condition stoppingCondition = getWalkingCondition(path[path.length - 1]);

        Antiban.activateRun();

        if (Antiban.getWalkingPreference(Player.getPosition().distanceTo(path[path.length - 1])) == WalkingPreference.SCREEN) {
            return Walking.walkScreenPath(path, stoppingCondition, General.random(4000, 5000));
        } else
            return Walking.walkPath(path, stoppingCondition, General.random(4000, 5000));
    }


}
