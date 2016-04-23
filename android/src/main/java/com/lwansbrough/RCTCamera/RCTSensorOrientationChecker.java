/**
 * Created by rpopovici on 23/03/16.
 */

package com.lwansbrough.RCTCamera;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.facebook.react.bridge.ReactApplicationContext;

interface RCTSensorOrientationListener {
    void orientationEvent();
}

public class RCTSensorOrientationChecker {

    int mOrientation = 0;
    private SensorEventListener mSensorEventListener;
    private SensorManager mSensorManager;
    private RCTSensorOrientationListener mListener = null;

    public RCTSensorOrientationChecker( ReactApplicationContext reactContext) {
        mSensorEventListener = new Listener();
        mSensorManager = (SensorManager) reactContext.getSystemService(Context.SENSOR_SERVICE);

    }

    /**
     * Call on activity onResume()
     */
    public void onResume() {
        mSensorManager.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Call on activity onPause()
     */
    public void onPause() {
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    private class Listener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];

            if (x<5 && x>-5 && y > 5)
                mOrientation = 0;
            else if (x<-5 && y<5 && y>-5)
                mOrientation = 3;
            else if (x<5 && x>-5 && y<-5)
                mOrientation = 2;
            else if (x>5 && y<5 && y>-5)
                mOrientation = 1;

            if (mListener != null) {
                mListener.orientationEvent();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void registerOrientationListener(RCTSensorOrientationListener listener) {
        this.mListener = listener;
    }

    public void unregisterOrientationListener() {
        mListener = null;
    }
}
