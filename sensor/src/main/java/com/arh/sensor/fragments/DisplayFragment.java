package com.arh.sensor.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arh.sensor.R;
import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

/**
 * UI fragment managing the visual interface of the application and touch events.
 * It receives information from the base activity to display a chart of motion speed.
 * Created by alex r.hoyling on 02/05/14.
 */
public class DisplayFragment extends Fragment implements View.OnTouchListener {
    private static final double GRAPH_Y_MAX   = 35;
    private static final int    VIEWPORT_SIZE = 100;
    public static final String  LOG_FORMAT    = "%2.2f";
    public static final String  GRAPH_TITLE   = "Motion sampling";


    private LogActionListener   _listener;
    private TextView            _textView;
    private Button              _logButton;
    private LinearLayout        _layout;

    // GraphView
    private GraphView           _graphView;
    private GraphViewSeries     _graphSeries;
    private long                _counter = 0;

    // Coordinates of the current touch. 0.0f if there is no touch.
    private float               _x;
    private float               _y;

    public void setButtonLabel(String label) {
        _logButton.setText(label);
    }

    public float getX() {
        return _x;
    }

    public float getY() {
        return _y;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            _listener = (LogActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnLogStateListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stats, container, false);
        _layout = (LinearLayout) rootView.findViewById(R.id.stats_layout);
        rootView.setOnTouchListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        _textView = (TextView) getView().findViewById(R.id.motion);
        _logButton = (Button) getView().findViewById(R.id.action_log);

        _logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _listener.onLogPressed();
            }
        });

        _graphSeries = new GraphViewSeries(
                GRAPH_TITLE,
                new GraphViewSeries.GraphViewSeriesStyle(Color.rgb(184, 61, 61),3),
                new GraphView.GraphViewData[0]);
        _graphView = buildGraphView();
        _graphView.addSeries(_graphSeries);
        _layout.addView(_graphView);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                _x = 0.0f;
                _y = 0.0f;
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                _x = event.getX();
                _y = event.getY();
                break;
        }
        return true;
    }

    /**
     * Instanciate and configure a GraphView object.
     * @return
     */
    private GraphView buildGraphView() {
        LineGraphView graphView = new LineGraphView(getActivity(), "");
        graphView.setHorizontalLabels(new String[0]);
        graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (!isValueX) {
                    return String.valueOf(Math.round(value));
                }
                return null;
            }
        });
        graphView.setManualYAxisBounds(GRAPH_Y_MAX, 0);

        GraphViewStyle graphStyle = graphView.getGraphViewStyle();
        graphStyle.setGridColor(Color.LTGRAY);
        graphStyle.setVerticalLabelsColor(Color.rgb(0,99,99));
        graphStyle.setTextSize(32);
        graphStyle.setNumVerticalLabels(VIEWPORT_SIZE);
        graphStyle.setNumVerticalLabels(20);

        graphView.setShowLegend(true);
        graphView.setLegendAlign(GraphView.LegendAlign.TOP);
        graphView.setLegendWidth(300);
        graphView.setViewPort(0, VIEWPORT_SIZE);
        graphView.setScrollable(true);
        graphView.setScalable(false);
        graphView.setDisableTouch(true);    // Make sure GraphView propagates touch events.

        return graphView;
    }

    /**
     * Update GraphView and TextView with a new motion value.
     * @param motion
     */
    public void updateMotion(double motion) {
        _graphSeries.appendData(new GraphView.GraphViewData(_counter++, motion), true, VIEWPORT_SIZE);
        _textView.setText(String.format(LOG_FORMAT, motion));
    }

    /**
     * Interface used to communicate with the activity
     */
    public interface LogActionListener {
        public void onLogPressed();
    }
}
