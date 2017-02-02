package com.texasgamer.zephyr.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.texasgamer.zephyr.manager.MetricsManager;
import com.texasgamer.zephyr.R;
import com.texasgamer.zephyr.manager.StatusNotificationManager;
import com.texasgamer.zephyr.socket.ConnectionStatus;
import com.texasgamer.zephyr.util.TokenUtils;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketService extends Service {

    private final String TAG = this.getClass().getSimpleName();

    private SocketServiceReceiver serviceReceiver;
    private MetricsManager mMetricsManager;
    private TokenUtils mTokenUtils;

    private Socket socket;
    private String serverAddr;
    private boolean connected = false;

    @Override
    public void onCreate() {
        super.onCreate();
        if(!connected) {
            serviceReceiver = new SocketServiceReceiver();
            mMetricsManager = new MetricsManager(this);
            mTokenUtils = TokenUtils.getInstance(this);

            IntentFilter filter = new IntentFilter();
            filter.addAction("com.texasgamer.zephyr.SOCKET_SERVICE");
            registerReceiver(serviceReceiver, filter);

            serverAddr = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_last_addr), "");

            StatusNotificationManager.showNotification(this, ConnectionStatus.DISCONNECTED);
        } else {
            Log.w(TAG, "onCreate() called while already connected!");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(serviceReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void connect(String address) {
        if(connected) {
            Log.i(TAG, "Already connected to a server! Disconnect first.");
            return;
        }

        if (!mTokenUtils.doesTokenExist()) {
            Log.i(TAG, "Not logged in! Login before connecting.");
            return;
        }

        Log.i(TAG, "Connecting to " + address + "...");

        serverAddr = address;

        Bundle b = new Bundle();
        b.putString(getString(R.string.analytics_param_server_addr), serverAddr);
        mMetricsManager.logEvent(R.string.analytics_event_connect, b);

        try {
            socket = IO.socket("http://" + address + "/");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(socket != null) {
            setUpEvents();
            socket.connect();
            StatusNotificationManager.showNotification(this, ConnectionStatus.CONNECTING);
        }
    }

    private void disconnect() {
        Log.i(TAG, "Disconnecting...");

        StatusNotificationManager.showNotification(this, ConnectionStatus.DISCONNECTING);

        Bundle b = new Bundle();
        b.putString(getString(R.string.analytics_param_server_addr), serverAddr);
        mMetricsManager.logEvent(R.string.analytics_event_disconnect, b);

        if(socket != null)
            socket.disconnect();
    }

    private void setUpEvents() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Bundle b = new Bundle();
                b.putString(getString(R.string.analytics_param_server_addr), serverAddr);
                mMetricsManager.logEvent(R.string.analytics_event_version, b);

                JSONObject token = new JSONObject();
                try {
                    String t = TokenUtils.getInstance(getBaseContext()).getAuthToken(getBaseContext());
                    token.put("token", t);
                    // TODO: If t == null, something went wrong and we want to alert the user
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                socket.emit("authenticate", token);
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "Disconnected from server.");

                Bundle b = new Bundle();
                b.putString(getString(R.string.analytics_param_server_addr), serverAddr);
                mMetricsManager.logEvent(R.string.analytics_event_disconnected, b);

                Intent i = new  Intent("com.texasgamer.zephyr.MAIN_ACTIVITY");
                i.putExtra("type", "disconnected");
                i.putExtra("address", serverAddr);
                sendBroadcast(i);
                connected = false;

                StatusNotificationManager.showNotification(getBaseContext(), ConnectionStatus.DISCONNECTED);
            }
        }).on("unauthorized", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, args[0].toString());
                try {
                    JSONObject error = new JSONObject(args[0].toString());
                    if (error.getString("message").equals("jwt expired")) {
                        // TODO: Renew expired token and retry
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                StatusNotificationManager.showNotification(getBaseContext(), ConnectionStatus.ERROR, R.string.connection_status_error_auth);
            }
        }).on("authenticated", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Intent i = new  Intent("com.texasgamer.zephyr.MAIN_ACTIVITY");
                i.putExtra("type", "connected");
                i.putExtra("address", serverAddr);
                sendBroadcast(i);
                connected = true;

                StatusNotificationManager.showNotification(getBaseContext(), ConnectionStatus.CONNECTED);
            }
        });
    }

    private boolean isSmartConnectEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_smart_connect), true);
    }

    class SocketServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            if(type.equals("notification")) {
                if(connected) {
                    String title = intent.getStringExtra("title");
                    String text = intent.getStringExtra("text");
                    socket.emit(mTokenUtils.getRoom() + "-notifications", buildNotificationJSONObject(title, text, ""));
                }
            } else if(type.equals("connect")) {
                if(connected)
                    disconnect();

                connect(intent.getStringExtra("address"));
            } else if(type.equals("disconnect")) {
                disconnect();
            } else if(type.equals("status")) {
                Intent i = new Intent("com.texasgamer.zephyr.MAIN_ACTIVITY");
                i.putExtra("type", connected ? "connected" : "disconnected");
                i.putExtra("address", serverAddr);
                i.putExtra("silent", true);
                sendBroadcast(i);
            } else if(type.equals("test")) {
                if(connected) {
                    socket.emit("notification", buildNotificationJSONObject("Test Notification",
                            "This is a test notification.", "").toString());
                }
            } else if (type.equals("update-notification")) {
                if (connected) {
                    StatusNotificationManager.showNotification(getBaseContext(), ConnectionStatus.CONNECTED);
                } else {
                    StatusNotificationManager.showNotification(getBaseContext(), ConnectionStatus.DISCONNECTED);
                }
            }
        }

        private JSONObject buildNotificationJSONObject(String title, String text, String icon) {
            try {
                JSONObject notif = new JSONObject();
                notif.put("title", title);
                notif.put("text", text);
                notif.put("icon", icon);

                return notif;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return new JSONObject();
        }
    }
}
