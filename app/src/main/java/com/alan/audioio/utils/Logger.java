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
package com.alan.audioio.utils;

import android.util.Log;
import com.alan.audioio.BuildConfig;

/**
 * Author: AlanWang4523.
 * Date: 2019-06-16 00:03.
 * Mail: alanwang4523@gmail.com
 */
public class Logger {
    private static boolean DEBUG = BuildConfig.DEBUG;

    public static void setDebug(boolean isDebug) {
        DEBUG = isDebug;
    }

    public static void i(String msg) {
        if (isDebug()) {
            Log.i(getCallerName(), msg);
        }
    }

    public static void d(String msg) {
        if (isDebug()) {
            Log.d(getCallerName(), msg);
        }
    }

    public static void v(String msg) {
        if (isDebug()) {
            Log.v(getCallerName(), msg);
        }
    }

    public static void e(String msg) {
        if (isDebug()) {
            Log.e(getCallerName(), msg);
        }
    }

    public static void e(Throwable e) {
        if (isDebug()) {
            Log.e(getCallerName(), "error", e);
        }
    }

    public static void e(String msg, Throwable e) {
        if (isDebug()) {
            Log.e(getCallerName(), msg, e);
        }
    }

    public static void w(String msg) {
        if (isDebug()) {
            Log.w(getCallerName(), msg);
        }
    }

    private static String getCallerName() {
        StackTraceElement[] elements = new Throwable().getStackTrace();
        return elements[2].getClassName();
    }

    private static Boolean isDebug() {
        return DEBUG;
    }
}
