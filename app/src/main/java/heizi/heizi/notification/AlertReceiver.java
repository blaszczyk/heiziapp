package heizi.heizi.notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import heizi.heizi.HeiziPreferences;
import heizi.heizi.data.DataSet;
import heizi.heizi.data.HeiziClient;
import heizi.heizi.data.HeiziDataEvaluator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertReceiver extends BroadcastReceiver {


    private static PendingIntent lastIntent = null;

    private static String SERVER_FAILS = "server.fails";

    public static void scheduleAlert(final Context context) {
        scheduleAlert(context, 300, 0);
    }

    private static void scheduleAlert(final Context context, int waitTimeSeconds, int serverFails) {
        final AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if(lastIntent != null) {
            alarmManager.cancel(lastIntent);
        }
        final Intent receiverIntent = new Intent(context, AlertReceiver.class);
        receiverIntent.putExtra(SERVER_FAILS, serverFails);
        lastIntent = PendingIntent.getBroadcast(context, 1, receiverIntent, 0);
        final long nextAlert = System.currentTimeMillis() + waitTimeSeconds * 1000L;
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlert, lastIntent);
    }


    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String serviceHost = new HeiziPreferences(context).getServiceHost();
        if(serviceHost == null) {
            scheduleAlert(context);
            return;
        }
        final HeiziClient client = new HeiziClient(serviceHost);
        client.request().latest().enqueue(new Callback<DataSet>() {
            @Override
            public void onResponse(Call<DataSet> call, Response<DataSet> response) {
                final HeiziDataEvaluator.Message message = HeiziDataEvaluator.getMessage(response.body());
                if(message != null) {
                    final HeiziNotification notification = new HeiziNotification(context);
                    final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                    final Notification notification1 = notification.builder()
                            .setContentText(message.getText())
                            .setContentTitle(message.getTitle())
                            .build();
                    notificationManager.notify(0, notification1);
                    scheduleAlert(context);
                }
                else {
                    scheduleAlert(context);
                }
            }

            @Override
            public void onFailure(Call<DataSet> call, Throwable t) {
                final int serverFails = intent.getIntExtra(SERVER_FAILS, 0) + 1;
                final int waitTime = serverFails > 7 ? 900 : 5 << serverFails; // 5 * 2^fails
                scheduleAlert(context, waitTime, serverFails);
            }
        });
    }
}
