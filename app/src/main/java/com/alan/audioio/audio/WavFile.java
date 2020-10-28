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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 封装文件头尾 44 字节长度的 WavFile，支持对 WavFile 的读、写操作，
 * 以 {@link #WavFile(String)} 方式创建时，为[读]模式，可以通过 {@link #getHeadInfo()} 获取 wav 文件的头信息
 * 以 {@link #WavFile(String, HeadInfo)} 方式创建时，为[写]模式
 *
 * Author: AlanWang4523.
 * Date: 2020/10/28 20:35.
 * Mail: alanwang4523@gmail.com
 */
public class WavFile {
    private HeadInfo mHeadInfo;
    private RandomAccessFile mWavFile;
    private int mAudioDataLenInBytes;
    private volatile boolean isWriteMode;
    private volatile boolean isClosed;

    /**
     * 构建一个 WavFile，该文件已存在，以读模式打开
     * @param filePath wav file 文件路径
     * @throws IOException IOException
     */
    public WavFile(String filePath) throws IOException {
        this(filePath, null);
    }

    /**
     * 根据 HeadInfo 创建一个 WavFile，以写模式打开
     * @param filePath wav file 文件路径
     * @param wavHeaderInfo wavHeaderInfo
     * @throws IOException IOException
     */
    public WavFile(String filePath, HeadInfo wavHeaderInfo) throws IOException {
        mWavFile = new RandomAccessFile(filePath, "rw");
        if (wavHeaderInfo != null) {
            mHeadInfo = wavHeaderInfo;
            byte[] wavHeader = generateWavHeader(
                    wavHeaderInfo.getSampleRate(),
                    wavHeaderInfo.getChannelCount(),
                    wavHeaderInfo.getBytePerSample() * 8,
                    0);
            mWavFile.write(wavHeader);
            isWriteMode = true;
        } else {
            mHeadInfo = getWavHeader(mWavFile);
            isWriteMode = false;
        }
        mWavFile.seek(44);
        mAudioDataLenInBytes = 0;
        isClosed = false;
    }

    /**
     * 获取 wav 头信息
     * @return HeadInfo
     */
    public HeadInfo getHeadInfo() {
        return mHeadInfo;
    }

    /**
     * 读取 PCM 数据
     * @param data pcm 数据存放的位置
     * @param off offset
     * @param len 想要读取的长度，单位：字节
     * @return 读取的长度，单位：字节
     * @throws IOException IOException
     */
    public int read(byte[] data, int off, int len) throws IOException {
        if (isWriteMode) {
            throw new IOException("The current file is not read mode.");
        }
        if (isClosed) {
            return 0;
        }
        return mWavFile.read(data, off, len);
    }

    /**
     * 写 PCM 数据
     * @param data 音频数据
     * @param offset offset
     * @param len 数据长度，单位：字节
     */
    public void write(byte[] data, int offset, int len) throws IOException {
        if (!isWriteMode) {
            throw new IOException("The current file is not write mode.");
        }
        if (isClosed || data == null || len <= 0) {
            return;
        }
        mWavFile.write(data, offset, len);
        mAudioDataLenInBytes += len;
    }

    /**
     * 更新 wav 文件头信息，并关闭文件
     * @throws IOException IOException
     */
    public void close() throws IOException {
        if (isClosed) {
            return;
        }
        isClosed = true;

        // 如果是写入模式，则更新文件头中的数据长度信息
        if (isWriteMode) {
            int totalFileLenIncludeHeader = mAudioDataLenInBytes + 44;
            //更新wav文件头04H— 08H的数据长度：该长度 = 文件总长 - 8
            mWavFile.seek(4);
            mWavFile.write(int2ByteArray(totalFileLenIncludeHeader - 8));

            //更新wav文件头28H— 2CH,实际PCM采样数据长度
            mWavFile.seek(40);
            mWavFile.write(int2ByteArray(totalFileLenIncludeHeader - 44));
        }

        mWavFile.close();
    }

    /**
     * 生成 44 字节 WAV 文件头
     * @param sampleRate 采样率，如 44100
     * @param channels 通道数，如立体声为2
     * @param bitsPerSample 采样精度，即每个采样所占数据位数，如 16，表示每个采样 16bit 数据，即 2 个字节
     * @param audioDataLenInBytes 音频数据长度
     * @return 44 字节 WAV 头信息
     */
    private byte[] generateWavHeader(int sampleRate, int channels, int bitsPerSample, int audioDataLenInBytes) {
        if (bitsPerSample != 16 && bitsPerSample != 32) {
            throw new IllegalArgumentException("The bitsPerSample is not 16 or 32!");
        }
        if (audioDataLenInBytes < 0) {
            throw new IllegalArgumentException("Audio data len could not be negative!");
        }
        byte[] wavHeader = new byte[44];

        // 这个长度不包括"RIFF"标志(4字节)和文件长度本身所占字节(4字节),即该长度等于整个 Wav文件长度(包含44字节头) - 8
        // 也等于纯音频数据的长度 + 36
        int ckTotalSize = 36 + audioDataLenInBytes;

        // 生成文件头默认纯音频数据长度为 0
        int audioDataLen = audioDataLenInBytes;

        // 音频数据传送速率, 单位是字节。其值为采样率×每次采样大小。播放软件利用此值可以估计缓冲区的大小。
        // bytePerSecond = sampleRate * (bitsPerSample / 8) * channels
        int bytePerSecond = sampleRate * (bitsPerSample / 8) * channels;

        //ckid：4字节 RIFF 标志，大写
        wavHeader[0]  = 'R';
        wavHeader[1]  = 'I';
        wavHeader[2]  = 'F';
        wavHeader[3]  = 'F';

        //cksize：4字节文件长度，这个长度不包括"RIFF"标志(4字节)和文件长度本身所占字节(4字节),即该长度等于整个文件长度 - 8
        wavHeader[4]  = (byte)(ckTotalSize & 0xff);
        wavHeader[5]  = (byte)((ckTotalSize >> 8) & 0xff);
        wavHeader[6]  = (byte)((ckTotalSize >> 16) & 0xff);
        wavHeader[7]  = (byte)((ckTotalSize >> 24) & 0xff);

        //fcc type：4字节 "WAVE" 类型块标识, 大写
        wavHeader[8]  = 'W';
        wavHeader[9]  = 'A';
        wavHeader[10] = 'V';
        wavHeader[11] = 'E';

        //ckid：4字节 表示"fmt" chunk的开始,此块中包括文件内部格式信息，小写, 最后一个字符是空格
        wavHeader[12] = 'f';
        wavHeader[13] = 'm';
        wavHeader[14] = 't';
        wavHeader[15] = ' ';

        //cksize：4字节，文件内部格式信息数据的大小，过滤字节（一般为00000010H）
        wavHeader[16] = 0x10;
        wavHeader[17] = 0;
        wavHeader[18] = 0;
        wavHeader[19] = 0;

        //FormatTag：2字节，音频数据的编码方式，1：表示是PCM 编码
        wavHeader[20] = 1;
        wavHeader[21] = 0;

        //Channels：2字节，声道数，单声道为1，双声道为2
        wavHeader[22] = (byte) channels;
        wavHeader[23] = 0;

        //SamplesPerSec：4字节，采样率，如44100
        wavHeader[24] = (byte)(sampleRate & 0xff);
        wavHeader[25] = (byte)((sampleRate >> 8) & 0xff);
        wavHeader[26] = (byte)((sampleRate >> 16) & 0xff);
        wavHeader[27] = (byte)((sampleRate >> 24) & 0xff);

        //BytesPerSec：4字节，音频数据传送速率, 单位是字节。其值为采样率×每次采样大小。播放软件利用此值可以估计缓冲区的大小；
        //bytePerSecond = sampleRate * (bitsPerSample / 8) * channels
        wavHeader[28] = (byte)(bytePerSecond & 0xff);
        wavHeader[29] = (byte)((bytePerSecond >> 8) & 0xff);
        wavHeader[30] = (byte)((bytePerSecond >> 16) & 0xff);
        wavHeader[31] = (byte)((bytePerSecond >> 24) & 0xff);

        //BlockAlign：2字节，每次采样的大小 = 采样精度*声道数/8(单位是字节); 这也是字节对齐的最小单位, 譬如 16bit 立体声在这里的值是 4 字节。
        //播放软件需要一次处理多个该值大小的字节数据，以便将其值用于缓冲区的调整
        wavHeader[32] = (byte)(bitsPerSample * channels / 8);
        wavHeader[33] = 0;

        //BitsPerSample：2字节，每个声道的采样精度; 譬如 16bit 在这里的值就是16。如果有多个声道，则每个声道的采样精度大小都一样的；
        wavHeader[34] = (byte) bitsPerSample;
        wavHeader[35] = 0;

        //ckid：4字节，数据标志符（data），表示 "data" chunk的开始。此块中包含音频数据，小写；
        wavHeader[36] = 'd';
        wavHeader[37] = 'a';
        wavHeader[38] = 't';
        wavHeader[39] = 'a';

        //cksize：音频数据的长度，4字节，audioDataLen = ckSize - 36 = fileLenIncludeHeader - 44
        wavHeader[40] = (byte)(audioDataLen & 0xff);
        wavHeader[41] = (byte)((audioDataLen >> 8) & 0xff);
        wavHeader[42] = (byte)((audioDataLen >> 16) & 0xff);
        wavHeader[43] = (byte)((audioDataLen >> 24) & 0xff);

        return wavHeader;
    }

    /**
     * 从 wav 文件中获取头信息
     * 注意：必须是 44 字节头的 wav 文件
     * @param randomAccessFile wav 文件
     * @return HeadInfo
     * @throws IOException IOException
     */
    private HeadInfo getWavHeader(RandomAccessFile randomAccessFile) throws IOException {

        //读取channelCount，第22~23位
        randomAccessFile.seek(22);
        byte[] channelCountArray = new byte[2];
        randomAccessFile.read(channelCountArray);
        int channelCount = byteArray2Short(channelCountArray);

        //读取sampleRate，第24~27位
        randomAccessFile.seek(24);
        byte[] sampleRateArray = new byte[4];
        randomAccessFile.read(sampleRateArray);
        int sampleRate = byteArray2Int(sampleRateArray);

        //读取BitsPerSample，第34~35位
        randomAccessFile.seek(34);
        byte[] bitsPerSampleArray = new byte[2];
        randomAccessFile.read(bitsPerSampleArray);
        int bytePerSample = byteArray2Short(bitsPerSampleArray) / 8;

        return HeadInfo.build().
                setSampleRate(sampleRate).
                setChannelCount(channelCount).
                setBytePerSample(bytePerSample);
    }

    /**
     * 将 byte 数组转成short
     * @param b byte 数组
     * @return 返回 short 数值
     */
    private static short byteArray2Short(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    /**
     * 将整型转成 byte 数组
     * @param data 要转换的数字
     * @return byte 数组
     */
    private static byte[] int2ByteArray(int data) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
    }

    /**
     * 将 byte 数组转成整型
     * @param b byte 数组
     * @return int 数值
     */
    private static int byteArray2Int(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }


    public static class HeadInfo {
        /**
         * 采样率
         */
        private int sampleRate;

        /**
         * 通道数
         */
        private int channelCount;

        /**
         * 每个采样点的大小，
         * short 型 PCM 是 2 字节
         * float 型 PCM 是 4 字节
         * 单位：字节
         */
        private int bytePerSample = 2;

        /**
         * 构造 HeadInfo
         * @return HeadInfo 实例
         */
        public static HeadInfo build() {
            return new HeadInfo();
        }

        /**
         * 获取音频采样率，如：44100、48000
         * @return 设置参数后的 HeadInfo 实例
         */
        public int getSampleRate() {
            return sampleRate;
        }

        /**
         * 设置音频采样率
         * @param sampleRate 采样率，如：44100、48000
         * @return 设置参数后的 HeadInfo 实例
         */
        public HeadInfo setSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
            return this;
        }

        /**
         * 获取音频通道数，如：1、2
         * @return 设置参数后的 HeadInfo 实例
         */
        public int getChannelCount() {
            return channelCount;
        }

        /**
         * 设置音频通道数
         * @param channelCount 音频通道数
         * @return 设置参数后的 HeadInfo 实例
         */
        public HeadInfo setChannelCount(int channelCount) {
            this.channelCount = channelCount;
            return this;
        }

        /**
         * 获取每个采样点大小，单位：字节
         * short 型 PCM 是 2 字节
         * float 型 PCM 是 4 字节
         * @return 每个采样点占的字节数
         */
        public int getBytePerSample() {
            return bytePerSample;
        }

        /**
         * 设置每个采样占的字节数
         * @param bytePerSample bytePerSample
         * @return 设置参数后的 HeadInfo 实例
         */
        public HeadInfo setBytePerSample(int bytePerSample) {
            this.bytePerSample = bytePerSample;
            return this;
        }
    }
}
