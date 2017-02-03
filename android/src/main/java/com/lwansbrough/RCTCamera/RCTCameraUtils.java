package com.lwansbrough.RCTCamera;

import android.content.Context;
import android.media.AudioManager;

import com.facebook.react.bridge.ReactApplicationContext;

import java.util.ArrayList;

public class RCTCameraUtils {
    private static boolean sMuteSystemSoundsForRecordStartStopOngoing = false;
    private static int MUTE_SYSTEM_SOUNDS_FOR_RECORD_START_STOP_DURATION = 1250;

    /**
     * List of system sound stream ids that should be muted for record start/stop, when
     * the play-sound-on-capture React prop is OFF.
     * <p>
     * Note that this list may be subject to change, especially if this doesn't seem to mute for
     * certain devices, as it seems there is variation between devices of which stream is used by
     * the system for the media recorder sound. That said, the number of streams muted should be
     * minimized as much as possible while maximizing device coverage, as we don't want to mute any
     * streams that we don't necessarily need to mute.
     */
    private static int[] MUTE_SYSTEM_SOUND_STREAM_IDS_FOR_RECORD_START_STOP = new int[]{
            AudioManager.STREAM_SYSTEM,
            AudioManager.STREAM_RING,
            AudioManager.STREAM_NOTIFICATION,
    };

    /**
     * Temporarily mute, then unmute, system sounds related to capture record start/stop.
     * <p>
     * Note that this temporary-mute-then-unmute approach seems to work well, while also
     * ensuring that the streams are always unmuted. An alternative would be to call a different
     * function to mute, then record start/stop, then call another function to unmute the streams
     * that were initially muted; this works well in theory, but in practice it seems the system
     * often plays the audio sound asynchronously (not while blocking on media recorder's
     * start/stop), such that the system audio gets played AFTER we've already muted, waited for
     * start/stop, and unmuted again, with the net effect of the sound not getting muted at all.
     * <p>
     * The time that the streams are temporarily muted is controlled by
     * MUTE_SYSTEM_SOUNDS_FOR_RECORD_START_STOP_DURATION. In some basic testing, this seems to be
     * around 1-1.25 seconds for debug settings, and 0.5-1 for release settings. We currently take
     * the upper bound of this range (1.25 seconds), but this may be changed over time if this
     * interacts with other streams too much.
     */
    public static void tempMuteSystemSoundsForRecordStartStop(final ReactApplicationContext reactApplicationContext) {
        // Do nothing if insufficient Android API level. Note that API level 23 is required to
        // properly check if streams are muted via isStreamMute below; without this, we might end up
        // unmuting a stream that we didn't mute in the first place (i.e., muted by system or user
        // beforehand).
        if (android.os.Build.VERSION.SDK_INT < 23) {
            return;
        }

        // If there is already a temp-mute request in progress, do nothing. Otherwise, we might
        // pick up on a mute/unmute that WE did, not the existing setting from the user/system, and
        // would not actually be restoring to the proper state overall. Note that this can happen
        // when we try to start a recording session, but it immediately fails, resulting in two
        // calls to this function almost back-to-back.
        if (sMuteSystemSoundsForRecordStartStopOngoing) {
            return;
        }
        // Otherwise, set that temp-mute request is ongoing now.
        sMuteSystemSoundsForRecordStartStopOngoing = true;

        // Get audio manager.
        final AudioManager audioManager = (AudioManager) reactApplicationContext.getSystemService(Context.AUDIO_SERVICE);

        // Init list of stream ids to unmute later.
        final ArrayList<Integer> mutedStreamIds = new ArrayList<Integer>();

        // Iterate through each stream id we want to mute.
        for (int streamId : MUTE_SYSTEM_SOUND_STREAM_IDS_FOR_RECORD_START_STOP) {
            // If stream is already muted, do nothing.
            if (audioManager.isStreamMute(streamId)) {
                continue;
            }

            // Set stream to muted.
            audioManager.adjustStreamVolume(streamId, AudioManager.ADJUST_MUTE, 0);

            // Add stream id to list of muted streams.
            mutedStreamIds.add(streamId);
        }

        // If empty list of stream ids, then just complete the temp-mute request and exit.
        if (mutedStreamIds.isEmpty()) {
            sMuteSystemSoundsForRecordStartStopOngoing = false;
            return;
        }

        // Otherwise, set handler to be run in the near future to unmute these streams.
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // Iterate through each stream id we want to unmute.
                        for (int streamId : mutedStreamIds) {
                            // Set stream to unmuted.
                            audioManager.adjustStreamVolume(streamId, AudioManager.ADJUST_UNMUTE, 0);
                        }

                        // Set that temp-mute request is completed.
                        sMuteSystemSoundsForRecordStartStopOngoing = false;
                    }
                },
                MUTE_SYSTEM_SOUNDS_FOR_RECORD_START_STOP_DURATION);
    }
}

