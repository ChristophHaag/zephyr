package com.texasgamer.zephyr.manager;

import android.content.Context;
import android.preference.PreferenceManager;

import com.texasgamer.zephyr.R;

public class LoginManager {

    private Context mContext;
    private ConfigManager mConfigManager;

    public LoginManager(Context context) {
        mContext = context;
        mConfigManager = new ConfigManager(context);
    }

    public boolean shouldShowLoginCard() {
        return !isLoginCardHidden() && !isLoggedIn();
    }

    public boolean isLoggedIn() {
        if (!mConfigManager.isLoginEnabled()) {
            return false;
        }
        return true; //TODO: whatever
    }

    public boolean isLoginCardHidden() {
        if (!mConfigManager.isLoginEnabled()) {
            return true;
        }

        return PreferenceManager.getDefaultSharedPreferences(mContext)
                .getBoolean(mContext.getString(R.string.pref_hide_login_card), false);
    }

    public void setLoginCardHidden(boolean hideLoginCard) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putBoolean(mContext.getString(R.string.pref_hide_login_card), hideLoginCard)
                .apply();
    }
}
