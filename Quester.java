package scripts;

import org.tribot.api.General;
import org.tribot.script.Script;
import org.tribot.script.interfaces.Arguments;
import org.tribot.script.interfaces.Painting;
import scripts.api.data.Bag;
import scripts.api.patterns.BaseScript;
import scripts.api.util.ACamera;

import java.awt.*;
import java.util.HashMap;

/**
 * Created by Spencer on 9/17/2016.
 */
public class Quester extends Script implements BaseScript, Arguments, Painting{
    private ACamera camera = new ACamera();
    private Bag bag = new Bag();

    @Override
    public void run() {
        scripts.quester.Quester.start(this);
    }

    @Override
    public void passArguments(HashMap<String, String> hashMap) {

    }

    @Override
    public void onPaint(Graphics graphics) {

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
        General.println(status);
    }

    @Override
    public void update(String message) {
        General.println(message);
    }
}
