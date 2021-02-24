/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.cameraview;

import android.media.CamcorderProfile;

import java.util.ArrayList;
import java.util.List;

public class CamcorderProfileHelper {

    public List<CamcorderProfile> getSupportedProfiles(int cameraId) {
        List<CamcorderProfile> profiles = new ArrayList<>();
        int[] qualities = {
            CamcorderProfile.QUALITY_HIGH, CamcorderProfile.QUALITY_1080P,
            CamcorderProfile.QUALITY_720P, CamcorderProfile.QUALITY_480P,
            CamcorderProfile.QUALITY_CIF, CamcorderProfile.QUALITY_QVGA,
            CamcorderProfile.QUALITY_QCIF, CamcorderProfile.QUALITY_LOW
        };

        for (int i = 0; i < qualities.length; i++) {
            int quality = qualities[i];
            if( CamcorderProfile.hasProfile(cameraId, quality) ) {
                CamcorderProfile profile = CamcorderProfile.get(cameraId, quality);
                profiles.add(profile);
            }
        }

        return profiles;
    }
}

