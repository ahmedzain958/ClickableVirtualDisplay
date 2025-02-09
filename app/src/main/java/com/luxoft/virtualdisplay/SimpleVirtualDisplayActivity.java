package com.luxoft.virtualdisplay;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class SimpleVirtualDisplayActivity extends Activity {

    private VirtualDisplay virtualDisplay;
    private SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                createVirtualDisplay(holder.getSurface());
                Toast.makeText(SimpleVirtualDisplayActivity.this, "Surface Created", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                if (virtualDisplay != null) {
                    virtualDisplay.release();
                }
            }
        });

        surfaceView.setOnTouchListener((v, event) -> {
            Toast.makeText(SimpleVirtualDisplayActivity.this, "SurfaceView touched!",
                    Toast.LENGTH_SHORT).show();
            return false;
        });
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SimpleVirtualDisplayActivity.this, "SurfaceView clicked!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        virtualDisplay.release();
    }

    private void createVirtualDisplay(Surface surface) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int density = metrics.densityDpi;

        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        virtualDisplay = displayManager.createVirtualDisplay(
                "VirtualDisplay",
                width,
                height,
                density,
                surface,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
    }
}

