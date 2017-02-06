package com.skayne.torch;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import static android.view.View.VISIBLE;

@RequiresApi(api = Build.VERSION_CODES.M)

public class MainActivity extends Activity {
    private static final int REF_TORCHLIGHT = 1;
    private static final int REF_BLINKLIGHT = 2;
    private static final int REF_ECOLIGHT = 3;
    public Handler handler;
    protected TextView tvState;
    Context context = this;
    boolean bBlink = false;
    boolean bTorch = false;
    boolean hasFlash = false;
    private ToggleButton tbTorch;
    private ToggleButton tbBlink;
    private ToggleButton tbEco;
    private Thread torchThread;
    private int lightState = 0;
    private int mode = 2; //TODO: Add blink mode 'quick' or 'slow'

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tbTorch = (ToggleButton) findViewById(R.id.torchButton);
        tbBlink = (ToggleButton) findViewById(R.id.blinkButton);
        tbEco = (ToggleButton) findViewById(R.id.ecoButton);
        tvState = (TextView) findViewById(R.id.textLight);
        tvState.setVisibility(View.INVISIBLE);
        handler = new Handler();

        //Check flash is supported
        TextView tvSupported = (TextView) findViewById(R.id.textSupported);
        tvSupported.setVisibility(View.INVISIBLE);
        hasFlash = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!hasFlash) {
            Toast.makeText(this, "The device has no integrated flash.", Toast.LENGTH_LONG).show();
            tvSupported.setVisibility(VISIBLE);
        }
    }

    public void onTorch(View view) {
        boolean checked = ((ToggleButton) view).isChecked();

        if (lightState != REF_TORCHLIGHT) tbTurnOff(lightState);  //Toggle actual mode
        lightState = 0;

        tbTurnOff(lightState);  //Toggle actual mode
        if (checked)             //Turn ON new mode if exists
        {
            lightState = REF_TORCHLIGHT;
            tvState.setText(R.string.text_on);
            tvState.setVisibility(View.VISIBLE);

            new Thread(new TorchManagerThread()).start();
        } else tvState.setText(R.string.text_off);
    }

    public void onBlink(View view) {
        boolean checked = ((ToggleButton) view).isChecked();

        if (lightState != REF_BLINKLIGHT) tbTurnOff(lightState);  //Toggle actual mode
        lightState = 0;

        tbTurnOff(lightState);  //Toggle actual mode
        if (checked)             //Turn ON new mode if exists
        {
            lightState = REF_BLINKLIGHT;
            tvState.setText(R.string.text_blink);
            tvState.setVisibility(View.VISIBLE);

            new Thread(new TorchManagerThread()).start();
        } else tvState.setText(R.string.text_off);
    }

    public void onEco(View view) {
        boolean checked = ((ToggleButton) view).isChecked();

        if (lightState != REF_ECOLIGHT) tbTurnOff(lightState);  //Toggle actual mode
        lightState = 0;

        if (checked)             //Turn ON new mode if exists
        {
            lightState = REF_ECOLIGHT;
            tvState.setText(R.string.text_eco);
            tvState.setVisibility(View.VISIBLE);

            new Thread(new TorchManagerThread()).start();
        } else tvState.setText(R.string.text_off);
    }

    private void tbTurnOff(int lightState) {
        switch (lightState) {
            case REF_TORCHLIGHT:
                tbTorch.toggle();
                break;
            case REF_BLINKLIGHT:
                tbBlink.toggle();
                break;
            case REF_ECOLIGHT:
                tbEco.toggle();
                break;
        }
    }

    private class TorchManagerThread implements Runnable {
        private CameraManager manager;

        @Override
        public void run() {
            final int state = lightState;
            manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                if (!hasFlash || state == 0) return;

                //End the thread when variable 'lightState' changes
                while (state == lightState) {
                    //TODO: New strategy to avoid double click bug (2 or more threads created)
                    manager.setTorchMode(manager.getCameraIdList()[0], true);

                    if (lightState == REF_BLINKLIGHT) {
                        //Flashlight process

                        // Mode blink 1 : |_    __________               | (1.420s)
                        if (mode == 1) {
                            Thread.sleep(70);
                            manager.setTorchMode(manager.getCameraIdList()[0], false);
                            Thread.sleep(150);
                            manager.setTorchMode(manager.getCameraIdList()[0], true);
                            Thread.sleep(500);
                            manager.setTorchMode(manager.getCameraIdList()[0], false);
                            Thread.sleep(200);
                        }

                        // Mode blink 2 : |_   ___     __________        | (1.770s)
                        if (mode == 2) {
                            Thread.sleep(70);
                            manager.setTorchMode(manager.getCameraIdList()[0], false);
                            Thread.sleep(150);
                            manager.setTorchMode(manager.getCameraIdList()[0], true);
                            Thread.sleep(100);
                            manager.setTorchMode(manager.getCameraIdList()[0], false);
                            Thread.sleep(250);
                            manager.setTorchMode(manager.getCameraIdList()[0], true);
                            Thread.sleep(500);
                            manager.setTorchMode(manager.getCameraIdList()[0], false);
                            Thread.sleep(200);
                        }
                    } else if (lightState == REF_ECOLIGHT) {
                        // Mode blink eco : |________           | (1.000s)
                        Thread.sleep(300);
                        manager.setTorchMode(manager.getCameraIdList()[0], false);
                        Thread.sleep(200);
                    }
                    Thread.sleep(500);
                }

                //End thread turning OFF
                manager.setTorchMode(manager.getCameraIdList()[0], false);
            } catch (InterruptedException | CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
}