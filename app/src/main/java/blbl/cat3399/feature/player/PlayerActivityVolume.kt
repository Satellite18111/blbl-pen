package blbl.cat3399.feature.player

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.ImageButton
import blbl.cat3399.R
import kotlin.math.roundToInt

/**
 * [pen-build] OSD volume buttons: the e-reader pen has no hardware volume keys, so the player
 * exposes "+/-" steps on the control row. Tap = one media step, hold = repeat.
 */
internal fun PlayerActivity.adjustMediaVolume(deltaSteps: Int) {
    val manager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
    val max = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
    val current = manager.getStreamVolume(AudioManager.STREAM_MUSIC)
    val target = (current + deltaSteps).coerceIn(0, max)
    if (target != current) {
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
    }
    val percent = (target * 100f / max.toFloat()).roundToInt().coerceIn(0, 100)
    showSeekHint(getString(R.string.player_touch_volume_fmt, percent), hold = false)
    noteUserInteraction()
}

internal fun PlayerActivity.installVolumeButton(button: ImageButton, deltaSteps: Int) {
    val handler = Handler(Looper.getMainLooper())
    var holding = false
    val repeat =
        object : Runnable {
            override fun run() {
                if (!holding) return
                adjustMediaVolume(deltaSteps)
                handler.postDelayed(this, 120L)
            }
        }

    button.setOnClickListener {
        adjustMediaVolume(deltaSteps)
        setControlsVisible(true)
    }
    button.setOnLongClickListener {
        holding = true
        handler.postDelayed(repeat, 350L)
        true
    }
    button.setOnTouchListener { _, event ->
        if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            holding = false
            handler.removeCallbacks(repeat)
        }
        false
    }
}
