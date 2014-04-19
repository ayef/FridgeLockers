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

public class SettingsActivity extends Activity {

    // UI Elements
    Button btnConnect;
    Button btnDisconnect;
    Button btnMeasure;
    TextView tvStatus;
    TextView tvTemperature;

    // Sensordrone Objects
    Drone myDrone;
    DroneEventHandler myDroneEventHandler;
 //   DroneConnectionHelper myHelper;
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // The library file needed has been included in the /libs directory, as well.
        // The latest version of this library can be found at https://github.com/Sensorcon/Sensordrone
        // Also, don't forget to add the proper permissions in the Manifest! (BLUETOOTH and BLUETOOTH_ADMIN)

        // Set up our Sensordrone object
        myDrone = new Drone();
        myDrone.btConnect("00:17:EC:11:C0:0F");

        tvStatus = (TextView)findViewById(R.id.main_tv_connection_status);
        tvTemperature = (TextView)findViewById(R.id.main_tv_temperature);
        updateTextViewFromUI(tvStatus, "Connected");
        myDrone.enableTemperature();
        

        // This button will take a Temperature measurement when pressed
        btnMeasure = (Button)findViewById(R.id.main_btn_measure);
        btnMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myDrone.isConnected) {
                    myDrone.measureTemperature();
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

                if (droneEventObject.matches(DroneEventObject.droneEventType.CONNECTED)) {
                    myDrone.setLEDs(0,0,126);

                    updateTextViewFromUI(tvStatus, "Connected");

                    myDrone.enableTemperature();
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.DISCONNECTED)) {
                    // This is triggered when the disconnect method is called.
                    updateTextViewFromUI(tvStatus, "Not connected");
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.CONNECTION_LOST)) {
                    updateTextViewFromUI(tvStatus, "Connection lost!");
                    uiToast("Connection lost!");
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.TEMPERATURE_ENABLED)) {
                    myDrone.measureTemperature();
                }
                else if (droneEventObject.matches(DroneEventObject.droneEventType.TEMPERATURE_MEASURED)) {
                    String temp = String.format("%.2f \u00B0C",myDrone.temperature_Celsius);
                    updateTextViewFromUI(tvTemperature, temp);

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
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
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
    	SettingsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);
            }
        });
    }

    // A method to display a Toast notification from the UI thread
    public void uiToast(final String msg) {
    	SettingsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}


