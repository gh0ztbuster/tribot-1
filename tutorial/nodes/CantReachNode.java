/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts.tutorial.nodes;

import org.tribot.api2007.types.RSInterface;
import scripts.api.patterns.BaseScript;
import scripts.api.patterns.Node;
import scripts.api.util.InterfaceUtil;

public class CantReachNode extends Node {
    public CantReachNode(BaseScript script) {
        super(script);
    }

    
    @Override
    public void execute() {
        System.out.println("Executing CantReachNode.");
        RSInterface cont = InterfaceUtil.get(162, 33);
        if (cont != null) {
            cont.click();
        }
    }
    
    @Override
    public boolean validate() {
        return InterfaceUtil.isOpen(162, 33);
    }
}
