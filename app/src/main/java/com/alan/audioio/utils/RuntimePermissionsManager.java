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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Author: AlanWang4523.
 * Date: 2019-06-14 23:15.
 * Mail: alanwang4523@gmail.com
 */
public class RuntimePermissionsManager {

    private static final int DENY_COUNT_MAX = 3;

    /**
     * A {@link Map} of permission names mapped to deny counts so we can keep track of the number of
     * times the user has denied each permission
     */
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
     * Check whether every requested permission has been granted
     * @return true if all requested permission has been granted
     */
    public boolean isAllPermissionsGranted() {
        boolean allPermissionsGranted = true;
        if (isRuntimePermissionRequiredByApiLevel()) {
            for (String permissionName : mDenyCountMap.keySet()){
                if (!isPermissionGranted(mActivity, permissionName)){
                    allPermissionsGranted = false;
                }
            }
        }
        return allPermissionsGranted;
    }

    /**
     * Tell Android to show the user a dialog asking for the specified permission.  We request the
     * permissions without an explanation because the reason for the request should be obvious to the user.
     */
    public void showPermissionsRequest() {
        Set<String> permissionsSet = new TreeSet<>();
        for (String permissionName : mDenyCountMap.keySet()) {
            if (!isPermissionGranted(mActivity, permissionName)) {
                if (!askedTooManyTimes(mDenyCountMap.get(permissionName))) {
                    permissionsSet.add(permissionName);
                } else {
                    if (listener != null) {
                        listener.onShowTooManyTimes();
                    }
                    return;
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
        if (requestCode == mRequestCode) {
            for (int i=0; i<grantResults.length;i++){
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    incrementDenyCount(permissions[i]);
                }
            }
            if (listener != null){
                listener.onPermissionsGranted(isAllPermissionsGranted());
            }
        }
    }

    private void incrementDenyCount(String permissionName) {
        Integer denyCount = mDenyCountMap.get(permissionName) + 1;
        mDenyCountMap.put(permissionName, denyCount);
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

    /**
     * Did we already ask the user for permission too many times?
     * @return
     * @param denyCount
     */
    private boolean askedTooManyTimes(int denyCount) {
        return denyCount >= DENY_COUNT_MAX;
    }

    /**
     * Determines whether runtime permissions are required, based on the version of Android that
     * the user is running.  API 23 (Marshmallow) or higher requires runtime permissions.
     *
     * @return
     */
    private boolean isRuntimePermissionRequiredByApiLevel() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public interface Listener {
        /**
         * Called when the user has either granted or denied a runtime permission request.  The
         * implementing {@link Listener}, can then
         * check whether it has the permissions to do what it needs to do.
         *
         * @param isAllPermissionsGranted
         */
        void onPermissionsGranted(boolean isAllPermissionsGranted);

        /**
         * show too many times
         */
        void onShowTooManyTimes();
    }
}
