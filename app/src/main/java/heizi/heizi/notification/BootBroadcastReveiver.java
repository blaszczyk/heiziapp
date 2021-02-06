package heizi.heizi.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReveiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlertReceiver.scheduleAlert(context);
    }

}
