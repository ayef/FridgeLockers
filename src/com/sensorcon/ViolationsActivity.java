package com.sensorcon;

import android.app.Activity;
import android.os.Bundle;

// This 
public class ViolationsActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.violations);

    }
    protected void onResume() {
        super.onResume();

        //MainActivity.db.addViolation("alice", "none");
        //MainActivity.db.addViolation("bob", "none");
    }

    protected void onPause() {
        super.onPause();
    }
}
