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

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/10 21:19.
 * Mail: alanwang4523@gmail.com
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        IOStatus.UNINITIATED, IOStatus.INITIATED,
        IOStatus.START, IOStatus.PAUSE,
        IOStatus.RESUME, IOStatus.STOP})
public @interface IOStatus {
    int UNINITIATED = -1;
    int INITIATED   = 0;
    int START       = 1;
    int PAUSE       = 2;
    int RESUME      = 3;
    int STOP        = 4;
}
