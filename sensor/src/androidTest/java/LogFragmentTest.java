import android.test.suitebuilder.annotation.MediumTest;

import com.arh.sensor.fragments.LogFragment;
import com.arh.sensor.activities.BaseActivity;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

/**
 * Test main functionalities of the log fragment.
 * Created by alex r.hoyling on 03/05/14.
 */
public class LogFragmentTest extends ActivityTest {
    private static final String FILENAME = "testFile.csv";
    private static final String VENDOR   = "vendor";
    private LogFragment         _fragment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _fragment = (LogFragment)
                    _activity.getFragmentManager().findFragmentByTag(BaseActivity.LOGFRAGMENT_TAG);
    }

    /**
     * Make sure this fragment is view less
     */
    public void testViewLess() {
        assertNull(_fragment.getView());
    }

    /**
     * Test that the log fragment captures logs correctly (even if asynchronous);
     */
    @MediumTest
    public void testLoggingToFile() throws Exception {
        _fragment.startLogging(FILENAME, VENDOR);

        String proof = "Vendor," + VENDOR + "\n\n";
        proof += "#,Motion,Screen On,Touched, X, Y, Rate\n";

        int count = 0;
        Random rnd = new Random();
        int entryCount = rnd.nextInt(25);
        for (int i = 0; i < entryCount; i++) {
            double motion = rnd.nextDouble();
            boolean isScreenOn = true;
            float x = rnd.nextFloat();
            float y = rnd.nextFloat();
            int period = rnd.nextInt(20);


            proof += count++ + "," + motion + "," + isScreenOn + "," +
                    (x != 0.0f || y != 0.0f) + "," + x + "," + y + "," +
                    ((period == 0)? 0.0f : 1000.0f / period) + "\n";

            _fragment.log(motion, isScreenOn, x, y, period);
        }

        _fragment.stopLogging();

        // Start another logging process to make sure it does not corrupt the previous process.
        _fragment.startLogging(FILENAME + "2", VENDOR);
        _fragment.log(0.0f, false, 0.0f, 0.0f, 0);
        _fragment.stopLogging();

        // Check that our output file has the right content.
        File file = new File(getActivity().getExternalFilesDir(null), FILENAME);

        InputStream     proofStream = new ByteArrayInputStream(proof.getBytes());
        BufferedReader  proofBuffer = new BufferedReader(new InputStreamReader(proofStream));

        InputStream     checkStream = new FileInputStream(file);
        BufferedReader  checkBuffer = new BufferedReader(new InputStreamReader(checkStream));

        String checkLine, proofLine;
        // It is easier to debug line by line.
        while ((checkLine = checkBuffer.readLine()) != null) {
            assertNotNull(proofLine = proofBuffer.readLine());
            assertEquals(proofLine, checkLine);
        }

        proofStream.close();
        proofBuffer.close();
        checkStream.close();
        checkStream.close();
    }
}
