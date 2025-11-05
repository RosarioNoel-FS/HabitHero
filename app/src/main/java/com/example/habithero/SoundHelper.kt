package com.example.habithero

import android.content.Context
import android.media.MediaPlayer

object SoundHelper {

    enum class SoundType {
        COMPLETION, DELETE, CLICK, TAP, DENY, NEXT
    }

    fun playSound(context: Context, soundType: SoundType) {
        val soundResourceId = getSoundResourceId(soundType)
        val mediaPlayer = MediaPlayer.create(context, soundResourceId)
        mediaPlayer.setOnCompletionListener { it.release() }
        mediaPlayer.start()
    }

    private fun getSoundResourceId(soundType: SoundType): Int {
        return when (soundType) {
            SoundType.COMPLETION -> R.raw.completion_sound
            SoundType.DELETE -> R.raw.delete_sound
            SoundType.CLICK -> R.raw.click_sound
            SoundType.TAP -> R.raw.tap_sound
            SoundType.DENY -> R.raw.deny_sound
            SoundType.NEXT -> R.raw.next_sound
        }
    }
}
