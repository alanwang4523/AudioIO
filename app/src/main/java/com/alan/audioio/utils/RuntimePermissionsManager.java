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

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: AlanWang4523.
 * Date: 2019-06-14 23:15.
 * Mail: alanwang4523@gmail.com
 */
public class RuntimePermissionsManager {

    private static final int DENY_COUNT_MAX = 3;
    private final ArrayMap<String, Integer> mDenyCountMap;
    private Activity mActivity;
    private int mRequestCode;
    private Listener listener;

    public RuntimePermissionsManager(@NonNull Activity activity,
                                     @NonNull String... permissionName) {
        mActivity = activity;
        mRequestCode = generateRequestCode(activity);
        mDenyCountMap = new ArrayMap<>();
        for (int i=0; i< permissionName.length; i++){
            mDenyCountMap.put(permissionName[i], 0);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * Check whether all requested permission has been granted
     * @return
     */
    public boolean isAllPermissionsGranted() {
        boolean allPermissionsGranted = true;
        if (isRuntimePermissionRequired()) {
            for (String permissionName : mDenyCountMap.keySet()){
                if (!isPermissionGranted(mActivity, permissionName)){
                    allPermissionsGranted = false;
                }
            }
        }
        return allPermissionsGranted;
    }

    /**
     * Make a request of permissions that we need
     * @return
     */
    public void makeRequest() {
        if (!isAllPermissionsGranted()){
            if (isAskedTooManyTimes()){
                if (listener != null) {
                    listener.onAskedTooManyTimes();
                }
            } else {
                tryToRequestPermissions();
            }
        } else {
            if (listener != null) {
                listener.onPermissionsGranted(true);
            }
        }
    }

    /**
     * Try to request the permissions
     */
    private void tryToRequestPermissions() {
        Set<String> permissionsSet = new HashSet<>();
        for (String permissionName : mDenyCountMap.keySet()) {
            if (!isPermissionGranted(mActivity, permissionName)) {
                if (isDenyTooManyTimes(mDenyCountMap.get(permissionName))) {
                    if (listener != null) {
                        listener.onAskedTooManyTimes();
                    }
                    return;
                } else {
                    permissionsSet.add(permissionName);
                }
            }
        }
        String[] permissionsList = permissionsSet.toArray(new String[permissionsSet.size()]);
        if (permissionsList.length > 0) {
            ActivityCompat.requestPermissions(mActivity, permissionsList, mRequestCode);
        }
    }

    /**
     * Updates the map that track whether the user has granted or denied
     * the requested permissions and notifies the {@link Listener}.
     * Call this method from {@code onRequestPermissionsResult()} of the {@link Activity} that
     * originated the permissions request.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions.length == 0 && grantResults.length == 0){
            return;
        }
        if (requestCode != mRequestCode) {
            return;
        }
        for (int i=0; i<grantResults.length;i++){
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                incrementDenyCount(permissions[i]);
            }
        }
        if (listener != null){
            listener.onPermissionsGranted(isAllPermissionsGranted());
        }
    }

    private void incrementDenyCount(String permissionName) {
        Integer denyCount = mDenyCountMap.get(permissionName) + 1;
        mDenyCountMap.put(permissionName, denyCount);
    }

    /**
     * Did we already ask the user for permission too many times?
     *
     * @return
     */
    private boolean isAskedTooManyTimes() {
        boolean shouldShowHint = false;
        if (isRuntimePermissionRequired()) {
            for (Integer denyCount : mDenyCountMap.values()){
                if (isDenyTooManyTimes(denyCount)){
                    shouldShowHint = true;
                    break;
                }
            }
        }
        return shouldShowHint;
    }

    /**
     * Did we already ask the user for permission too many times?
     * @return
     * @param denyCount
     */
    private boolean isDenyTooManyTimes(int denyCount) {
        return denyCount >= DENY_COUNT_MAX;
    }

    /**
     * Determines whether runtime permissions are required. API 23 (Android M, 6.0) or higher requires runtime permissions.
     * @return
     */
    private boolean isRuntimePermissionRequired() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Check whether the permission have been granted
     * @param activity the activity
     * @param permissionName the permission to check
     * @return
     */
    private static boolean isPermissionGranted(Activity activity, String permissionName) {
        return ContextCompat.checkSelfPermission(activity, permissionName) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Generate a request code(>= 0) that will be unique to the calling context,
     * as specified by Activity.requestPermissions().
     * @param context
     * @return
     */
    private static int generateRequestCode(Object context) {
        return context.hashCode() & 0xFF;
    }


    public interface Listener {
        /**
         * Called when the user has either granted or denied a runtime permission request.
         * @param isAllPermissionsGranted
         */
        void onPermissionsGranted(boolean isAllPermissionsGranted);

        /**
         * Called when asked too many times
         */
        void onAskedTooManyTimes();
    }
}
