package com.texasgamer.zephyr.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.squareup.picasso.Picasso;
import com.texasgamer.zephyr.Constants;
import com.texasgamer.zephyr.R;
import com.texasgamer.zephyr.service.SocketService;
import com.texasgamer.zephyr.util.TokenUtils;
import com.texasgamer.zephyr.view.RoundedTransformation;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity {

    private TokenUtils mTokenUtils;
    private MainAcvitiyReceiver mainAcvitiyReceiver;
    private BottomSheetBehavior mBottomSheetBehavior;

    private final String mServerAddr = Constants.ZEPHYR_BASE_WS_URL;
    private boolean mConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        mTokenUtils = TokenUtils.getInstance(this);

        if (mTokenUtils.doesTokenExist()) {
            populateInterface();
        }

        setupConnectButton();
        startSocketService();
        requestConnectionStatus();
        setupHints();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mTokenUtils.doesTokenExist()) {
            Log.i(TAG, "Logged out, going to LoginActivity.");
            Toast.makeText(this, getString(R.string.logged_out_toast), Toast.LENGTH_SHORT).show();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
            return;
        }

        mainAcvitiyReceiver = new MainAcvitiyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.texasgamer.zephyr.MAIN_ACTIVITY");
        registerReceiver(mainAcvitiyReceiver, filter);

        if(NotificationManagerCompat.getEnabledListenerPackages(this).contains("com.texasgamer.zephyr")) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            mMetricsManager.logEvent(R.string.analytics_show_enable_notif_perm, null);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mainAcvitiyReceiver != null) {
            unregisterReceiver(mainAcvitiyReceiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                mMetricsManager.logEvent(R.string.analytics_tap_settings, null);

                Intent i = new Intent(MainActivity.this, PreferencesActivity.class);
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateInterface() {
        ((TextView) findViewById(R.id.username)).setText(mTokenUtils.getName());
        Picasso.with(this)
                .load(mTokenUtils.getAvatar())
                .transform(new RoundedTransformation(8, 8))
                .into((ImageView) findViewById(R.id.avatar));

        mBottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        findViewById(R.id.enableNotificationsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMetricsManager.logEvent(R.string.analytics_tap_enable_notif_perm, null);
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
            }
        });
    }

    private void startSocketService() {
        Intent i = new Intent(this, SocketService.class);
        startService(i);
    }

    private void requestConnectionStatus() {
        Intent i = new  Intent("com.texasgamer.zephyr.SOCKET_SERVICE");
        i.putExtra("type", "status");
        sendBroadcast(i);
    }

    private void updateConnectBtn() {
        runOnUiThread(new Runnable() {
            public void run() {
                ((Button) findViewById(R.id.connect_btn)).setText(mConnected ? R.string.btn_disconnect : R.string.btn_connect);
            }
        });
    }

    private void setupHints() {
        findViewById(R.id.steamHint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Opening Steam store page...");
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.ZEPHYR_STEAM_STORE)));
            }
        });

        findViewById(R.id.redditHint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Opening subreddit...");
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.ZEPHYR_SUBREDDIT)));
            }
        });
    }

    private void setupConnectButton() {
        final Button connectBtn = (Button) findViewById(R.id.connect_btn);
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mConnected) {
                    if(connectBtn.getText().toString().equals(getString(R.string.btn_connecting))) {
                        connectBtn.setText(R.string.btn_connect);
                        Intent i = new  Intent("com.texasgamer.zephyr.SOCKET_SERVICE");
                        i.putExtra("type", "disconnect");
                        sendBroadcast(i);
                    } else {
                        connectBtn.setText(R.string.btn_connecting);
                        Intent i = new  Intent("com.texasgamer.zephyr.SOCKET_SERVICE");
                        i.putExtra("type", "connect");
                        i.putExtra("address", mServerAddr);
                        sendBroadcast(i);
                    }

                    Bundle b = new Bundle();
                    b.putString(getString(R.string.analytics_param_server_addr), mServerAddr);
                    mMetricsManager.logEvent(R.string.analytics_tap_connect, b);
                } else {
                    connectBtn.setText(R.string.btn_disconnecting);
                    Intent i = new  Intent("com.texasgamer.zephyr.SOCKET_SERVICE");
                    i.putExtra("type", "disconnect");
                    sendBroadcast(i);

                    Bundle b = new Bundle();
                    b.putString(getString(R.string.analytics_param_server_addr), mServerAddr);
                    mMetricsManager.logEvent(R.string.analytics_tap_disconnect, b);
                }
            }
        });
    }

    class MainAcvitiyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            switch (type) {
                case "connected":
                    mConnected = true;
                    updateConnectBtn();
                    if (!intent.getBooleanExtra("silent", false)) {
                        Snackbar.make(findViewById(R.id.main_content), R.string.snackbar_connected, Snackbar.LENGTH_SHORT).show();
                    }
                    break;
                case "disconnected":
                    mConnected = false;
                    updateConnectBtn();
                    if (!intent.getBooleanExtra("silent", false)) {
                        Snackbar.make(findViewById(R.id.main_content), R.string.snackbar_disconnected, Snackbar.LENGTH_SHORT).show();
                    }
                    break;
                case "notif-sent":
                    Snackbar.make(findViewById(R.id.main_content), R.string.snackbar_notif_confirm, Snackbar.LENGTH_SHORT).show();
                    break;
                case "notif-failed":
                    Snackbar.make(findViewById(R.id.main_content), R.string.snackbar_notif_fail, Snackbar.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
