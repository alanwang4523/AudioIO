package com.alan.audioio.audio.common;

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Author: AlanWang4523.
 * Date: 2019-06-13 23:18.
 * Mail: alanwang4523@gmail.com
 */
public class Type {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({AudioApi.Unspecified, AudioApi.OpenSLES, AudioApi.AAudio})
    public @interface AudioApi {
        /**
         * Try to use AAudio. If not available then use OpenSL ES.
         */
        int Unspecified = 0;

        /**
         * Use OpenSL ES.
         */
        int OpenSLES = 1;

        /**
         * Try to use AAudio. Fail if unavailable.
         */
        int AAudio = 2;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Direction.Output, Direction.Input})
    public @interface Direction {
        /**
         * Used for playback.
         */
        int Output = 0;

        /**
         * Used for recording.
         */
        int Input = 1;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SharingMode.Exclusive, SharingMode.Shared})
    public @interface SharingMode {
        /**
         * This will be the only stream using a particular source or sink.
         * This mode will provide the lowest possible latency.
         * You should close EXCLUSIVE streams immediately when you are not using them.
         *
         * If you do not need the lowest possible latency then we recommend using Shared,
         * which is the default.
         */
        int Exclusive = 0;

        /**
         * Multiple applications can share the same device.
         * The data from output streams will be mixed by the audio service.
         * The data for input streams will be distributed by the audio service.
         *
         * This will have higher latency than the EXCLUSIVE mode.
         */
        int Shared = 1;
    }
}
