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

import com.alan.audioio.audio.common.AudioIOBuilder;

/**
 * Author: AlanWang4523.
 * Date: 19/6/2 00:59.
 * Mail: alanwang4523@gmail.com
 */
public abstract class AudioIOStream implements IAudioIO {

    private static native final void native_init();

    static {
        System.loadLibrary("audio_io");
        native_init();
    }

    @Override
    public void start() {
        native_start();
    }

    @Override
    public void stop() {
        native_stop();
    }

    @Override
    public void release() {
        native_release();
    }

    /////////////////////////// Native functions ///////////////////////////

    protected native final void native_create(AudioIOBuilder builder);

    private native final void native_start();

    private native final void native_stop();

    private native final void native_release();
}
