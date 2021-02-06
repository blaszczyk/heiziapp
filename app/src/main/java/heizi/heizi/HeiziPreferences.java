package heizi.heizi;

import android.content.Context;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;

public class HeiziPreferences {

    private static final String HOST_KEY = "service.host";

    private final Context context;
    public HeiziPreferences(Context context) {
        this.context  = context;
    }

    public String getServiceHost() {
        return shared().getString(HOST_KEY, null);
    }

    public void setServiceHost(final String host) {
        shared().edit().putString(HOST_KEY, host).apply();
    }

    // TODO use androidx bit fix dupe dependency
    private SharedPreferences shared() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
