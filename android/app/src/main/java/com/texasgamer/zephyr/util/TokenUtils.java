package com.texasgamer.zephyr.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.texasgamer.zephyr.BuildConfig;
import com.texasgamer.zephyr.Constants;
import com.texasgamer.zephyr.R;
import com.texasgamer.zephyr.model.Token;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TokenUtils {

    private final String TAG = this.getClass().getSimpleName();
    private static TokenUtils instance;

    private Token mToken;
    private long mLastCheck;

    public static TokenUtils getInstance(Context context) {
        if (instance == null) {
            instance = new TokenUtils(context);
        }

        return instance;
    }

    private TokenUtils(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        mLastCheck = sharedPrefs.getLong(context.getString(R.string.token_last_checked_key), 0);

        String token = sharedPrefs.getString(context.getString(R.string.token_key), "");
        mToken = jsonToToken(token);

        updateTokenFromServer(context);
    }

    public void saveToken(Context context, Token token) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(context.getString(R.string.token_key), token == null ? "" : token.toString())
                .apply();

        mToken = token;

        updateTokenFromServer(context);
    }

    public void destroyToken(Context context) {
        saveToken(context, null);
        mToken = null;
        mLastCheck = 0;
    }

    public boolean doesTokenExist() {
        return mToken != null;
    }

    private void updateTokenFromServer(final Context context) {
        if (!shouldCheckToken()) {
            return;
        }

        Log.i(TAG, "Updating token from server...");

        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("token", getToken());
            bodyJson.put("device", PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(context.getString(R.string.pref_device_name), context.getString(R.string.pref_default_device_name)));

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyJson.toString());
            Request request = new Request.Builder()
                    .header("Authorization", BuildConfig.ZEPHYR_API_KEY)
                    .url(Constants.ZEPHYR_BASE_WEB_URL + "/api/v2/2/verify")
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject responseBody = new JSONObject(response.body().string());
                        if (responseBody.getBoolean("valid")) {
                            Log.i(TAG, "Token valid.");
                            JSONObject user = responseBody.getJSONObject("user");
                            mToken.updateUser(user.getString("name"), user.getString("avatar"));
                            mLastCheck = System.currentTimeMillis();
                            PreferenceManager.getDefaultSharedPreferences(context).edit()
                                    .putLong(context.getString(R.string.token_last_checked_key), mLastCheck)
                                    .apply();

                            saveToken(context, mToken);
                        } else {
                            Log.i(TAG, "Token is invalid! Destroying...");
                            destroyToken(context);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i(TAG, e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAuthToken(final Context context) {
        Log.i(TAG, "Getting auth token from server...");

        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("token", getToken());
            bodyJson.put("device", PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(context.getString(R.string.pref_device_name), context.getString(R.string.pref_default_device_name)));

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyJson.toString());
            Request request = new Request.Builder()
                    .header("Authorization", BuildConfig.ZEPHYR_API_KEY)
                    .url(Constants.ZEPHYR_BASE_WEB_URL + "/api/v2/2/verify")
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject responseBody = new JSONObject(response.body().string());
            if (responseBody.getBoolean("valid")) {
                Log.i(TAG, "Token valid. Returning auth token.");
                JSONObject user = responseBody.getJSONObject("user");
                mToken.updateUser(user.getString("name"), user.getString("avatar"));
                mLastCheck = System.currentTimeMillis();
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putLong(context.getString(R.string.token_last_checked_key), mLastCheck)
                        .apply();

                saveToken(context, mToken);
                return responseBody.getString("jwtToken");
            } else {
                Log.i(TAG, "Token is invalid! Destroying...");
                destroyToken(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean shouldCheckToken() {
        return doesTokenExist() && System.currentTimeMillis() - mLastCheck > Constants.ZEPHYR_TOKEN_CHECK_TIME;
    }

    public String getName() {
        if (doesTokenExist()) {
            return mToken.getName();
        } else {
            return null;
        }
    }

    public String getAvatar() {
        if (doesTokenExist()) {
            return mToken.getAvatar();
        } else {
            return null;
        }
    }

    public String getRoom() {
        if (doesTokenExist()) {
            return mToken.getRoom();
        } else {
            return null;
        }
    }

    public String getToken() {
        if (doesTokenExist()) {
            return mToken.getToken();
        } else {
            return null;
        }
    }

    private Token jsonToToken(String tokenString) {
        if (tokenString.isEmpty()) {
            return null;
        } else {
            try {
                JSONObject tokenObj = new JSONObject(tokenString);
                return new Token(tokenObj.getString("name"), tokenObj.getString("avatar"),
                        tokenObj.getString("room"), tokenObj.getString("token"));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
