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

/**
 * Author: AlanWang4523.
 * Date: 19/6/1 23:05.
 * Mail: alanwang4523@gmail.com
 */

#include <jni.h>
#include "Log.h"

extern int register_AudioIOStream(JNIEnv* env);

jint JNI_OnLoad(JavaVM* jvm, void* reserved){
    JNIEnv* env = NULL;
    jint result = -1;

    if (jvm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }

    LOGD("JNIOnLoad", "JNI_OnLoad()--->register_Audio_IO");
    if (register_AudioIOStream(env) < 0) {
        return result;
    }

    LOGD("JNIOnLoad", "JNI_OnLoad()--->success");
    return JNI_VERSION_1_4;
}