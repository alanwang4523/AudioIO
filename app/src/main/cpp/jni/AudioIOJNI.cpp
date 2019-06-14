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
 * Date: 19/6/2 00:50.
 * Mail: alanwang4523@gmail.com
 */

#include <jni.h>
#include "JNIHelper.h"
#include "Log.h"

struct fields_t {
    jfieldID context;
};
static fields_t gFields;

static const char *className = "com/alan/audioio/audio/AudioIOStreamStream";

static int AudioIOStream_native_init(JNIEnv *env) {
    LOGD("AudioIOStreamJNI", "AudioIOStream_native_init()-->>\n");

    jclass clazz = (env)->FindClass(className);
    if (clazz == NULL) {
        LOGE("AudioIOStreamJNI", "Unable to find class\n");
        return -1;
    }
    gFields.context = env->GetFieldID(clazz, "mNativeContext", "J");
    return 0;
}

static void AudioIOStream_native_create(JNIEnv *env, jobject obj, jobject jBuilder) {
//    AudioIOStream * AudioIOStream = new AudioIOStream(cStr);
//    env->SetLongField(obj, gFields.context, (jlong)AudioIOStream);
}

static void AudioIOStream_native_start(JNIEnv *env, jobject obj) {
//    AudioIOStream * AudioIOStream = (AudioIOStream *)env->GetLongField(obj, gFields.context);
//    if (!AudioIOStream) {
//        return;
//    }
}

static void AudioIOStream_native_stop(JNIEnv *env, jobject obj) {
//    AudioIOStream * AudioIOStream = (AudioIOStream *)env->GetLongField(obj, gFields.context);
//    if (!AudioIOStream) {
//        return;
//    }
}

static void AudioIOStream_native_release(JNIEnv *env, jobject obj) {
//    AudioIOStream * AudioIOStream = (AudioIOStream *)env->GetLongField(obj, gFields.context);
//    if (AudioIOStream) {
//        delete AudioIOStream;
//    }
    env->SetLongField(obj, gFields.context, (jlong)0);
}

static JNINativeMethod gJni_Methods[] = {
        {"native_init", "()V", (void*)AudioIOStream_native_init},
        {"native_create", "(Lcom/alan/audioio/audio/common/AudioIOBuilder;)V", (void*)AudioIOStream_native_create},
        {"native_start", "()V", (void*)AudioIOStream_native_start},
        {"native_start", "()V", (void*)AudioIOStream_native_stop},
        {"native_release", "()V", (void*)AudioIOStream_native_release},
};

int register_AudioIOStream(JNIEnv* env) {
    return jniRegisterNativeMethods(env, className, gJni_Methods, NELEM(gJni_Methods));
}