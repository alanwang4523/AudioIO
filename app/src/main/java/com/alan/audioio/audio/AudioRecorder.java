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
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import com.alan.audioio.audio.common.AudioIOBuilder;
import com.alan.audioio.audio.common.IDataAvailableListener;
import com.alan.audioio.audio.common.IOStatus;
import com.alan.audioio.audio.common.Type;
import com.alan.audioio.audio.exception.AudioException;
import java.nio.ByteBuffer;

/**
 * Author: AlanWang4523.
 * Date: 2019-06-14 22:59.
 * Mail: alanwang4523@gmail.com
 */
public class AudioRecorder {
    private final static String TAG = AudioRecorder.class.getSimpleName();
    private IDataAvailableListener mDataAvailableListener;
    private volatile @IOStatus int mStatus;
    private final Object mLock = new Object();
    private Thread mWorkThread;
    private AudioRecord mAudioRecord;
    private ByteBuffer mDataBuffer;
    private int mBufferSizePerFrame;

    public AudioRecorder() {
        mStatus = IOStatus.UNINITIATED;
    }

    /**
     * 设置 IDataAvailableListener
     * @param dataAvailableListener 用于处理采集到的音频数据
     */
    public void setDataAvailableListener(IDataAvailableListener dataAvailableListener) {
        this.mDataAvailableListener = dataAvailableListener;
    }

    /**
     * 初始化
     * @param ioBuilder AudioIOBuilder
     * @throws AudioException AudioException
     */
    public void init(AudioIOBuilder ioBuilder) throws AudioException {
        try {
            if (ioBuilder.getBufferSize() <= 0) {
                throw new AudioException("The buffer size must be greater than 0!", null);
            }
            int sampleRateInHz = ioBuilder.getSampleRate();
            int channelConfig = ioBuilder.getChannelCount() == Type.ChannelCount.Stereo ?
                    AudioFormat.CHANNEL_IN_STEREO : AudioFormat.CHANNEL_IN_MONO;

            int audioFormat = AudioFormat.ENCODING_PCM_16BIT; // 默认采样 short 型格式
            if (ioBuilder.getFormat() == Type.AudioFormat.PCM_Float &&
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                throw new AudioException("The current os version is not support pcm float format!", null);
            }
            if (ioBuilder.getFormat() == Type.AudioFormat.PCM_Float) {
                audioFormat = AudioFormat.ENCODING_PCM_FLOAT;
            }

            int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRateInHz, channelConfig, audioFormat, minBufferSize);

            mBufferSizePerFrame = ioBuilder.getBufferSize();
            mDataBuffer = ByteBuffer.allocateDirect(mBufferSizePerFrame);
            mStatus = IOStatus.INITIATED;
        } catch (Exception e) {
            throw new AudioException("Init AudioRecorder Failed!", e);
        }
    }

    /**
     * 开始录音
     */
    public void start() {
        synchronized (mLock) {
            if (mStatus == IOStatus.INITIATED) {
                mWorkThread = new Thread(null, new WorkRunnable(),
                        TAG + "-" + System.currentTimeMillis());
                mStatus = IOStatus.START;
                mWorkThread.start();
            } else if (mStatus == IOStatus.PAUSE) {
                mAudioRecord.startRecording();
                mStatus = IOStatus.RESUME;
                mLock.notify();
            } else if (mStatus == IOStatus.RESUME) {
                return;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * 暂停录音
     */
    public void pause() {
        synchronized (mLock) {
            if (mStatus == IOStatus.START || mStatus == IOStatus.RESUME) {
                mAudioRecord.stop();
                mStatus = IOStatus.PAUSE;
            }
        }
    }

    /**
     * 恢复录音
     */
    public void resume() {
        synchronized (mLock) {
            if (mStatus == IOStatus.PAUSE) {
                mAudioRecord.startRecording();
                mStatus = IOStatus.RESUME;
                mLock.notify();
            }
        }
    }

    /**
     * 停止录音
     */
    public void stop() {
        synchronized (mLock) {
            if (mStatus == IOStatus.START || mStatus == IOStatus.RESUME) {
                mStatus = IOStatus.STOP;
                // 需要调用 notify，避免在 pause 状态调用 stop 时，work thread 还在 wait
                mLock.notify();
            }
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        synchronized (mLock) {
            // 如果初始化后还没开始录制则释放资源
            if (mStatus == IOStatus.INITIATED) {
                mAudioRecord.release();
            } else {
                mStatus = IOStatus.STOP;
                // 需要调用 notify，避免在 pause 状态调用 release 时，work thread 还在 wait
                mLock.notify();
            }
        }
        // 等待工作线程结束
        if (mWorkThread != null) {
            try {
                mWorkThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.mDataAvailableListener = null;
    }

    private class WorkRunnable implements Runnable {

        @Override
        public void run() {
            int readLen;
            int totalReadLen;
            int needReadLen;
            boolean isFirstFrame = true;
            mAudioRecord.startRecording();
            while (true) {
                // 状态处理
                synchronized (mLock) {
                    while (mStatus == IOStatus.PAUSE) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException e) {
                            // do nothing
                        }
                    }
                    if (mStatus == IOStatus.STOP || mStatus == IOStatus.UNINITIATED) {
                        break;
                    }
                }

                ByteBuffer dataBuffer = mDataBuffer;
                // 从 AudioRecord 中读取指定数量（mBufferSizePerFrame）的音频数据
                totalReadLen = 0;
                needReadLen = mBufferSizePerFrame;
                dataBuffer.position(totalReadLen);
                do {
                    readLen = mAudioRecord.read(dataBuffer.array(),
                            dataBuffer.arrayOffset() + totalReadLen, needReadLen);
                    if (readLen > 0) {
                        needReadLen -= readLen;
                        totalReadLen += readLen;
                    }
                } while ((needReadLen > 0) && (readLen >= 0));

                dataBuffer.limit(totalReadLen);
                dataBuffer.rewind();
                if (totalReadLen >= 0 && mDataAvailableListener != null) {
                    // 通知外层可以取录音数据
                    mDataAvailableListener.onDataAvailable(dataBuffer);
                }
            }

            try {
                mAudioRecord.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            try {
                mAudioRecord.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }
}
