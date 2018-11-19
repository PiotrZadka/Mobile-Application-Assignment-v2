package RFID_Publisher;
import com.phidget22.*;

public class cardReaderController {
	// Creating new instance of Motor and Card Reader devices
	static RCServo doorlock;
	static RFID rfid;
	static cardReaderPublisher cardReader = new cardReaderPublisher();
	boolean locked = true;
	
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
	   			if(e.getTag().equals("Ming") && locked == true) {
	   				cardReader.start("open");
	   				locked = false;
	   			}
	   			else if(e.getTag().equals("Ming") && locked == false) {
	   				cardReader.start("close");
	   				locked = true;
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
}