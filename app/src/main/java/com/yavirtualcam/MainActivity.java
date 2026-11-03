package com.yavirtualcam;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.Gravity;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView screen = new TextView(this);
        screen.setText("YavirtualCam\n\nYour own Virtual Camera project.");
        screen.setTextSize(24);
        screen.setGravity(Gravity.CENTER);

        setContentView(screen);
    }
}
