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
package com.alan.audioio.audio;

import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.alan.audioio.audio.common.APPContext;
import com.alan.audioio.audio.common.AudioConstants;
import com.alan.audioio.audio.exception.AudioException;
import com.alan.audioio.utils.ALog;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Author: AlanWang4523.
 * Date: 2020/10/17 14:57.
 * Mail: alanwang4523@gmail.com
 */
public class AndroidSoundPool {
    private static final String TAG = AndroidSoundPool.class.getSimpleName();
    private static final int MSG_FADE_OUT = 1001;
    private static final int FADE_DURATION = 30;
    private static final int FADE_INTERVAL_TIME = 6;
    private static final float FADE_INTERVAL_VOLUME = (1.0f / (1.0f * FADE_DURATION / FADE_INTERVAL_TIME));

    private SoundPool mSoundPool;
    private int mMaxStreamCount;
    private ArrayList<Integer> mSoundIdList;
    private ArrayList<Integer> mPlayingIdList;
    private CountDownLatch mCountDownLatch;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private float mCurPlayVolume = 1.0f;

    /**
     * 构造函数
     */
    public AndroidSoundPool(int maxStreamCount) {
        mMaxStreamCount = maxStreamCount;
        mSoundIdList = new ArrayList<>();
        mPlayingIdList = new ArrayList<>();

        mSoundPool = createSoundPool(mMaxStreamCount);
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                ALog.d("onLoadComplete()--->>sampleId = " + sampleId + ", status = " + status);
                if (mCountDownLatch != null) {
                    mCountDownLatch.countDown();
                }
            }
        });

        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new InternalHandler(mHandlerThread.getLooper(), this);
    }

    /**
     * 创建 SoundPool
     * @param maxStream 同时播放的最大流数量
     * @return SoundPool
     */
    private SoundPool createSoundPool(int maxStream) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(maxStream);
            AudioAttributes.Builder attributesBuilder = new AudioAttributes.Builder();
            attributesBuilder.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC);
            attributesBuilder.setFlags(256);
            attributesBuilder.setUsage(AudioAttributes.USAGE_MEDIA);
            attributesBuilder.setLegacyStreamType(3);
            builder.setAudioAttributes(attributesBuilder.build());
            return builder.build();
        } else {
            return new SoundPool(maxStream, AudioManager.STREAM_MUSIC, 0);
        }
    }

    /**
     * 加载资源列表
     * @param audioPathList 要加载的音频资源列表
     * @return soundIDList
     * @throws AudioException 加载失败会抛出 AudioException
     */
    public List<Integer> load(List<String> audioPathList) throws AudioException {
        mSoundIdList.clear();
        ArrayList<Integer> soundIDList = new ArrayList<>();
        if (audioPathList == null) {
            return soundIDList;
        }

        mCountDownLatch = new CountDownLatch(audioPathList.size());
        for (String audioPath : audioPathList) {
            int soundID = this.load(audioPath);
            soundIDList.add(soundID);
        }
        try {
            mCountDownLatch.await(audioPathList.size() * 2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // do nothing
        }
        return soundIDList;
    }

    /**
     * 加载资源文件
     * @param audioPath 音频资源路径，支持协议如下：
     *        assets://piano/A.m4a
     *        exfile:///sdcard/Alan/Audio/piano/A.m4a
     *        /sdcard/Alan/Audio/piano/A.m4a
     *
     * @return soundID，可以用于播放或 unload
     * @throws AudioException 加载失败抛出 AudioException
     */
    private int load(String audioPath) throws AudioException {
        int soundID;
        String realPath;
        if (AudioConstants.isAssetsPath(audioPath)) {
            // assets 文件
            realPath = audioPath.replace(AudioConstants.HOST_ASSETS, "");
            try {
                AssetFileDescriptor assetFileDescriptor = APPContext.getAssetManager().openFd(realPath);
                soundID = mSoundPool.load(assetFileDescriptor, 0);
            } catch (IOException e) {
                throw new AudioException("Load asset file failed.", e);
            }
        } else if (AudioConstants.isExFilePath(audioPath)) {
            // 外部存储文件
            realPath = audioPath.replace(AudioConstants.HOST_EXFILE, "");
            soundID = mSoundPool.load(realPath, 0);
        } else {
            // 其他绝对路径不带前缀的文件
            realPath = audioPath;
            soundID = mSoundPool.load(realPath, 0);
        }
        mSoundIdList.add(soundID);
        return soundID;
    }

    /**
     * 播放某个资源
     * @param soundID soundID，由 {@link #load(String)} 返回
     */
    public void play(int soundID) {
        mCurPlayVolume = 1.0f;
        int playingId = mSoundPool.play(soundID,
                1.0f, 1.0f, 0, 0, 1.0f);
        synchronized (AndroidSoundPool.this) {
            if ((playingId != 0) && !mPlayingIdList.contains(playingId)) {
                mPlayingIdList.add(playingId);
            }
            if (mPlayingIdList.size() > mMaxStreamCount) {
                mPlayingIdList.remove(0);
            }
        }
    }

    /**
     * 停止播放，停止时会做 fade out
     */
    public void stopPlay() {
        mHandler.removeMessages(MSG_FADE_OUT);
        mHandler.sendEmptyMessage(MSG_FADE_OUT);
        try {
            Thread.sleep(FADE_DURATION + FADE_INTERVAL_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 卸载某个资源
     * @param soundID soundID，由 {@link #load(String)} 返回
     */
    public void unload(int soundID) {
        mSoundPool.unload(soundID);
        int idIndex = -1;
        for (int i = 0; i < mSoundIdList.size(); i++) {
            if (soundID == mSoundIdList.get(i)) {
                idIndex = i;
            }
        }
        if (idIndex >= 0) {
            mSoundIdList.size();
            mSoundIdList.remove(idIndex);
        }
    }

    /**
     * 卸载所有资源
     */
    public void unloadAll() {
        for (Integer soundID : mSoundIdList) {
            mSoundPool.unload(soundID);
        }
        mSoundIdList.clear();
    }

    /**
     * 释放资源
     */
    public void release() {
        unloadAll();
        mSoundPool.release();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mHandlerThread.quitSafely();
        } else {
            mHandlerThread.quit();
        }
    }

    private void handleFadeOut() {
        mCurPlayVolume -= FADE_INTERVAL_VOLUME;
        setVolume(mCurPlayVolume);
        if (mCurPlayVolume > 0) {
            mHandler.sendEmptyMessageDelayed(MSG_FADE_OUT, FADE_INTERVAL_TIME);
        } else {
            synchronized (AndroidSoundPool.this) {
                for (Integer playingId : mPlayingIdList) {
                    mSoundPool.stop(playingId);
                }
                mPlayingIdList.clear();
            }
        }
    }

    private void setVolume(float volume) {
        ALog.d("setVolume()----->>>" + volume + ", PlayingIdList = " + mPlayingIdList.toString());
        if (volume > 1.0f) {
            volume = 1.0f;
        } else if (volume < 0.01f) {
            volume = 0.0f;
        }
        synchronized (AndroidSoundPool.this) {
            try {
                for (Integer playingId : mPlayingIdList) {
                    mSoundPool.setVolume(playingId, volume, volume);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class InternalHandler extends Handler {
        private WeakReference<AndroidSoundPool> weakRefSoundPool;
        public InternalHandler(Looper looper, AndroidSoundPool androidSoundPool) {
            super(looper);
            weakRefSoundPool = new WeakReference<>(androidSoundPool);
        }

        @Override
        public void handleMessage(Message msg) {
            AndroidSoundPool androidSoundPool = weakRefSoundPool.get();
            if (androidSoundPool == null) {
                return;
            }
            if (msg.what == MSG_FADE_OUT) {
                androidSoundPool.handleFadeOut();
            }
        }
    }
}
