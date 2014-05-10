import android.graphics.Point;
import android.os.SystemClock;
import android.test.TouchUtils;
import android.test.UiThreadTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.arh.sensor.R;
import com.arh.sensor.fragments.DisplayFragment;
import com.jjoe64.graphview.GraphView;

import java.util.Random;

/**
 * Test UI and user interactions.
 * Created by alex r.hoyling on 03/05/14.
 */
public class DisplayFragmentTest extends ActivityTest {
    private static final long   LOG_TIME     = 2000L;
    private DisplayFragment     _fragment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _fragment = (DisplayFragment)
                _activity.getFragmentManager().findFragmentById(R.id.fragment_stats);
    }

    /**
     * Check that everything is in place. There must be a log button.
     */
    public void testLogButton() {
        Button logButton = (Button) _activity.findViewById(R.id.action_log);
        assertNotNull(logButton);

        final ViewGroup.LayoutParams layoutParams = logButton.getLayoutParams();
        assertNotNull(layoutParams);
        assertEquals(layoutParams.width, WindowManager.LayoutParams.MATCH_PARENT);
        assertEquals(layoutParams.height, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public void testLogView() {
        TextView logView = (TextView) _activity.findViewById(R.id.motion);
        assertNotNull(logView);

        final ViewGroup.LayoutParams layoutParams = logView.getLayoutParams();
        assertNotNull(layoutParams);
        assertEquals(layoutParams.width, WindowManager.LayoutParams.WRAP_CONTENT);
        assertEquals(layoutParams.height, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public void testGraphView() {
        ViewGroup rootView = (ViewGroup)_fragment.getView();
        assertEquals(3, rootView.getChildCount());

        View graphView = rootView.getChildAt(2);
        assertTrue(graphView instanceof GraphView);

        final ViewGroup.LayoutParams layoutParams = graphView.getLayoutParams();
        assertNotNull(layoutParams);
        assertEquals(layoutParams.width, WindowManager.LayoutParams.MATCH_PARENT);
        assertEquals(layoutParams.height, WindowManager.LayoutParams.MATCH_PARENT);

        // Make sure GraphView does not intercept touch events
        assertTrue(((GraphView) graphView).isDisableTouch());
    }

    /**
     * Test that touch events are captured correctly.
     * @throws Exception
     */
    public void testTouchEvent() throws Exception {
        Display display = _activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        View rootView = _fragment.getView();
        // Test that fragment's x and y are 0.0f when the screen is not touched.
        assertEquals(0.0f, _fragment.getX());
        assertEquals(0.0f, _fragment.getY());

        //
        Random rnd = new Random();
        float x = (float) rnd.nextInt(size.x);
        float y = (float) rnd.nextInt(size.y);

        long downTime = SystemClock.uptimeMillis();
        MotionEvent event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(),
                                                MotionEvent.ACTION_DOWN, x, y, 0);
        rootView.dispatchTouchEvent(event);

        assertEquals(x, _fragment.getX());
        assertEquals(y, _fragment.getY());

        //
        x = (float) rnd.nextInt(size.x);
        y = (float) rnd.nextInt(size.y);

        event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(),
                MotionEvent.ACTION_MOVE, x, y, 0);
        rootView.dispatchTouchEvent(event);

        assertEquals(x, _fragment.getX());
        assertEquals(y, _fragment.getY());

        // Test that coordinates are back to 0 once the touch is over.
        event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis() + 1000,
                                    MotionEvent.ACTION_UP, x, y, 0);
        rootView.dispatchTouchEvent(event);

        assertEquals(0.0f, _fragment.getX());
        assertEquals(0.0f, _fragment.getY());
    }

    @UiThreadTest
    public void testUpdateValue() {
        TextView logView = (TextView) _activity.findViewById(R.id.motion);
        assertNotNull(logView);

        int count = new Random().nextInt(50);
        for (int i = 0; i < count; i++) {
            final double motion = new Random().nextDouble() * 40.0f;

            _fragment.updateMotion(motion);

            // Check that the logView was updated
            assertEquals(logView.getText(), String.format(_fragment.LOG_FORMAT, motion));
        }
    }

    /**
     * Check that the button is updated correctly when touched or when time's out.
     */
    @MediumTest
    public void testLogPressed() throws InterruptedException {
        Button logButton = (Button) _activity.findViewById(R.id.action_log);
        assertNotNull(logButton);

        TouchUtils.clickView(this, logButton);
        assertEquals(_activity.getResources().getString(R.string.action_stop_logging),
                    logButton.getText());

        TouchUtils.clickView(this, logButton);
        assertEquals(_activity.getResources().getString(R.string.action_start_logging),
                    logButton.getText());

        // Test that the button recovers its state when the log time is out.
        _activity.setMaxLogTime(LOG_TIME);
        TouchUtils.clickView(this, logButton);
        assertEquals(_activity.getResources().getString(R.string.action_stop_logging),
                logButton.getText());

        Thread.sleep(LOG_TIME + 1000);

        assertEquals(_activity.getResources().getString(R.string.action_start_logging),
                    logButton.getText());

    }
}
