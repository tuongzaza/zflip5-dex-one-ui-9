package dev.opencover.phone

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import dev.opencover.COVER_DISPLAY
import dev.opencover.quick.ShizukuShell
import dev.opencover.watcher.CoverAccessibility
import java.util.concurrent.Executors

/**
 * Holds a common direction on the cover, or restores each app's saved direction when it comes forward.
 */
object RotationFeature {
    private const val RESET_HOLD_MS = 1_500L

    private val handler = Handler(Looper.getMainLooper())
    private val shell = Executors.newSingleThreadExecutor { Thread(it, "rotation-shell").apply { isDaemon = true } }

    /** Last app whose window came to the cover (accessibility thread). */
    @Volatile private var foreground: String? = null
    private var sensorTurns: Int? = null

    /** An app's window came to the cover: pin its saved rotation. */
    fun onWindow(ctx: Context, pkg: String) {
        if (Desktop.connected(ctx)) return upright(ctx) // per-app rotation and density would follow the app onto the monitor
        foreground = pkg
        val prefs = PhonePrefs(ctx)
        pin(ctx, commonTurns(ctx, prefs) ?: prefs.turnsFor(pkg))
        AppDensity.onWindow(ctx, pkg)
    }

    /** OpenCover or Samsung's cover home is in front. */
    fun upright(ctx: Context) {
        foreground = null
        pin(ctx, commonTurns(ctx, PhonePrefs(ctx)) ?: 0)
        AppDensity.onWindow(ctx, null)
    }

    /** Apply a changed setting immediately, including when OpenCover itself is visible. */
    fun applyCoverRotation(ctx: Context) {
        val prefs = PhonePrefs(ctx)
        val turns = commonTurns(ctx, prefs) ?: foreground?.let(prefs::turnsFor) ?: 0
        run(holdRotation(turns))
    }

    fun onOrientation(degrees: Int) {
        val turns = sensedCoverTurns(degrees, sensorTurns) ?: return
        if (turns == sensorTurns) return
        sensorTurns = turns
        run(holdRotation(turns))
    }

    fun clearOrientation() { sensorTurns = null }

    fun toggleAuto(ctx: Context): String? {
        val service = CoverAccessibility.instance ?: return "Enable OpenCover in Accessibility"
        handler.post {
            val prefs = PhonePrefs(ctx)
            if (prefs.coverAutoRotation) prefs.coverTurns = displayTurns(ctx)
            prefs.coverAutoRotation = !prefs.coverAutoRotation
            service.refreshScope()
            service.syncAutoRotation()
            service.updateNavBar()
            applyCoverRotation(ctx)
        }
        return null
    }

    /**
     * Puts the cover back at rotation 0, then at what the app on it wants. A mirror session leaves the cover at 180 and
     * nothing turns it back. Stays pending until it has run, so it also happens when the accessibility service only
     * returns after the session ended.
     */
    fun resetToDefault(ctx: Context) {
        val app = ctx.applicationContext
        PhonePrefs(app).rotationResetPending = true
        run(holdRotation(commonTurns(app, PhonePrefs(app)) ?: 0))
        handler.postDelayed({
            PhonePrefs(app).rotationResetPending = false
            foreground?.let { onWindow(app, it) }
        }, RESET_HOLD_MS)
    }

    private fun commonTurns(ctx: Context, prefs: PhonePrefs): Int? =
        if (prefs.coverAutoRotation) sensorTurns ?: displayTurns(ctx) else prefs.coverTurns

    private fun displayTurns(ctx: Context): Int =
        ctx.getSystemService(DisplayManager::class.java).getDisplay(COVER_DISPLAY)?.rotation ?: 0

    private fun pin(ctx: Context, turns: Int) {
        if (turns != displayTurns(ctx)) run(holdRotation(turns))
    }

    private fun run(script: String) { shell.execute { ShizukuShell.run("sh", "-c", script) } }
}
