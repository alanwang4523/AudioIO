package com.alan.audioio.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.alan.audioio.R;
import com.alan.audioio.audio.AudioRecorder;
import com.alan.audioio.audio.WavFile;
import com.alan.audioio.audio.common.AudioIOBuilder;
import com.alan.audioio.audio.common.IDataAvailableListener;
import com.alan.audioio.audio.common.Type;
import com.alan.audioio.audio.exception.AudioException;
import com.alan.audioio.utils.ALog;
import java.io.IOException;
import java.nio.ByteBuffer;
import androidx.appcompat.app.AppCompatActivity;

public class TestRecordToWavActivity extends AppCompatActivity implements View.OnClickListener {

    public static void launchMe(Context context) {
        context.startActivity(new Intent(context, TestRecordToWavActivity.class));
    }

    private String mVocalSavePath = "/sdcard/Alan/audio/record_wrapper.wav";
    private AudioRecorder mAudioRecorder;
    private WavFile mWavFile;
    private TextView mBtnCommonTest;
    private boolean mIsStartTest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_test);

        mBtnCommonTest = findViewById(R.id.btnCommonTest);
        mBtnCommonTest.setOnClickListener(this);
        mBtnCommonTest.setText("StarTest");

    }

    @Override
    protected void onDestroy() {
        if (mIsStartTest) {
            mIsStartTest = false;
        }
        releaseRecorder();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnCommonTest) {
            if (!mIsStartTest) {
                mIsStartTest = true;
                mBtnCommonTest.setText("Stop");

                ALog.e("onClick::startTest--------------------->>");
                initRecorder();
                mAudioRecorder.start();

            } else {
                mIsStartTest = false;
                mBtnCommonTest.setText("StarTest");
                mAudioRecorder.stop();

            }
        }
    }

    private void initRecorder() {
        AudioIOBuilder ioBuilder = AudioIOBuilder.builder()
                .setSampleRate(44100)
                .setChannelCount(Type.ChannelCount.Mono)
                .setFormat(Type.AudioFormat.PCM_I16)
                .setBufferSize(1024);

        WavFile.HeadInfo headInfo = WavFile.HeadInfo.build()
                .setSampleRate(ioBuilder.getSampleRate())
                .setChannelCount(ioBuilder.getChannelCount())
                .setBytePerSample(2);

        try {
            mWavFile = new WavFile(mVocalSavePath, headInfo);

            mAudioRecorder = new AudioRecorder();

            mAudioRecorder.init(ioBuilder);
            mAudioRecorder.setDataAvailableListener(new IDataAvailableListener() {
                @Override
                public void onDataAvailable(ByteBuffer byteBuffer) {
                    try {
                        mWavFile.write(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.limit());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (AudioException | IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "init failed.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void releaseRecorder() {
        if (mAudioRecorder != null) {
            mAudioRecorder.release();
        }
        if (mWavFile != null) {
            try {
                mWavFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
