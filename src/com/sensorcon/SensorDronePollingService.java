package com.sensorcon;

import java.util.Calendar;
import java.util.Random;
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
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.sensorcon.sensordrone.DroneEventHandler;
import com.sensorcon.sensordrone.DroneEventObject;
import com.sensorcon.sensordrone.android.Drone;

public class SensorDronePollingService extends Service {
	
	int mStartMode;       // indicates how to behave if the service is killed
	public static String globalString;
	public static int violationsCounter = 0;	// Not used anymore - was used to simulate violations
	public static int currViolationState = 0;	// State variable to check whether a violation is ongoing
	public static SensorDronePollingService instance = null;	// Used by MainActivity to check whether the service is running
	private static MainActivity MAIN_ACTIVITY;					// Reference to the main activity window
	
	// Variables for polling
	private Timer timer = new Timer();
	private static long UPDATE_INTERVAL = 5000;
	
    // Sensordrone Objects
    Drone myDrone;
    DroneEventHandler myDroneEventHandler;
    
    // Random number generator for simulating luminance values from sensordrone
    Random r;
    
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

		// Initialize service variables
		instance = this;
		globalString="";
		r = new Random();

		// Fetch a value from the preferences
		SharedPreferences prefs = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);
		UPDATE_INTERVAL = prefs.getInt(getString(R.string.pref_snsrdrnPollInterval), R.string.pref_snsrdrnPollInterval_default);
		UPDATE_INTERVAL = UPDATE_INTERVAL *1000;	// Convert to milliseconds
		
        // Set up our Sensordrone object
        myDrone = new Drone();
        myDrone.btConnect("00:17:E9:50:E1:75");
        myDrone.enableTemperature();
        myDrone.enableHumidity();
        myDrone.enableRGBC();

        myDroneEventHandler = new DroneEventHandler() {
            @Override
            public void parseEvent(DroneEventObject droneEventObject) {
                if (droneEventObject.matches(DroneEventObject.droneEventType.CONNECTED)) {
                    myDrone.setLEDs(0,0,126);

                    myDrone.enableTemperature();
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.TEMPERATURE_ENABLED)) {
                    myDrone.measureTemperature();
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.TEMPERATURE_MEASURED)) {
                    String temp = String.format("%.2f \u00B0C",myDrone.temperature_Celsius);
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.TEMPERATURE_DISABLED)) {
                    // We disable the sensor right before we disconnect, so there's nothing
                    // to go here really, but if you wanted to do something in such a case,
                    // here is where it would go ;-)
                }
            }
        };

        myStartService();	// Start the background polling service

        if (MAIN_ACTIVITY != null) 
        	Toast.makeText(MAIN_ACTIVITY, "Sensordrone polling started", Toast.LENGTH_SHORT).show();
	}

	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myDrone.registerDroneListener(myDroneEventHandler);
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
	

	// Polling sensordrone at intervals
	private void pollSensordrone(){
		myDrone.measureRGBC();

		float temp =  myDrone.rgbcLux;			// The true luminance value, but we use a random value for simulating changes.
		float fakeLumValue = r.nextFloat();
		globalString = globalString + fakeLumValue + " ";

		boolean isViolationResult = isViolation(fakeLumValue);	// Send a random value between [0, 1.0)

		if(currViolationState == 0 && isViolationResult) {		// Violation started, enter violation state
			currViolationState = 1;
			applyViolationHandling();			
		}
		else if(currViolationState == 1 && !isViolationResult) {	// Violation ended, turn off violation state
			currViolationState = 0;	
		}
		
		
	}
	
	// Ring alarm for 3 seconds
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
		if(rtmp != null) {
			rtmp.play();
			Log.d("Debug", "ringtone EXISTS");
		}
		else {
			Log.d("Debug", "ringtone is null");
		}
		
		long ringDelay = 3000;
		TimerTask task = new TimerTask() {
		    @Override
		    public void run() {
		        rtmp.stop();
		    }
		};
	
		Timer timer = new Timer();
		timer.schedule(task, ringDelay);	
	
	}
	
	// Post tweet on maazahmad account using asynchronous task
	private void postTweet() {
		
		// Perform the tweet post asynchronously
		new postTweetTask().execute(null, null, null);
		
		// Show a view declaring that the tweet has been sent --- This is not necessary _
		// the user will see it on their twitter account, and displaying this view messes up the flow of the app because _
		// when multiple violations occur, the user will have to close these views
		Intent dialogIntent = new Intent(getBaseContext(), ViolationsActivity.class);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(dialogIntent);
	}
	
	// Store values for user in DB using Asynchronous task. Values stored for both alice and bob so we can see some statistics.
	private void collectStats() {
		// Store in DB asynchronously
		new storeViolationInDBTask().execute(null, null, null);
	}
	
	// Check if this is a violation -  banned times should be retrieved from SharedPreferences
	private boolean isViolation(float fValue) {
		SharedPreferences prefs = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);
		int banStart = prefs.getInt(getString(R.string.pref_bannedTimeStart), R.string.pref_bannedTimeStart_default);
		int banEnd = prefs.getInt(getString(R.string.pref_bannedTimeEnd), R.string.pref_bannedTimeEnd_default);
		
		Calendar c = Calendar.getInstance();
		int hour_of_day = c.get(Calendar.HOUR_OF_DAY);

		// TODO: Should be checking banned times here like : (hour_of_day >= banStart && hour_of_day <= banEnd) _
		// But it makes testing difficult so add this later
		if(fValue > 0.2 ) {		// Since this is receiving a random value between [0, 1.0), the violation will be triggered 80% of the time
			return true;
		}
		else {
			return false;	
		}

	}
	
	private void applyViolationHandling() {
		SharedPreferences prefs = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);
		boolean isRingAlarm = prefs.getBoolean(getString(R.string.pref_alarm), true);
		boolean isTweet = prefs.getBoolean(getString(R.string.pref_tweet), true);
		boolean isFB = prefs.getBoolean(getString(R.string.pref_postFB), false);
		boolean isCollectStats = prefs.getBoolean(getString(R.string.pref_collectStats), true);

		if(isRingAlarm)
			ringAlarm();

		if(isTweet)
			postTweet();

		if(isCollectStats)
			collectStats();
	}

	private void myStopService() {
		if (timer != null) 
			timer.cancel();
		
		currViolationState = 0;
		violationsCounter = 0;
	}

	// Class for handling tweeting asynchronously
    private class postTweetTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			Log.d("DEBUG", "InDoBackground");
			String token ="SrCEqEg2BxAbNWXdokByKVF5j"; 
		    String secret = "hajRpzIgBaXW5xLV1sdIK9m7kTk69Zl4y0y6KMjwVWi9X0V2t1";
		    String access_token="47413503-Uf7OwfX1TGBsv2IUuiLa6m3NpfJYwFifTOLre7z7P";
		    String access_secret="XMAgGXddUZyN7M1OgTXyRaJGtZdYXaGxCV8Esv0amjqz8";
		    AccessToken a = new AccessToken(access_token,access_secret);
		    Twitter twitter = new TwitterFactory().getInstance();
		    twitter.setOAuthConsumer("SrCEqEg2BxAbNWXdokByKVF5j", "hajRpzIgBaXW5xLV1sdIK9m7kTk69Zl4y0y6KMjwVWi9X0V2t1");
		    twitter.setOAuthAccessToken(a);
		    try {
		    	Random r = new Random();
		    	twitter.updateStatus("MM Test tweet #" + r.nextInt());
		    	
		    } catch (TwitterException e) {
		        e.printStackTrace();
		    }
			return null;
		}
    }

    // Class for handling DB update asynchronously
    private class storeViolationInDBTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			// Store one violation for alice, store two for bob (bob simulates another user using FridgeLocker whose data _ 
			// would also be on the cloud). The users can be used later to show on aggregated statistics on the 'Statistics' page
			MainActivity.db.addViolation("alice", "");
	        
			MainActivity.db.addViolation("bob", "");
	        MainActivity.db.addViolation("bob", "");

	        return null;
		}
    }
    
}
