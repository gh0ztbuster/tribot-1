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
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSTile;
import scripts.api.patterns.BaseScript;
import scripts.api.patterns.Node;
import scripts.api.util.InterfaceUtil;
import scripts.api.util.Movement;
import scripts.api.util.NPCUtil;

public class MageNode extends Node {
    private final String NPC_NAME = "Magic Instructor";
    private final RSTile MAGE_TILE = new RSTile(3141, 3086);

    public MageNode(BaseScript script) {
        super(script);
    }
    @Override
    public void execute() {
        System.out.println("Executing MageNode.");
        if (shouldLeavePrayer() || shouldWalkToMage() && !isTalkingToMage() && !shouldLeaveIsland()) {
            script.setStatus("TUTORIAL: Walking to " + NPC_NAME + ".");
            RSNPC mage = NPCUtil.getNearest(NPC_NAME);
            if (mage == null) {
                if (Movement.isInLoadedRegion(MAGE_TILE)) {
                    System.out.println("Movement.walkTo");
                    Movement.walkTo(MAGE_TILE);
                } else {
                    System.out.println("Walking.blindWalkTo");
                    if (Movement.canReach(MAGE_TILE))
                        Walking.blindWalkTo(MAGE_TILE);
                    else
                        Movement.walkTo(MAGE_TILE);
                }
            } else {
                NPCUtil.interact(script.getCamera(), "Talk-to", mage, () -> isTalkingToMage());
            }
        }

        if (shouldOpenMagic()) {
            script.setStatus("TUTORIAL: Opening magic tab.");
            if (GameTab.open(GameTab.TABS.MAGIC)) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return isTalkingToMage();
                    }
                }, General.random(1000, 1250));
            }
        }
        
        if (isTalkingToMage()) {
            NPCChat.clickContinue(true);
        }
        
        if (shouldCastOnChicken()) {
            script.setStatus("TUTORIAL: Using Wind strike on chicken.");
            Movement.walkTo(new RSTile(3139, 3091));
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    return !Player.isMoving();
                }
            }, General.random(2000, 3000));
            if (!Magic.isSpellSelected())
                if (Magic.selectSpell("Wind Strike"))
                    Timing.waitCondition(new scripts.api.misc.Condition(() -> Magic.isSpellSelected()), General.random(1000, 1250));

            if (Magic.getSelectedSpellName() != null && Magic.getSelectedSpellName().equals("Wind Strike")) {
                if (NPCUtil.interact(script.getCamera(), "Cast Wind Strike -> Chicken", true, "Chicken")) {
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            return isTalkingToMage();
                        }
                    }, General.random(1000, 1250));
                }
            }
        }
        
        if (shouldLeaveIsland()) {
            if (NPCChat.selectOption("Yes.", true)) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return isTalkingToMage();
                    }
                }, General.random(1000, 1250));
            }
        }
    }

    private boolean shouldLeaveIsland() {
        return InterfaceUtil.isOpen("Do you want to go to the main");
    }

    private boolean shouldCastOnChicken() {
        return Game.getSetting(281) == 650;
    }

    private boolean shouldOpenMagic() {
        return Game.getSetting(281) == 630;
    }

    private boolean isTalkingToMage() {
        return NPCUtil.isTalkingTo(NPC_NAME) || NPCUtil.isTalkingTo(Player.getRSPlayer().getName()) || InterfaceUtil.isOpen("gives you some") || InterfaceUtil.isOpen("When you get to Lumbridge,") || InterfaceUtil.isOpen("the castle's court");
    }

    private boolean shouldWalkToMage() {
        return Game.getSetting(281) == 620 || Game.getSetting(281) == 640 || Game.getSetting(281) == 670;
    }

    private boolean shouldLeavePrayer() {
        return Game.getSetting(281) == 610;
    }

    @Override
    public boolean validate() {
        return shouldLeavePrayer() || shouldWalkToMage() || shouldOpenMagic() || shouldCastOnChicken() || shouldLeaveIsland();
    }
    
}
