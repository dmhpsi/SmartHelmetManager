package com.darkha.smarthelmetmanager;

import android.database.ContentObserver;
import android.os.Handler;

public class AudioContentObserver extends ContentObserver {
    OnAudioChange onAudioChange;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public AudioContentObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        if (onAudioChange != null) {
            onAudioChange.onEvent();
        }
    }

    public void setOnAudioChange(OnAudioChange onAudioChange) {
        this.onAudioChange = onAudioChange;
    }

    interface OnAudioChange {
        void onEvent();
    }
}
