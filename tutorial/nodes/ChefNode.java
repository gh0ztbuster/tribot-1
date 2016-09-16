/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts.tutorial.nodes;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Game;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.NPCChat;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import scripts.api.patterns.BaseScript;
import scripts.api.patterns.Node;
import scripts.api.util.*;

/**
 *
 * @author Spencer
 */
public class ChefNode extends Node {
    private final String NPC_NAME = "Master Chef";

    public ChefNode(BaseScript script) {
        super(script);
    }
     
    @Override
    public void execute() {
        System.out.println("Executing ChefNode.");
        if (shouldOpenGate()) {
            script.setStatus("TUTORIAL: Walking to Chef.");
            RSNPC cook = NPCUtil.getNearest(NPC_NAME);
            if (cook != null) {
                NPCUtil.interact(script.getCamera(), "Talk-to", NPC_NAME, () -> NPCUtil.isTalkingTo(NPC_NAME));
            } else {
                Movement.walkTo(new RSTile(3079, 3084, 0));
            }
        } else if (shouldOpenDoorToCook() || (shouldTalkToCook() && !NPCUtil.isTalkingTo(NPC_NAME) && !InterfaceUtil.isOpen("The Cooking Guide gives you"))) {
            script.setStatus("TUTORIAL: Talking with Chef.");
            NPCUtil.interact(script.getCamera(), "Talk-to", NPC_NAME, () -> NPCUtil.isTalkingTo(NPC_NAME));
        } else if (NPCUtil.isTalkingTo(NPC_NAME) || InterfaceUtil.isOpen("The Cooking Guide gives you")) {
            NPCChat.clickContinue(true);
        } else if (shouldMakeDough()) {
            script.setStatus("TUTORIAL: Making dough.");
            if (!InventoryUtil.isUsing("Pot of flour"))
                InventoryUtil.interact("Pot of flour", "Use");

            if (InventoryUtil.isUsing("Pot of flour")) {
                if (InventoryUtil.interact("Bucket of water", "Use Pot of flour -> Bucket of water")) {
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            return shouldMakeBread();
                        }
                    }, General.random(2000, 3000));
                }
            }
        } else if (shouldMakeBread()) {
            script.setStatus("TUTORIAL: Cooking bread.");
            RSObject range = ObjectUtil.getNearest("Range");
            if (range != null) {
                if (!ObjectUtil.isVisable(range))
                    script.getCamera().turnToTile(range);

                if (!InventoryUtil.isUsing("Bread dough")) {
                    InventoryUtil.interact("Bread dough", "Use");
                } else {
                    ObjectUtil.interact(script.getCamera(), Util.array("Use Bread dough -> Range"), range, () -> shouldOpenMusic(), 4000, 6000);
                }
            }
        } else if (shouldOpenMusic()) {
            script.setStatus("TUTORIAL: Opening music tab.");
            if (GameTab.open(GameTab.TABS.MUSIC)) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return shouldLeaveChef();
                    }
                }, General.random(1000, 1250));
            }
        } else if (shouldLeaveChef()) {
            script.setStatus("TUTORIAL: Leaving chef area.");
            Movement.walkTo(new RSTile(3071, 3092), () -> !validate());
        }
    }

    private boolean shouldLeaveChef() {
        return Game.getSetting(281) == 180;
    }

    private boolean shouldOpenMusic() {
        return Game.getSetting(281) == 170;
    }

    private boolean shouldMakeBread() {
        return Game.getSetting(281) == 160;
    }

    private boolean shouldMakeDough() {
        return Game.getSetting(281) == 150;
    }

    private boolean shouldTalkToCook() {
        return Game.getSetting(281) == 140;
    }

    private boolean shouldOpenDoorToCook() {
        return Game.getSetting(281) == 130;
    }


    private boolean shouldOpenGate() {
        return Game.getSetting(281) == 120;
    }

    @Override
    public boolean validate() {
        return shouldOpenGate() || shouldOpenDoorToCook() || shouldTalkToCook() || shouldMakeDough() || shouldMakeBread() || shouldOpenMusic() || shouldLeaveChef();
    }
    
}