package scripts.tutorial;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.Game;
import org.tribot.api2007.Login;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Arguments;
import org.tribot.script.interfaces.Painting;
import scripts.api.data.Bag;
import scripts.api.patterns.BaseScript;
import scripts.api.patterns.Node;
import scripts.api.util.ACamera;
import scripts.api.util.Util;
import scripts.api.web.AccountCreator;
import scripts.quester.Quester;
import scripts.tutorial.nodes.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Spencer on 9/4/2016.
 */

@ScriptManifest(authors = { "Deluxes" }, category = "Tools", name = "DeluxeTutorial", version = 1.00, description = "Completes tutorial island. Account creation agruments can be found in the script thread.", gameMode = 1)
public class Tutorial {
    static ArrayList<Node> nodes = new ArrayList<>();

    public static boolean start(BaseScript script) {
        script.getBag().addOrUpdate("tutorialStartTime", System.currentTimeMillis());

        nodes.clear();
        Collections.addAll(nodes, new CharacterStyleNode(script), new GettingStartedNode(script), new SurvivalNode(script), new ChefNode(script), new QuestNode(script), new MiningNode(script), new CombatNode(script), new BankNode(script), new PrayerNode(script), new MageNode(script), new CantReachNode(script));
        while (Login.getLoginState() != Login.STATE.INGAME) {
            if (Util.isBanned(script)) {
                System.out.println("Account is banned or locked.");
                return false;
            }
        }

        General.sleep(5000, 7500);

        script.update("{\"type\": \"CLIENT_UPDATE\", \"clientdata\": {\"clientid\": \"" + script.getBag().get("clientid", "") + "\", \"email\": \"" + script.getBag().get("email", "") + "\", \"ign\": \"" + Player.getRSPlayer().getName() + "\"}}");

        while (Game.getSetting(21) != 67108864) {
            if (Util.isBanned(script)) {
                System.out.println("Account is banned or locked.");
                return false;
            }

            RSTile lastPosition = script.getBag().get("lastPosition", null);
            if (lastPosition != null) {
                if (lastPosition.distanceTo(Player.getPosition()) > 10) {
                    script.getBag().addOrUpdate("lastPosition", Player.getPosition());
                    lastPosition = script.getBag().get("lastPosition");
                    script.update("{\"type\": \"CLIENT_UPDATE\", \"clientdata\": {\"clientid\": \"" + script.getBag().get("clientid", "") + "\", \"location\": \"" + lastPosition.getX() + "," + lastPosition.getY() + "," + lastPosition.getPlane() + "\"}}");
                }
            } else {
                script.getBag().addOrUpdate("lastPosition", Player.getPosition());
            }

            loop();

            if (Game.getSetting(29) != 0) break;
        }
        return true;
    }

    private static void loop() {
        for (final Node node : nodes) {
            if (node.validate()) {
                node.execute();
                General.sleep(General.random(100, 250));
            }
        }
    }
}
