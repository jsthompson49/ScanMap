package main.java.edu.pi.scanmap.manager;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;

public abstract class MessageManager {

    private static final String START_EVENT_FROMAT = "{\"event\":\"started\",\"timestamp\":%s}";
    private ObjectMapper objectMapper = new ObjectMapper();

    public abstract void publish(final String message) throws Exception;

    public void publishData(final Object messageData) throws Exception {
        final String message = objectMapper.writeValueAsString(messageData);
        publish(message);
    }

    public void start(final Instant timestmap) throws Exception {
        publish(String.format(START_EVENT_FROMAT, timestmap.toEpochMilli()));
    }
}

