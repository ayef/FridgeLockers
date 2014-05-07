package com.sensorcon;

import java.util.Timer;
import java.util.TimerTask;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

import com.sensorcon.sensordrone.DroneEventHandler;
import com.sensorcon.sensordrone.DroneEventObject;
import com.sensorcon.sensordrone.android.Drone;

public class SensorDronePollingService extends Service {
	
	int mStartMode;       // indicates how to behave if the service is killed
	public static String globalString;
	public static int violationsCounter = 0;
	public static int currViolationState = 0;
	public static SensorDronePollingService instance = null;
	private static MainActivity MAIN_ACTIVITY;	// Reference to the main activity window
	
	// Variables for polling
	private Timer timer = new Timer();
	private static final long UPDATE_INTERVAL = 8000;
	
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
        myDrone.enableTemperature();
        myDrone.enableHumidity();

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
        // DroneEventHandler won't handle any notifications if it's not registered to a Sensordrone
        myDrone.registerDroneListener(myDroneEventHandler);
        // The service is starting, due to a call to startService()
        return Service.START_STICKY;
    }
	
	
	@Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
		super.onDestroy();
		instance = null;
		myStopService();
		myDrone.unregisterDroneListener(myDroneEventHandler);
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
			      2000,
			      UPDATE_INTERVAL);
	}
	
	
	private void pollSensordrone(){
		myDrone.measureTemperature();
		String temp = String.format("%.2f \u00B0C",myDrone.temperature_Celsius);
		globalString = globalString + temp + " ";
		boolean isViolationResult = isViolation(myDrone.temperature_Celsius);
		if(currViolationState == 0 && isViolationResult) {
			currViolationState = 1;
			//ringAlarm();
			applyViolationHandling();			
		}
		else if(currViolationState == 1 && isViolationResult) {
			// Do nothing
		}
		else if(currViolationState == 1 && !isViolationResult) {
			currViolationState = 0;
		}
		
		
	}
	
	private void ringAlarm() {
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		Ringtone r = RingtoneManager.getRingtone(getBaseContext(), alert);

		if(r == null){
			alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			r = RingtoneManager.getRingtone(getBaseContext(), alert);

			if(r == null){  
				alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
				r = RingtoneManager.getRingtone(getBaseContext(), alert);               
			}
		}
		final Ringtone rtmp = r;
		if(rtmp != null) 
			rtmp.play();
		long ringDelay = 5000;
		TimerTask task = new TimerTask() {
		    @Override
		    public void run() {
		        rtmp.stop();
		    }
		};
	
		Timer timer = new Timer();
		timer.schedule(task, ringDelay);	
	
	}
	
	private void postTweet() {
		//Code to launch an activity
		Intent dialogIntent = new Intent(getBaseContext(), ViolationsActivity.class);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(dialogIntent);
	}
	
	private boolean isViolation(float intValue) {
		SharedPreferences prefs = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);
		// Supposed to check
		
		if(intValue > 0 && (violationsCounter % 2 == 0)){
			violationsCounter = (violationsCounter + 1) % 2;
			return true;
			
		}
		violationsCounter = (violationsCounter + 1) % 2;
		return false;
	}
	
	private void applyViolationHandling() {
		SharedPreferences prefs = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);
		boolean isRingAlarm = prefs.getBoolean("pref_alarm", true);
		boolean isTweet = prefs.getBoolean(getString(R.string.pref_tweet), true);
		boolean isFB = prefs.getBoolean(getString(R.string.pref_postFB), false);
		boolean isCollectStats = prefs.getBoolean(getString(R.string.pref_collectStats), true);

		if(isRingAlarm)
			ringAlarm();

		if(isTweet)
			postTweet();
	}
	private void myStopService() {
		if (timer != null) 
			timer.cancel();
		
		currViolationState = 0;
		violationsCounter = 0;
	}
	
}
