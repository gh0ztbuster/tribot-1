package scripts;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.Login;
import org.tribot.script.Script;
import org.tribot.script.interfaces.Arguments;
import org.tribot.script.interfaces.Painting;
import scripts.api.data.Bag;
import scripts.api.patterns.BaseScript;
import scripts.api.util.ACamera;
import scripts.api.web.AccountCreator;
import scripts.quester.*;

import java.awt.*;
import java.util.HashMap;

/**
 * Created by Spencer on 9/17/2016.
 */
public class Tutorial extends Script implements BaseScript, Arguments, Painting{
    private ACamera camera = new ACamera();
    private Bag bag = new Bag();

    @Override
    public void run() {
        General.println("Starting tutorial script.");
        this.setLoginBotState(false);
        if (Integer.parseInt(AccountCreator.getBag().get("accountsToMake", "0")) == 0) {
            this.setLoginBotState(true);
            if (scripts.tutorial.Tutorial.start(this)) {
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

                            if (scripts.tutorial.Tutorial.start(this)) {
                                System.out.println("DeluxeTutorial took " + Timing.msToString(System.currentTimeMillis() - bag.get("tutorialStartTime", System.currentTimeMillis())) + " to complete.");
                                if (Boolean.valueOf(AccountCreator.getBag().get("7qp", "false"))) {
                                    if (scripts.quester.Quester.start(this)) {
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
    public void passArguments(HashMap<String, String> hashMap) {
        AccountCreator.setup(hashMap);
    }

    @Override
    public void onPaint(Graphics g) {
        g.setColor(new Color(255, 255, 255));
        g.drawString("Runtime: " + Timing.msToString(this.getRunningTime()), 50, 50);
        g.drawString("Number of accounts to create: " + AccountCreator.getBag().get("accountsToMake", "0"), 50, 80);
        g.drawString("Current task: " + bag.get("status", ""), 50, 110);
    }

    @Override
    public ACamera getCamera() {
        return camera;
    }

    @Override
    public Bag getBag() {
        return bag;
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
        General.println(message);
    }
}
