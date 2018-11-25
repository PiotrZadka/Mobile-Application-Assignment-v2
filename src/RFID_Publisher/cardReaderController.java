package RFID_Publisher;
import com.google.gson.Gson;
import com.phidget22.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class cardReaderController {
	static RCServo doorlock;
	static RFID rfid;
	static cardReaderPublisher cardReader = new cardReaderPublisher();
	static cardReaderData cardReaderData = new cardReaderData("unknown","unknown","unknown");
	boolean locked = true;
	Gson gson = new Gson();
	
	
	public static void main(String[] args) throws PhidgetException {
		// Execute card reader detection
		cardReaderController reader = new cardReaderController();
		reader.checkTag();
	}

	
	// This method checks the card ID being used and reader ID therefore broadcast through MQTT each attempt
	public void checkTag() throws PhidgetException{
		// When called, first of all create new object of RFID upon which we execute listeners.
		rfid = new RFID();
		// Waiting for card reader to be detected
		rfid.addTagListener(new RFIDTagListener() {
			//UploadData upload;
	   		public void onTag(RFIDTagEvent e) {
	   			
	   			// Set tag id that is being read by RFID (any card)
	   			String tagID = e.getTag();
	   			cardReaderData.setTagId(tagID);
	   			
	   			try {
	   				//Set device id which is being used
					String rfidID = String.valueOf(rfid.getDeviceID());
					cardReaderData.setReaderId(rfidID);
				} catch (PhidgetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	   			// Before broadcasting check if card is valid.
	   			// Locked variable is to keep track on each attempt of card being read
	   			// therefore we can distinguish if card is trying to open the door or close
	   			if(validateCard(cardReaderData).equals("success") && locked == true) {
	   				//if everything is fine set state to open (this opens door lock only when card is valid)
	   				cardReaderData.setDoorState("open");
	   				// encapsulate everything in JSON
	   				String cardReaderDataJson = gson.toJson(cardReaderData);
	   				//Broadcast
	   				cardReader.start(cardReaderDataJson);
	   				//keep track on attempts
	   				locked = false;

	   			}
	   			// Same as above but this triggers every odd time when card is trying to open or close therefore "locked" is set as false
	   			else if(validateCard(cardReaderData).equals("success") && locked == false) {
	   				cardReaderData.setDoorState("close");
	   				String cardReaderDataJson = gson.toJson(cardReaderData);
	   				cardReader.start(cardReaderDataJson);
	   				// keep track on attempts
	   				locked = true;
	   			}
	   			// If card is not recognized still broadcast details over so android app can inform about attempt.
	   			// Perhaps someone who is not validated is trying to get access.
	   			else {
	   				String cardReaderDataJson = gson.toJson(cardReaderData);
	   				cardReader.start(cardReaderDataJson);
	   				System.out.print("Card not recognised");
	   			}
	   		}
	    });
	       
		// Small listener to inform if reader lost detection of card
		rfid.addTagLostListener(new RFIDTagLostListener() {
      		  // What to do when a tag is lost
			public void onTagLost(RFIDTagLostEvent e) {
				System.out.println("Card not detected");
			}
		});
		
		//Switch on reader
		rfid.open(5000);
		rfid.setAntennaEnabled(true);
	          
	    System.out.println("Card detection enabled for 1 minute");
	    	try {
	    		// Keep card detection for 1 minute
	    		Thread.sleep(100000);
	   		} catch (InterruptedException e1) {
	   			// TODO Auto-generated catch block
	   			e1.printStackTrace();
	   		}
	    
	    //Switch off reader
	    rfid.close();
	    System.out.println("Card detection disabled");
	}
	
	// Card validation
	public String validateCard(cardReaderData data) {
		
		String sensorServerURL = "http://localhost:8080/AssignmentServer/CardValidator";
		URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String dataToJson = gson.toJson(data);
        try {
        	dataToJson = URLEncoder.encode(dataToJson, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
        
        String fullURL = sensorServerURL + "?validationData="+dataToJson;
        System.out.println("Sending data to: "+fullURL);  // DEBUG confirmation message
        String line;
        String result = "";
        try {
           url = new URL(fullURL);
           conn = (HttpURLConnection) url.openConnection();
           conn.setRequestMethod("GET");
           rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
           // Request response from server to enable URL to be opened
           while ((line = rd.readLine()) != null) {
              result += line;
              System.out.print(result);
           }
           rd.close();
        } catch (Exception e) {
           e.printStackTrace();
        }
        return result;    
	}
}