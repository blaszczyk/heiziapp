package heizi.heizi.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;

import heizi.heizi.MainActivity;
import heizi.heizi.R;

public class HeiziNotification extends ContextWrapper {

    private static final String CHANNEL_ID = "heizi-alert";

    private static final long[] VIBRATE_PATTERN = { 200, 200, 600, 200, 200, 200, 600, 200, 200, 200, 1200 };

    public HeiziNotification(Context base) {
        super(base);
        if (versionO()) {
            createNotificationChannel();
        }
    }

    public Notification.Builder builder() {
        final Intent mainIntent = new Intent(this, MainActivity.class);
        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(mainIntent);
        final PendingIntent pi = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        final Notification.Builder builder = versionO() ? builderO() : new Notification.Builder(this);
        return builder.setSmallIcon(R.drawable.ic_flame)
                .setLights(Color.RED, 2000, 2000)
                .setVibrate(VIBRATE_PATTERN)
                .setAutoCancel(true)
                .setLargeIcon(Icon.createWithResource(this, R.mipmap.ic_launcher_round))
                .setContentIntent(pi);
    }

    private static boolean versionO() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private Notification.Builder builderO() {
        return new Notification.Builder(this, CHANNEL_ID);
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        final NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if(notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Heizi Alarm", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setVibrationPattern(VIBRATE_PATTERN);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
