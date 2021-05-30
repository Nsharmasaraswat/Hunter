package com.gtp.hunter.structure;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.view.PreviewView;

import java.util.concurrent.TimeUnit;

public class FocusPreviewView extends PreviewView {
    private static final long MIN_FOCUS_TIME = TimeUnit.SECONDS.toNanos(2);
    private static final String TAG = "FocusPreviewView";

    private float touchX, touchY;
    private long lastTouch;
    private Camera cam;

    public FocusPreviewView(@NonNull Context context) {
        super(context);
    }

    public FocusPreviewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusPreviewView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCamera(Camera cam) {
        this.cam = cam;
        final ViewTreeObserver viewTreeObserver = getViewTreeObserver();

        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    startAutoFocus();
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                long elapsed = SystemClock.elapsedRealtimeNanos() - lastTouch;

                if (elapsed >= MIN_FOCUS_TIME) {
                    touchX = event.getX();
                    touchY = event.getY();
                    performClick();
                    lastTouch = SystemClock.elapsedRealtimeNanos();
                }
            case MotionEvent.ACTION_DOWN:
                break;
        }
        return true;
    }

    // Because we call this from onTouchEvent, this code will be executed for both
    // normal touch events and for when the system calls this using Accessibility
    @Override
    public boolean performClick() {
        super.performClick();
        startAutoFocus();
        Log.d(TAG, "Click!");
        return true;
    }

    private void startAutoFocus() {
        if (cam != null) {
            if (touchX == 0) touchX = getWidth() / 2f;
            if (touchY == 0) touchY = getHeight() / 2f;
            Log.d(TAG, "StartAutoFocus X: " + touchX + " Y: " + touchY);
            cam.getCameraControl().cancelFocusAndMetering();
            MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(getWidth(), getHeight());
            MeteringPoint point = factory.createPoint(touchX, touchY);
            FocusMeteringAction action = new FocusMeteringAction
                    .Builder(point, FocusMeteringAction.FLAG_AF | FocusMeteringAction.FLAG_AWB | FocusMeteringAction.FLAG_AE)
                    .setAutoCancelDuration(1, TimeUnit.SECONDS)
                    .build();
            cam.getCameraControl().startFocusAndMetering(action);
        } else throw new IllegalStateException("Set Camera First");
    }
}
