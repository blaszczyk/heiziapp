package heizi.heizi.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Random;

import heizi.heizi.HeiziPreferences;
import heizi.heizi.data.DataSet;
import heizi.heizi.data.HeiziClient;
import heizi.heizi.data.HeiziDataEvaluator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlertReceiver extends BroadcastReceiver {

    private static String SERVER_FAILS = "server.fails";

    private static final Random random = new Random();

    public static void scheduleAlert(final Context context) {
        scheduleAlert(context, 300, 0);
    }

    private static void scheduleAlert(final Context context, int waitTimeSeconds, int serverFails) {
        final AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        final Intent receiverIntent = new Intent(context, AlertReceiver.class);
        receiverIntent.putExtra(SERVER_FAILS, serverFails);

        final PendingIntent newIntent = PendingIntent.getBroadcast(context, 1, receiverIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        final long nextAlert = System.currentTimeMillis() + waitTimeSeconds * 1000L + random.nextInt(10_000);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextAlert, newIntent);
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
                    new HeiziNotification(context).notify(message.getTitle(), message.getText());
                }
                scheduleAlert(context);
            }

            @Override
            public void onFailure(Call<DataSet> call, Throwable t) {
                final int serverFails = intent.getIntExtra(SERVER_FAILS, 0) + 1;
                final int waitTime = serverFails > 6 ? 900 : 10 << serverFails; // 10 * 2^fails
                scheduleAlert(context, waitTime, serverFails);
            }
        });
    }

}
