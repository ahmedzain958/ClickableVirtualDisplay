package com.luxoft.virtualdisplay;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class ClickableVirtualDisplay extends Activity {
    private static final String TAG = "VirtualDisplayExample";
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int VIRTUAL_DISPLAY_WIDTH = 720;  // Virtual display dimensions
    private static final int VIRTUAL_DISPLAY_HEIGHT = 1280;

    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private Surface mSurface;
    private SurfaceView mSurfaceView;
    private DisplayManager mDisplayManager;
    private Presentation mPresentation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clickable_virtual_display);
        mSurfaceView = findViewById(R.id.surface_view);
        mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurface = holder.getSurface();
                startScreenCapture();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mVirtualDisplay != null) {
                    mVirtualDisplay.release();
                    mVirtualDisplay = null;
                }
            }
        });

        mProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private void startScreenCapture() {
        if (mMediaProjection != null) {
            setupVirtualDisplay();
            return;
        }

        startActivityForResult(
                mProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != RESULT_OK) {
                Log.e(TAG, "User cancelled screen sharing permission");
                return;
            }

            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            setupVirtualDisplay();
        }
    }

    private void setupVirtualDisplay() {
        // Create the virtual display
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                "SecondActivity",
                VIRTUAL_DISPLAY_WIDTH, VIRTUAL_DISPLAY_HEIGHT,
                getResources().getDisplayMetrics().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC |
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION,
                mSurface, null, null
        );

        // Get the created virtual display
        Display presentationDisplay = null;
        for (Display display : mDisplayManager.getDisplays()) {
            if (display.getDisplayId() == mVirtualDisplay.getDisplay().getDisplayId()) {
                presentationDisplay = display;
                break;
            }
        }

        if (presentationDisplay != null) {
            // Create and show the presentation containing SecondActivity
            mPresentation = new SecondActivityPresentation(this, presentationDisplay);
            mPresentation.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresentation != null) {
            mPresentation.dismiss();
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
    }

    // Custom Presentation class to host SecondActivity
    private class SecondActivityPresentation extends Presentation {
        public SecondActivityPresentation(Context context, Display display) {
            super(context, display);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Launch SecondActivity in the virtual display
            Intent intent = new Intent(getContext(), SecondActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }
    }
}