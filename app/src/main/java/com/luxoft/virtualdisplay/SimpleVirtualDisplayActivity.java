package com.luxoft.virtualdisplay;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

public class SimpleVirtualDisplayActivity extends Activity {

    private VirtualDisplay virtualDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // إنشاء SurfaceView لعرض محتوى افتراضي
        SurfaceView surfaceView = new SurfaceView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                400, // العرض
                300  // الطول
        );
        surfaceView.setLayoutParams(params);
        setContentView(surfaceView);

        // إنشاء Virtual Display باستخدام DisplayManager
        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        virtualDisplay = displayManager.createVirtualDisplay(
                "SimpleVirtualDisplay",
                400,
                300,
                metrics.densityDpi,
                surfaceView.getHolder().getSurface(),
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
        );


        // إضافة تفاعل (Clickable) على العرض
        surfaceView.setOnClickListener(v -> {
            Toast.makeText(SimpleVirtualDisplayActivity.this, "Virtual Display Clicked", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
    }
}
