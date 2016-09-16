package scripts.api.patterns;

import org.tribot.script.Script;
import scripts.api.data.Bag;
import scripts.api.util.ACamera;

/**
 * Created by Spencer on 8/11/2016.
 */
public interface BaseScript {
    ACamera getCamera();
    Bag getBag();
    Script getScript();
    void setStatus(String status);
    void update(String message);
}
