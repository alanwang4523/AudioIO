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
 * Date: 19/6/1 22:42.
 * Mail: alanwang4523@gmail.com
 */

#include "JNIHelper.h"
#include "Log.h"

int jniRegisterNativeMethods(JNIEnv* env, const char* className, const JNINativeMethod* gMethods, int numMethods) {

    LOGD("JNIHelper", "Start registering %s native methods.\n", className);
    jclass clazz = (env)->FindClass(className);
    if (clazz == NULL) {
        LOGE("JNIHelper", "Native registration unable to find class '%s'.\n", className);
        return -1;
    }

    int result = 0;
    if ((env)->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        LOGE("JNIHelper", "RegisterNatives failed for '%s'.\n", className);
        result = -1;
    }

    (env)->DeleteLocalRef(clazz);
    LOGD("JNIHelper", "Registering %s native methods success.\n", className);
    return result;
}