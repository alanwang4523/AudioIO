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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alan.audioio.R;
import com.alan.audioio.app.ui.PianoKeyItemView;
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
    private static final String MUSIC_GUITAR_DIR = "assets://guitar/";
    private static final String[] CHORD_NAME = {"A", "B", "C", "D", "E",};
    private static final String FILE_SUFFIX = ".m4a";

    private String mCurMusicDir = MUSIC_PIANO_DIR;

    private static final int CHORDS_COUNT = 5;
    private static final int MAX_STREAM_COUNT = 5;
    private int[] btnChordsIdArr;// 按钮id
    private PianoKeyItemView[] chordItemViewArr;
    private HashMap<Integer, Integer> btnIdIndexMap = new HashMap<>(CHORDS_COUNT);
    private HashMap<Integer, Integer> btnIdAndStreamIdMap = new HashMap<>(CHORDS_COUNT);
    private LinearLayout llChordContainer;
    private TextView btnStartSing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_pool);

        llChordContainer = findViewById(R.id.ll_chord_container);

        btnStartSing = findViewById(R.id.btn_stop);
        btnStartSing.setOnClickListener(this);

        btnChordsIdArr = new int[CHORDS_COUNT];
        btnChordsIdArr[0] = R.id.btn_chord_A;
        btnChordsIdArr[1] = R.id.btn_chord_B;
        btnChordsIdArr[2] = R.id.btn_chord_C;
        btnChordsIdArr[3] = R.id.btn_chord_D;
        btnChordsIdArr[4] = R.id.btn_chord_E;

        chordItemViewArr = new PianoKeyItemView[CHORDS_COUNT];
        for (int i = 0; i < chordItemViewArr.length; i++) {
            btnIdIndexMap.put(btnChordsIdArr[i], i);
            chordItemViewArr[i] = findViewById(btnChordsIdArr[i]);
            chordItemViewArr[i].setOnClickListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_stop) {

        } else {
            int index = btnIdIndexMap.get(view.getId());
            if (index >= 0) {
                PianoKeyItemView chordItemView = chordItemViewArr[index];
                int streamId = btnIdAndStreamIdMap.get(view.getId());
                ALog.e("onClick::Chord--------------->> " + chordItemView.getChordName()
                        + ", streamId = " + streamId + ", currentTimeMillis = " + System.currentTimeMillis());
            }
        }
    }
}

