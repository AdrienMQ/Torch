package com.skayne.torch;

//Classic import

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

//More specific import


@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends Activity {
    Context context = this;
    boolean bBlink = false;
    boolean bTorch = false;
    boolean hasFlash;

    private TextView textView;
    private TextView notSupported;
    private Button btTorch;
    private Button btBlink;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = (TextView) findViewById(R.id.textLight);
        btTorch = (Button) findViewById(R.id.toggleButton);
        btBlink = (Button) findViewById(R.id.blinkButton);
        notSupported = (TextView) findViewById(R.id.notSupported);
        textView.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);

        handler = new Handler();

        hasFlash = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!hasFlash) {
            Toast.makeText(this, "The device has no integrated flash.", Toast.LENGTH_LONG).show();
            textView.setVisibility(View.VISIBLE);
        }
    }

    public void changeLightState(View view) {
        boolean checked = ((ToggleButton) view).isChecked();
        Thread torchThread = new Thread(new TorchManagerThread());

        if (hasFlash && checked) {
            textView.setText(R.string.text_on);
            textView.setVisibility(View.VISIBLE);
            bTorch = true;

            //Start thread
            torchThread.start();
        } else if (hasFlash) {
            textView.setText(R.string.text_off);
            bTorch = false;
        }
    }

    public void changeLightMode(View view) {
        bBlink = ((ToggleButton) view).isChecked();
    }

    private class TorchManagerThread implements Runnable {

        private CameraManager manager;

        @Override
        public void run() {
            manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                while (bTorch) {

                    manager.setTorchMode(manager.getCameraIdList()[0], true);
                    if (bBlink) {
                        //Flashlight process
                        // Mode 1 : |_    __________               | (1.420s)
                        /*
                        manager.setTorchMode(manager.getCameraIdList()[0], true);
                        Thread.sleep(70);
                        manager.setTorchMode(manager.getCameraIdList()[0], false);
                        Thread.sleep(150);
                        manager.setTorchMode(manager.getCameraIdList()[0], true);
                        Thread.sleep(500);
                        manager.setTorchMode(manager.getCameraIdList()[0], false);
                        Thread.sleep(200);
                        */

                        // Mode 2 : |_   ___     __________        | (1.770s)
                        manager.setTorchMode(manager.getCameraIdList()[0], true);
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

                    Thread.sleep(500);
                }
                manager.setTorchMode(manager.getCameraIdList()[0], false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
}