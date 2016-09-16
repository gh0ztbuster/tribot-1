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
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import scripts.api.patterns.BaseScript;
import scripts.api.patterns.Node;
import scripts.api.util.NPCUtil;
import scripts.api.util.ObjectUtil;
import scripts.api.util.Util;

/**
 *
 * @author Spencer
 */
public class GettingStartedNode extends Node {
    private final String NPC_NAME = "RuneScape Guide";

    public GettingStartedNode(BaseScript script) {
        super(script);
    }
    @Override
    public void execute() {
        System.out.println("Executing GettingStartedNode.");
        if (shouldTalkTo() && !NPCUtil.isTalkingTo(NPC_NAME)) {
            script.setStatus("TUTORIAL: Talking to " + NPC_NAME + ".");
            NPCUtil.interact(script.getCamera(), "Talk-to", NPC_NAME, () -> NPCUtil.isTalkingTo(NPC_NAME));
        } else if (NPCUtil.isTalkingTo(NPC_NAME)) {
            NPCChat.clickContinue(true);
        } else if (shouldOpenSettings()) {
            script.setStatus("TUTORIAL: Opening options tab.");
            if (GameTab.open(GameTab.TABS.OPTIONS)) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return didOpenSettings();
                    }
                }, General.random(1500, 2000));
            }
        } else if (shouldOpenDoor()) {
            script.setStatus("TUTORIAL: Leaving " + NPC_NAME + " room.");
            RSObject door = ObjectUtil.getAt(new RSTile(3098, 3107, 0), "Door");
            if (door != null) {
                ObjectUtil.interact(script.getCamera(), Util.array("Open"), door, () -> didOpenDoor() && !Player.isMoving());
            }
        }
    }

    private boolean shouldOpenSettings() {
        return Game.getSetting(281) == 3;
    }

    private boolean didOpenSettings() {
        return Game.getSetting(281) == 7;
    }

    private boolean shouldOpenDoor() {
        return Game.getSetting(281) == 10;
    }

    private boolean didOpenDoor() {
        return Game.getSetting(281) == 20;
    }

    private boolean shouldTalkTo() {
        return (Game.getSetting(22) == 33554432 && Game.getSetting(281) == 0) || didOpenSettings();
    }

    @Override
    public boolean validate() {
        return shouldTalkTo() || shouldOpenSettings() || didOpenSettings() || shouldOpenDoor();
    }
    
}
