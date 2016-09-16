package scripts.api.misc;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Player;
import scripts.api.data.Bag;
import scripts.api.util.NPCUtil;


/**
 * @author Laniax
 */
public class Condition extends org.tribot.api.types.generic.Condition implements BooleanLambda {

    private BooleanLambda lambda;
    private Bag bag;

    /**
     * Create a regular Condition object.
     * Note that you should override the #active(); method.
     */
    public Condition() {
        super();
        this.lambda = null;
    }

    /**
     * Create a regular Condition object.
     * You can specify your active method in the constructor as a lambda.
     * Example: new Condition(() -> return Inventory.isEmpty());
     * @param lambda
     */
    public Condition(BooleanLambda lambda) {
        super();
        this.lambda = lambda;
    }

    /**
     * Returns a dynamic bag that can be used to store extra data.
     * Usefull when using lambdas.
     * @return
     */
    public Bag getBag() {
        if (this.bag == null) {
            this.bag = new Bag();
        }
        return this.bag;
    }

    /**
     * Sets/replace the lambda.
     * @param lambda
     */
    public void setLambda(BooleanLambda lambda) {
        this.lambda = lambda;
    }

    @Override
    public boolean active() {
        int shortReaction = getBag().get("reaction_short", 50);
        int longReaction = getBag().get("reaction_long", 100);
        if (lambda != null) {
            General.sleep(shortReaction, longReaction);
            return lambda.active();
        }

        throw new RuntimeException("You have to override the active() method for a Condition object or specify a lambda in the constructor.");
    }

    /**
     * Waits for this condition with the given timeout
     * See {@link Timing#waitCondition(org.tribot.api.types.generic.Condition, long)} for more details.
     * @param timeout
     * @return
     */
    public boolean execute(long timeout) {
        return Timing.waitCondition(this, timeout);
    }

    /**
     * Waits for this condition with the given timeout, interpreted as General.random(min,max)
     * See {@link Timing#waitCondition(org.tribot.api.types.generic.Condition, long)} for more details.
     * @param min
     * @param max
     * @return
     */
    public boolean execute(int min, int max) {
        return Timing.waitCondition(this, General.random(min, max));
    }

    /**
     * Returns a condition that returns true when the bank is open and loaded.
     */

    public static boolean MovementWait(BooleanLambda lambda, int t1, int t2) {
        Timing.waitCondition(new Condition(() -> Player.getRSPlayer().isMoving() || lambda.active()), General.random(t1, t2));
        if (Player.isMoving() && !lambda.active()) {
            Timing.waitCondition(new Condition(() -> !Player.isMoving() || lambda.active()), General.random(5000, 7500));
            if (!lambda.active()) {
                return Timing.waitCondition(new Condition(() -> lambda.active()), General.random(t1, t2));
            }
            return true;
        }
        return lambda.active();
    }
}
