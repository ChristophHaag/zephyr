package com.texasgamer.zephyr.manager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.texasgamer.zephyr.R;
import com.texasgamer.zephyr.activity.MainActivity;
import com.texasgamer.zephyr.socket.ConnectionStatus;

public class StatusNotificationManager {

    private static final int NOTIF_ID = 0;

    public static void showNotification(Context context, ConnectionStatus status) {
        if(!shouldShowNotification(context, status)) {
            hideNotification(context);
            return;
        }

        int priority = NotificationCompat.PRIORITY_MIN;
        if (status == ConnectionStatus.ERROR) {
            priority = NotificationCompat.PRIORITY_DEFAULT;
        }

        showNotification(context, context.getString(status.getMessage()), priority);
    }

    public static void showNotification(Context context, ConnectionStatus status, @StringRes int additional) {
        if(!shouldShowNotification(context, status)) {
            hideNotification(context);
            return;
        }

        String message = context.getString(status.getMessage()) + ": " + context.getString(additional);

        int priority = NotificationCompat.PRIORITY_MIN;
        if (status == ConnectionStatus.ERROR) {
            priority = NotificationCompat.PRIORITY_DEFAULT;
        }

        showNotification(context, message, priority);
    }

    public static void hideNotification(Context context) {
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(NOTIF_ID);
    }

    private static void showNotification(Context context, String message, int priority) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_alert)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        .setOngoing(true)
                        .setPriority(priority)
                        .setAutoCancel(false)
                        .setContentIntent(intent)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(message);

        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(NOTIF_ID, mBuilder.build());
    }

    private static boolean shouldShowNotification(Context context, ConnectionStatus status) {
        return (status != ConnectionStatus.DISCONNECTED) ||
                PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(context.getString(R.string.pref_disconnect_notif), false);
    }
}
