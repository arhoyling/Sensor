import android.app.Fragment;
import android.view.ViewGroup;

import com.arh.sensor.R;

/**
 * Created by alex r.hoyling on 03/05/14.
 */
public class BaseActivityTest extends ActivityTest {
    private Fragment     _fragment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        _fragment = _activity.getFragmentManager().findFragmentById(R.id.fragment_stats);
    }

    /**+
     * Test that the base activity layout contains the display fragment.
     */
    public void testDisplayLayoutExists() {
        assertNotNull(_fragment);
    }

    /**
     * Test that the base activity layout contains only a single element.
     */
    public void testLayout() {
        ViewGroup rootView = (ViewGroup) _activity.findViewById(android.R.id.content);
        assertEquals(rootView.getChildCount(), 1);
    }
}
