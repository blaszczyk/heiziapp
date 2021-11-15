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

    // TODO use androidx bit fix dupe dependency
    private SharedPreferences shared() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
