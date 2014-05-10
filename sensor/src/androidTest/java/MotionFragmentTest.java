import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.arh.sensor.activities.BaseActivity;
import com.arh.sensor.fragments.MotionFragment;

/**
 * Created by alex r.hoyling on 03/05/14.
 */
public class MotionFragmentTest extends ActivityTest {
    private MotionFragment      _fragment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _fragment = (MotionFragment)
                _activity.getFragmentManager().findFragmentByTag(BaseActivity.MOTIONFRAGMENT_TAG);
    }

    /**
     * Make sure this fragment is view less
     */
    public void testViewLess() {
        assertNull(_fragment.getView());
    }

    public void testVendor() {
        SensorManager manager = (SensorManager) _activity.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        assertEquals(sensor.getVendor(), _fragment.getVendor());
    }
}
