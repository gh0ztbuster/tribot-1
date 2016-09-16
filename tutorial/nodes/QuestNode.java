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

public class QuestNode extends Node {
    private final String NPC_NAME = "Quest Guide";
    private final int EMOTE_INTERFACE = 216;
    private final int EMOTE_INTERFACE_CHILD = 1;
    private final RSTile DOOR_TILE = new RSTile(3086 + General.random(-1, 1), 3127 + General.random(-1, 1));

    public QuestNode(BaseScript script) {
        super(script);
    }
    
    @Override
    public void execute() {
        System.out.println("Executing QuestNode.");
        if (Player.getPosition().getPlane() == 1) {
            RSObject stairs = ObjectUtil.getNearest("Staircase");
            if (stairs != null) {
                ObjectUtil.interact(script.getCamera(), Util.array("Climb-down", "Climb down"), stairs, () -> Player.getPosition().getPlane() == 0);
            }
        } else if (shouldOpenEmotes()) {
            script.setStatus("TUTORIAL: Opening emote tab.");
            if (GameTab.open(GameTab.TABS.EMOTES)) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return shouldUseEmote();
                    }
                }, General.random(1000, 1250));
            }
        } else if (shouldUseEmote()) {
            script.setStatus("TUTORIAL: Using a random emote.");
            RSInterface emote = InterfaceUtil.get(EMOTE_INTERFACE, EMOTE_INTERFACE_CHILD, General.random(0, 19));
            if (emote != null) {
                if (emote.click()) {
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            return shouldOpenTools();
                        }
                    }, General.random(1000, 1250));
                }
            }
        } else if (shouldOpenTools()) {
            script.setStatus("TUTORIAL: Enabling run.");
            if (GameTab.open(GameTab.TABS.OPTIONS)) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return shouldEnableRun();
                    }
                }, General.random(1000, 1250));
            }
        } else if (shouldEnableRun()) {
            Options.setRunOn(true);
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    return shouldWalkToQuestDoor();
                }
            }, General.random(1000, 1250));
        } else if (shouldWalkToQuestDoor()) {
            script.setStatus("TUTORIAL: Running to the quest NPC.");
            RSNPC guide = NPCUtil.getNearest(NPC_NAME);
            if (guide != null) {
                NPCUtil.interact(script.getCamera(), "Talk-to", guide, () -> NPCUtil.isTalkingTo(NPC_NAME));
            } else {
                Movement.walkTo(DOOR_TILE);
                General.sleep(3000, 5000);
            }
        } else if (shouldTalkToQuest() && ! NPCUtil.isTalkingTo(NPC_NAME)) {
            script.setStatus("TUTORIAL: Talking to " + NPC_NAME + ".");
            NPCUtil.interact(script.getCamera(), "Talk-to", NPC_NAME, () -> NPCUtil.isTalkingTo(NPC_NAME));
        } else if (NPCUtil.isTalkingTo(NPC_NAME)) {
            NPCChat.clickContinue(true);
        } else if (shouldOpenQuestTab()) {
            script.setStatus("TUTORIAL: Opening quest tab.");
            if (GameTab.open(GameTab.TABS.QUESTS)) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return shouldTalkToQuest();
                    }
                }, General.random(1000, 1250));
            }
        } else if (shouldClimbDown()) {
            script.setStatus("TUTORIAL: Leaving quest area.");
            RSObject ladder = ObjectUtil.getNearest("Ladder");
            if (ladder != null)
                ObjectUtil.interact(script.getCamera(), Util.array("Climb-down"), ladder, () -> !validate());
        }
    }

    private boolean shouldClimbDown() {
        return Game.getSetting(281) == 250;
    }

    private boolean shouldOpenQuestTab() {
        return Game.getSetting(281) == 230;
    }

    private boolean shouldTalkToQuest() {
        return Game.getSetting(281) == 220 || Game.getSetting(281) == 240;
    }

    private boolean shouldWalkToQuestDoor() {
        return Game.getSetting(281) == 210;
    }

    private boolean shouldEnableRun() {
        return Game.getSetting(281) == 200;
    }

    private boolean shouldOpenTools() {
        return Game.getSetting(281) == 190;
    }

    private boolean shouldUseEmote() {
        return Game.getSetting(281) == 187;
    }

    private boolean shouldOpenEmotes() {
        return Game.getSetting(281) == 183;
    }

    @Override
    public boolean validate() {
        return shouldOpenEmotes() || shouldUseEmote() || shouldOpenTools() || shouldEnableRun() || shouldWalkToQuestDoor() || shouldTalkToQuest() || shouldOpenQuestTab() || shouldClimbDown();
    }
    
}
