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
 * Date: 2020/10/17 15:29.
 * Mail: alanwang4523@gmail.com
 */
public class AudioConstants {
    public static final String HOST_ASSETS = "assets://";
    public static final String HOST_EXFILE = "exfile://";

    /**
     * 是否是 assets 路径
     * @param url url
     * @return 是否是 assets 路径
     */
    public static boolean isAssetsPath(String url) {
        return url.startsWith(HOST_ASSETS);
    }

    /**
     * 是否是外置路径
     * @param url url
     * @return 是否是外置路径
     */
    public static boolean isExFilePath(String url) {
        return url.startsWith(HOST_EXFILE);
    }
}
