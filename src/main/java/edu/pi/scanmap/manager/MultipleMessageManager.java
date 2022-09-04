package main.java.edu.pi.scanmap.manager;

import java.util.List;

public class MultipleMessageManager extends MessageManager {

    private List<MessageManager> managers;

    public MultipleMessageManager(final List<MessageManager> managers) {
        this.managers = managers;
    }

    @Override
    public void publish(String message) throws Exception {
        for (MessageManager manager : managers) {
            manager.publish(message);
        }
    }
}
