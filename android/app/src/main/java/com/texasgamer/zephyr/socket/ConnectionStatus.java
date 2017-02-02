package com.texasgamer.zephyr.socket;

import android.support.annotation.StringRes;

import com.texasgamer.zephyr.R;

public enum ConnectionStatus {

    DISCONNECTED(R.string.connection_status_disconnected),

    CONNECTED(R.string.connection_status_connected),

    DISCONNECTING(R.string.connection_status_disconnecting),

    CONNECTING(R.string.connection_status_connecting),

    ERROR(R.string.connection_status_error);

    private int mMessage;

    ConnectionStatus(@StringRes int message) {
        mMessage = message;
    }

    public int getMessage() {
        return mMessage;
    }
}
