package heizi.heizi;

import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String HOST_KEY = "service.host";

    private HeiziClient client;

    private TextView txtTag;
    private TextView txtTy;
    private TextView txtPo;
    private TextView txtPu;

    private TextView txtAge;
    private TextView txtMessage;

    private ImageButton btnRefresh;
    private ImageButton btnLocate;

    private boolean refreshPending = false;
    private boolean serviceFound = false;
    private int requestFails = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("--INFO","activity started");

        final String serviceHost = preferences().getString(HOST_KEY, null);
        serviceFound = serviceHost != null;
        client = new HeiziClient(serviceHost);
        txtTag = (TextView) findViewById(R.id.valueTag);
        txtTy = (TextView) findViewById(R.id.valueTy);
        txtPo = (TextView) findViewById(R.id.valuePo);
        txtPu = (TextView) findViewById(R.id.valuePu);
        txtAge = (TextView) findViewById(R.id.dataAge);
        txtMessage = (TextView) findViewById(R.id.message);
        btnRefresh = (ImageButton) findViewById(R.id.refreshButton);
        btnLocate = (ImageButton) findViewById(R.id.locateButton);

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchLatestData();
            }
        });
        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locateService();
            }
        });
        if(serviceFound) {
            btnLocate.setVisibility(View.INVISIBLE);
        }
        fetchLatestData();
    }

    private void locateService() {
        serviceFound = false;
        btnRefresh.setVisibility(View.INVISIBLE);
        btnLocate.setVisibility(View.INVISIBLE);
        client.locateService("192.168.2.", new HeiziClient.HostNameConsumer() {
            @Override
            public void consume(String host) {
                preferences().edit().putString(HOST_KEY, host).apply();
                client = new HeiziClient(host);
                serviceFound = true;
                fetchLatestData();
                btnRefresh.setVisibility(View.VISIBLE);
            }
            @Override
            public void fail() {
                txtMessage.setText("Server nicht gefunden");
                btnLocate.setVisibility(View.VISIBLE);
            }
            @Override
            public void message(String message) {
                txtMessage.setText(message);
            }
        });
    }

    private SharedPreferences preferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void fetchLatestData() {
        if(!serviceFound) {
            return;
        }
        final Animation rotation = AnimationUtils.loadAnimation(this.getApplicationContext(), R.anim.rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        btnRefresh.startAnimation(rotation);
        final boolean requestRequired = !refreshPending;
        client.request().latest().enqueue(new Callback<DataSet>() {
            @Override
            public void onResponse(Call<DataSet> call, Response<DataSet> response) {
                final DataSet data = response.body();
                txtTag.setText(data.getTag() + " °C");
                txtTy.setText(data.getTy() + " °C");
                txtPo.setText(data.getPo() + " °C");
                txtPu.setText(data.getPu() + " °C");
                final Date time = new Date(data.getTime() * 1000L);
                txtAge.setText(new SimpleDateFormat("HH:mm:ss").format(time));
                txtMessage.setText("");
                btnLocate.setVisibility(View.INVISIBLE);
                rotation.setRepeatCount(0);
                requestFails = 0;

                if(requestRequired) {
                    refreshPending = true;
                    delayRequest();
                }
            }

            @Override
            public void onFailure(Call<DataSet> call, Throwable t) {
                Log.e("ERROR", "fetching data failed", t);
                txtMessage.setText("Server nicht erreichbar");
                btnLocate.setVisibility(View.VISIBLE);
                rotation.setRepeatCount(0);
                if( requestFails++ < 5) {
                    delayRequest();
                }
            }
        });
    };

    private void delayRequest() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshPending = false;
                fetchLatestData();
            }
        }, 5000);
    }
}
