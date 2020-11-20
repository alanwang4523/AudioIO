/*
 * Copyright (c) 2019-present AlanWang4523 <alanwang4523@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alan.audioio.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.alan.audioio.R;
import com.alan.audioio.app.ui.PianoKeyItemView;
import com.alan.audioio.audio.AndroidSoundPool;
import com.alan.audioio.audio.common.APPContext;
import com.alan.audioio.audio.exception.AudioException;
import com.alan.audioio.utils.ALog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: AlanWang4523.
 * Date: 2020/10/17 18:38.
 * Mail: alanwang4523@gmail.com
 */
public class TestSoundPoolActivity  extends AppCompatActivity implements View.OnClickListener {

    public static void launchMe(Context context) {
        context.startActivity(new Intent(context, TestSoundPoolActivity.class));
    }

    private static final String MUSIC_PIANO_DIR = "assets://piano/";
    private static final String[] KEY_NAMES = {"A", "B", "C", "D", "E",};
    private static final String FILE_SUFFIX = ".m4a";

    private static final int PIANO_KEYS_COUNT = 5;
    private static final int MAX_SOUND_COUNT = 5;
    private int[] btnPianoKeysIdArr;// 按钮id
    private PianoKeyItemView[] pianoKeyItemViewArr;
    private HashMap<Integer, Integer> btnIdIndexMap = new HashMap<>(PIANO_KEYS_COUNT);
    private HashMap<Integer, Integer> btnIdAndSoundIdMap = new HashMap<>(PIANO_KEYS_COUNT);
    private TextView btnStopPlay;
    private ProgressDialog mProgressDialog;
    private AndroidSoundPool mAndroidSoundPool;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_pool);

        btnStopPlay = findViewById(R.id.btn_stop);
        btnStopPlay.setOnClickListener(this);

        APPContext.getInstance().setContext(this);

        btnPianoKeysIdArr = new int[PIANO_KEYS_COUNT];
        btnPianoKeysIdArr[0] = R.id.btn_key_A;
        btnPianoKeysIdArr[1] = R.id.btn_key_B;
        btnPianoKeysIdArr[2] = R.id.btn_key_C;
        btnPianoKeysIdArr[3] = R.id.btn_key_D;
        btnPianoKeysIdArr[4] = R.id.btn_key_E;

        pianoKeyItemViewArr = new PianoKeyItemView[PIANO_KEYS_COUNT];
        for (int i = 0; i < pianoKeyItemViewArr.length; i++) {
            btnIdIndexMap.put(btnPianoKeysIdArr[i], i);
            pianoKeyItemViewArr[i] = findViewById(btnPianoKeysIdArr[i]);
            pianoKeyItemViewArr[i].setOnClickListener(this);
        }

        loadMusicInstrument();
    }

    private void loadMusicInstrument() {
        ALog.e("loadMusicInstrument--------------->>");

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("正在加载乐器...");
        }
        mProgressDialog.show();

        if (mAndroidSoundPool != null) {
            mAndroidSoundPool.release();
        }
        mAndroidSoundPool = new AndroidSoundPool(MAX_SOUND_COUNT);

        final ArrayList<String> audioFileList = new ArrayList<>();
        for (int i = 0; i < pianoKeyItemViewArr.length; i++) {
            audioFileList.add(getAudioPath(i));
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Integer> soundIdList = mAndroidSoundPool.load(audioFileList);
                    for (int i = 0; i < soundIdList.size(); i++) {
                        ALog.e("loadAudioAsync:: i = " + i + ", soundId = " + soundIdList.get(i)
                                + ", keyPath = " + getAudioPath(i));
                        btnIdAndSoundIdMap.put(btnPianoKeysIdArr[i], soundIdList.get(i));
                    }
                    TestSoundPoolActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                        }
                    });
                } catch (AudioException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String getAudioPath(int i) {
        return MUSIC_PIANO_DIR + KEY_NAMES[i] + FILE_SUFFIX;
    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        if (mAndroidSoundPool != null) {
            mAndroidSoundPool.stopPlay();
            mAndroidSoundPool.release();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_stop) {
            if (mAndroidSoundPool != null) {
                mAndroidSoundPool.stopPlay();
            }
        } else {
            int index = btnIdIndexMap.get(view.getId());
            if (index >= 0) {
                PianoKeyItemView keyItemView = pianoKeyItemViewArr[index];
                int soundId = btnIdAndSoundIdMap.get(view.getId());
                ALog.e("PlayPiano--->> " + keyItemView.getKeyName() + ", soundId = " + soundId);
                if (mAndroidSoundPool != null) {
                    mAndroidSoundPool.play(soundId);
                }
            }
        }
    }
}

