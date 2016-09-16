/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts.quester.nodes;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.*;
import org.tribot.api2007.types.*;
import scripts.api.patterns.BaseScript;
import scripts.api.patterns.Node;
import scripts.api.util.*;

public class CooksAssistantNode extends Node {
    private static final int SETTING = 29;
    
    private final String NPC_NAME = "Cook";
    private final RSArea COOK_AREA = new RSArea(new RSTile(3208, 3214, 0), 3);
    private final RSTile COW_TILE = new RSTile(3253, 3272);
    private final int BUCKET_ID = 1925;
    private final int MILK_BUCKET_ID = 1927;
    
    private final RSTile EGG_TILE = new RSTile(3229, 3298);
    private final int EGG_ID = 1944;
    
    private final RSTile WHEAT_TILE = new RSTile(3161, 3295);
    private final int WHEAT_ID = 1947;
    
    private final RSTile WINDMILL_TILE = new RSTile(3167, 3306);
    private final int POT_ID = 1931;
    private final int POT_FLOUR_ID = 1933;
    private boolean processWheat = false;
    private final int PROCESSED = 695;
    
    private boolean complete = false;
    public CooksAssistantNode(BaseScript script) {
        super(script);
    }

    @Override
    public void execute() {
        if (Game.getSetting(SETTING) == 2) {
            script.setStatus("COOKS ASSISTANT: Quest Complete.");
        } else if (Game.getSetting(SETTING) == 0) {
            RSNPC cook = NPCUtil.getNearest(NPC_NAME);
            if (cook == null) {
                script.setStatus("COOKS ASSISTANT: Walking to Cook.");
                Movement.walkTo(COOK_AREA.getRandomTile());
            } else {
                script.setStatus("COOKS ASSISTANT: Talking with Cook.");
                if (InterfaceUtil.isOpen("Can you make me a cake?")) {
                    NPCChat.selectOption("What's wrong?", true);
                } else if (InterfaceUtil.isOpen("I'm always happy to help a cook in distress.")) {
                    NPCChat.selectOption("I'm always happy to help a cook in distress.", true);
                } else if (InterfaceUtil.isOpen("Actually, I know where to find this stuff.")) {
                    NPCChat.selectOption("Actually, I know where to find this stuff.", true);
                } else if (NPCUtil.isTalkingTo(NPC_NAME)) {
                    NPCChat.clickContinue(true);
                } else {
                    NPCUtil.interact(script.getCamera(), "Talk-to", cook, () -> NPCUtil.isTalkingTo(NPC_NAME));
                }
            }
        } else if (Game.getSetting(SETTING) == 1) {
            if ((Inventory.getCount(EGG_ID) > 0 && Inventory.getCount(POT_FLOUR_ID) > 0 && Inventory.getCount(MILK_BUCKET_ID) > 0) || complete) {
                complete = true;
                if (!COOK_AREA.contains(Player.getPosition())) {
                    script.setStatus("COOKS ASSISTANT: Walking to Cook.");
                    Movement.walkTo(COOK_AREA.getRandomTile());
                } else {
                    script.setStatus("COOKS ASSISTANT: Talking to Cook.");
                    if (!NPCUtil.isTalkingTo(NPC_NAME))
                        NPCUtil.interact(script.getCamera(), "Talk-to", NPC_NAME, () -> NPCUtil.isTalkingTo(NPC_NAME));
                    else {
                        NPCChat.clickContinue(true);
                        if (InterfaceUtil.isOpen(277, 15)) {
                            RSInterfaceChild child = Interfaces.get(277, 15);
                            if (child != null) {
                                child.click();
                                Timing.waitCondition(new Condition() {
                                    @Override
                                    public boolean active() {
                                        return !InterfaceUtil.isOpen(277, 15);
                                    }
                                }, General.random(1500, 2500));
                            }
                        }
                    }
                }
            } else if (Inventory.getCount(MILK_BUCKET_ID) == 0 && Inventory.getCount(BUCKET_ID) > 0) {
                if (Player.getPosition().distanceTo(COW_TILE) > 5) {
                    script.setStatus("COOKS ASSISTANT: Walking to Dairy cow.");
                    Movement.walkTo(COW_TILE);
                } else {
                    script.setStatus("COOKS ASSISTANT: Milking Dairy cow.");
                    RSObject cow = ObjectUtil.getNearest("Dairy cow");
                    if (cow != null) {
                        ObjectUtil.interact(script.getCamera(), Util.array("Milk"), cow, () -> Inventory.getCount(MILK_BUCKET_ID) > 0 && Player.getAnimation() == -1, 4000, 5000);
                    }
                }
            } else if (Inventory.getCount(EGG_ID) == 0) {
                if (Player.getPosition().distanceTo(EGG_TILE) > 5) {
                    script.setStatus("COOKS ASSISTANT: Walking to eggs.");
                    Movement.walkTo(EGG_TILE);
                } else {
                    script.setStatus("COOKS ASSISTANT: Grabbing egg.");
                    RSGroundItem[] eggs = GroundItems.findNearest(EGG_ID);
                    if (eggs != null && eggs.length > 0) {
                        RSGroundItem egg = eggs[0];
                        if (Clicking.click("Take Egg", egg)) {
                            Timing.waitCondition(new Condition() {
                                @Override
                                public boolean active() {
                                    return Inventory.getCount(EGG_ID) > 0;
                                }
                            }, General.random(1500, 2500));
                        }
                    }
                }
            } else if (Inventory.getCount(WHEAT_ID) == 0 && !processWheat && Inventory.getCount(POT_FLOUR_ID) == 0) {
                if (Player.getPosition().distanceTo(WHEAT_TILE) > 5) {
                    script.setStatus("COOKS ASSISTANT: Walking to wheat.");
                    Movement.walkTo(WHEAT_TILE);
                } else {
                    script.setStatus("COOKS ASSISTANT: Picking wheat.");
                    RSObject wheat = ObjectUtil.getNearest("Wheat");
                    if (wheat != null) {
                        if (ObjectUtil.interact(script.getCamera(), Util.array("Pick"), wheat)) {
                            Timing.waitCondition(new Condition() {
                                @Override
                                public boolean active() {
                                    return Inventory.getCount(WHEAT_ID) > 0;
                                }
                            }, General.random(3000, 5000));
                        }
                    }
                }
            } else if (Inventory.getCount(POT_FLOUR_ID) == 0) {
                if (Player.getPosition().distanceTo(WINDMILL_TILE) > 5) {
                    script.setStatus("COOKS ASSISTANT: Walking to windmill.");
                    Movement.walkTo(WINDMILL_TILE);
                } else {
                    if (Inventory.getCount(WHEAT_ID) > 0 && Player.getPosition().getPlane() != 2) {
                        script.setStatus("COOKS ASSISTANT: Climbing windmill.");
                        RSObject ladder = ObjectUtil.getNearest("Ladder");
                        if (ladder != null) {
                            final int plane = Player.getPosition().getPlane();
                            if (ObjectUtil.interact(script.getCamera(), Util.array("Climb-up"), ladder)) {
                                Timing.waitCondition(new Condition() {
                                    @Override
                                    public boolean active() {
                                        return Player.getPosition().getPlane() != plane;
                                    }
                                }, General.random(1500, 2500));
                            }
                        }
                    } else if (Player.getPosition().getPlane() == 2 && (!processWheat || Game.getSetting(PROCESSED) == 0)) {
                        if (Inventory.getCount(WHEAT_ID) > 0) {
                            script.setStatus("COOKS ASSISTANT: Adding grain to hopper.");
                            if (!InventoryUtil.isUsing("Grain"))
                                InventoryUtil.interact("Grain", "Use");
                            else {
                                RSObject hopper = ObjectUtil.getNearest("Hopper");
                                if (ObjectUtil.interact(script.getCamera(), Util.array("Use Grain -> Hopper"), hopper)) {
                                    Timing.waitCondition(new Condition() {
                                        @Override
                                        public boolean active() {
                                            return Inventory.getCount(WHEAT_ID) == 0;
                                        }
                                    }, General.random(3000, 5000));
                                    processWheat = true;
                                }
                            }
                        } else if (Game.getSetting(PROCESSED) == 0) {
                            script.setStatus("COOKS ASSISTANT: Operating hopper.");
                            RSObject controls = ObjectUtil.getNearest("Hopper controls");
                            if (controls != null) {
                                if (ObjectUtil.interact(script.getCamera(), Util.array("Operate"), controls)) {
                                    Timing.waitCondition(new Condition() {
                                        @Override
                                        public boolean active() {
                                            return Game.getSetting(PROCESSED) == 1;
                                        }
                                    }, General.random(1500, 2500));
                                }
                            }
                        }
                    } else if (Game.getSetting(PROCESSED) == 1) {
                        if (Player.getPosition().getPlane() != 0) {
                            script.setStatus("COOKS ASSISTANT: Climbing down windmill.");
                            RSObject ladder = ObjectUtil.getNearest("Ladder");
                            if (ladder != null) {
                                final int plane = Player.getPosition().getPlane();
                                if (ObjectUtil.interact(script.getCamera(), Util.array("Climb-down"), ladder)) {
                                    Timing.waitCondition(new Condition() {
                                        @Override
                                        public boolean active() {
                                            return Player.getPosition().getPlane() != plane;
                                        }
                                    }, General.random(1500, 2500));
                                }
                            } 
                        } else {
                            script.setStatus("COOKS ASSISTANT: Taking flour.");
                            RSObject bin = ObjectUtil.getNearest("Flour bin");
                            if (bin != null) {
                                if (ObjectUtil.interact(script.getCamera(), Util.array("Empty"), bin)) {
                                    Timing.waitCondition(new Condition() {
                                        @Override
                                        public boolean active() {
                                            return Inventory.getCount(POT_FLOUR_ID) > 0;
                                        }
                                    }, General.random(1500, 2500));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean validate() {
        return Game.getSetting(SETTING) != 2;
    }
    
}