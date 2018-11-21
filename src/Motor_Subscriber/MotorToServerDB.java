package Motor_Subscriber;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class MotorToServerDB  {
    
    public static String sensorServerURL = "http://localhost:8080/AssignmentServer/MotorServerDB";

    public String sendToServer(String motorDataJson){
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        try {
        	motorDataJson = URLEncoder.encode(motorDataJson, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
        String fullURL = sensorServerURL + "?sensordata="+motorDataJson;
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
           }
           rd.close();
        } catch (Exception e) {
           e.printStackTrace();
        }
        return result;    	
    }
}