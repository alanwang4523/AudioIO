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

import java.nio.ByteBuffer;

/**
 * Author: AlanWang4523.
 * Date: 2020/11/10 21:40.
 * Mail: alanwang4523@gmail.com
 */
public interface IDataAvailableListener {
    /**
     * 有数据到来
     * @param byteBuffer 具体数据
     *      如果是来自采集端可以从 byteBuffer 读取数据，有效数据长度为 byteBuffer.limit()
     *      如果是来自播放端可以往 byteBuffer 写入数据，写完后调用 byteBuffer.limit(count) 设置有效数据长度
     */
    void onDataAvailable(ByteBuffer byteBuffer);
}
