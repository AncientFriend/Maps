package com.example.janis.maps;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class Logs {
    private Activity activity;
    public Logs (Activity activity) {
        this.activity = activity;
    }

    public void log(String tag, Object content) {
        if(BuildConfig.DEBUG) {
            Toast.makeText(activity, String.valueOf(content), Toast.LENGTH_LONG).show();
        }

        Log.d(tag, String.valueOf(content));
    }
}
