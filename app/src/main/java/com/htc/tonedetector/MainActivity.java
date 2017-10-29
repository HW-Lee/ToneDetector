package com.htc.tonedetector;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "ToneDetector::MainActivity";

    private final static int INPUT_SOURCE = MediaRecorder.AudioSource.MIC;
    private final static int SAMPLINE_RATE = 16000;
    private final static int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

    private Handler mHandler;

    private TextView mConsoleMsgView;
    private DataView mSignalView;
    private DataView mSpectrumView;
    private RealTimeRecordRunner mRunner;
    private int mBufsizeMillis = 100;

    private static String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(FFT.getVersion());

        mHandler = new MsgHandler(this);
        mConsoleMsgView = (TextView) findViewById(R.id.console_msg);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRunner == null) {
                    mRunner = new RealTimeRecordRunner(
                            INPUT_SOURCE, SAMPLINE_RATE, CHANNEL_CONFIG, mBufsizeMillis*SAMPLINE_RATE/1000);

                    mRunner.setListener(new RealTimeRecordRunner.RealTimeRecorderRunnerListener() {
                        @Override
                        public void onDataReceived(short[] data) {
                            ArrayList<Double> dataToPlot = new ArrayList<>(data.length);
                            double[] signal = new double[data.length];
                            for (int i = 0; i < data.length; i++) {
                                double value = (double) data[i] / 32768;
                                dataToPlot.add(value);
                                signal[i] = value;
                            }

                            long elapsedTimeMillis = System.currentTimeMillis();
                            double[] spectrumAbs = FFT.transformAbs(signal);
                            elapsedTimeMillis = System.currentTimeMillis() - elapsedTimeMillis;
                            Log.d(TAG, "native FFT costs " + elapsedTimeMillis + " ms");

                            int N = Math.round(1000f / ((float) SAMPLINE_RATE / spectrumAbs.length));
                            ArrayList<Double> spectrumToPlot = new ArrayList<>(N);
                            double maxValue = -1;
                            int maxIdx = -1;
                            for (int i = 0; i < N; i++) {
                                double v = spectrumAbs[i];
                                if (v > maxValue) {
                                    maxValue = v;
                                    maxIdx = i;
                                }
                                spectrumToPlot.add(v / 10.0);
                            }
                            float maxHz = maxIdx * ((float) SAMPLINE_RATE / spectrumAbs.length);

                            String msg_text = "Detected Tone: " + maxHz + " Hz\nAmplitude: " + spectrumAbs[maxIdx];
                            Message msg = mHandler.obtainMessage();
                            msg.obj = msg_text;
                            mHandler.sendMessage(msg);

                            mSignalView.plot(dataToPlot);
                            mSpectrumView.plot(spectrumToPlot);
                        }
                    });
                    new Thread(mRunner).start();
                } else {
                    mRunner.stop();
                    mRunner = null;
                }
            }
        });

        mSignalView = (DataView) findViewById(R.id.signal_view);
        mSpectrumView = (DataView) findViewById(R.id.spectrum_view);
        mSpectrumView.setGridSlotsX(10);

        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
                break;
            }
        }
    }

    private void setConsoleMsg(String msg) {
        mConsoleMsgView.setText(msg);
    }

    private static class MsgHandler extends Handler {
        private WeakReference<Activity> mActRef;

        MsgHandler(Activity act) {
            mActRef = new WeakReference<>(act);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity act = mActRef.get();
            if (act != null) {
                ((MainActivity) act).setConsoleMsg((String) msg.obj);
            }
        }
    }
}
