/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts.quester.nodes;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.*;
import org.tribot.api2007.Banking;
import org.tribot.api2007.types.*;
import scripts.api.patterns.BaseScript;
import scripts.api.patterns.Node;
import scripts.api.util.*;

public class SheepShearNode extends Node {
    private static final int SETTING = 179;
    private final int SHEAR_ID = 1735;
    private final int WOOL_ID = 1737;
    private final int BALL_WOOL_ID = 1759;

    private final RSTile FARMER_TILE = new RSTile(3189, 3277, 0);
    private final RSArea SHEEP_PEN = new RSArea(new RSTile[] { new RSTile(3194, 3257, 0), new RSTile(3193, 3276, 0), new RSTile(3204, 3276, 0), new RSTile(3206, 3275, 0), new RSTile(3209, 3275, 0), new RSTile(3211, 3275, 0), new RSTile(3211, 3273, 0), new RSTile(3212, 3268, 0), new RSTile(3212, 3259, 0), new RSTile(3212, 3257, 0) });
    private final String NPC_NAME = "Fred the Farmer";
    private boolean complete = false;
    public SheepShearNode(BaseScript script) {
        super(script);
    }

    @Override
    public void execute() {
        if (Game.getSetting(SETTING) == 21) {

        } else if (Game.getSetting(SETTING) == 0) {
            if (Inventory.getCount(SHEAR_ID, WOOL_ID, BALL_WOOL_ID) == 0 && getInventoryCount() > 0) {
                script.setStatus("SHEEP SHEARER: Banking items.");
                if (!Banking.isInBank()) {
                    WebWalking.walkToBank();
                } else if (!Banking.isBankScreenOpen()) {
                    Banking.openBank();
                } else {
                    Banking.depositAll();
                }
            } else if (Banking.isBankScreenOpen()) {
                Banking.close();
            } else {
                RSNPC fred = NPCUtil.getNearest(NPC_NAME);
                if (fred == null) {
                    script.setStatus("SHEEP SHEARER: Walking to Fred.");
                    if (!Movement.walkTo(FARMER_TILE)) {
                        WebWalking.walkTo(FARMER_TILE);
                    }
                } else {
                    script.setStatus("SHEEP SHEARER: Talking to Fred.");
                    if (NPCUtil.isTalkingTo(NPC_NAME)) {
                        NPCChat.clickContinue(true);
                    } else if (InterfaceUtil.isOpen("I'm looking for something to kill")) {
                        NPCChat.selectOption("I'm looking for a quest", true);
                    } else if (InterfaceUtil.isOpen("That doesn't sound a very exciting quest")) {
                        NPCChat.selectOption("Yes okay. I can do that.", true);
                    } else {
                        NPCUtil.interact(script.getCamera(), "Talk-to", fred, () -> NPCUtil.isTalkingTo(NPC_NAME));
                    }
                }
            }
        } else {
            if (Inventory.getCount(SHEAR_ID) == 0) {
                script.setStatus("SHEEP SHEARER: Grabbing shear.");
                if (Player.getPosition().distanceTo(FARMER_TILE) > 3) {
                    Movement.walkTo(FARMER_TILE);
                } else {
                    RSGroundItem[] shears = GroundItems.findNearest(SHEAR_ID);
                    if (shears != null && shears.length > 0) {
                        RSGroundItem shear = shears[0];
                        if (!shear.isOnScreen() || !shear.isClickable()) {
                            Movement.walkTo(shear.getPosition());
                        } else {
                            shear.click();
                            General.sleep(2500);
                        }
                    }
                }
            } else {
                if (Inventory.getCount(BALL_WOOL_ID, WOOL_ID) < 20 && !complete) {
                    if (!SHEEP_PEN.contains(Player.getPosition())) {
                        script.setStatus("SHEEP SHEARER: Walking to sheep.");
                        if (!Movement.walkTo(SHEEP_PEN.getRandomTile())) {
                            WebWalking.walkTo(SHEEP_PEN.getRandomTile());
                        }
                    } else {
                        script.setStatus("SHEEP SHEARER: Shearing sheep.");
                        RSNPC[] sheep = NPCs.findNearest(new Filter<RSNPC>() {
                            @Override
                            public boolean accept(RSNPC rsnpc) {
                                boolean valid = false;
                                for (String action : rsnpc.getActions()) {
                                    if (action.equals("Shear"))
                                        valid = true;
                                    else if (action.equals("Talk-to"))
                                        return false;
                                }

                                if (!SHEEP_PEN.contains(rsnpc))
                                    valid = false;
                                return valid;
                            }
                        });

                        if (sheep != null && sheep.length > 0) {
                            if (NPCUtil.interact(script.getCamera(), "Shear", false, sheep[0], () -> Player.getAnimation() != -1)) {
                                Condition condition = Movement.animationWait(() -> Player.getAnimation() == -1, 5000);
                                while (!condition.active())
                                    General.sleep(250, 500);
                            }
                        }
                    }
                } else if (Inventory.getCount(BALL_WOOL_ID) < 20 && !complete) {
                    if (Player.getPosition().distanceTo(new RSTile(3209, 3213, 1)) > 3) {
                        script.setStatus("SHEEP SHEARER: Walking to spinning wheel.");
                        WebWalking.walkTo(new RSTile(3209, 3213, 1));
                    } else {
                        if (Player.getPosition().getPlane() == 0) {
                            RSObject staircase = ObjectUtil.getNearest("Staircase");
                            if (staircase != null) {
                                ObjectUtil.interact(script.getCamera(), Util.array("Climb-up"), staircase);
                            }
                        } else {
                            script.setStatus("SHEEP SHEARER: Spinning wool.");
                            if (InterfaceUtil.isOpen(459, 131)) {
                                RSInterfaceChild exit = Interfaces.get(459, 131);
                                if (exit != null) {
                                    exit.click();
                                    Timing.waitCondition(new Condition() {
                                        @Override
                                        public boolean active() {
                                            return !InterfaceUtil.isOpen(459, 131);
                                        }
                                    }, General.random(1000, 1500));
                                }
                            }
                            if (!InventoryUtil.isUsing("Wool"))
                                InventoryUtil.interact("Wool", "Use");

                            if (InventoryUtil.isUsing("Wool")) {
                                RSObject wheel = ObjectUtil.getNearest("Spinning wheel");
                                if (wheel != null) {
                                    script.getCamera().setCameraAngle(100);
                                    if (ObjectUtil.interact(script.getCamera(), Util.array("Use Wool -> Spinning wheel"), wheel)) {
                                        final int lastCount = Inventory.getCount("Wool");
                                        Condition condition = Movement.animationWait(() -> Inventory.getCount("Wool") != lastCount, 5000);
                                        while (!condition.active())
                                            General.sleep(250, 500);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    complete = true;
                    RSNPC fred = NPCUtil.getNearest(NPC_NAME);
                    if (fred == null) {
                        script.setStatus("SHEEP SHEARER: Walking to Fred.");
                        if (!Movement.walkTo(FARMER_TILE)) {
                            WebWalking.walkTo(FARMER_TILE);
                        }
                    } else {
                        script.setStatus("SHEEP SHEARER: Talking to Fred.");
                        if (NPCUtil.isTalkingTo(NPC_NAME)) {
                            NPCChat.clickContinue(true);
                        } else if (InterfaceUtil.isOpen("Fred! Fred! I've seen The")) {
                            NPCChat.selectOption("I'm back!", true);
                        } else {
                            NPCUtil.interact(script.getCamera(), "Talk-to", fred, () -> NPCUtil.isTalkingTo(NPC_NAME));
                        }
                    }
                }
            }
        }
    }

    private int getInventoryCount() {
        RSItem[] items = Inventory.getAll();
        int count = 0;
        if (items != null && items.length > 0) {
            for (RSItem item : items) {
                count += item.getStack();
            }
        }

        return count;
    }

    @Override
    public boolean validate() {
        return Game.getSetting(SETTING) != 21;
    }
    
}