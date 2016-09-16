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
import scripts.api.util.Movement;
import scripts.api.util.NPCUtil;

public class PrayerNode extends Node {
    private final String NPC_NAME = "Brother Brace";
    private final RSTile MONK_TILE = new RSTile(3127, 3107);

    public PrayerNode(BaseScript script) {
        super(script);
    }
    @Override
    public void execute() {
        System.out.println("Executing PrayerNode.");
        if (shouldTalkToMonk() && !isTalkingToMonk()) {
            script.setStatus("TUTORIAL: Walking to " + NPC_NAME);
            RSNPC monk = NPCUtil.getNearest(NPC_NAME);
            if (monk == null) {
                Movement.walkTo(MONK_TILE);
            } else {
                NPCUtil.interact(script.getCamera(), "Talk-to", monk, () -> isTalkingToMonk());
            }
        }
        
        if (isTalkingToMonk()) {
            NPCChat.clickContinue(true);
        }
        
        if (shouldOpenPrayer()) {
            script.setStatus("TUTORIAL: Opening prayer tab.");
            if (GameTab.open(GameTab.TABS.PRAYERS)) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return shouldTalkToMonk();
                    }
                }, General.random(1000, 1250));
            }
        }

        if (shouldOpenFriends()) {
            script.setStatus("TUTORIAL: Opening friends tab.");
            if (GameTab.open(GameTab.TABS.FRIENDS)) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return shouldOpenIgnore();
                    }
                }, General.random(1000, 1250));
            }
        }
        
        if (shouldOpenIgnore()) {
            script.setStatus("TUTORIAL: Opening ignore tab.");
            if (GameTab.open(GameTab.TABS.IGNORE)) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return shouldTalkToMonk();
                    }
                }, General.random(1000, 1250));
            }
        }
    }

    private boolean shouldOpenFriends() {
        return Game.getSetting(281) == 580;
    }

    private boolean shouldOpenIgnore() {
        return Game.getSetting(281) == 590;
    }

    private boolean shouldOpenPrayer() {
        return Game.getSetting(281) == 560;
    }

    private boolean isTalkingToMonk() {
        return NPCUtil.isTalkingTo(NPC_NAME) || NPCUtil.isTalkingTo(Player.getRSPlayer().getName());
    }

    private boolean shouldTalkToMonk() {
        return Game.getSetting(281) == 550 || Game.getSetting(281) == 570 || Game.getSetting(281) == 600;
    }

    @Override
    public boolean validate() {
        return shouldTalkToMonk() || shouldOpenPrayer() || shouldOpenFriends() || shouldOpenIgnore();
    }
    
}
