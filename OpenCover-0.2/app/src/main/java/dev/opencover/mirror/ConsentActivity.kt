package dev.opencover.mirror

import android.app.AlertDialog
import android.hardware.display.DisplayManager
import android.view.Display
import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Toast
import dev.opencover.quick.ShizukuShell

/**
 * Translucent activity that asks for the system screen-capture consent.
 * On accept: starts MirrorService then opens MirrorActivity on the cover.
 */
class ConsentActivity : Activity() {

    private var sourceDisplayId = Display.DEFAULT_DISPLAY
    private var requestingConsent = false

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("capture_requested", requestingConsent)
        outState.putInt(MirrorService.KEY_SOURCE_DISPLAY, sourceDisplayId)
        super.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sourceDisplayId = savedInstanceState?.getInt(MirrorService.KEY_SOURCE_DISPLAY) ?: Display.DEFAULT_DISPLAY
        requestingConsent = savedInstanceState?.getBoolean("capture_requested") == true
        if (requestingConsent) return
        val dm = getSystemService(DisplayManager::class.java)
        val sources = mirrorSources(dm.displays.map { MirrorSource(it.displayId, it.name, (it.flags and Display.FLAG_PRESENTATION) == 0) })
        AlertDialog.Builder(this)
            .setTitle("Chọn màn hình để mirror")
            .setItems(sources.map {
                if (it.id == Display.DEFAULT_DISPLAY) "Màn hình trong"
                else "Desktop: ${it.name} (ID ${it.id})"
            }.toTypedArray()) { _, index ->
                sourceDisplayId = sources[index].id
                if (sourceDisplayId != Display.DEFAULT_DISPLAY && !ShizukuShell.allowed()) {
                    Toast.makeText(this, "Cần cấp quyền Shizuku để mirror Desktop", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val mpm = getSystemService(MediaProjectionManager::class.java)
                    requestingConsent = true
                    startActivityForResult(mpm.createScreenCaptureIntent(), RC)
                }
            }
            .setOnCancelListener { finish() }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != RC || resultCode != RESULT_OK || data == null) { finish(); return }

        val serviceIntent = Intent(this, MirrorService::class.java)
            .putExtra(MirrorService.KEY_SOURCE_DISPLAY, sourceDisplayId)
            .putExtra(MirrorService.KEY_RESULT_CODE, resultCode)
            .putExtra(MirrorService.KEY_DATA, data)
        startForegroundService(serviceIntent)
        finish()
    }

    companion object { private const val RC = 1 }
}
