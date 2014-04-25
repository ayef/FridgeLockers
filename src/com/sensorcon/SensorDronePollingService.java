package com.sensorcon;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.sensorcon.sensordrone.DroneEventHandler;
import com.sensorcon.sensordrone.DroneEventObject;
import com.sensorcon.sensordrone.android.Drone;

public class SensorDronePollingService extends Service {
	
	int mStartMode;       // indicates how to behave if the service is killed
	public static String globalString;
	public static SensorDronePollingService instance = null;
	private static MainActivity MAIN_ACTIVITY;	// Reference to the main activity window
	
	// Variables for polling
	private Timer timer = new Timer();
	private static final long UPDATE_INTERVAL = 5000;
	
    // Sensordrone Objects
    Drone myDrone;
    DroneEventHandler myDroneEventHandler;

	// Check if service is running
    public static boolean isInstanceCreated() { 
	      return instance != null; 
	}
	@Override
	public IBinder onBind(Intent arg0) {
		// Not using IPC so return NULL
		return null;
	}
	
	public static void setMainActivity(MainActivity activity) {
		  MAIN_ACTIVITY = activity;
	}

	@Override 
	public void onCreate() {
		  super.onCreate();
		  instance = this;
		  globalString="";
		  
        // Set up our Sensordrone object
        myDrone = new Drone();
        myDrone.btConnect("00:17:E9:50:E1:75");
        myDroneEventHandler = new DroneEventHandler() {
            @Override
            public void parseEvent(DroneEventObject droneEventObject) {
                if (droneEventObject.matches(DroneEventObject.droneEventType.CONNECTED)) {
                    // This is triggered when a connection is made.
                    // Set the LEDs blue
                    myDrone.setLEDs(0,0,126);
                    //updateTextViewFromUI(tvStatus, "Connected");

                    myDrone.enableTemperature();
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.DISCONNECTED)) {
                    // This is triggered when the disconnect method is called.
                    //updateTextViewFromUI(tvStatus, "Not connected");
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.CONNECTION_LOST)) {
                    // This is triggered when a connection is lost.
                    //updateTextViewFromUI(tvStatus, "Connection lost!");
                    //uiToast("Connection lost!");
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.TEMPERATURE_ENABLED)) {
                    // This is triggered when the temperature has been enabled

                    // Let's go ahead and take a reading for the user!
                    myDrone.measureTemperature();
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.TEMPERATURE_MEASURED)) {
                    // This gets fired when the temperature is measured

                    String temp = String.format("%.2f \u00B0C",myDrone.temperature_Celsius);
                    //updateTextViewFromUI(tvTemperature, temp);

                    // We will show a toast message to assure the user a measurement has been made.
                    //uiToast("Temperature updated!");

                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.TEMPERATURE_DISABLED)) {
                    // We disable the sensor right before we disconnect, so there's nothing
                    // to go here really, but if you wanted to do something in such a case,
                    // here is where it would go ;-)
                }
            }
        };

		  // init the service here
		  myStartService();

		  if (MAIN_ACTIVITY != null) 
			  Toast.makeText(MAIN_ACTIVITY, "Sensordrone polling started", Toast.LENGTH_SHORT).show();
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        return Service.START_STICKY;
    }
	
	
	@Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
		super.onDestroy();
		instance = null;
		myStopService();
		
		if (MAIN_ACTIVITY != null) 
			Toast.makeText(MAIN_ACTIVITY, "Sensordrone polling stopped", Toast.LENGTH_SHORT).show();
				
    }
	
	
	private void myStartService() {
		timer.scheduleAtFixedRate( 
			      new TimerTask() {
					public void run() {
						// Poll sensordrone at regular intervals
						pollSensordrone();
					}
			      },
			      0,
			      UPDATE_INTERVAL);
	}
	
	
	private void pollSensordrone(){
		myDrone.measureTemperature();
		String temp = String.format("%.2f \u00B0C",myDrone.temperature_Celsius);
		globalString = globalString + temp + " ";
		//if (MAIN_ACTIVITY != null) 
		//	Toast.makeText(MAIN_ACTIVITY, "Temperature is " + temp, Toast.LENGTH_SHORT).show();
		
	}

	private void myStopService() {
		if (timer != null) 
			timer.cancel();
	}
	
}
