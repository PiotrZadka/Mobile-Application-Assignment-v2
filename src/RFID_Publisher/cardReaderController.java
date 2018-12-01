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
	static RFID rfid;
	static cardReaderPublisher cardReader = new cardReaderPublisher();
	static cardReaderData cardReaderData = new cardReaderData("unknown","unknown","unknown","unknown");
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
	   			System.out.println("Tag ID detected => "+e.getTag());
	   			// Before broadcasting check if card is valid.
	   			// Locked variable is to keep track on each attempt of card being read
	   			// therefore we can distinguish if card is trying to open the door or close
	   			System.out.println("Validating Card ID");
	   			cardReaderData validationResult = (validateCard(cardReaderData));
	   			System.out.println("Checking what is being returned from validateCard()");
	   			System.out.println("CardName =>"+validationResult.getTagId()+" CardReaderID =>"+validationResult.getReaderId()+" MotorID =>"+validationResult.getMotorId());
	   			
	   			if(validationResult.getMotorId()!= null && locked == true) {
	   				//if everything is fine set state to open (this opens door lock only when card is valid)
	   				validationResult.setDoorState("open");
	   				// encapsulate everything in JSON
	   				String cardReaderDataJson = gson.toJson(validationResult);
	   				//Broadcast
	   				cardReader.start(cardReaderDataJson);
	   				//keep track on attempts
	   				locked = false;

	   			}
	   			// Same as above but this triggers every odd time when card is trying to open or close therefore "locked" is set as false
	   			else if(validationResult.getMotorId()!= null && locked == false) {
	   				validationResult.setDoorState("close");
	   				String cardReaderDataJson = gson.toJson(validationResult);
	   				cardReader.start(cardReaderDataJson);
	   				// keep track on attempts
	   				locked = true;
	   			}
	   			// If card is not recognized still broadcast details over so android app can inform about attempt.
	   			// Perhaps someone who is not validated is trying to get access.
	   			else {
	   				String cardReaderDataJson = gson.toJson(validationResult);
	   				validationResult.setDoorState("unknown");
	   				cardReader.start(cardReaderDataJson);
	   				System.out.print("Card "+validationResult.getTagId()+" is not recognised");
	   			}
	   		}
	    });
	       
		// Small listener to inform if reader lost detection of card
		rfid.addTagLostListener(new RFIDTagLostListener() {
      		  // What to do when a tag is lost
			public void onTagLost(RFIDTagLostEvent e) {
				System.out.print("Card moved away from reader");
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
	   			e1.printStackTrace();
	   		}
	    
	    //Switch off reader
	    rfid.close();
	    System.out.println("Card detection disabled");
	}
	
	// Card validation
	public cardReaderData validateCard(cardReaderData data) {
		
		// Targeting servlet for card validation
		String sensorServerURL = "http://localhost:8080/AssignmentServer/CardValidator";
		URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        //Converting data from with tagID to JSON
        String dataToJson = gson.toJson(data);
        try {
        	dataToJson = URLEncoder.encode(dataToJson, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
        
        String fullURL = sensorServerURL + "?validationData="+dataToJson;
        System.out.println("Sending data to: "+fullURL);  
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
           }
           rd.close();
        } catch (Exception e) {
           e.printStackTrace();
        }
        // Unpack received JSON and return as object
        cardReaderData resultData = gson.fromJson(result, cardReaderData.class);
        return resultData;    
	}
}