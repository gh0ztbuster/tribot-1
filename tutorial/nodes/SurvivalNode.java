/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts.tutorial.nodes;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.*;
import org.tribot.api2007.types.*;
import scripts.api.patterns.BaseScript;
import scripts.api.patterns.Node;
import scripts.api.util.*;

public class SurvivalNode extends Node {
    private final int INTERFACE = 548;
    private final int INVENTORY_TAB = 48;
    
    private final String NPC_NAME = "Survival Expert";

    public SurvivalNode(BaseScript script) {
        super(script);
    }
     
    @Override
    public void execute() {
        System.out.println("Executing SurvivalNode.");
        if (shouldTalkToExpert() && !NPCUtil.isTalkingTo(NPC_NAME)) {
            script.setStatus("TUTORIAL: Talking to " + NPC_NAME + ".");
            NPCUtil.interact(script.getCamera(), "Talk-to", NPC_NAME, () -> NPCUtil.isTalkingTo(NPC_NAME));
        } else if (shouldTalkToExpert()) {
            NPCChat.clickContinue(true);
        } else if (shouldOpenInventory()) {
            script.setStatus("TUTORIAL: Opening inventory.");
            if (Game.getSetting(1021) == 0) {
                NPCChat.clickContinue(true);
            } else {
                RSInterface inventory = InterfaceUtil.get(INTERFACE, INVENTORY_TAB);
                if (inventory != null) {
                    inventory.click();
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            return Game.getSetting(1021) == 0;
                        }
                    }, General.random(1000, 1250));
                }
            }
        } else if (shouldCutTree()) {
            script.setStatus("TUTORIAL: Chopping tree.");
            RSObject tree = ObjectUtil.getNearest("Tree");
            if (tree != null) {
                if (ObjectUtil.interact(script.getCamera(), Util.array("Cut-down Tree", "Chop down Tree", "Cut down Tree", "Chop-down Tree"), tree)) {
                    Condition condition = Movement.animationWait(() -> shouldMakeFire(), 5000);
                    while (!condition.active())
                        General.sleep(250, 500);
                }
            }
        } else if (shouldMakeFire()) {
            script.setStatus("TUTORIAL: Making a fire.");
            if (shouldContinue()) {
                General.sleep(200, 400);
            } else {
                RSObject[] fires = ObjectUtil.getAt(Player.getPosition());
                while (fires != null) {
                    RSTile pos = Player.getPosition();
                    pos = new RSTile(pos.getX() + General.random(-2, 2), pos.getY() + General.random(-2, 2));
                    fires = ObjectUtil.getAt(pos);
                    if (fires == null) {
                        Movement.walkTo(pos);
                        Timing.waitCondition(new Condition() {
                            @Override
                            public boolean active() {
                                return !Player.isMoving();
                            }
                        }, General.random(2000, 3000));
                    }

                    General.sleep(250, 500);
                }

                if (!InventoryUtil.isUsing("Tinderbox"))
                    InventoryUtil.interact("Tinderbox", "Use");
                if (InventoryUtil.isUsing("Tinderbox")) {
                    if (InventoryUtil.interact("Logs", "Use Tinderbox -> Logs")) {
                        if (Timing.waitCondition(new Condition() {
                            @Override
                            public boolean active() {
                                return Player.getAnimation() != -1;
                            }
                        }, General.random(1250, 2500))) {
                            Condition condition = Movement.animationWait(() -> shouldOpenStats(), 5000);
                            while (!condition.active())
                                General.sleep(250, 500);
                        }
                    }
                }
            }
        } else if (shouldOpenStats()) {
            script.setStatus("TUTORIAL: Opening stats tab.");
            if (GameTab.open(GameTab.TABS.STATS)) {
                Timing.waitCondition(new scripts.api.misc.Condition(() -> shouldTalkToExpert()), General.random(1250, 2000));
            }
        } else if (shouldFish()) {
            if (shouldContinue()) {
                General.sleep(200, 400);
            } else {
                script.setStatus("TUTORIAL: Fishing some shrimp.");
                if (Player.getAnimation() == -1) {
                    RSNPC spot = NPCUtil.getNearest("Fishing spot", "Fishing Spot");
                    if (spot != null) {
                        if (NPCUtil.interact(script.getCamera(), "Net", spot, () -> Player.getAnimation() != -1)) {
                            Condition condition = Movement.animationWait(() -> Player.getAnimation() == -1, 5000);
                            while (!condition.active())
                                General.sleep(250, 500);
                        }
                    }
                }
            }
        } else if (shouldCook()) {
            script.setStatus("TUTORIAL: Cooking shrimp.");
            if (!InventoryUtil.isUsing("Raw shrimps"))
                InventoryUtil.interact("Raw shrimps", "Use");
            if (Timing.waitUptext("Use Raw shrimps ->", 750)) {
                RSObject fire = ObjectUtil.getNearest("Fire");
                if (fire != null) {
                    if (ObjectUtil.interact(script.getCamera(), Util.array("Use Raw shrimps -> Fire"), fire)) {
                        Condition condition = Movement.animationWait(() -> Player.getAnimation() == -1, 5000);
                        while (!condition.active())
                            General.sleep(250, 500);
                    }
                }
            }
        }
    }

    private boolean shouldContinue() {
        RSInterfaceChild click = NPCChat.getClickContinueInterface();
        if (click != null && click.isClickable() && !click.isHidden()) {
            click.click();
            return true;
        }
        return false;
    }

    private boolean shouldCook() {
        return (Game.getSetting(281) >= 90 && Game.getSetting(281) <= 110) && InventoryUtil.getCount("Raw shrimps", "Burnt shrimp", "Cooked shrimp") >= 2;
    }

    private boolean shouldFish() {
        return Game.getSetting(281) == 80 || (Game.getSetting(281) == 90 && InventoryUtil.getCount("Raw shrimps", "Burnt shrimp", "Cooked shrimp") < 2);
    }

    private boolean shouldOpenStats() {
        return Game.getSetting(281) == 60;
    }

    private boolean shouldMakeFire() {
        return Game.getSetting(281) == 50;
    }

    private boolean shouldCutTree() {
        return Game.getSetting(281) == 40;
    }

    private boolean shouldOpenInventory() {
        return Game.getSetting(281) == 30;
    }

    private boolean shouldTalkToExpert() {
        return Game.getSetting(281) == 20 || Game.getSetting(281) == 70;
    }

    @Override
    public boolean validate() {
        return shouldTalkToExpert() || shouldOpenInventory() || shouldCutTree() || shouldMakeFire() || shouldOpenStats() || shouldFish() || shouldCook();
    }
    
}
