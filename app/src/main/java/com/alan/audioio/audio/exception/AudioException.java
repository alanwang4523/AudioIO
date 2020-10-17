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
package com.alan.audioio.audio.exception;

/**
 * Author: AlanWang4523.
 * Date: 2020/10/17 15:25.
 * Mail: alanwang4523@gmail.com
 */
public class AudioException extends Exception {

    protected int errorCode;
    protected String errorMsg;

    public AudioException(String errorMsg) {
        this(-1, errorMsg, null);
    }

    public AudioException(String errorMsg, Throwable throwable) {
        this(-1, errorMsg, throwable);
    }

    public AudioException(int errorCode, String errorMsg) {
        this(errorCode, errorMsg, null);
    }

    public AudioException(int errorCode, String errorMsg, Throwable throwable) {
        super(throwable);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    /**
     * 获取错误码
     * @return errCode
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * 获取错误信息
     * @return errMsg
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public String toString() {
        return "AudioException{" +
                "errCode=" + errorCode +
                ", errMsg='" + errorMsg + '\'' +
                '}';
    }
}
