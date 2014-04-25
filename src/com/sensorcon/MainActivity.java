package com.sensorcon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sensorcon.sensordrone.DroneEventHandler;
import com.sensorcon.sensordrone.DroneEventObject;
import com.sensorcon.sensordrone.android.Drone;

public class MainActivity extends Activity {

    // UI Elements
    Button btnConnect;
    Button btnDisconnect;
    Button btnMeasure;
    TextView tvStatus;
    TextView tvTemperature;
    int toggleService = 0;

    // Sensordrone Objects
    Drone myDrone;
    DroneEventHandler myDroneEventHandler;
    
    /** Called when the user clicks the Settings button
     * Opens the Settings Activity */
    public void viewSettings(View view) {
    	Intent intent = new Intent(this, SettingsActivity.class);
    	startActivity(intent);
    	
    }

    /** Called when the user clicks the 'View Readings' button 
     * Opens the Readings Activity */

    public void viewReadings(View view) {
    	Intent intent = new Intent(this, ReadingsActivity.class);
    	startActivity(intent);
    	
    }

    /** Called when the user clicks the 'View Statistics' button  
     * Opens the Statistics Activity */
    public void viewStatistics(View view) {
    	Intent intent = new Intent(this, StatisticsActivity.class);
    	startActivity(intent);
    	
    }
 // Method to start the service
    public void startService() {
    	toggleService = 1;
    	SensorDronePollingService.setMainActivity(this);
       startService(new Intent(getBaseContext(), SensorDronePollingService.class));
    }

    // Method to stop the service
    public void stopService() {
    	toggleService = 0;
    	uiToast(SensorDronePollingService.globalString);
       stopService(new Intent(getBaseContext(), SensorDronePollingService.class));
    }

