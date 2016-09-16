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
import org.tribot.api2007.types.RSInterfaceChild;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import scripts.api.patterns.BaseScript;
import scripts.api.patterns.Node;
import scripts.api.util.*;

public class MiningNode extends Node {
    private final int SMITH_INTERFACE = 312;
    private final int SMITH_DAGGER = 2;
    
    private final String NPC_NAME = "Mining Instructor";
    private final RSTile NPC_TILE = new RSTile(3079, 9504);

    public MiningNode(BaseScript script) {
        super(script);
    }
    @Override
    public void execute() {
        System.out.println("Execute MiningNode.");
        if (shouldTalkToInstructor() && !isTalkingToInstructor()) {
            script.setStatus("TUTORIAL: Talking to " + NPC_NAME);
            RSNPC npc = NPCUtil.getNearest(NPC_NAME);
            if (npc == null) {
                Movement.walkTo(NPC_TILE);
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return !Player.isMoving();
                    }
                }, General.random(2000, 3000));
            } else {
                NPCUtil.interact(script.getCamera(), "Talk-to", NPC_NAME, () -> NPCUtil.isTalkingTo(NPC_NAME));
            }
        }

        if (isTalkingToInstructor()) {
            NPCChat.clickContinue(true);
        }
        
        if (shouldProspectTin()) {
            script.setStatus("TUTORIAL: Prospecting tin.");
            RSObject rock = ObjectUtil.getNearestBy(new RSTile(3078, 9504), "Rocks");
            if (rock != null) {
                if (ObjectUtil.interact(script.getCamera(), Util.array("Prospect"), rock)) {
                    Timing.waitCondition(new scripts.api.misc.Condition(() -> shouldProspectCopper()), General.random(5000, 7500));
                }
            }
        }
        
        if (shouldProspectCopper()) {
            script.setStatus("TUTORIAL: Prospecting copper.");
            RSObject rock = ObjectUtil.getNearestBy(new RSTile(3082, 9501), "Rocks");
            if (rock != null) {
                if (ObjectUtil.interact(script.getCamera(), Util.array("Prospect"), rock)) {
                    Timing.waitCondition(new scripts.api.misc.Condition(() -> shouldTalkToInstructor()), General.random(5000, 7500));
                }
            }
        }
        
        if (shouldMineTin()) {
            script.setStatus("TUTORIAL: Mining tin.");
            RSObject rock = ObjectUtil.getNearestBy(new RSTile(3078, 9504), "Rocks");
            if (rock != null) {
                if (ObjectUtil.interact(script.getCamera(), Util.array("Mine"), rock, () -> Player.getAnimation() != -1, 2500, 4000)) {
                    Condition condition = Movement.animationWait(() -> shouldMineCopper(), 5000);
                    while (!condition.active())
                        General.sleep(250, 500);
                }
            }
        }
        
        if (shouldMineCopper()) {
            script.setStatus("TUTORIAL: Mining copper.");
            RSObject rock = ObjectUtil.getNearestBy(new RSTile(3082, 9501), "Rocks");
            if (rock != null) {
                if (ObjectUtil.interact(script.getCamera(), Util.array("Mine"), rock, () -> Player.getAnimation() != -1)) {
                    Condition condition = Movement.animationWait(() -> shouldMakeBar(), 5000);
                    while (!condition.active())
                        General.sleep(250, 500);
                }
            }
        }

        if (shouldMakeBar()) {
            script.setStatus("TUTORIAL: Making bronze bar.");
            RSObject furnace = ObjectUtil.getNearest("Furnace");
            if (furnace != null) {
                if (!ObjectUtil.canInteract(furnace)) {
                    Walking.walkTo(furnace);
                } else {
                    if (!InventoryUtil.isUsing("Tin ore"))
                        InventoryUtil.interact("Tin ore", "Use");

                    if (InventoryUtil.isUsing("Tin ore")) {
                        ObjectUtil.interact(script.getCamera(), Util.array("Use Tin ore -> Furnace"), furnace, () -> didGetBar(), 6000, 8000);
                    }
                }
            }
        }

        if (didGetBar()) {
            NPCChat.clickContinue(true);
            RSInterfaceChild click = Interfaces.get(162, 32);
            if (click != null) {
                click.click();
            }
        }
        
        if (shouldUseAnvil()) {
            script.setStatus("TUTORIAL: Making dagger.");
            RSObject anvil = ObjectUtil.getNearest("Anvil");
            if (anvil != null) {
                if (!ObjectUtil.isVisable(anvil)) {
                    script.getCamera().turnToTile(anvil);
                }

                if (!InventoryUtil.isUsing("Bronze bar"))
                    InventoryUtil.interact("Bronze bar", "Use");

                if (InventoryUtil.isUsing("Bronze bar")) {
                    ObjectUtil.interact(script.getCamera(), Util.array("Use Bronze bar -> Anvil"), anvil, () -> shouldMakeDagger(), 2500, 4000);
                }
            }
        }
        
        if (shouldMakeDagger()) {
            RSInterfaceChild dagger = Interfaces.get(SMITH_INTERFACE, SMITH_DAGGER);
            if (dagger != null) {
                if (dagger.click()) {
                    Timing.waitCondition(new scripts.api.misc.Condition(() -> shouldOpenGate()), General.random(2500, 4000));
                }
            } else {
                RSObject anvil = ObjectUtil.getNearest("Anvil");
                if (anvil != null) {
                    if (!InventoryUtil.isUsing("Bronze bar"))
                        InventoryUtil.interact("Bronze bar", "Use");

                    if (InventoryUtil.isUsing("Bronze bar")) {
                        ObjectUtil.interact(script.getCamera(), Util.array("Use Bronze bar -> Anvil"), anvil, () -> shouldMakeDagger(), 2500, 4000);
                    }
                }
            }
        }
        
        if (shouldOpenGate()) {
            script.setStatus("TUTORIAL: Leaving mining area.");
            RSObject gate = ObjectUtil.getNearestWith("Open");
            if (gate != null) {
                ObjectUtil.interact(script.getCamera(), Util.array("Open"), gate, () -> !validate());
            }
        }
    }

    private boolean shouldOpenGate() {
        return Game.getSetting(281) == 360;
    }

    private boolean shouldUseAnvil() {
        return Game.getSetting(281) == 340;
    }

    private boolean shouldMakeDagger() {
        return Game.getSetting(281) == 350;
    }

    private boolean shouldMakeBar() {
        return Game.getSetting(281) == 320;
    }

    private boolean shouldMineCopper() {
        return Game.getSetting(281) == 310;
    }

    private boolean shouldMineTin() {
        return Game.getSetting(281) == 300;
    }

    private boolean didGetBar() {
        return InterfaceUtil.isOpen("You retrieve a");
    }

    private boolean isTalkingToInstructor() {
        return NPCUtil.isTalkingTo(NPC_NAME) || NPCUtil.isTalkingTo(Player.getRSPlayer().getName()) || InterfaceUtil.isOpen("gives you a") || didGetBar();
    }

    private boolean shouldProspectCopper() {
        return Game.getSetting(281) == 280;
    }

    private boolean shouldProspectTin() {
        return Game.getSetting(281) == 270;
    }

    private boolean shouldTalkToInstructor() {
        return Game.getSetting(281) == 260 || Game.getSetting(281) == 290 || Game.getSetting(281) == 330;
    }

    @Override
    public boolean validate() {
        return shouldTalkToInstructor() || shouldProspectTin() || shouldProspectCopper() || shouldMineTin() || shouldMineCopper() || shouldMakeBar() || shouldUseAnvil() || shouldMakeDagger() || shouldOpenGate();
    }
    
}
