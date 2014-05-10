package com.arh.sensor.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;

import com.arh.sensor.R;
import com.arh.sensor.fragments.DisplayFragment;
import com.arh.sensor.fragments.LogFragment;
import com.arh.sensor.fragments.MotionFragment;

/**
 * Base activity managing the different fragments of the application (UI fragment, and view-less
 * fragments).
 * Created by alex r.hoyling on 03/05/14.
 */
public class BaseActivity   extends     Activity
        implements  MotionFragment.OnSensorStateChangedListener,
        DisplayFragment.LogActionListener {
    // Tag to retrieve the view-less fragments
    public static final String MOTIONFRAGMENT_TAG   = "MotionFragment";
    public static final String LOGFRAGMENT_TAG      = "LogFragment";
    public static final String LOG_FILE             = "log_";
    public static final long   MAX_LOG_TIME         = 300000L; // 5 minutes

    private long            _maxLogTime = MAX_LOG_TIME;
    private DisplayFragment _displayFragment = null;
    private LogFragment _logFragment   = null;
    private MotionFragment  _motionFragment   = null;

    private PowerManager    _powerManager;
    private Handler         _logHandler = new Handler();
    private boolean         _shouldLog = false;

    public void setMaxLogTime(long maxLogTime) {
        _maxLogTime = maxLogTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        _motionFragment = new MotionFragment();
        transaction.add(_motionFragment, MOTIONFRAGMENT_TAG);

        _logFragment = new LogFragment();
        transaction.add(_logFragment, LOGFRAGMENT_TAG);

        transaction.commit();
    }

    private Runnable _stopLogging = new Runnable() {
        @Override
        public void run() {
            stop();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        _displayFragment = (DisplayFragment) getFragmentManager().findFragmentById(R.id.fragment_stats);
        _powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    }

    // MotionFragment.OnSensorStateChangedListener interface implementation
    public void onSensorStateChanged(double motion, long period) {
        _displayFragment.updateMotion(motion);

        if (_shouldLog) {
            _logFragment.log(motion, _powerManager.isScreenOn(),
                    _displayFragment.getX(), _displayFragment.getY(),
                    period);
        }
    }

    // DisplayFragment.LogActionListener interface implementation
    public void onLogPressed() {
        if (_shouldLog) {
            stop();
        } else {
            _logHandler.postDelayed(_stopLogging, _maxLogTime);
            _logFragment.startLogging(  LOG_FILE + System.currentTimeMillis() + ".csv",
                    _motionFragment.getVendor());
            _displayFragment.setButtonLabel(getResources().getString(R.string.action_stop_logging));
        }

        _shouldLog = !_shouldLog;
    }

    /**
     * Stop logging sensor information.
     * Turns off the logging timer and restores the logging button label.
     */
    private void stop() {
        _logHandler.removeCallbacks(_stopLogging);
        _logFragment.stopLogging();
        _displayFragment.setButtonLabel(getResources().getString(R.string.action_start_logging));
    }
}
