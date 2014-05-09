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
	public static int violationsCounter = 0;
	public static int currViolationState = 0;
	public static SensorDronePollingService instance = null;
	private static MainActivity MAIN_ACTIVITY;	// Reference to the main activity window
	
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
		  instance = this;
		  globalString="";
		  
	  //db = new FridgeLockerUserDBHelper(this);
		  r = new Random();
		  
		SharedPreferences prefs = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);
		UPDATE_INTERVAL = prefs.getInt(getString(R.string.pref_snsrdrnPollInterval), R.string.pref_snsrdrnPollInterval_default);
		UPDATE_INTERVAL = UPDATE_INTERVAL *1000;	// Convert to milliseconds
		
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
		myDrone.measureTemperature();
		myDrone.measureRGBC();

		float temp =  myDrone.rgbcLux;			// The true luminance value, byut we use a random value for simulating changes. 
		globalString = globalString + temp + " ";

		boolean isViolationResult = isViolation(r.nextFloat());	// Send a random value between [0, 1.0)

		if(currViolationState == 0 && isViolationResult) {		// Violation started, enter violation state
			currViolationState = 1;
			applyViolationHandling();			
		}
		else if(currViolationState == 1 && !isViolationResult) {	// Violation ended, so turn off violation state
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
		//Code to launch an activity
		new postTweetTask().execute(null, null, null);
		Intent dialogIntent = new Intent(getBaseContext(), ViolationsActivity.class);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(dialogIntent);
	}
	
	// Store values for user in DB using Asynchronous task. Values stored for both alice and bob so we can see some statistics.
	private void collectStats() {
		//Code to access DB
	}
	
	// Check if this is a violation -  banned times should be retrieved from SharedPreferences
	private boolean isViolation(float fValue) {
		SharedPreferences prefs = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);
		int banStart = prefs.getInt(getString(R.string.pref_bannedTimeStart), R.string.pref_bannedTimeStart_default);
		int banEnd = prefs.getInt(getString(R.string.pref_bannedTimeEnd), R.string.pref_bannedTimeEnd_default);
		
		/*// Toggling violations on and off to generate violations for simulation
		 *  if(fValue > 0.2 && (violationsCounter % 2 == 0)){
		 *  violationsCounter = (violationsCounter + 1) % 2;
			return true;
		}
		violationsCounter = (violationsCounter + 1) % 2;
		*/

		Calendar c = Calendar.getInstance();
		int hour_of_day = c.get(Calendar.HOUR_OF_DAY);

		// TODO: Should be checking banned times here like : (hour_of_day >= banStart && hour_of_day <= banEnd) But it makes testing difficult so later
		if(fValue > 0.2 )  {		// Since this is receiving a random value between [0, 1.0), the violation will be triggered 80% of the time
			return true;
		}
		else {
			return false;	
		}

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


        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            //showDialog("Downloaded " + result + " bytes");
        }

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
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
			return null;
		}
    }
    
}
