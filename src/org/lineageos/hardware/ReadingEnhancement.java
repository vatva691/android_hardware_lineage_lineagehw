/*
 * Copyright (C) 2018 The LineageOS Project
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

package org.lineageos.hardware;

import android.app.ActivityThread;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;

import com.android.server.LocalServices;
import com.android.server.display.DisplayTransformManager;
import static com.android.server.display.DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE;

import org.lineageos.internal.util.FileUtils;

/**
 * Reader mode
 */
public class ReadingEnhancement {

    private static final String TAG = "ReadingEnhancement";

    private static final int LEVEL_COLOR_MATRIX_READING = LEVEL_COLOR_MATRIX_GRAYSCALE + 1;

    private static final int MODE_UNSUPPORTED          = 0;
    private static final int MODE_HWC2_COLOR_TRANSFORM = 1;

    private static final int sMode;

    /**
     * Matrix and offset used for converting color to grayscale.
     * Copied from com.android.server.accessibility.DisplayAdjustmentUtils.MATRIX_GRAYSCALE
     */
    private static final float[] MATRIX_GRAYSCALE = new float[] {
        .2126f, .2126f, .2126f, 0,
        .7152f, .7152f, .7152f, 0,
        .0722f, .0722f, .0722f, 0,
             0,      0,      0, 1
    };

    /** Full color matrix and offset */
    private static final float[] MATRIX_NORMAL = new float[] {
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1
    };

    private static DisplayTransformManager sDTMService;

    static {
        // Determine mode of operation.
        if (ActivityThread.currentApplication().getApplicationContext().getResources().getBoolean(
                    com.android.internal.R.bool.config_setColorTransformAccelerated)) {
            sMode = MODE_HWC2_COLOR_TRANSFORM;
        } else {
            sMode = MODE_UNSUPPORTED;
        }
    }

    public static boolean isSupported() {
        return sMode != MODE_UNSUPPORTED;
    }

    public static boolean setGrayscale(boolean state) {
        if (sDTMService == null) {
            sDTMService = LocalServices.getService(DisplayTransformManager.class);
            if (sDTMService == null) {
                return false;
            }
        }
        sDTMService.setColorMatrix(LEVEL_COLOR_MATRIX_READING,
                state ? MATRIX_GRAYSCALE : MATRIX_NORMAL);
        return true;
    }

}
