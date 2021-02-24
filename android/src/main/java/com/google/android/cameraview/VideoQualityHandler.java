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
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class VideoQualityHandler {

    CameraViewImpl mImpl;

    public VideoQualityHandler(CameraViewImpl mImpl) {
        this.mImpl = mImpl;
    }

    public SortedSet<Size> getAvailableVideoSizes(@NonNull AspectRatio ratio) {
        SortedSet<Size> result = new TreeSet<Size>();

        List<CamcorderProfile> profiles = mImpl.getSupportedProfiles();
        SortedSet<Size> sizes = mImpl.getSupportedVideoSizes(ratio);
        Size[] sizesArray = sizes.toArray( new Size[sizes.size()] );

        for (int i = 0; i < profiles.size(); i++) {
            CamcorderProfile profile = profiles.get(i);
            addVideoResolutions(result, sizesArray, profile);
        }
        return result;
    }

    private void addVideoResolutions(SortedSet<Size> result, Size[] sizes, CamcorderProfile baseProfile) {
        int minResWidth = baseProfile.videoFrameWidth;
        int minResHeight = baseProfile.videoFrameHeight;
        for (int i = 0; i < sizes.length; i++) {
            Size size = sizes[i];

            if (size.getWidth() == minResWidth && size.getHeight() == minResHeight) {
                result.add(size);
                return;
            }

            if (baseProfile.quality == CamcorderProfile.QUALITY_LOW || size.getWidth() * size.getHeight() >= minResWidth * minResHeight) {
                result.add(new Size(size.getWidth(), size.getHeight()));
            }
        }
    }
}
