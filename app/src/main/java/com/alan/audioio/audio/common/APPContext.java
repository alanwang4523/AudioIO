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

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.Keep;

/**
 * Author: AlanWang4523.
 * Date: 2020/10/17 15:31.
 * Mail: alanwang4523@gmail.com
 */
public class APPContext {
    private static final APPContext sInstance = new APPContext();

    public static APPContext getInstance() {
        return sInstance;
    }

    private APPContext() {
    }

    private Context context;

    /**
     * 获取 AssetManager
     * @return AssetManager
     */
    @Keep
    public static AssetManager getAssetManager() {
        return sInstance.getContext().getAssets();
    }

    /**
     * 获取 Context
     * @return Context
     */
    public Context getContext() {
        return context;
    }

    /**
     * 设置 Context
     * @param context context
     */
    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }
}
