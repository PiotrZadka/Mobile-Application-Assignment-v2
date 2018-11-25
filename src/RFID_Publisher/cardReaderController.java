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
	// Creating new instance of Motor and Card Reader devices
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

	
	public void checkTag() throws PhidgetException{
		// When called, first of all create new object of RFID upon which we execute listeners.
		rfid = new RFID();
		// Waiting for card reader to be detected
		rfid.addTagListener(new RFIDTagListener() {
			//UploadData upload;
	   		public void onTag(RFIDTagEvent e) {
	   			// Change "card" to the name of your ID card you are using.
	   			// If doors are closed and card detected
	   			String tagID = e.getTag();
	   			cardReaderData.setTagId(tagID);
	   			
	   			try {
					String rfidID = String.valueOf(rfid.getDeviceID());
					cardReaderData.setReaderId(rfidID);
				} catch (PhidgetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	   			if(validateCard(cardReaderData).equals("success") && locked == true) {
	   				//cardReader.start("open");
	   				cardReaderData.setDoorState("open");
	   				String cardReaderDataJson = gson.toJson(cardReaderData);
	   				cardReader.start(cardReaderDataJson);
	   				locked = false;

	   			}
	   			else if(validateCard(cardReaderData).equals("success") && locked == false) {
	   				//cardReader.start("close");
	   				cardReaderData.setDoorState("close");
	   				String cardReaderDataJson = gson.toJson(cardReaderData);
	   				cardReader.start(cardReaderDataJson);
	   				locked = true;
	   			}
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