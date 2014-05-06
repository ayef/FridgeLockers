package com.sensorcon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sensorcon.sensordrone.DroneEventHandler;
import com.sensorcon.sensordrone.DroneEventObject;
import com.sensorcon.sensordrone.android.Drone;

public class ReadingsActivity extends Activity {

    // UI Elements
    Button btnConnect;
    Button btnDisconnect;
    Button btnMeasure;
    TextView tvStatus;
    TextView tvTemperature;

    // Sensordrone Objects
    Drone myDrone;
    DroneEventHandler myDroneEventHandler;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readings);
        myDrone = new Drone();
        myDrone.btConnect("00:17:EC:11:C0:0F");
        tvStatus = (TextView)findViewById(R.id.main_tv_connection_status);
        tvTemperature = (TextView)findViewById(R.id.main_tv_temperature);
        updateTextViewFromUI(tvStatus, "Connected");
        myDrone.enableTemperature();
        myDrone.enableHumidity();
        myDrone.enableRGBC();

     // This button will take a Temperature measurement when pressed
        btnMeasure = (Button)findViewById(R.id.main_btn_refresh);
        btnMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myDrone.isConnected) {
                	myDrone.measureHumidity();
                    myDrone.measureTemperature();
                    myDrone.measureRGBC();
                }
                else if (myDrone.isConnected && !myDrone.temperatureStatus) {
                    // If the sensor isn't enabled, tell the user
                    genericDialog("Whoa!","The temperature sensor hasn't been enabled!");
                }
                else {
                    // If we weren't connected, tell the user
                    genericDialog("Whoa!","You are not currently connected to a Sensordrone");
                }
            }
        });
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
                    myDrone.enableHumidity();
                    myDrone.enableRGBC();
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
                	myDrone.measureHumidity();
                	myDrone.measureTemperature();
                	myDrone.measureRGBC();

                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.TEMPERATURE_MEASURED)) {
                    // This gets fired when the temperature is measured

                    // Let's update the TextView with our most recent reading
                    // You can put in logic to control what units are displayed,
                    // but we will just show Celsius here.
                    //
                    String temp = String.format("%.2f \u00B0C",myDrone.temperature_Celsius);
                    updateTextViewFromUI(tvTemperature, temp);
                    
                    
                    String temp1 = String.format("%.2f",myDrone.humidity_Percent);
                    temp1 = temp1 + " %";
                    updateTextViewFromUI((TextView)findViewById(R.id.main_tv_humidity), temp1);

                    String temp2 = String.format("%.2f",myDrone.rgbcLux);
                    temp2 = temp2 + " Lux";
                    updateTextViewFromUI((TextView)findViewById(R.id.main_tv_luminosity), temp2);
                    
                    
                    // We will show a toast message to assure the user a measurement has been made.
                    uiToast("Values Refreshed!");

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
        AlertDialog.Builder builder = new AlertDialog.Builder(ReadingsActivity.this);
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
        ReadingsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);
            }
        });
    }

    // A method to display a Toast notification from the UI thread
    public void uiToast(final String msg) {
        ReadingsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ReadingsActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
