package main.java.edu.pi.scanmap.manager;

import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import main.java.edu.pi.scanmap.util.AwsCredentials;

public class IotMessageManager extends MessageManager {
    private static final String clientId = "ClientId-1";   // replace with your own client ID. Use unique client IDs for concurrent connections.
    private static final String TOPIC = "dev/scan-map";

    private AWSIotMqttClient client;

    public IotMessageManager(final String clientEndpoint, final AwsCredentials awsCredentials) throws Exception {
        client = new AWSIotMqttClient(clientEndpoint, clientId, awsCredentials.getAccessKeyId(),
                awsCredentials.getSecretAccessKey());

        // optional parameters can be set before connect()
        client.connect();
    }

    public void publish(final String message) throws Exception {
        client.publish(TOPIC, AWSIotQos.QOS0, message);
    }
}
