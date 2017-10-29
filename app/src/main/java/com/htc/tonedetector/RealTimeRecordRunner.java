package com.htc.tonedetector;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

/**
 * Created by HWLee on 29/10/2017.
 */

public class RealTimeRecordRunner implements Runnable {
    private static final String TAG = "RealTimeRecorderRunner";

    private boolean exitPending = false;
    private AudioRecord mRecord;
    private int mSource;
    private int mSampleRate;
    private int mChannels;
    private int mEncoding;
    private int mBufsize;
    private short[] mBuf;
    private RealTimeRecorderRunnerListener mListener = null;

    RealTimeRecordRunner(int source, int sampleRate, int channels, int bufsize) {
        mSource = source;
        mSampleRate = sampleRate;
        mChannels = channels;
        mEncoding = AudioFormat.ENCODING_PCM_16BIT;
        mBufsize = bufsize;

        int minBufsize = AudioRecord.getMinBufferSize(mSampleRate, mChannels, mEncoding);
        if (mBufsize < minBufsize) {
            mBufsize = minBufsize;
            Log.w(TAG, "The specified buffer size is too small, set it as the minimum " + minBufsize);
        }
    }

    @Override
    public void run() {
        init();

        while (!exitPending) {
            long elapsedTimeMillis = System.currentTimeMillis();
            mRecord.read(mBuf, 0, mBufsize);
            elapsedTimeMillis = System.currentTimeMillis() - elapsedTimeMillis;
            Log.d(TAG, "reading data costs " + elapsedTimeMillis + " ms");
            if (mListener != null) {
                mListener.onDataReceived(mBuf);
            }
        }

        deinit();
    }

    private void init() {
        mRecord = new AudioRecord(mSource, mSampleRate, mChannels, mEncoding, mBufsize*2);
        mBuf = new short[mBufsize];
        mRecord.startRecording();
    }

    private void deinit() {
        mRecord.stop();
        mRecord.release();
        mRecord = null;
    }

    public void stop() {
        exitPending = true;
    }

    public void setListener(RealTimeRecorderRunnerListener listener) {
        mListener = listener;
    }

    public interface RealTimeRecorderRunnerListener {
        void onDataReceived(short[] data);
    }
}
