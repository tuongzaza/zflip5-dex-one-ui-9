package dev.opencover.mirror

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.ComponentName
import android.content.ServiceConnection
import rikka.shizuku.Shizuku
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import dev.opencover.apps.AppLauncher
import dev.opencover.quick.ShizukuShell
import dev.opencover.phone.HeatGuard
import android.view.Display
import android.view.Surface
import android.widget.Toast

/**
 * Foreground service owning the MediaProjection and VirtualDisplay.
 * Started by ConsentActivity after the user accepts the system consent dialog.
 */
class MirrorService : Service() {

    private var projection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    var captureWidth = 0; private set
    var captureHeight = 0; private set
    private var captureDpi = 0
    var sourceDisplayId = Display.DEFAULT_DISPLAY; private set
    private var enteredInnerEnvironment = false
    private var selectingSource = false
    private var desktopSourceReady = false
    private var pendingSurface: Surface? = null
    private var projectionToken: IBinder? = null
    private var sourceConnection: ServiceConnection? = null
    private val sourceArgs by lazy {
        Shizuku.UserServiceArgs(ComponentName(packageName, DesktopMirrorSource::class.java.name))
            .daemon(false).processNameSuffix("mirror-source").version(3)
    }
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stop(); return START_NOT_STICKY }
        if (intent == null) { stopSelf(); return START_NOT_STICKY }
        if (instance != null) return START_NOT_STICKY
        sourceDisplayId = intent.getIntExtra(KEY_SOURCE_DISPLAY, Display.DEFAULT_DISPLAY)
        val dm = getSystemService(DisplayManager::class.java)
        val source = dm.getDisplay(sourceDisplayId)
        if (source == null || selectedMirrorSource(dm.displays.map {
                MirrorSource(it.displayId, it.name, (it.flags and Display.FLAG_PRESENTATION) == 0)
            }, sourceDisplayId) == null || (sourceDisplayId != Display.DEFAULT_DISPLAY && !ShizukuShell.allowed())) {
            Toast.makeText(this, "Màn hình đã đóng hoặc chưa có quyền Shizuku", Toast.LENGTH_LONG).show()
            stopSelf()
            return START_NOT_STICKY
        }
        val resultCode = intent.getIntExtra(KEY_RESULT_CODE, 0)
        val data: Intent = intent.getParcelableExtra(KEY_DATA, Intent::class.java) ?: run { stopSelf(); return START_NOT_STICKY }

        ensureChannel()
        val stopIntent = PendingIntent.getService(
            this, 0, Intent(this, MirrorService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle(if (sourceDisplayId == Display.DEFAULT_DISPLAY) "Mirroring inner screen" else "Mirroring Desktop: ${source.name}")
            .addAction(Notification.Action.Builder(null, "Stop", stopIntent).build())
            .setOngoing(true)
            .build()
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)

        if (HeatGuard.hot()) { // checked before anything changes: stopping mid-start would leave the phone forced open
            Toast.makeText(this, "Phone is too hot to mirror. Let it cool.", Toast.LENGTH_LONG).show()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        readMetrics(source)
        instance = this
        HeatGuard.evaluate() // mirroring counts as a heat source

        if (sourceDisplayId != Display.DEFAULT_DISPLAY) {
            acquireProjection(resultCode, data)
            return START_NOT_STICKY
        }
        enteredInnerEnvironment = true
        // Folded, display 0 is off and renders nothing, so MediaProjection would capture the cover
        // (infinite mirror). Z Cover Power forces the unfolded device state first; so do we.
        val ready = Runnable { acquireProjection(resultCode, data) }
        val listener = object : DisplayManager.DisplayListener {
            var fired = false
            override fun onDisplayChanged(displayId: Int) {
                if (displayId != Display.DEFAULT_DISPLAY || fired) return
                fired = true
                dm.unregisterDisplayListener(this)
                handler.removeCallbacks(ready)
                handler.postDelayed(ready, 150)
            }
            override fun onDisplayAdded(displayId: Int) {}
            override fun onDisplayRemoved(displayId: Int) {}
        }
        dm.registerDisplayListener(listener, handler)
        // ponytail: 1.5 s fallback copied from Z Cover Power in case display 0 never reports a change.
        handler.postDelayed({ if (!listener.fired) { listener.fired = true; dm.unregisterDisplayListener(listener); ready.run() } }, 1500)
        val app = applicationContext
        MirrorEnv.later {
            Log.d(TAG, "mirror env -> ${MirrorEnv.enter(app)?.trim() ?: "skipped"}")
            // Putting the phone's home on the inner screen also brings Samsung's cover home up over the mirror.
            handler.post { if (instance === this && mirrorShown) showMirror(clear = false) }
        }
        return START_NOT_STICKY
    }

    private fun readMetrics(d: Display) {
        val m = DisplayMetrics()
        d.getRealMetrics(m)
        if (m.widthPixels > 0 && m.heightPixels > 0) {
            captureWidth = m.widthPixels; captureHeight = m.heightPixels; captureDpi = m.densityDpi
        }
    }

    private fun acquireProjection(resultCode: Int, data: Intent) {
        if (instance == null) return
        getSystemService(DisplayManager::class.java).getDisplay(sourceDisplayId)?.let(::readMetrics)
        val mpm = getSystemService(MediaProjectionManager::class.java)
        projectionToken = data.extras?.getBinder("android.media.projection.extra.EXTRA_MEDIA_PROJECTION")
        projection = try { mpm.getMediaProjection(resultCode, data) } catch (_: Exception) { null }
        val p = projection ?: run { stop(); return }
        p.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() { if (instance === this@MirrorService) this@MirrorService.stop() }
        }, handler)
        Log.d(TAG, "projection ready ${captureWidth}x$captureHeight")
        // Launch only now: the device-state switch moves whatever is on the cover to display 0.
        showMirror(clear = true)
        mirrorShown = true
        MirrorInput.start(this, sourceDisplayId) // live touch on the selected source
        LiveGesture.displayId = sourceDisplayId
        innerWatch.wasOn = getSystemService(DisplayManager::class.java).getDisplay(Display.DEFAULT_DISPLAY)?.state == Display.STATE_ON
        getSystemService(DisplayManager::class.java).registerDisplayListener(innerWatch, handler)
        val app = applicationContext
        if (enteredInnerEnvironment) MirrorEnv.later { MirrorEnv.hideAccessibility(app) }
    }

    /** The mirror screen has been opened for this session (so bringing it back to the front is safe). */
    private var mirrorShown = false

    /** Opens the mirror screen on the cover; [clear] starts it fresh, otherwise it just comes back to the front. */
    private fun showMirror(clear: Boolean) {
        val i = Intent(this, MirrorActivity::class.java)
        AppLauncher.start(this, if (clear) i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) else i)
    }

    fun setSurface(s: Surface?) {
        pendingSurface = s
        if (s != null && s.isValid) {
            val p = projection ?: return
            if (virtualDisplay == null) {
                virtualDisplay = p.createVirtualDisplay(
                    "OpenCover-mirror", captureWidth, captureHeight, captureDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    if (sourceDisplayId == Display.DEFAULT_DISPLAY) s else null, null, null,
                )
                if (sourceDisplayId != Display.DEFAULT_DISPLAY) selectDesktopSource(virtualDisplay!!.display.displayId)
            } else {
                virtualDisplay?.surface = if (sourceDisplayId == Display.DEFAULT_DISPLAY || desktopSourceReady) s else null
            }
        } else {
            virtualDisplay?.surface = null
        }
    }

    private fun selectDesktopSource(destination: Int) {
        if (selectingSource) return
        selectingSource = true
        val source = sourceDisplayId
        val token = projectionToken
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                MirrorEnv.later {
                    val ok = runCatching { IDesktopMirror.Stub.asInterface(binder).selectSource(source, destination, token) }.getOrDefault(false)
                    handler.post {
                        selectingSource = false
                        if (instance === this@MirrorService) {
                            if (ok) {
                                desktopSourceReady = true
                                virtualDisplay?.surface = pendingSurface?.takeIf { it.isValid }
                            } else sourceFailed()
                        }
                    }
                }
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                if (instance === this@MirrorService) sourceFailed()
            }
        }
        sourceConnection = connection
        runCatching { Shizuku.bindUserService(sourceArgs, connection) }.onFailure { sourceFailed() }
        handler.postDelayed({ if (instance === this && selectingSource) sourceFailed() }, 10_000)
    }

    private fun sourceFailed() {
        Toast.makeText(this, "Không chuyển được nguồn Desktop trên thiết bị này", Toast.LENGTH_LONG).show()
        stop()
    }

    /**
     * The system can drop the forced-open state by itself (it does when its requester isn't on top), which turns the
     * inner screen off: the session is then over, so end it as the stop button would rather than mirror nothing.
     */
    private val innerWatch = object : DisplayManager.DisplayListener {
        var wasOn = false
        override fun onDisplayChanged(id: Int) {
            if (id != sourceDisplayId || instance !== this@MirrorService) return
            if (sourceDisplayId != Display.DEFAULT_DISPLAY) return
            val on = getSystemService(DisplayManager::class.java).getDisplay(id)?.state == Display.STATE_ON
            if (on) wasOn = true
            else if (wasOn) { Log.i(TAG, "inner screen turned off: ending the session"); stop() }
        }
        override fun onDisplayAdded(id: Int) {}
        override fun onDisplayRemoved(id: Int) {
            if (id == sourceDisplayId && instance === this@MirrorService) stop()
        }
    }

    fun stop() {
        runCatching { getSystemService(DisplayManager::class.java).unregisterDisplayListener(innerWatch) }
        sourceConnection?.let { runCatching { Shizuku.unbindUserService(sourceArgs, it, true) } }
        sourceConnection = null
        MirrorInput.stop()
        if (instance === this && enteredInnerEnvironment) MirrorEnv.leave(this)
        enteredInnerEnvironment = false
        instance = null
        HeatGuard.evaluate()
        runCatching { virtualDisplay?.release() }
        runCatching { projection?.stop() }
        virtualDisplay = null
        projection = null
        pendingSurface = null
        projectionToken = null
        desktopSourceReady = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stop()
        super.onDestroy()
    }

    private fun ensureChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Mirroring", NotificationManager.IMPORTANCE_LOW)
                    .apply { description = "Shown while the inner screen is mirrored on the cover" },
            )
        }
    }

    companion object {
        const val KEY_SOURCE_DISPLAY = "source_display"
        const val KEY_RESULT_CODE = "rc"
        const val KEY_DATA = "data"
        const val ACTION_STOP = "stop"
        private const val TAG = "OpenCoverMirror"
        private const val CHANNEL_ID = "mirror"
        private const val NOTIFICATION_ID = 4 // 1 is CoverWatcher's

        @Volatile var instance: MirrorService? = null
            private set
    }
}
