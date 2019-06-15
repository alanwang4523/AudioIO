package com.alan.audioio;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.alan.audioio.utils.Logger;
import com.alan.audioio.utils.RuntimePermissionsManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText("Audio IO");

        RuntimePermissionsManager runtimePermissionsManager = new RuntimePermissionsManager(this,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        runtimePermissionsManager.setListener(new RuntimePermissionsManager.Listener() {
            @Override
            public void onPermissionsGranted(boolean isAllPermissionsGranted) {
                Logger.d("onPermissionsGranted()-->> isAllPermissionsGranted = " + isAllPermissionsGranted);
            }

            @Override
            public void onAskedTooManyTimes() {
                Logger.d("onAskedTooManyTimes()-->>");
            }
        });
        runtimePermissionsManager.makeRequest();
    }

}
