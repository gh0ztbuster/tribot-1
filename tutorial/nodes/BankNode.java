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
import org.tribot.api2007.Banking;
import org.tribot.api2007.types.*;
import scripts.api.patterns.BaseScript;
import scripts.api.patterns.Node;
import scripts.api.util.*;

/**
 *
 * @author Spencer
 */
public class BankNode extends Node {
        private final String NPC_NAME = "Financial Advisor";
    private final int POLL_INTERFACE = 345;
    private final int POLL_CLOSE_CHILD = 1;
    private final int POLL_CLOSE_BUTTON = 11;

    public BankNode(BaseScript script) {
        super(script);
    }
    
    @Override
    public void execute() {
        if (shouldTalkToBank() && !isTalkingToBanker() && !isSelectOption()) {
            script.setStatus("TUTORIAL: Walking to the bank.");
            ObjectUtil.interact(script.getCamera(), Util.array("Bank"), "Bank booth", () -> isTalkingToBanker(), 2500, 4000);
        } else if (isTalkingToBanker()) {
            script.setStatus("TUTORIAL: Talking to banker.");
            NPCChat.clickContinue(true);
        } else if (isSelectOption()) {
            NPCChat.selectOption("Yes.", true);
        } else if (shouldContinue()) {
            General.sleep(200, 300);
        } else if (shouldTalkToPoll()) {
            script.setStatus("TUTORIAL: Talking to the poll.");
            if (Banking.isBankScreenOpen()) {
                Banking.close();
            } else {
                ObjectUtil.interact(script.getCamera(), Util.array("Use"), "Poll booth", () -> InterfaceUtil.isOpen("Poll booths are found in towns across"), 2500, 4000);
            }
        } else if (shouldWalkToFinance() && shouldClosePoll()) {
            script.setStatus("TUTORIAL: Closing poll booth.");
            RSInterface poll = InterfaceUtil.get(POLL_INTERFACE, POLL_CLOSE_CHILD, POLL_CLOSE_BUTTON);
            if (InterfaceUtil.click(poll, "")) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return shouldWalkToFinance() && !shouldClosePoll();
                    }
                }, General.random(2000, 3000));
            }
        } else if (shouldWalkToFinance() || shouldTalkToFinance() && !isTalkingToFinance()) {
            script.setStatus("TUTORIAL: Talking to " + NPC_NAME + ".");
            NPCUtil.interact(script.getCamera(), "Talk-to", NPC_NAME, () -> isTalkingToFinance());
        } else if (isTalkingToFinance()) {
            NPCChat.clickContinue(true);
        } else if (shouldLeaveFinance()) {
            script.setStatus("TUTORIAL: Leaving finance room.");
            if (Movement.walkTo(new RSTile(3130, 3124))) {
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return !validate();
                    }
                }, General.random(2000, 3000));
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

    private boolean shouldLeaveFinance() {
        return Game.getSetting(281) == 540;
    }

    private boolean isTalkingToFinance() { return NPCUtil.isTalkingTo(NPC_NAME) || NPCUtil.isTalkingTo(Player.getRSPlayer().getName()); }

    private boolean shouldTalkToFinance() {
        return Game.getSetting(281) == 530;
    }

    private boolean shouldWalkToFinance() {
        return Game.getSetting(281) == 525;
    }

    private boolean shouldClosePoll() {
        return InterfaceUtil.isOpen(POLL_INTERFACE, POLL_CLOSE_CHILD);
    }

    private boolean shouldTalkToPoll() {
        return Game.getSetting(281) == 520;
    }

    private boolean isSelectOption() {
        return InterfaceUtil.isOpen("No thanks");
    }

    private boolean isTalkingToBanker() { return NPCUtil.isTalkingTo("Banker") || NPCUtil.isTalkingTo(Player.getRSPlayer().getName()); }

    private boolean shouldTalkToBank() {
        return Game.getSetting(281) == 510;
    }

    @Override
    public boolean validate() {
        return shouldTalkToBank() || shouldTalkToPoll() || shouldWalkToFinance() || shouldTalkToFinance() || shouldLeaveFinance();
    }
}
