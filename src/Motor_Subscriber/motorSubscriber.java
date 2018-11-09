package Motor_Subscriber;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;


public class motorSubscriber {
	public static final String BROKER_URL = "tcp://iot.eclipse.org:1883";
    // public static final String BROKER_URL = "tcp://broker.mqttdashboard.com:1883";

    public static final String userid = "14056838"; // change this to be your student-id
    String clientId = userid + "-sub";


    private MqttClient mqttClient;

    public motorSubscriber() {

        try {
            mqttClient = new MqttClient(BROKER_URL, clientId);
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void start() {
        try {
        	mqttClient.setCallback(new motorController());
            mqttClient.connect();

            //Subscribe to correct topic
            final String topic = userid+"/doorState";
            mqttClient.subscribe(topic);

            System.out.println("Subscriber is now listening to "+topic);

        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String... args) {
        final motorSubscriber subscriber = new motorSubscriber();
        subscriber.start();
    }
}
