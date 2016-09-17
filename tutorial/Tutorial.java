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
public class Tutorial extends Script implements BaseScript, Arguments, Painting {
    static ArrayList<Node> nodes = new ArrayList<>();
    private ACamera camera = new ACamera();
    private Bag bag = new Bag();

    @Override
    public void run() {
        if (Integer.parseInt(AccountCreator.getBag().get("accountsToMake", "0")) == 0) {
            if (start(this)) {
                System.out.println("DeluxeTutorial took " + Timing.msToString(System.currentTimeMillis() - bag.get("tutorialStartTime", System.currentTimeMillis())) + " to complete.");
            } else {
                System.out.println("DeluxeTutorial ran for " + Timing.msToString(System.currentTimeMillis() - bag.get("tutorialStartTime", System.currentTimeMillis())) + " but was unable to complete.");
            }
            Login.logout();
        } else {
            while (Integer.parseInt(AccountCreator.getBag().get("accountsToMake", "0")) > 0) {
                this.setLoginBotState(false);
                AccountCreator.CreationResult result = AccountCreator.createAccount();
                switch (result) {
                    case SUCCESS:
                    case EMAIL_IN_USE:
                        setStatus("ACCOUNT CREATOR: Creating account.");
                        if (Boolean.valueOf(AccountCreator.getBag().get("completeTutorial", "false"))) {
                            setStatus("ACCOUNT CREATOR: Account creation successful.");
                            bag.addOrUpdate("email", AccountCreator.getBag().get("email"));
                            bag.addOrUpdate("password", AccountCreator.getBag().get("password"));

                            if (start(this)) {
                                System.out.println("DeluxeTutorial took " + Timing.msToString(System.currentTimeMillis() - bag.get("tutorialStartTime", System.currentTimeMillis())) + " to complete.");
                                if (Boolean.valueOf(AccountCreator.getBag().get("7qp", "false"))) {
                                    if (Quester.start(this)) {
                                        System.out.println("DeluxeQuester took " + Timing.msToString(System.currentTimeMillis() - bag.get("questStartTime", System.currentTimeMillis())) + " to complete.");
                                    } else {
                                        System.out.println("DeluxeQuester ran for " + Timing.msToString(System.currentTimeMillis() - bag.get("questStartTime", System.currentTimeMillis())) + " but was unable to complete.");
                                    }
                                }
                            } else {
                                System.out.println("DeluxeTutorial ran for " + Timing.msToString(System.currentTimeMillis() - bag.get("tutorialStartTime", System.currentTimeMillis())) + " but was unable to complete.");
                            }
                            Login.logout();
                        }
                        break;
                    case BAN:
                        setStatus("ACCOUNT CREATOR: Waiting 15 minute creation ban.");
                        General.sleep(15*60*1000, 20*60*1000); //Waitout 15minute creation ban
                        break;
                    case TIMEOUT:
                        setStatus("ACCOUNT CREATOR: Proxy request timed out.");
                        General.sleep(30*1000, 60*1000); //Wait as the proxy is being stupid
                        break;
                }
            }
        }
    }

    @Override
    public void onPaint(Graphics g) {
        g.setColor(new Color(255, 255, 255));
        g.drawString("Runtime: " + Timing.msToString(this.getRunningTime()), 50, 50);
        g.drawString("Number of accounts to create: " + AccountCreator.getBag().get("accountsToMake", "0"), 50, 80);
        g.drawString("Current task: " + bag.get("status", ""), 50, 110);
    }

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
        bag.addOrUpdate("status", status);
        General.println(status);
    }

    @Override
    public void update(String message) {

    }

    @Override
    public void passArguments(HashMap<String, String> hashMap) {
        AccountCreator.setup(hashMap);
    }
}
