package com.luxoft.virtualdisplay.chatGptExample

import android.app.Activity
import android.app.ActivityOptions
import android.app.Presentation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.view.Display
import android.view.LayoutInflater
import android.view.SurfaceView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.luxoft.virtualdisplay.ProjectionService
import com.luxoft.virtualdisplay.ProjectionService.LocalBinder
import com.luxoft.virtualdisplay.R

class ChatGptMainActivity : AppCompatActivity() {
    private lateinit var surfaceView: SurfaceView
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null

    companion object {
        const val REQUEST_CODE = 1001
    }

    private var projectionService: ProjectionService? = null


    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocalBinder
            projectionService = binder.service
            requestScreenCapturePermission()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            projectionService = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_gpt_main)
        surfaceView = findViewById(R.id.surfaceView)
        val btnStartActivity = findViewById<Button>(R.id.btnStartProjection)

        // Initialize MediaProjectionManager
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        btnStartActivity.setOnClickListener {
            startProjectionService()
        }
    }

    private fun requestScreenCapturePermission() {
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(intent, REQUEST_CODE)
    }

    private fun startProjectionService() {
        val serviceIntent = Intent(
            this,
            ProjectionService::class.java
        )
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
        startForegroundService(serviceIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            startVirtualDisplay()
        }
    }

    private fun startVirtualDisplay() {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ChatGptVirtualDisplay",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surfaceView.holder.surface,
            null,
            null
        )
        virtualDisplay?.display?.let { display ->
            showSecondActivityOnVirtualDisplay(display)
//            virtualDisplay?.let { startNavigationActivity(it) }

        }
    }


    private fun startNavigationActivity(virtualDisplay: VirtualDisplay) {
        try {
            // Create the intent for the second activity
            val intent = Intent()
            val componentName = ComponentName(
                "com.luxoft.virtualdisplay.chatGptExample",
                "com.luxoft.virtualdisplay.chatGptExample.ChatGptSecondActivity"
            )
            intent.component = componentName

            // Configure activity options to set the virtual display
            val options = ActivityOptions.makeBasic()
                .setLaunchDisplayId(virtualDisplay.display.displayId)

            // Start the second activity
            startActivity(intent, options.toBundle())
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun showSecondActivityOnVirtualDisplay(display: Display) {
        val presentation = ChatGptPresentation(this, display)
        presentation.show()
    }

    private class ChatGptPresentation(
        context: Context,
        display: Display
    ) : Presentation(context, display) {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // Inflate the layout for the second activity
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.activity_chat_gpt_second, null)
            // Set the content view of the Presentation
            setContentView(view)
            view.findViewById<Button>(R.id.btnClickMe).setOnClickListener {
                Toast.makeText(
                    context, "SurfaceView touched!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        releaseResources()
        unbindService(serviceConnection)
    }

    private fun releaseResources() {
        virtualDisplay?.release()
        mediaProjection?.stop()
    }
}