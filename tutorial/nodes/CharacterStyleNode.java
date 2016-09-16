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
import org.tribot.api2007.Login;
import org.tribot.api2007.types.RSInterface;

import scripts.api.patterns.BaseScript;
import scripts.api.patterns.Node;
import scripts.api.util.InterfaceUtil;

/**
 *
 * @author Spencer
 */
public class CharacterStyleNode extends Node {
    private final int length = (int) (25 * Math.random());
    private final int[] design = new int[length];

    private final int INTERFACE_MASTER = 269;
    private final int FINISH_BUTTON = 100;

    public CharacterStyleNode(BaseScript script) {
        super(script);
    }
    private enum MODIFIERS {
        HEAD_LEFT(106), JAW_LEFT(107), TORSO_LEFT(108), ARMS_LEFT(109), HANDS_LEFT(110), LEGS_LEFT(111), FEET_LEFT(112),
        HEAD_RIGHT(113), JAW_RIGHT(114), TORSO_RIGHT(115), ARMS_RIGHT(116), HANDS_RIGHT(117), LEGS_RIGHT(118), FEET_RIGHT(119),
        HAIR2_LEFT(105), TORSO2_LEFT(123), LEGS2_LEFT(122), FEET2_LEFT(124), SKIN_LEFT(125),
        HAIR2_RIGHT(121), TORSO2_RIGHT(127), LEGS2_RIGHT(129), FEET2_RIGHT(130), SKIN_RIGHT(131);
    
        private final int id;
        private MODIFIERS(int id) { this.id = id; }
        public int getValue() { return id; }
    }
    
    @Override
    public void execute() {
        System.out.println("Executing CharacterStyleNode.");
        int mod = MODIFIERS.values().length - 1;
        for (int i = 0; i < design.length; i++) {
            design[i] = MODIFIERS.values()[(int) (mod * Math.random())].getValue();
        }

        script.setStatus("TUTORIAL: Designing character.");
        for (int i = 0; i < design.length; i++) {
            RSInterface rsInterface = InterfaceUtil.get(INTERFACE_MASTER, design[i]);
            if (InterfaceUtil.isOpen(rsInterface)) {
                rsInterface.click("Change", "Recolour");
                General.sleep(100, 250);
            }
        }

        script.setStatus("TUTORIAL: Closing design interface.");
        RSInterface rsInterface = InterfaceUtil.get(INTERFACE_MASTER, FINISH_BUTTON);
        if (InterfaceUtil.isOpen(rsInterface)) {
            rsInterface.click();
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    return !validate();
                }
            }, General.random(2000, 3000));
        }
    }
    
    @Override
    public boolean validate() {
        return Login.getLoginState() == Login.STATE.INGAME && Game.getSetting(22) == 0;
    }
}
