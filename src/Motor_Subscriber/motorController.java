package Motor_Subscriber;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.*;

import com.google.gson.Gson;
import com.phidget22.PhidgetException;
import com.phidget22.RCServo;

public class motorController implements MqttCallback{
	public static final String userid = "14056838"; // change this to be your student-id
	RCServo servo;
	MotorData data = new MotorData("door1","unknown");
	Gson gson = new Gson();
	String dataJson = new String();
	MotorToServerDB sendData = new MotorToServerDB();
	
    @Override
    public void connectionLost(Throwable cause) {
        //This is called when the connection is lost. We could reconnect here.
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if(message.toString().equals("open")) {
        	System.out.println("Opening Door");
        	openLatch(servo);
        	//send data with successful attempt
        	data.setAttempt("open");
        	dataJson = gson.toJson(data);
        	sendData.sendToServer(dataJson);
        }
        else if(message.toString().equals("close")) {
        	System.out.println("Closing Door");
        	closeLatch(servo);
        	//send data with unsuccessful attempt
        	data.setAttempt("close");
        	dataJson = gson.toJson(data);
        	sendData.sendToServer(dataJson);
        }

        if ((userid+"/LWT").equals(topic)) {
            System.err.println("Sensor gone!");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
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
