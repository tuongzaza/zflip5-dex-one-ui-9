package dev.opencover.desk

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

/** Opens the phone's mode picker from Android Quick Settings, never on the HDMI desktop. */
class PhoneModeTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            label = "Chế độ điện thoại"
            val touchpad = Desk.running && Desk.phoneTouchpad(this@PhoneModeTileService)
            state = if (touchpad) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            subtitle = when {
                !Desk.running -> "Chưa kết nối màn hình"
                touchpad -> "Bàn di chuột"
                else -> "Điện thoại"
            }
            contentDescription = "$label: $subtitle"
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        unlockAndRun {
            val intent = Intent(this, PhoneModeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val options = ActivityOptions.makeBasic().setLaunchDisplayId(PhoneModeActivity.phoneDisplay(this))
            val pending = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE, options.toBundle())
            startActivityAndCollapse(pending)
        }
    }
}
