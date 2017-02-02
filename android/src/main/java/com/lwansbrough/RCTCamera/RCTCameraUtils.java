package com.lwansbrough.RCTCamera;

import android.content.Context;
import android.media.AudioManager;

import com.facebook.react.bridge.ReactApplicationContext;

import java.util.ArrayList;

public class RCTCameraUtils {

    /**
     * List of system sound stream ids that should be muted for record start/stop, when
     * the play-sound-on-capture React prop is OFF.
     *
     * Note that this list may be subject to change, especially if this doesn't seem to mute for
     * certain devices, as it seems there is variation between devices of which stream is used by
     * the system for the media recorder sound. That said, the number of streams muted should be
     * minimized as much as possible while maximizing device coverage, as we don't want to mute any
     * streams that we don't necessarily need to mute.
     */
    private static int[] SYSTEM_SOUND_STREAM_IDS_FOR_MUTE_RECORD_START_STOP = new int[]{
            AudioManager.STREAM_SYSTEM,
            AudioManager.STREAM_RING,
            AudioManager.STREAM_NOTIFICATION,
    };

    /**
     * Mutes system sounds pertaining to the capture record start/stop sound.
     *
     * This should typically be called immediately before starting/stopping a media recorder capture
     * session to disable the start/stop sound if desired. This mutes the appropriate streams, then
     * returns a list of StreamIdRestoreVolume objects that can be passed in to
     * restoreSystemSoundsAfterRecordStartStop to restore the system streams to their previous
     * volumes. Note that extra care should be taken to ALWAYS call the restore function; otherwise,
     * we will be mangling system sound settings and not returning them back to the way they were!
     */
    public static ArrayList<StreamIdRestoreVolume> saveAndMuteSystemSoundsForRecordStartStop(final ReactApplicationContext reactApplicationContext) {
        // Get audio manager from context.
        final AudioManager audioManager = (AudioManager) reactApplicationContext.getSystemService(Context.AUDIO_SERVICE);

        // Init list of StreamIdRestoreVolume objects for restoring later.
        final ArrayList<StreamIdRestoreVolume> streamIdRestoreVolumes = new ArrayList<StreamIdRestoreVolume>();

        // Iterate through each stream id we want to mute.
        for (int streamId : SYSTEM_SOUND_STREAM_IDS_FOR_MUTE_RECORD_START_STOP) {
            // Get existing stream volume that we will restore to later.
            final int restoreVolume = audioManager.getStreamVolume(streamId);
            // Add this stream id and its restoreVolume to the list of restore items.
            streamIdRestoreVolumes.add(new StreamIdRestoreVolume(streamId, restoreVolume));

            // Set stream volume to zero/muted. Note that setStreamMute is deprecated as of API
            // level 23.
            if (android.os.Build.VERSION.SDK_INT < 23) {
                audioManager.setStreamMute(streamId, true);
            } else {
                audioManager.adjustStreamVolume(streamId, AudioManager.ADJUST_MUTE, 0);
            }
        }

        // Return list of StreamIdToRestoreVolume objects.
        return streamIdRestoreVolumes;
    }

    /**
     * Restore the system sound streams to their previous volumes.
     *
     * This should typically be called immediately after starting/stopping a media recorder capture
     * session to disable the start/stop sound if desired. The previous volumes to restore to should
     * be passed in, typically derived from saveAndMuteSystemSoundsForRecordStartStop.
     */
    public static void restoreSystemSoundsAfterRecordStartStop(final ArrayList<StreamIdRestoreVolume> streamIdRestoreVolumes, final ReactApplicationContext reactApplicationContext) {
        // If list is empty, do nothing.
        if (streamIdRestoreVolumes == null || streamIdRestoreVolumes.size() == 0) {
            return;
        }

        // Get audio manager from context.
        final AudioManager audioManager = (AudioManager) reactApplicationContext.getSystemService(Context.AUDIO_SERVICE);

        // Restore stream volume by id.
        for (StreamIdRestoreVolume streamIdRestoreVolume : streamIdRestoreVolumes) {
            audioManager.setStreamVolume(streamIdRestoreVolume.streamId, streamIdRestoreVolume.restoreVolume, 0);
        }
    }

    /**
     * Simple helper class to store a stream id and the volume it should be restored to.
     *
     * Useful for tracking volumes to restore to when muting audio streams around capture record
     * start/stops.
     */
    public static class StreamIdRestoreVolume {
        int streamId;
        int restoreVolume;

        public StreamIdRestoreVolume(final int streamId, final int restoreVolume) {
            this.streamId = streamId;
            this.restoreVolume = restoreVolume;
        }
    }
}
