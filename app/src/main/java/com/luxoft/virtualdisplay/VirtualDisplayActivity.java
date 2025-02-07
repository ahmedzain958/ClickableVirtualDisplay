package com.luxoft.virtualdisplay;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
public class VirtualDisplayActivity extends Activity {
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ProjectionService projectionService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ProjectionService.LocalBinder binder = (ProjectionService.LocalBinder) service;
            projectionService = binder.getService();
            requestMediaProjection();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            projectionService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        Button createDisplayButton = new Button(this);
        createDisplayButton.setText("Create Virtual Display");
        createDisplayButton.setOnClickListener(v -> startProjectionService());
        layout.addView(createDisplayButton);

        setContentView(layout);

        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }
    private void requestMediaProjection() {
        if (mediaProjectionManager != null) {
            startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION
            );
        }
    }
    private void startProjectionService() {
        Intent serviceIntent = new Intent(this, ProjectionService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        startForegroundService(serviceIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode == RESULT_OK && projectionService != null) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            createVirtualDisplay();
        }
    }

    private void createVirtualDisplay() {
        SurfaceView surfaceView = new SurfaceView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                400,
                300
        );
        params.gravity = Gravity.CENTER;
        surfaceView.setBackgroundColor(Color.BLUE);
        addContentView(surfaceView, params);

        virtualDisplay = mediaProjection.createVirtualDisplay(
                "VirtualDisplay",
                400,
                300,
                getResources().getDisplayMetrics().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                surfaceView.getHolder().getSurface(),
                null,
                null
        );

        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(VirtualDisplayActivity.this, "surface view clicked", Toast.LENGTH_SHORT).show();

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaProjection != null) mediaProjection.stop();
        if (virtualDisplay != null) virtualDisplay.release();
        unbindService(serviceConnection);
    }
}
