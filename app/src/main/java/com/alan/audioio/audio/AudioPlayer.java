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

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import com.alan.audioio.audio.common.AudioIOBuilder;
import com.alan.audioio.audio.common.IDataAvailableListener;
import com.alan.audioio.audio.common.IOStatus;
import com.alan.audioio.audio.common.Type;
import com.alan.audioio.audio.exception.AudioException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: AlanWang4523.
 * Date: 2019-06-14 23:04.
 * Mail: alanwang4523@gmail.com
 */
public class AudioPlayer {
    private final static String TAG = AudioPlayer.class.getSimpleName();
    private IDataAvailableListener mDataAvailableListener;
    private volatile @IOStatus
    int mNewStatus;
    private volatile @IOStatus
    int mCurStatus;
    private boolean mIsStatusChanged = false;
    private Thread mWorkThread;
    private AudioTrack mAudioTrack;
    private int mChannelCount;
    private ByteBuffer mDataBuffer;
    private final ReentrantLock mLock = new ReentrantLock();
    private final Condition mStatusCondition = mLock.newCondition();
    private final Condition mPlayStateCondition = mLock.newCondition();

    /**
     * 构造函数
     */
    public AudioPlayer() {
        mNewStatus = IOStatus.UNINITIATED;
        mCurStatus = IOStatus.UNINITIATED;
    }

    /**
     * 设置 IDataAvailableListener
     * @param dataAvailableListener 用于获取要播放的数据
     */
    public void setDataAvailableListener(IDataAvailableListener dataAvailableListener) {
        this.mDataAvailableListener = dataAvailableListener;
    }

