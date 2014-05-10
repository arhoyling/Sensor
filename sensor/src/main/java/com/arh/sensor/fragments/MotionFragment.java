package com.arh.sensor.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

/**
 * Monitors sensor state and relays information to the base activity using the specified
 * sample rate.
 * Created by alex r.hoyling on 03/05/14.
 */
public class MotionFragment extends Fragment implements SensorEventListener {
    private static final int                 SAMPLE_PERIOD = 15;

    private OnSensorStateChangedListener    _listener;
    private SensorManager                   _sensorManager;
    private Sensor                          _motionSensor;

    private int                             _samplePeriod = SAMPLE_PERIOD;

    public String getVendor() {
        return _motionSensor.getVendor();
    }

    public void setSampleRate(float sampleRate) {
        if (sampleRate == 0.0f) return;
        _samplePeriod = Math.round(1000 / sampleRate);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        _motionSensor = _sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        _sensorManager.registerListener(this, _motionSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            _listener = (OnSensorStateChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnSensorStateChangedListener");
        }
    }

    private long _lastTick = System.currentTimeMillis();

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            long tick = System.currentTimeMillis();
            long localPeriod = tick - _lastTick;

            if (localPeriod > _samplePeriod) {
                _lastTick = tick;
                double motion = Math.sqrt(   Math.pow(event.values[0], 2) +
                        Math.pow(event.values[1], 2) +
                        Math.pow(event.values[2], 2));

                // Warn the activity that we sampled a new value.
                _listener.onSensorStateChanged(motion, localPeriod);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Interface used to communicate with the activity
     */
    public interface OnSensorStateChangedListener {
        public void onSensorStateChanged(double motion, long period);
    }
}
