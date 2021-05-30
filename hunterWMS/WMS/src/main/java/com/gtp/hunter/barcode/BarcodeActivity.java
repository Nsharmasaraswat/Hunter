package com.gtp.hunter.barcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;

import com.google.android.gms.common.annotation.KeepName;
import com.google.mlkit.common.MlKitException;
import com.gtp.hunter.R;
import com.gtp.hunter.structure.FocusPreviewView;
import com.gtp.hunter.structure.viewmodel.CameraXViewModel;
import com.gtp.hunter.vision.GraphicOverlay;
import com.gtp.hunter.vision.VisionImageProcessor;
import com.gtp.hunter.vision.barcodescanner.BarcodeScannerProcessor;
import com.gtp.hunter.vision.preference.SettingsActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Live preview demo app for ML Kit APIs using CameraX.
 */
@KeepName
@RequiresApi(VERSION_CODES.LOLLIPOP)
public final class BarcodeActivity extends AppCompatActivity implements OnRequestPermissionsResultCallback {
    private static final String TAG = "CameraXLivePreview";
    private static final int PERMISSION_REQUESTS = 1;

    private static final String DISPLAY_VALUE = "DISPLAY_VALUE";
    private static final String RAW_VALUE = "RAW_VALUE";

    private static final Size TARGET_SIZE = new Size(1200, 1600);

    //    private static final String OBJECT_DETECTION = "Object Detection";
//    private static final String OBJECT_DETECTION_CUSTOM = "Custom Object Detection (Bird)";
//    private static final String FACE_DETECTION = "Face Detection";
//    private static final String TEXT_RECOGNITION = "Text Recognition";
    private static final String BARCODE_SCANNING = "Barcode Scanning";
//    private static final String IMAGE_LABELING = "Image Labeling";
//    private static final String IMAGE_LABELING_CUSTOM = "Custom Image Labeling (Bird)";
//    private static final String AUTOML_LABELING = "AutoML Image Labeling";

    private static final String STATE_SELECTED_MODEL = "selected_model";
    private static final String STATE_LENS_FACING = "lens_facing";

    private FocusPreviewView previewView;
    private GraphicOverlay graphicOverlay;

    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable
    private Preview previewUseCase;
    @Nullable
    private ImageAnalysis analysisUseCase;
    @Nullable
    private VisionImageProcessor imageProcessor;
    private boolean needUpdateGraphicOverlayImageSourceInfo;

    private String selectedModel = BARCODE_SCANNING;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private CameraSelector cameraSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if (savedInstanceState != null) {
            selectedModel = savedInstanceState.getString(STATE_SELECTED_MODEL, BARCODE_SCANNING);
            lensFacing = savedInstanceState.getInt(STATE_LENS_FACING, CameraSelector.LENS_FACING_BACK);
        }
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        setContentView(R.layout.activity_barcode);
        previewView = findViewById(R.id.preview_view);
        if (previewView == null) {
            Log.d(TAG, "previewView is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        new ViewModelProvider(this, AndroidViewModelFactory.getInstance(getApplication()))
                .get(CameraXViewModel.class)
                .getProcessCameraProvider()
                .observe(
                        this,
                        provider -> {
                            cameraProvider = provider;
                            init();
                            if (allPermissionsGranted()) {
                                bindAllCameraUseCases();
                            }
                        });

        ImageView settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(
                v -> {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    intent.putExtra(
                            SettingsActivity.EXTRA_LAUNCH_SOURCE,
                            SettingsActivity.LaunchSource.CAMERAX_LIVE_PREVIEW);
                    startActivity(intent);
                });

        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(STATE_SELECTED_MODEL, selectedModel);
        bundle.putInt(STATE_LENS_FACING, lensFacing);
    }

    public void init() {
        Log.d(TAG, "Set facing");
        if (cameraProvider == null) {
            return;
        }
        int newLensFacing = CameraSelector.LENS_FACING_BACK;
//        int newLensFacing = lensFacing == CameraSelector.LENS_FACING_FRONT ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;
        CameraSelector newCameraSelector = new CameraSelector.Builder().requireLensFacing(newLensFacing).build();

        try {
            if (cameraProvider.hasCamera(newCameraSelector)) {
                lensFacing = newLensFacing;
                cameraSelector = newCameraSelector;
                bindAllCameraUseCases();
                return;
            }
        } catch (CameraInfoUnavailableException e) {
            // Falls through
        }
        Toast.makeText(getApplicationContext(), "This device does not have lens with facing: " + newLensFacing, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        bindAllCameraUseCases();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    private void bindAllCameraUseCases() {
        bindPreviewUseCase();
        bindAnalysisUseCase();
    }

    private void bindPreviewUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        previewUseCase = new Preview.Builder()
                .setTargetResolution(TARGET_SIZE)
                .build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
        final Camera cam = cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, previewUseCase);
        previewView.setCamera(cam);
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }
        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
        imageProcessor = new BarcodeScannerProcessor(this, barcode -> {
            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(DISPLAY_VALUE, barcode.getDisplayValue());
            intent.putExtra(RAW_VALUE, barcode.getRawValue());

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        });
        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();

        builder.setTargetResolution(TARGET_SIZE);
        analysisUseCase = builder.build();
        needUpdateGraphicOverlayImageSourceInfo = true;
        analysisUseCase.setAnalyzer(
                // imageProcessor.processImageProxy will use another thread to run the detection underneath,
                // thus we can just runs the analyzer itself on main thread.
                ContextCompat.getMainExecutor(this),
                imageProxy -> {
                    if (needUpdateGraphicOverlayImageSourceInfo) {
                        boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT;
                        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                        if (rotationDegrees == 0 || rotationDegrees == 180) {
                            graphicOverlay.setImageSourceInfo(
                                    imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                        } else {
                            graphicOverlay.setImageSourceInfo(
                                    imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                        }
                        needUpdateGraphicOverlayImageSourceInfo = false;
                    }
                    try {
                        imageProcessor.processImageProxy(imageProxy, graphicOverlay);
                    } catch (MlKitException e) {
                        Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
    }

    private String[] getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        String[] ret = new String[0];

        try {
            PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);

            if (info.requestedPermissions != null)
                permissions = Arrays.asList(info.requestedPermissions);
            permissions.remove("android.permission.USE_FINGERPRINT");
            permissions.remove("android.permission.USE_BIOMETRIC");
        } catch (Exception ignored) {
            //passthrough
        }
        ret = permissions.toArray(ret);
        return ret;
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (isPermissionDenied(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (isPermissionDenied(this, permission) && !permission.equals("android.permission.USE_BIOMETRIC") && !permission.equals("android.permission.USE_FINGERPRINT")) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            bindAllCameraUseCases();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionDenied(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return false;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return true;
    }
}
