package com.alan.audioio.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alan.audioio.R;
import com.alan.audioio.audio.AudioPlayer;
import com.alan.audioio.audio.WavFile;
import com.alan.audioio.audio.common.AudioIOBuilder;
import com.alan.audioio.audio.common.IDataAvailableListener;
import com.alan.audioio.audio.common.Type;
import com.alan.audioio.audio.exception.AudioException;
import com.alan.audioio.utils.ALog;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import androidx.appcompat.app.AppCompatActivity;

public class TestPlayWavActivity extends AppCompatActivity implements View.OnClickListener {

    public static void launchMe(Context context) {
        context.startActivity(new Intent(context, TestPlayWavActivity.class));
    }

    private String mPlayWavPath = "/sdcard/Alan/audio/record_wrapper.wav";
    private AudioPlayer mAudioPlayer;
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
        releaseAudioRes();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnCommonTest) {
            if (!mIsStartTest) {
                mIsStartTest = true;
                mBtnCommonTest.setText("Stop");

                ALog.e("onClick::startTest--------------------->>");
                initAudioPlayer();
                if (mAudioPlayer != null) {
                    mAudioPlayer.start();
                }
            } else {
                mIsStartTest = false;
                mBtnCommonTest.setText("StarTest");
                if (mAudioPlayer != null) {
                    mAudioPlayer.stop();
                }
            }
        }
    }

    private void initAudioPlayer() {
        releaseAudioRes();

        AudioIOBuilder ioBuilder = AudioIOBuilder.builder()
                .setSampleRate(44100)
                .setChannelCount(Type.ChannelCount.Mono)
                .setFormat(Type.AudioFormat.PCM_I16)
                .setBufferSize(1024);

        try {
            File wavFile = new File(mPlayWavPath);
            if (!wavFile.exists()) {
                Toast.makeText(this, "The wav file is not exist.", Toast.LENGTH_SHORT).show();
                return;
            }

            mWavFile = new WavFile(mPlayWavPath);

            mAudioPlayer = new AudioPlayer();
            mAudioPlayer.init(ioBuilder);
            mAudioPlayer.setDataAvailableListener(new IDataAvailableListener() {
                @Override
                public void onDataAvailable(ByteBuffer byteBuffer) {
                    try {
                        mWavFile.read(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.limit());
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

    public void releaseAudioRes() {
        if (mAudioPlayer != null) {
            mAudioPlayer.release();
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