    /**
     * 初始化播放器
     * @param ioBuilder ioBuilder
     * @throws AudioException AudioException
     */
    public void init(AudioIOBuilder ioBuilder) throws AudioException {
        try {
            if (ioBuilder.getBufferSize() <= 0) {
                throw new AudioException("The buffer size must be greater than 0!", null);
            }
            int sampleRateInHz = ioBuilder.getSampleRate();
            int channelConfig = ioBuilder.getChannelCount() == Type.ChannelCount.Stereo ?
                    AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO;
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT; // 默认采样 short 型格式
            if (ioBuilder.getFormat() == Type.AudioFormat.PCM_Float &&
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                throw new AudioException("The current os version is not support pcm float format!", null);
            }
            if (ioBuilder.getFormat() == Type.AudioFormat.PCM_Float) {
                audioFormat = AudioFormat.ENCODING_PCM_FLOAT;
            }

            int minBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    sampleRateInHz, channelConfig, audioFormat, minBufferSize, AudioTrack.MODE_STREAM);

            mChannelCount = ioBuilder.getChannelCount();
            mDataBuffer = ByteBuffer.allocateDirect(ioBuilder.getBufferSize());
            mNewStatus = IOStatus.INITIATED;
            mCurStatus = IOStatus.INITIATED;
        } catch (Exception e) {
            throw new AudioException("Init AudioPlayer Failed!", e);
        }
    }

    /**
     * 开始播放，只能在初始化成功后调用
     */
    public void start() {
        if (mNewStatus == IOStatus.INITIATED) {
            mWorkThread = new Thread(null, new WorkRunnable(),
                    TAG + "-" + System.currentTimeMillis());
            mNewStatus = IOStatus.START;
            mIsStatusChanged = true;

            mWorkThread.start();

            mLock.lock();
            try {
                while (mIsStatusChanged) {
                    try {
                        mStatusCondition.await(1000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            } finally {
                mLock.unlock();
            }
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mNewStatus == IOStatus.PAUSE || mNewStatus == IOStatus.STOP) {
            return;
        }
        if (mNewStatus != IOStatus.START && mNewStatus != IOStatus.RESUME) {
            return;
        }
        mLock.lock();
        try {
            mNewStatus = IOStatus.PAUSE;
            mIsStatusChanged = true;

            while (mIsStatusChanged) {
                try {
                    mStatusCondition.await(1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        } finally {
            mLock.unlock();
        }
        mAudioTrack.pause();
    }

    /**
     * 恢复播放
     */
    public void resume() {
        if (mNewStatus != IOStatus.PAUSE) {
            return;
        }
        mLock.lock();
        try {
            mAudioTrack.play();
            mNewStatus = IOStatus.RESUME;
            mIsStatusChanged = true;
            mPlayStateCondition.signal();

            while (mIsStatusChanged) {
                try {
                    mStatusCondition.await(1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        } finally {
            mLock.unlock();
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (mNewStatus == IOStatus.STOP || mNewStatus == IOStatus.UNINITIATED
                || mNewStatus == IOStatus.INITIATED) {
            return;
        }
        mLock.lock();
        try {
            mNewStatus = IOStatus.STOP;
            mIsStatusChanged = true;
            // 需要调用 notify，避免在 pause 状态调用 stop 时，work thread 还在 wait
            mPlayStateCondition.signal();

            while (mIsStatusChanged) {
                try {
                    mStatusCondition.await(1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        } finally {
            mLock.unlock();
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        mLock.lock();
        try {
            // 如果初始化后还没开始播放则释放资源，否则统一在 WorkRunnable 中释放
            if (mNewStatus == IOStatus.INITIATED) {
                mAudioTrack.release();
            } else {
                mNewStatus = IOStatus.STOP;
                mIsStatusChanged = true;
                // 需要调用 notify，避免在 pause 状态调用 release 时，work thread 还在 wait
                mPlayStateCondition.signal();
            }
        } finally {
            mLock.unlock();
        }

        // 等待工作线程结束
        if (mWorkThread != null) {
            try {
                mWorkThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class WorkRunnable implements Runnable {

        @Override
        public void run() {
            mAudioTrack.play();
            while (true) {
                boolean isNeedFade = false;
                mLock.lock();
                if (mIsStatusChanged) {
                    mCurStatus = mNewStatus;
                    isNeedFade = true;
                }
                mLock.unlock();

                mDataBuffer.clear();
                if (mDataAvailableListener != null) {
                    // 外层将需要播放的数据放入 mDataBuffer
                    mDataAvailableListener.onDataAvailable(mDataBuffer);
                }

                ByteBuffer byteBuffer = mDataBuffer;
                if (byteBuffer == null || byteBuffer.limit() <= 0) {
                    if (isNeedFade) {
                        mLock.lock();
                        mIsStatusChanged = false;
                        mStatusCondition.signal();
                        mLock.unlock();
                    }
                    continue;
                }
                byteBuffer.rewind();

                // 如果状态发生改变，对播放数据做 Fade
                if (isNeedFade) {
                    if (mCurStatus == IOStatus.PAUSE || mCurStatus == IOStatus.STOP) {
                        shortFadeOut(byteBuffer, mChannelCount);
                    } else {
                        shortFadeIn(byteBuffer, mChannelCount);
                    }
                }

                mAudioTrack.write(byteBuffer.array(), byteBuffer.arrayOffset(), byteBuffer.limit());

                mLock.lock();
                try {
                    if (isNeedFade) {
                        mIsStatusChanged = false;
                        mStatusCondition.signal();
                    }
                    while ((mCurStatus == IOStatus.PAUSE) && !mIsStatusChanged) {
                        try {
                            mPlayStateCondition.await();
                        } catch (InterruptedException e) {
                            // do nothing
                        }
                    }
                    if (mCurStatus == IOStatus.STOP || mCurStatus == IOStatus.UNINITIATED) {
                        break;
                    }
                } finally {
                    mLock.unlock();
                }
            }
            try {
                mAudioTrack.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            try {
                mAudioTrack.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 对音频数据做 fade out
     * @param byteBuffer byteBuffer
     * @param channelCount channelCount
     */
    private void shortFadeOut(ByteBuffer byteBuffer, int channelCount) {
        int shortCount = byteBuffer.limit() / 2;
        if(1 == channelCount) {
            for(int i = 0; i < shortCount; i++) {
                short data = (short) (byteBuffer.getShort(i * 2) * 1.0f * (shortCount - i) / shortCount);
                byteBuffer.putShort(i * 2, data);
            }
        } else {
            for(int i = 0; i < shortCount; i += 2) {
                short data = (short) (byteBuffer.getShort(i * 2) * 1.0f * (shortCount - i) / shortCount);
                byteBuffer.putShort(i * 2, data);

                data = (short)(byteBuffer.getShort((i + 1) * 2) * 1.0f * (shortCount - i) / shortCount);
                byteBuffer.putShort((i + 1) * 2, data);
            }
        }
        byteBuffer.rewind();
    }

    /**
     * 对音频数据做 fade in
     * @param byteBuffer byteBuffer
     * @param channelCount channelCount
     */
    private void shortFadeIn(ByteBuffer byteBuffer, int channelCount) {
        int shortCount = byteBuffer.limit() / 2;
        if(1 == channelCount) {
            for(int i = 0; i < shortCount; i++) {
                short data = (short)(byteBuffer.getShort(i * 2) * 1.0f * i / shortCount);
                byteBuffer.putShort(i, data);
            }
        } else {
            for(int i = 0; i < shortCount; i += 2) {
                short data = (short)(byteBuffer.getShort(i * 2) * 1.0f * i / shortCount);
                byteBuffer.putShort(i * 2, data);

                data = (short)(byteBuffer.getShort((i + 1) * 2) * 1.0f * i / shortCount);
                byteBuffer.putShort((i + 1) * 2, data);
            }
        }
        byteBuffer.rewind();
    }
}
