/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gtp.hunter.vision.automl;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerOptions;
import com.gtp.hunter.vision.GraphicOverlay;
import com.gtp.hunter.vision.VisionProcessorBase;

import java.io.IOException;
import java.util.List;

/**
 * AutoML image labeler demo.
 */
public class AutoMLImageLabelerProcessor extends VisionProcessorBase<List<ImageLabel>> {

    private static final String TAG = "AutoMLProcessor";

    private final ImageLabeler imageLabeler;

    public AutoMLImageLabelerProcessor(Context context, AutoMLImageLabelerOptions options) {
        super(context);
        Log.d(TAG, "Local model used.");

        imageLabeler = ImageLabeling.getClient(options);
    }

    @Override
    public void stop() {
        super.stop();
        try {
            imageLabeler.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close the image labeler", e);
        }
    }

    @Override
    protected Task<List<ImageLabel>> detectInImage(InputImage image) {
        return imageLabeler.process(image);
    }

    @Override
    protected void onSuccess(
            @NonNull List<ImageLabel> labels, @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.add(new LabelGraphic(graphicOverlay, labels));
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Label detection failed.", e);
    }
}

