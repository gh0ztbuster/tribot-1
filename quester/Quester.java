package scripts.quester;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Arguments;
import scripts.api.data.Bag;
import scripts.api.patterns.BaseScript;
import scripts.api.patterns.Node;
import scripts.api.util.ACamera;
import scripts.api.util.Util;
import scripts.quester.nodes.CooksAssistantNode;
import scripts.quester.nodes.RomeoNode;
import scripts.quester.nodes.SheepShearNode;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Spencer on 9/4/2016.
 */

@ScriptManifest(authors = { "Deluxes" }, category = "Tools", name = "DeluxeQuester", version = 1.00, description = "Completes 7QP for bots.", gameMode = 1)
public class Quester {
    static ArrayList<Node> nodes = new ArrayList<>();

    public static boolean start(BaseScript script) {
        script.getBag().addOrUpdate("questStartTime", System.currentTimeMillis());

        CooksAssistantNode cookNode = new CooksAssistantNode(script);
        if (runNode(cookNode)) {
            SheepShearNode sheapNode = new SheepShearNode(script);
            if (runNode(sheapNode)) {
                RomeoNode romeoNode = new RomeoNode(script);
                return runNode(romeoNode);
            }
        }

        return false;
    }

    private static boolean runNode(Node node) {
        while (node.validate()) {
            if (Util.isBanned(node.script))
                return false;

            node.execute();
            General.sleep(100, 250);
        }
        return true;
    }
}
