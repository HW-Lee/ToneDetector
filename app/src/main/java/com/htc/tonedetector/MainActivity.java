package com.htc.tonedetector;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DataView mSignalView;
    private DataView mSpectrumView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(FFT.getVersion());

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Double> data = new ArrayList<>(1024);
                for (int i = 0; i < 1024; i++) {
                    data.add(i, Math.sin(Math.PI*2 / 44100 * i * 440));
                }
                mSignalView.plot(data);

                for (int i = 0; i < data.size(); i++) {
                    data.set(i, Math.cos(Math.PI*2 / 44100 * i * 1000));
                }
                mSpectrumView.plot(data);
            }
        });

        mSignalView = (DataView) findViewById(R.id.signal_view);
        mSpectrumView = (DataView) findViewById(R.id.spectrum_view);
    }
}
