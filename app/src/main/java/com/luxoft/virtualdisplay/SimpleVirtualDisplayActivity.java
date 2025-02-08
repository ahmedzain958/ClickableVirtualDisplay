package com.luxoft.virtualdisplay;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SimpleVirtualDisplayActivity extends Activity {
    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private DisplayManager displayManager;
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

        Button interactDisplayButton = new Button(this);
        interactDisplayButton.setText("Start Virtual Display");
        interactDisplayButton.setOnClickListener(v -> {
            if (virtualDisplay != null) {
                handleVirtualDisplayInteraction();
            } else {
                Toast.makeText(SimpleVirtualDisplayActivity.this,
                        "Create virtual display first",
                        Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(interactDisplayButton);

        setContentView(layout);

        displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
    private void handleVirtualDisplayInteraction() {
        // Example interaction - list virtual displays
        Display[] displays = displayManager.getDisplays();
        StringBuilder displayInfo = new StringBuilder();

        for (Display display : displays) {
            displayInfo.append("Display ID: ").append(display.getDisplayId())
                    .append(", Name: ").append(display.getName())
                    .append("\n");
        }

        Toast.makeText(this, displayInfo.toString(), Toast.LENGTH_SHORT).show();
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
                400, // width in pixels
                300  // height in pixels
        );
        surfaceView.setBackgroundColor(Color.BLUE);
        addContentView(surfaceView, params);

        virtualDisplay = mediaProjection.createVirtualDisplay(
                "VirtualDisplay",
                400, // match width
                300, // match height
                getResources().getDisplayMetrics().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                surfaceView.getHolder().getSurface(),
                null,
                null
        );


        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SimpleVirtualDisplayActivity.this, "surface view clicked", Toast.LENGTH_SHORT).show();

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

