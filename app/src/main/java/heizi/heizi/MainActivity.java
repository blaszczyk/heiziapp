package heizi.heizi;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import heizi.heizi.data.DataSet;
import heizi.heizi.data.HeiziClient;
import heizi.heizi.data.HeiziDataEvaluator;
import heizi.heizi.notification.AlertReceiver;
import heizi.heizi.notification.HeiziNotification;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final long REFRESH_INTERVAL = 5000L;

    private HeiziClient client;
    private HeiziPreferences preferences;

    private ImageButton btnRefresh;
    private ImageButton btnLocate;
    private ImageButton btnGraph;

    private long lastRefresh = 0;
    private boolean isRunning = true;
    private boolean serviceFound = false;
    private int requestFails = 0;

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
        fetchLatestData();
        new HeiziNotification(this).cancelAll();
        AlertReceiver.scheduleAlert(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AlertReceiver.scheduleAlert(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("main","activity started");

        preferences = new HeiziPreferences(this);
        final String serviceHost = preferences.getServiceHost();
        serviceFound = serviceHost != null;
        client = new HeiziClient(serviceHost);
        btnRefresh = (ImageButton) findViewById(R.id.refreshButton);
        btnLocate = (ImageButton) findViewById(R.id.locateButton);
        btnGraph = (ImageButton) findViewById(R.id.graphButton);

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
        btnGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(MainActivity.this, GraphActivity.class);
                startActivity(intent);
            }
        });
        if(serviceFound) {
            btnLocate.setVisibility(View.INVISIBLE);
        }
        else {
            btnGraph.setVisibility(View.INVISIBLE);
        }
    }

    private void setText(int id, String text) {
        ((TextView) findViewById(id)).setText(text);
    }

    private void setMessage(String message) {
        setText(R.id.message, message);
    }

    private void locateService() {
        serviceFound = false;
        btnRefresh.setVisibility(View.INVISIBLE);
        btnGraph.setVisibility(View.INVISIBLE);
        client.locateService("192.168.2.", new HeiziClient.HostNameConsumer() {
            @Override
            public void consume(String host) {
                preferences.setServiceHost(host);
                client = new HeiziClient(host);
                serviceFound = true;
                fetchLatestData();
                btnRefresh.setVisibility(View.VISIBLE);
            }
            @Override
            public void fail() {
                setMessage("Server nicht gefunden");
                btnLocate.setVisibility(View.VISIBLE);
            }
            @Override
            public void message(String message) {
                setMessage(message);
            }
        });
    }

    private void fetchLatestData() {
        lastRefresh = System.currentTimeMillis();
        if(!serviceFound) {
            return;
        }
        final Animation rotation = new RotateAnimation(0,360, Animation.RELATIVE_TO_SELF, 0.5F, Animation.RELATIVE_TO_SELF, 0.5F);
        rotation.setRepeatMode(Animation.RESTART);
        rotation.setRepeatCount(Animation.INFINITE);
        rotation.setDuration(1000);
        btnRefresh.startAnimation(rotation);

        client.request().latest().enqueue(new Callback<DataSet>() {
            @Override
            public void onResponse(Call<DataSet> call, Response<DataSet> response) {
                final DataSet data = response.body();
                setValue(R.id.rowTag, data.getTag(), data.getDtag());
                setValue(R.id.rowTy, data.getTy(), data.getDty());
                setValue(R.id.rowPo, data.getPo(), data.getDpo());
                setValue(R.id.rowPu, data.getPu(), data.getDpu());

                final Date time = new Date(data.getTime() * 1000L);
                setText(R.id.dataAge, new SimpleDateFormat("HH:mm:ss").format(time));
                final HeiziDataEvaluator.Message message = HeiziDataEvaluator.getMessage(data);
                setMessage(message != null ? message.getTitle() : "");
                btnLocate.setVisibility(View.INVISIBLE);
                btnGraph.setVisibility(View.VISIBLE);
                rotation.setRepeatCount(0);
                requestFails = 0;

                delayRequest();
            }

            @Override
            public void onFailure(Call<DataSet> call, Throwable t) {
                setMessage("Server nicht erreichbar");
                btnLocate.setVisibility(View.VISIBLE);
                btnGraph.setVisibility(View.INVISIBLE);
                rotation.setRepeatCount(0);
                if( requestFails++ < 5) {
                    delayRequest();
                }
            }
        });
    };

    private void setValue(int id, int value, double slope) {
        ((DataRowView) findViewById(id)).setData(value, slope);
    }

    private void delayRequest() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isRunning && System.currentTimeMillis() - lastRefresh >= REFRESH_INTERVAL) {
                    fetchLatestData();
                }
            }
        }, REFRESH_INTERVAL);
    }

}
