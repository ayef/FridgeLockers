package com.sensorcon;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

public class ViolationsActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.violations);
/*
	    String token ="SrCEqEg2BxAbNWXdokByKVF5j"; 
	    String secret = "hajRpzIgBaXW5xLV1sdIK9m7kTk69Zl4y0y6KMjwVWi9X0V2t1";
	    String access_token="47413503-Uf7OwfX1TGBsv2IUuiLa6m3NpfJYwFifTOLre7z7P";
	    String access_secret="XMAgGXddUZyN7M1OgTXyRaJGtZdYXaGxCV8Esv0amjqz8";
	    AccessToken a = new AccessToken(access_token,access_secret);
	    Twitter twitter = new TwitterFactory().getInstance();
	    twitter.setOAuthConsumer("SrCEqEg2BxAbNWXdokByKVF5j", "hajRpzIgBaXW5xLV1sdIK9m7kTk69Zl4y0y6KMjwVWi9X0V2t1");
	    twitter.setOAuthAccessToken(a);
	    try {
	        //twitter.updateStatus(userTweet.getText().toString()+" sent from: "+android_device_id);
	    	twitter.updateStatus("Test tweet");
	    } catch (TwitterException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }*/
    }
    protected void onResume() {
        super.onResume();
	    /*String token ="SrCEqEg2BxAbNWXdokByKVF5j"; 
	    String secret = "hajRpzIgBaXW5xLV1sdIK9m7kTk69Zl4y0y6KMjwVWi9X0V2t1";
	    String access_token="47413503-Uf7OwfX1TGBsv2IUuiLa6m3NpfJYwFifTOLre7z7P";
	    String access_secret="XMAgGXddUZyN7M1OgTXyRaJGtZdYXaGxCV8Esv0amjqz8";
	    AccessToken a = new AccessToken(access_token,access_secret);
	    Twitter twitter = new TwitterFactory().getInstance();
	    twitter.setOAuthConsumer("SrCEqEg2BxAbNWXdokByKVF5j", "hajRpzIgBaXW5xLV1sdIK9m7kTk69Zl4y0y6KMjwVWi9X0V2t1");
	    twitter.setOAuthAccessToken(a);
	    try {
	        //twitter.updateStatus(userTweet.getText().toString()+" sent from: "+android_device_id);
	    	twitter.updateStatus("Test tweet");
	    } catch (TwitterException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }*/
        new postTweetTask().execute(null, null, null);
        SensorDronePollingService.db.addViolation("alice", "none");
        SensorDronePollingService.db.addViolation("alice", "none");
    }

    protected void onPause() {
        super.onPause();
    }

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
		        //twitter.updateStatus(userTweet.getText().toString()+" sent from: "+android_device_id);
		    	twitter.updateStatus("Test tweett");
		    } catch (TwitterException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
			return null;
		}
    }

}
