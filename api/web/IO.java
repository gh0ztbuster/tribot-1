package scripts.api.web;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import scripts.io.socket.IOCallback;
import scripts.io.socket.SocketIO;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by Spencer on 7/31/2016.
 */
public class IO extends Thread {
    private SocketIO socket = null;
    private Properties props;
    private IOCallback callback;
    private String ip;
    private ArrayList<String> messages = new ArrayList<>();
    private static IO _instance = null;
    public long lastMessageTime = System.currentTimeMillis();
    private String lastMessage = "";

    public void setup(String ips, Properties props, IOCallback callback) {
        ip = ips;
        this.props = props;
        this.callback = callback;
        getSocket();
        this.start();
        General.println("Waiting for socket connection.");
    }

    public SocketIO getSocket() {
        try {
            if (socket == null) {
                System.out.println("Null socket...");
                return connect();
            } else if (!socket.isConnected()) {
                System.out.println("Reconnecting...");
                socket.reconnect();
                Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return socket != null && socket.isConnected();
                    }
                }, 5000);
            }

            return socket;
        } catch(Exception e) {
            return socket;
        }
    }

    public static IO get() {
        return _instance = _instance == null ? new IO() : _instance;
    }

    public SocketIO connect() {
        try {
            System.out.println("Connecting to socket...");
            socket = new SocketIO(ip, props, callback);
            Timing.waitCondition(new Condition() {
                @Override
                public boolean active() {
                    return socket != null && socket.isConnected();
                }
            }, 5000);
        } catch (MalformedURLException e) {
            General.println(e.toString());
        }
        return socket;
    }

    public void addMessage(String msg) {
        messages.add(msg);
    }

    @Override
    public void run() {
        while(true) {
            try {
                if (messages.size() > 0) {
                    if (socket.isConnected()) {
                        String m = messages.remove(0);
                        lastMessageTime = System.currentTimeMillis();

                        if (!m.equals(lastMessage)) {
                            System.out.println("Sending message: " + m);
                            socket.send(m);
                        }

                        lastMessage = m;
                    } else {
                        getSocket();
                    }
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                System.out.println("IO Run exception: " + e.toString());
            }

            if (!true)
                break;
        }

        System.out.println("Run loop ended...");
    }
}
