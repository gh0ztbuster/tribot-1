package scripts.quester;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.script.Script;
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
public class Quester extends Script implements BaseScript, Arguments {
    static ArrayList<Node> nodes = new ArrayList<>();
    private ACamera camera = new ACamera();
    private Bag bag = new Bag();

    @Override
    public void run() {
        if (Quester.start(this)) {
            System.out.println("DeluxeQuester took " + Timing.msToString(System.currentTimeMillis() - bag.get("questStartTime", System.currentTimeMillis())) + " to complete.");
        } else {
            System.out.println("DeluxeQuester ran for " + Timing.msToString(System.currentTimeMillis() - bag.get("questStartTime", System.currentTimeMillis())) + " but was unable to complete.");
        }
    }

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

    @Override
    public ACamera getCamera() {
        return this.camera;
    }

    @Override
    public Bag getBag() {
        return this.bag;
    }

    @Override
    public Script getScript() {
        return this;
    }

    @Override
    public void setStatus(String status) {
        General.println(status);
    }

    @Override
    public void update(String message) {

    }

    @Override
    public void passArguments(HashMap<String, String> hashMap) {

    }
}
