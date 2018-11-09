package RFID_Publisher;
import org.eclipse.paho.client.mqttv3.*;

public class cardReaderPublisher {

    // public static final String BROKER_URL = "tcp://broker.hivemq.com:1883";
     public static final String BROKER_URL = "tcp://iot.eclipse.org:1883";
    //public static final String BROKER_URL = "tcp://broker.mqttdashboard.com:1883";

    public static final String userid = "14056838"; // change this to be your student-id

    public static final String TOPIC_TEMPERATURE = userid +"/doorState";

    private MqttClient client;


    public cardReaderPublisher() {


        try {

            client = new MqttClient(BROKER_URL, userid);

        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    void start(String doorState) {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setMaxInflight(1000);
            options.setAutomaticReconnect(true);
            options.setWill(client.getTopic(userid+"/LWT"), "I'm gone :(".getBytes(), 0, false);

            client.connect(options);

            //Publish data once
            publishTemperature(doorState);
            //Disconnect client in order to wait for another door state
            client.disconnect();

        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void publishTemperature(String doorState) throws MqttException {
        final MqttTopic temperatureTopic = client.getTopic(TOPIC_TEMPERATURE);
        
        temperatureTopic.publish(new MqttMessage(doorState.getBytes()));

        System.out.println("Published data. Topic: " + temperatureTopic.getName() + "  Message: " + doorState);
    }
}
