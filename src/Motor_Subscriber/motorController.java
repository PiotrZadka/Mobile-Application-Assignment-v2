package Motor_Subscriber;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.*;

import com.google.gson.Gson;
import com.phidget22.PhidgetException;
import com.phidget22.RCServo;

import RFID_Publisher.cardReaderData;

public class motorController implements MqttCallback{
	public static final String userid = "14056838"; // change this to be your student-id
	RCServo servo;
	MotorData data = new MotorData("door1","unknown","unknown");
	Gson gson = new Gson();
	String dataJson = new String();
	MotorToServerDB sendData = new MotorToServerDB();
	
    @Override
    public void connectionLost(Throwable cause) {
        //This is called when the connection is lost. We could reconnect here.
    }
    
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
    	servo = new RCServo();
    	servo.open(3000);
    	data.setMotorId(String.valueOf((servo.getDeviceID()))); // MotorServoID: PN_1066
    	
    	// Decode message received from MQTT and check if the card is assigned to this motorID
    	cardReaderData messageJson = gson.fromJson(message.toString(), cardReaderData.class);
    	System.out.print("CHECKING MQTT MSG "+"MOTOR_ID"+messageJson.getMotorId()+" "+messageJson.getReaderId()
    	+" "+messageJson.getTagId()+" "+messageJson.getDoorState());
    	// If message is valid but the request is to open the door
        if(messageJson.getMotorId().equals(String.valueOf((servo.getDeviceID()))) && messageJson.getDoorState().equals("open")) {
        	System.out.println("Opening Door");
        	openLatch(servo);
        	//send data with successful attempt
        	data.setAttempt(messageJson.getDoorState());
        	data.setTagId(messageJson.getTagId());
        	dataJson = gson.toJson(data);
        	System.out.print(dataJson);
        	sendData.sendToServer(dataJson);
        }
        // If message is valid but the request is to close the door
        else if(messageJson.getMotorId().equals(String.valueOf((servo.getDeviceID()))) && messageJson.getDoorState().equals("close")) {
        	System.out.println("Closing Door");
        	closeLatch(servo);
        	//send data with unsuccessful attempt
        	data.setAttempt(messageJson.getDoorState());
        	data.setTagId(messageJson.getTagId());
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
        try {
        	//servo.open(3000);
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
        try {
        	//servo.open(3000);
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
