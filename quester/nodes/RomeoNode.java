/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts.quester.nodes;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.*;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import scripts.api.patterns.BaseScript;
import scripts.api.patterns.Node;
import scripts.api.util.*;

public class RomeoNode extends Node {
    private static final int SETTING = 144;

    private final RSTile ROMEO_TILE = new RSTile(3212, 3422, 0);
    private final RSTile UP_STAIRS_TILE = new RSTile(3160, 3434, 0);
    private final RSTile DOWN_STAIRS_TILE = new RSTile(3155, 3435, 1);
    private final RSArea JULIET_AREA = new RSArea(new RSTile[] { new RSTile(3154, 3427, 1), new RSTile(3162, 3427, 1), new RSTile(3162, 3424, 1), new RSTile(3154, 3424, 1) });
    private final String NPC_NAME_1 = "Romeo";
    private final String NPC_NAME_2 = "Juliet";
    private final String NPC_NAME_3 = "Father Lawrence";
    private final int CADAVA_ID = 753;

    private boolean complete = false;
    public RomeoNode(BaseScript script) {
        super(script);
    }

    @Override
    public void execute() {
        if (Game.getSetting(SETTING) == 0) {
            if (NPCUtil.isTalkingTo(NPC_NAME_1)) {
                NPCChat.clickContinue(true);
            } else if (InterfaceUtil.isOpen("Yes, I have seen her actually!")) {
                NPCChat.selectOption("Yes, I have seen her actually!", true);
            } else if (InterfaceUtil.isOpen("Yes, ok, I'll let her know")) {
                NPCChat.selectOption("Yes, ok, I'll let her know.", true);
            } else {
                RSNPC romeo = NPCUtil.getNearest(NPC_NAME_1);
                if (romeo == null) {
                    script.setStatus("ROMEO: Walking to Romeo.");
                    if (!Movement.walkTo(ROMEO_TILE)) {
                        WebWalking.walkTo(ROMEO_TILE);
                    }
                } else {
                    script.setStatus("ROMEO: Talking to Romeo.");
                    NPCUtil.interact(script.getCamera(), "Talk-to", NPC_NAME_1, () -> NPCUtil.isTalkingTo(NPC_NAME_1));
                }
            }
        } else if (Game.getSetting(SETTING) == 10){
            RSNPC juliet = NPCUtil.getNearest(NPC_NAME_2);
            if (juliet == null) {
                if (Player.getPosition().distanceTo(UP_STAIRS_TILE) > 5) {
                    script.setStatus("ROMEO: Walking to Juliets house.");
                    if (!Movement.walkTo(UP_STAIRS_TILE)) {
                        WebWalking.walkTo(UP_STAIRS_TILE);
                    }
                } else {
                    script.setStatus("ROMEO: Climbing up staircase.");
                    ObjectUtil.interact(script.getCamera(), Util.array("Climb-up"), UP_STAIRS_TILE, "Staircase", () -> Player.getPosition().getPlane() == 1);
                }
            } else {
                if (NPCUtil.isTalkingTo(NPC_NAME_2) || InterfaceUtil.isOpen("Juliet gives you a")) {
                    script.setStatus("ROMEO: Talking to Juliet.");
                    NPCChat.clickContinue(true);
                } else {
                    script.setStatus("ROMEO: Talking to Juliet.");
                    NPCUtil.interact(script.getCamera(), "Talk-to", juliet, () -> NPCUtil.isTalkingTo(NPC_NAME_2));
                }
            }
        } else if (Game.getSetting(SETTING) == 20) {
            if (Player.getPosition().getPlane() == 1) {
                script.setStatus("ROMEO: Climbing down staircase.");
                ObjectUtil.interact(script.getCamera(), Util.array("Climb-down"), DOWN_STAIRS_TILE, "Staircase", () -> Player.getPosition().getPlane() == 0);
            } else {
                if (NPCUtil.isTalkingTo(NPC_NAME_1) || InterfaceUtil.isOpen("You hand over Juliet's") || InterfaceUtil.isOpen("folds the message")) {
                    NPCChat.clickContinue(true);
                } else {
                    RSNPC romeo = NPCUtil.getNearest(NPC_NAME_1);
                    if (romeo == null) {
                        script.setStatus("ROMEO: Walking to Romeo.");
                        if (!Movement.walkTo(ROMEO_TILE)) {
                            WebWalking.walkTo(ROMEO_TILE);
                        }
                    } else {
                        script.setStatus("ROMEO: Talking to Romeo.");
                        NPCUtil.interact(script.getCamera(), "Talk-to", NPC_NAME_1, () -> NPCUtil.isTalkingTo(NPC_NAME_1));
                    }
                }
            }
        } else if (Game.getSetting(SETTING) == 30) {
            if (NPCUtil.isTalkingTo(NPC_NAME_3)) {
                NPCChat.clickContinue(true);
            } else if (InterfaceUtil.isOpen("Congregation")) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return InterfaceUtil.isOpen("did you think of my");
                    }
                }, General.random(12500, 15000));
            } else {
                RSNPC priest = NPCUtil.getNearest(NPC_NAME_3);
                if (priest == null) {
                    script.setStatus("ROMEO: Walking to " + NPC_NAME_3 + ".");
                    if (!Movement.walkTo(new RSTile(3254, 3482))) {
                        WebWalking.walkTo(new RSTile(3254, 3482));
                    }
                } else {
                    script.setStatus("ROMEO: Talking to " + NPC_NAME_3 + ".");
                    NPCUtil.interact(script.getCamera(), "Talk-to", priest, () -> NPCUtil.isTalkingTo(NPC_NAME_3));
                }
            }
        } else if (Game.getSetting(SETTING) == 40) {
            if (Inventory.getCount(CADAVA_ID) > 0) {
                if (NPCUtil.isTalkingTo("Apothecary")) {
                    NPCChat.clickContinue(true);
                } else {
                    RSNPC apoth = NPCUtil.getNearest("Apothecary");
                    if (apoth == null) {
                        script.setStatus("ROMEO: Walking to Apothecary.");
                        if (!Movement.walkTo(new RSTile(3195, 3403))) {
                            WebWalking.walkTo(new RSTile(3195, 3403));
                        }
                    } else {
                        script.setStatus("ROMEO: Talking to Apothecary.");
                        NPCUtil.interact(script.getCamera(), "Talk-to", apoth, () -> NPCUtil.isTalkingTo("Apothecary"));
                    }
                }
            } else {
                RSObject cadava = ObjectUtil.getNearest("Cadava bush");
                if (cadava == null) {
                    script.setStatus("ROMEO: Walking to Cadava bush.");
                    if (!Movement.walkTo(new RSTile(3270, 3368))) {
                        WebWalking.walkTo(new RSTile(3270, 3368));
                    }
                } else {
                    script.setStatus("ROMEO: Grabbing Cadava berry.");
                    ObjectUtil.interact(script.getCamera(), Util.array("Pick-from"), cadava, () -> Inventory.getCount(CADAVA_ID) > 0);
                }
            }
        } else if (Game.getSetting(SETTING) == 50) {
            if (Inventory.getCount(756) > 0) {
                if (Player.getPosition().getPlane() == 1) {
                    script.setStatus("ROMEO: Talking to Juliet.");
                    if (NPCUtil.isTalkingTo("Draul Leptoc") || NPCUtil.isTalkingTo("Phillipa") || NPCUtil.isTalkingTo(NPC_NAME_2) || InterfaceUtil.isOpen("You pass the suspicious potion to")) {
                        NPCChat.clickContinue(true);
                    } else {
                        NPCUtil.interact(script.getCamera(), "Talk-to", NPC_NAME_2, () -> NPCUtil.isTalkingTo(NPC_NAME_2));
                    }
                } else {
                    if (Player.getPosition().distanceTo(UP_STAIRS_TILE) > 5) {
                        script.setStatus("ROMEO: Walking to Juliet.");
                        if (!Movement.walkTo(UP_STAIRS_TILE)) {
                            WebWalking.walkTo(UP_STAIRS_TILE);
                        }
                    } else {
                        script.setStatus("ROMEO: Climbing up staircase.");
                        ObjectUtil.interact(script.getCamera(), Util.array("Climb-up"), UP_STAIRS_TILE, "Staircase", () -> Player.getPosition().getPlane() == 1);
                    }
                }
            } else {
                if (Game.getSetting(1021) != 0) {
                    NPCChat.clickContinue(true);
                } else {
                    if (JULIET_AREA.contains(Player.getPosition()) || NPCUtil.isTalkingTo("Draul Leptoc") || NPCUtil.isTalkingTo("Phillipa") || NPCUtil.isTalkingTo(NPC_NAME_2) || NPCUtil.isTalkingTo(Player.getRSPlayer().getName()) || InterfaceUtil.isOpen("You pass the suspicious potion to")) {
                        NPCChat.clickContinue(true);
                    } else {
                        if (NPCUtil.isTalkingTo("Apothecary")) {
                            NPCChat.clickContinue(true);
                        } else if (InterfaceUtil.isOpen("You hand over the berries, which")) {
                            NPCChat.clickContinue(true);
                            General.sleep(3000, 5000);
                        } else {
                            RSNPC apoth = NPCUtil.getNearest("Apothecary");
                            if (apoth == null) {
                                script.setStatus("ROMEO: Walking to Apothecary.");
                                if (!Movement.walkTo(new RSTile(3195, 3403))) {
                                    WebWalking.walkTo(new RSTile(3195, 3403));
                                }
                            } else {
                                script.setStatus("ROMEO: Talking to Apothecary.");
                                NPCUtil.interact(script.getCamera(), "Talk-to", apoth, () -> NPCUtil.isTalkingTo("Apothecary"));
                            }
                        }
                    }
                }
            }
        } else if (Game.getSetting(SETTING) == 60) {
            if (Game.getSetting(1021) != 0) {
                NPCChat.clickContinue(true);
            } else {
                if (Player.getPosition().getPlane() == 1) {
                    script.setStatus("ROMEO: Climbing down staircase.");
                    ObjectUtil.interact(script.getCamera(), Util.array("Climb-down"), DOWN_STAIRS_TILE, "Staircase", () -> Player.getPosition().getPlane() == 0);
                } else {
                    if (NPCUtil.isTalkingTo(NPC_NAME_1) || InterfaceUtil.isOpen("You hand over Juliet's") || InterfaceUtil.isOpen("folds away") || NPCUtil.isTalkingTo("Phillipa")) {
                        NPCChat.clickContinue(true);
                    } else {
                        RSNPC romeo = NPCUtil.getNearest(NPC_NAME_1);
                        if (romeo == null) {
                            script.setStatus("ROMEO: Walking to Romeo.");
                            if (!Movement.walkTo(ROMEO_TILE)) {
                                WebWalking.walkTo(ROMEO_TILE);
                            }
                        } else {
                            script.setStatus("ROMEO: Talking to Romeo.");
                            NPCUtil.interact(script.getCamera(), "Talk-to", NPC_NAME_1, () -> NPCUtil.isTalkingTo(NPC_NAME_1));
                        }
                    }
                }
            }
        } else if (Game.getSetting(SETTING) == 100) {
            if (Game.getSetting(101) == 7) {

            }
        }
    }

    @Override
    public boolean validate() {
        return Game.getSetting(SETTING) != 100;
    }
    
}