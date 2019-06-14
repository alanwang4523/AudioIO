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
package com.alan.audioio.audio.common;

/**
 * Author: AlanWang4523.
 * Date: 2019-06-14 22:44.
 * Mail: alanwang4523@gmail.com
 */
public class AudioIOBuilder {
    private int sampleRate;
    private int channelCount;
    private int bufferSize;
    private @Type.Direction int direction;
    private @Type.AudioApi int audioApi;
    private @Type.SharingMode int sharingMode;

    public static AudioIOBuilder builder() {
        return new AudioIOBuilder();
    }

    private AudioIOBuilder() {
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public AudioIOBuilder setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public AudioIOBuilder setChannelCount(int channelCount) {
        this.channelCount = channelCount;
        return this;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public AudioIOBuilder setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public @Type.Direction int getDirection() {
        return direction;
    }

    public AudioIOBuilder setDirection(@Type.Direction int direction) {
        this.direction = direction;
        return this;
    }

    public @Type.AudioApi int getAudioApi() {
        return audioApi;
    }

    public AudioIOBuilder setAudioApi(@Type.AudioApi int audioApi) {
        this.audioApi = audioApi;
        return this;
    }

    public @Type.SharingMode int getSharingMode() {
        return sharingMode;
    }

    public AudioIOBuilder setSharingMode(@Type.SharingMode int sharingMode) {
        this.sharingMode = sharingMode;
        return this;
    }
}