    // Starts or stops the service
    public void StartOrStopMonitoring (View view) {
    	if(SensorDronePollingService.isInstanceCreated()) {
    		stopService();
    	}
    	else {
    		startService();
    	}
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Set up our Sensordrone object
        myDrone = new Drone();
        myDrone.btConnect("00:17:EC:11:C0:0F");
        
    

        // We have set up a simple layout named main.xml
        // There are three Buttons, and two TextViews that we will interact with

        // This TextView will be used to show our connection status (connected/not connected/connection lost)
        tvStatus = (TextView)findViewById(R.id.main_tv_connection_status);

        // Most all of these events are triggered from a background worker thread.
        // you need to remember this if you plan to update UI elements!
        // The method below shows how to easily do this.
        updateTextViewFromUI(tvStatus, "Connected");

        // Since we know we want to measure temperature, we will automatically enable the
        // temperature sensor here at every connect. Now the user won't have to worry about it.
        //myDrone.enableTemperature();
        
     
        // The events triggered are run through a DroneEventHandler.
        // This is where the magic happens :-)
        // DroneEventHandler is an interface, so things like MainActivity or even a custom
        // TextView can implement it. You can have multiple DroneEventHandlers, but it is important
        // to remember that the same event will be fired on all (registered) DroneEventHandlers at
        // the same time!
        myDroneEventHandler = new DroneEventHandler() {
            @Override
            public void parseEvent(DroneEventObject droneEventObject) {
                // We check the droneEventObject to see what type of event it is,
                // and then perform logic based upon the type.
                //
                // The DroneEventObject has available enum types that can be filtered,
                // and provides a boolean matches() method, that compares the
                // fired droneEventObject with supplied type. The names of the types
                // should be self explanatory.
                //
                // The (latest) version of the DroneEventObject class can be viewed at
                // https://github.com/Sensorcon/Sensordrone/blob/master/src/com/sensorcon/sensordrone/DroneEventObject.java

                if (droneEventObject.matches(DroneEventObject.droneEventType.CONNECTED)) {
                    // This is triggered when a connection is made.
                    // Set the LEDs blue
                    myDrone.setLEDs(0,0,126);

                    // Most all of these events are triggered from a background worker thread.
                    // you need to remember this if you plan to update UI elements!
                    // The method below shows how to easily do this.
                    updateTextViewFromUI(tvStatus, "Connected");

                    // Since we know we want to measure temperature, we will automatically enable the
                    // temperature sensor here at every connect. Now the user won't have to worry about it.
                    myDrone.enableTemperature();
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.DISCONNECTED)) {
                    // This is triggered when the disconnect method is called.
                    updateTextViewFromUI(tvStatus, "Not connected");
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.CONNECTION_LOST)) {
                    // This is triggered when a connection is lost.
                    // More accurately, this event is triggered when the software realizes
                    // that the connection has been (or might have been) lost;
                    // this is usually after a non-responsive command.
                    // So, for example, if you blink your LEDs once a second, then you will promptly
                    // trigger this event if a connection is lost.
                    // This app doesn't communicate, except when one of the buttons is pressed.
                    // Try connecting in the app, resetting your Sensordrone, and see how/when
                    // a lost connection is handled.
                    updateTextViewFromUI(tvStatus, "Connection lost!");
                    uiToast("Connection lost!");
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.TEMPERATURE_ENABLED)) {
                    // This is triggered when the temperature has been enabled

                    // Let's go ahead and take a reading for the user!
                    // Since we have set it up so connecting to the Sensordrone triggers enabling
                    // the temperature, and enabling temperature triggers measuring temperature,
                    // the user will always be displayed a temp reading when first connecting via this app.
                    // You don't have to keep this type of configuration, but you can 'daisy chain' commands
                    // in this manner, which can be useful in certain situations.
                    myDrone.measureTemperature();
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.TEMPERATURE_MEASURED)) {
                    // This gets fired when the temperature is measured

                    // Let's update the TextView with our most recent reading
                    // You can put in logic to control what units are displayed,
                    // but we will just show Celsius here.
                    //
                    // Note that the Sensordrone library provides some values in different units
                    // (for temperature it is Celsius, Fahrenheit, and Kelvin). You can obviously
                    // take one of those numbers and put it to any unit you want. If there is some
                    // unit you want in the library, let us know,
                    // or fork them code, add it, and send in a pull request on github!
                    String temp = String.format("%.2f \u00B0C",myDrone.temperature_Celsius);
                    updateTextViewFromUI(tvTemperature, temp);

                    // We will show a toast message to assure the user a measurement has been made.
                    uiToast("Temperature updated!");

                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.TEMPERATURE_DISABLED)) {
                    // We disable the sensor right before we disconnect, so there's nothing
                    // to go here really, but if you wanted to do something in such a case,
                    // here is where it would go ;-)
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        // The DroneEventHandler won't handle any notifications if it's not registered to a Sensordrone!
        myDrone.registerDroneListener(myDroneEventHandler);

        // You can have multiple drone objects (if your app connects to multiple sensordrones)
        // and you can register the same (or different) listeners to each one.
    }

    @Override
    protected void onPause() {
        super.onPause();
        // If you don't want your app to keep doing things in the background, then unregister
        // your DroneEventHandler.
        // This can get tricky if you're not careful!
        // For example, if you register your DroneEventHandler in onResume, but
        // don't unregister it in onPause, then the same listener can be added every onResume,
        // and you will start to get multiple notifications for the same event!
        // You also should consider the flow of events too, especially if you have any 'daisy-chained' events;
        // If your first event doesn't get re-triggered, then the rest of the chain won't either!
        myDrone.unregisterDroneListener(myDroneEventHandler);
    }

    // A method to show a generic alert dialog with the supplied title and content
    public void genericDialog(String title, String msg) {
        Dialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    // A method to update a TextView from the UI thread
    public void updateTextViewFromUI(final TextView textView, final String text) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);
            }
        });
    }

    // A method to display a Toast notification from the UI thread
    public void uiToast(final String msg) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
