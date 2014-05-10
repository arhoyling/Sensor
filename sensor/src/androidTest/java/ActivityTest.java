import android.test.ActivityInstrumentationTestCase2;

import com.arh.sensor.activities.BaseActivity;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for all fragment and activity tests. It checks the app's directory to
 * restore its state at the end of a test.
 * Created by alex r.hoyling on 03/05/14.
 */
public class ActivityTest extends ActivityInstrumentationTestCase2<BaseActivity> {
    protected BaseActivity _activity;
    private List<File> _fileList = null;

    public ActivityTest() {
        super(BaseActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _activity = getActivity();
        File dir = _activity.getExternalFilesDir(null);
        if (dir != null)
            _fileList = Arrays.asList(dir.listFiles());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        File dir = _activity.getExternalFilesDir(null);
        if (dir == null) return;

        File[] newList = dir.listFiles();
        for (File file : newList) {
            if (!_fileList.contains(file)) {
                file.delete();
            }
        }
    }
}
