package com.texasgamer.zephyr.manager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.texasgamer.zephyr.Constants;
import com.texasgamer.zephyr.R;

public class ConfigManager {

    private final String TAG = this.getClass().getSimpleName();
    private final int CACHE_EXPIRATION_IN_SECONDS = 3600;

    public ConfigManager(Context context) {

    }

    public boolean isLoginEnabled() {
        return getBoolean("loginEnabled", Constants.FIREBASE_LOGIN_ENABLED);
    }

    public boolean isLoginCardNew() {
        return getBoolean("loginCardNew", false);
    }

    public boolean shouldShowLoginSuccessDialog() {
        return getBoolean("loginSuccessDialog", true);
    }

    private boolean getBoolean(String key, boolean fallback) {
        boolean result;
        result = fallback;

        Log.v(TAG, key + " --> " + result + " (" + fallback + ")");
        return result;
    }

}
