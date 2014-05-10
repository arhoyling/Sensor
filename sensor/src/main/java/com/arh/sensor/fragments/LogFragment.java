package com.arh.sensor.fragments;

import android.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Log fragment used to write logs to files. The writing is asynchronous to liberate the main thread
 * from the heavy work.
 * Created by alex r.hoyling on 03/05/14.
 */
public class LogFragment extends Fragment {
    private static final String             KILL_MSG        = "kill";
    private int                             _sampleCount    = 0;
    private ConcurrentLinkedQueue<String>   _logQueue;

    public void startLogging(String fileName, String vendor) {
        // The ConcurrentLinkedQueue should be a non-blocking queue. It should scale up.
        _logQueue = new ConcurrentLinkedQueue<String>();
        _sampleCount = 0;

        new Thread(new AsyncLogger(_logQueue, fileName)).start();

        // Header
        _logQueue.offer("Vendor," + vendor + "\n\n");
        _logQueue.offer("#,Motion,Screen On,Touched, X, Y, Rate\n");
    }

    public void stopLogging() {
        _logQueue.offer(KILL_MSG);
    }

    /**
     * Log a sample components.
     * @param motion
     * @param isScreenOn
     * @param x
     * @param y
     */
    public void log(double motion, boolean isScreenOn, float x, float y, long period) {
        if (_logQueue == null) return;

        float validPeriod = (period == 0)? 0.0f : 1000.0f/period;

        // Ideally we would move this job to the AsyncLogger.
        String msg = _sampleCount++ + "," +
                motion + "," + isScreenOn + "," +
                (x != 0.0f || y != 0.0f) + "," + x + "," + y + "," +
                validPeriod + "\n";

        _logQueue.offer(msg);
        synchronized (_logQueue) {
            _logQueue.notifyAll();
        }

    }

    /**
     * Runnable performing the actual I/O work. It is asynchronous and writes down whatever appears
     * in its queue until it receives the message KILL.
     */
    private class AsyncLogger implements Runnable {
        private Queue<String>       _queue;
        private String              _fileName;

        public AsyncLogger(Queue<String> queue, String fileName) {
            _queue = queue;
            _fileName = fileName;
        }

        @Override
        public void run() {
            FileOutputStream outputStream;
            File file = new File(getActivity().getExternalFilesDir(null), _fileName);

            try {
                outputStream = new FileOutputStream(file);
                logToStream(outputStream);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * This function does the actual work of polling the queue and dumping the messages in the
         * log file.
         * @param outputStream
         * @throws Exception
         */
        public void logToStream(FileOutputStream outputStream) throws Exception {
            while (true) {
                while (!_queue.isEmpty()) {
                    String msg = _queue.poll();

                    if (msg != null) {
                        // This message indicates that there will be no more messages from that queue.
                        // Ever.
                        if (msg.equals(KILL_MSG)) return;

                        outputStream.write(msg.getBytes());
                    }
                }

                synchronized (_queue) {
                    _queue.wait();
                }
            }
        }
    }
}
