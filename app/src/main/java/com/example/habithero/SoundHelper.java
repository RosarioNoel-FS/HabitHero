package com.example.habithero;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundHelper {

    // Enum to define different sound types
    public enum SoundType {
        COMPLETION, DELETE, CLICK, TAP, DENY, NEXT
    }

    // Method to play different sounds based on the type
    public static void playSound(Context context, SoundType soundType) {
        int soundResourceId = getSoundResourceId(soundType);
        MediaPlayer mediaPlayer = MediaPlayer.create(context, soundResourceId);
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mediaPlayer.start();
    }

    // Method to get the resource ID of the sound based on the type
    private static int getSoundResourceId(SoundType soundType) {
        switch (soundType) {
            case COMPLETION:
                return R.raw.completion_sound;
            case DELETE:
                return R.raw.delete_sound;
            case CLICK:
                return R.raw.click_sound;
            case TAP:
                return R.raw.tap_sound;
            case DENY:
                return R.raw.deny_sound;
            case NEXT:
                return R.raw.next_sound;
            default:
                throw new IllegalArgumentException("Unknown SoundType: " + soundType);
        }
    }
}
