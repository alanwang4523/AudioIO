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
import com.alan.audioio.audio.common.Type;

/**
 * Author: AlanWang4523.
 * Date: 2019-06-14 23:04.
 * Mail: alanwang4523@gmail.com
 */
public class AudioPlayer extends AudioIOStream {

    public AudioPlayer(AudioIOBuilder builder) {
        builder.setDirection(Type.Direction.Output);
        native_create(builder);
    }
}
