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

public class CombatNode extends Node {
    private final int EQUIP_INTERFACE = 387;
    private final int EQUIP_BUTTON = 17;
    private final int OPEN_STAT_INTERFACE = 84;
    private final int OPEN_STAT_CLOSE = 4;
    private final RSArea pen = new RSArea(new RSTile[] { new RSTile(3110, 9518, 0), new RSTile(3109, 9514, 0), new RSTile(3106, 9512, 0), 
        new RSTile(3103, 9512, 0), new RSTile(3100, 9512, 0), new RSTile(3097, 9516, 0), new RSTile(3098, 9520, 0), 
        new RSTile(3100, 9522, 0), new RSTile(3102, 9525, 0), new RSTile(3106, 9523, 0), new RSTile(3109, 9522, 0), 
        new RSTile(3111, 9520, 0), new RSTile(3111, 9518, 0) });
    
    private final String NPC_NAME = "Combat Instructor";
    private final int NPC_ID = 3307;
    private final int RAT_ID = 3313;
    private final RSTile GATE_TILE = new RSTile(3112, 9519);

    public CombatNode(BaseScript script) {
        super(script);
    }
    
    @Override
    public void execute() {
        System.out.println("Executing CombatNode.");
        if (shouldTalkToInstructor() && !isTalkingToInstructor() && !InterfaceUtil.isOpen(OPEN_STAT_INTERFACE, OPEN_STAT_CLOSE)) {
            script.setStatus("TUTORIAL: Talking to " + NPC_NAME + ".");
            NPCUtil.interact(script.getCamera(), "Talk-to", NPC_NAME, () -> isTalkingToInstructor());
        } else if (isTalkingToInstructor()) {
            NPCChat.clickContinue(true);
        } else if (shouldOpenEquip()) {
            script.setStatus("TUTORIAL: Opening equipment tab.");
            if (GameTab.open(GameTab.TABS.EQUIPMENT)) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return shouldOpenViewer();
                    }
                }, General.random(2000, 3000));
            }
        } else if (shouldOpenViewer()) {
            RSInterfaceChild equip = Interfaces.get(EQUIP_INTERFACE, EQUIP_BUTTON);
            if (equip != null) {
                equip.click();
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return shouldEquipDagger();
                    }
                }, General.random(2000, 3000));
            }
        } else if (shouldEquipDagger()) {
            script.setStatus("TUTORIAL: Equipping bronze dagger.");
            InventoryUtil.interact("Bronze dagger", ()-> shouldTalkToInstructor(), 2000, 3000,  "Wield", "Equip");
        } else if (shouldTalkToInstructor() && InterfaceUtil.isOpen(OPEN_STAT_INTERFACE, OPEN_STAT_CLOSE)) {
            RSInterface close = InterfaceUtil.get(OPEN_STAT_INTERFACE, OPEN_STAT_CLOSE);
            if (close != null) {
                close.click();
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return !InterfaceUtil.isOpen(OPEN_STAT_INTERFACE, OPEN_STAT_CLOSE);
                    }
                }, General.random(1000, 2000));
            }
        } else if (shouldEquipSwordShield()) {
            script.setStatus("TUTORIAL: Equipping sword/shield");
            if (InventoryUtil.getFirst("Bronze sword") != null)
                InventoryUtil.interact("Bronze sword", "Wield", "Equip");
            
            if (InventoryUtil.getFirst("Wooden shield") != null)
                InventoryUtil.interact("Wooden shield", "Wield", "Equip");

            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    return shouldOpenCombat();
                }
            }, General.random(2000, 3000));
        } else if (shouldOpenCombat()) {
            script.setStatus("TUTORIAL: Opening combat tab.");
            if (GameTab.open(GameTab.TABS.COMBAT)) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return shouldJoinPit();
                    }
                }, General.random(2000, 3000));
            }
        } else if (shouldJoinPit() || (shouldFightRat() && !pen.contains(Player.getPosition()))) {
            script.setStatus("TUTORIAL: Going to rat pit.");
            RSNPC rat = NPCUtil.getNearest(RAT_ID);
            if (rat != null) {
                Movement.walkTo(rat);
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return !Player.isMoving();
                    }
                }, General.random(2000, 3000));
            }
        } else if (shouldFightRat() && pen.contains(Player.getPosition())) {
            script.setStatus("TUTORIAL: Attacking rat.");
            RSNPC[] rats = NPCUtil.get(RAT_ID);
            if (rats != null) {
                for (RSNPC rat : rats) {
                    if (!rat.isInCombat() && rat.isClickable() && !Player.getRSPlayer().isInCombat()) {
                        NPCUtil.interact(script.getCamera(), "Attack", rat, () -> Player.getAnimation() != -1);
                        break;
                    }
                }
            }

            Condition condition = Movement.animationWait(() -> shouldLeavePit(), 5000);
            while (!condition.active())
                General.sleep(250, 500);
        } else if (shouldLeavePit() && !isTalkingToInstructor()) {
            script.setStatus("TUTORIAL: Talking to " + NPC_NAME + ".");
            NPCUtil.interact(script.getCamera(), "Talk-to", NPC_NAME, () -> isTalkingToInstructor());
        } else if (shouldContinue()) {
            General.sleep(200, 300);
        } else if (shouldEquipBowArrow() || shouldRangeRat()) {
            script.setStatus("TUTORIAL: Equipping bow/arrow.");
            if (InventoryUtil.getFirst("Shortbow") != null)
                InventoryUtil.interact("Shortbow", "Wield", "Equip");
            
            if (InventoryUtil.getFirst("Bronze arrow") != null)
                InventoryUtil.interact("Bronze arrow", "Wield", "Equip");
            
            if (InventoryUtil.getFirst("Bronze arrow") == null && InventoryUtil.getFirst("Shortbow") == null) {
                script.setStatus("TUTORIAL: Ranging rat.");
                if (Player.getPosition().distanceTo(GATE_TILE) > 3) {
                    if (Movement.walkTo(GATE_TILE)) {
                        Timing.waitCondition(new Condition() {
                            @Override
                            public boolean active() {
                                return !Player.isMoving();
                            }
                        }, General.random(2000, 3000));
                    }
                } else if (Player.getPosition().distanceTo(GATE_TILE) <= 3) {
                    RSNPC[] rats = NPCUtil.get(RAT_ID);
                    if (rats != null) {
                        for (RSNPC rat : rats) {
                            if (!rat.isInCombat() && rat.isClickable()) {
                                if (NPCUtil.interact(script.getCamera(), "Attack", true, rat, () -> Player.getAnimation() != -1, 1500, 2000)) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            Condition condition = Movement.animationWait(() -> shouldClimbLadder(), 5000);
            while (!condition.active())
                General.sleep(250, 500);
        } else if (shouldClimbLadder()) {
            script.setStatus("TUTORIAL: Leaving combat area.");
            RSObject ladder = ObjectUtil.getNearest("Ladder");
            if (ladder != null) {
                ObjectUtil.interact(script.getCamera(), Util.array("Climb-up"), ladder, () -> !validate());
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

    private boolean shouldClimbLadder() {
        return Game.getSetting(281) == 500;
    }

    private boolean shouldRangeRat() {
        return Game.getSetting(281) == 490;
    }

    private boolean shouldEquipBowArrow() {
        return Game.getSetting(281) == 480;
    }

    private boolean shouldLeavePit() {
        return Game.getSetting(281) == 470;
    }

    private boolean shouldFightRat() {
        return Game.getSetting(281) == 450 || Game.getSetting(281) == 460;
    }

    private boolean shouldJoinPit() {
        return Game.getSetting(281) == 440;
    }

    private boolean shouldOpenCombat() {
        return Game.getSetting(281) == 430;
    }

    private boolean shouldEquipSwordShield() {
        return Game.getSetting(281) == 420;
    }

    private boolean shouldEquipDagger() {
        return Game.getSetting(281) == 405;
    }

    private boolean shouldOpenViewer() {
        return Game.getSetting(281) == 400;
    }

    private boolean shouldOpenEquip() {
        return Game.getSetting(281) == 390;
    }

    private boolean isTalkingToInstructor() {
        return NPCUtil.isTalkingTo(NPC_NAME) || NPCUtil.isTalkingTo(Player.getRSPlayer().getName()) || InterfaceUtil.isOpen("gives you a") || InterfaceUtil.isOpen("gives you some");
    }

    private boolean shouldTalkToInstructor() {
        return Game.getSetting(281) == 370 || Game.getSetting(281) == 410;
    }

    @Override
    public boolean validate() {
        return shouldTalkToInstructor() || shouldOpenEquip() || shouldOpenViewer() || shouldEquipDagger() || shouldEquipSwordShield() || shouldOpenCombat() || shouldJoinPit() || shouldFightRat() || shouldLeavePit() || shouldEquipBowArrow() || shouldRangeRat() || shouldClimbLadder();
    }
    
}
