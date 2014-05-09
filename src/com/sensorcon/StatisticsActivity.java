package com.sensorcon;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class StatisticsActivity extends Activity {
    
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);

        List<String> badUsers = SensorDronePollingService.db.getAllUsersInfo();      
        TextView tv = (TextView)findViewById(R.id.main_tv_DBdata);
        for (String str : badUsers) {
            String log = str;
            Log.d("DEBUG", log);
            updateTextViewFromUI(tv, log + " \n" + tv.getText() );
        }

	}

    public void updateTextViewFromUI(final TextView textView, final String text) {
        StatisticsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);
            }
        });
    }

}