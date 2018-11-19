package Motor_Subscriber;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.*;

import com.google.gson.Gson;
import com.phidget22.PhidgetException;
import com.phidget22.RCServo;

public class motorController implements MqttCallback{
	RCServo servo;
	public static final String userid = "14056838"; // change this to be your student-id

    @Override
    public void connectionLost(Throwable cause) {
        //This is called when the connection is lost. We could reconnect here.
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if(message.toString().equals("open")) {
        	System.out.println("Opening Door");
        	openLatch(servo);
        	
        	//Collect motor data and send
        	MotorData data = new MotorData("unknown","unknown");
        	data.setTagId("door1");
        	data.setAttempt("open");
        	Gson gson = new Gson();
        	String oneSensorJson = new String();
        	MotorToServerDB sendData = new MotorToServerDB();
        	oneSensorJson = gson.toJson(data);
        	sendData.sendToServer(oneSensorJson); //String
        }
        else if(message.toString().equals("close")) {
        	System.out.println("Closing Door");
        	closeLatch(servo);
        }

        if ((userid+"/LWT").equals(topic)) {
            System.err.println("Sensor gone!");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //no-op
    }
    
	public static void openLatch(RCServo servo) throws InterruptedException, PhidgetException {    	
		servo = new RCServo();
        try {
        	servo.open(3000);
        	servo.setTargetPosition(180);
        	servo.setEngaged(true);  
        	servo.setTargetPosition(90);
            Thread.sleep(1000);
            servo.close();
            
        } catch (PhidgetException ex) {
            System.out.println(ex.getDescription());
        }
    }
	// Method to inform motor how to behave when we want to close the Latch
	public static void closeLatch(RCServo servo) throws InterruptedException, PhidgetException {    	
		servo = new RCServo();
        try {
        	servo.open(3000);
        	servo.setTargetPosition(90);
        	servo.setEngaged(true);  
        	servo.setTargetPosition(180);
            Thread.sleep(1000);
            servo.close();
            
        } catch (PhidgetException ex) {
            System.out.println(ex.getDescription());
        }
}
}
