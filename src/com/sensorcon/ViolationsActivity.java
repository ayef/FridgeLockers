package com.sensorcon;

import android.app.Activity;
import android.os.Bundle;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

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
	    }
    }

    protected void onPause() {
        super.onPause();
    }
}
