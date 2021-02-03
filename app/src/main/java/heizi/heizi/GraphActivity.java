package heizi.heizi;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import heizi.heizi.data.DataRange;
import heizi.heizi.data.HeiziClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GraphActivity extends AppCompatActivity {

    private GraphView graphView;
    private Viewport viewPort;

    private HeiziClient client;

    private LinearLayout rangeButtons;
    private Button selectedButton;

    private Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        typeface =Typeface.createFromAsset(getApplicationContext().getAssets(), "font/open24display.ttf");

        graphView = (GraphView) findViewById(R.id.viewGraph);
        viewPort = graphView.getViewport();
        GridLabelRenderer gridLabelRenderer = graphView.getGridLabelRenderer();
        gridLabelRenderer.setLabelFormatter(new DateAsXAxisLabelFormatter(GraphActivity.this, new SimpleDateFormat("HH:mm")));
        gridLabelRenderer.setGridColor(Color.WHITE);
        gridLabelRenderer.setHorizontalLabelsColor(Color.WHITE);
        gridLabelRenderer.setVerticalLabelsColor(Color.WHITE);

        rangeButtons = (LinearLayout) findViewById(R.id.rangeButtons);
        addButton(3, true);
        addButton(10, false);
        addButton(24, false);
        addButton(72, false);

        final String hostName = getIntent().getStringExtra(MainActivity.HOST_KEY);
        client = new HeiziClient(hostName);
        requestRange(3);
    }

    private void addButton(final long hours, final boolean select) {
        final Button button = new Button(getApplicationContext());
        button.setText(hours + " h");
        button.setTextColor(Color.RED);
        button.setBackground(null);
        button.setWidth(20);
        button.setHeight(20);
        button.setTextSize(20);
        if(select) {
            select(button);
        }
        else {
            unselect(button);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select(button);
                requestRange(hours);
            }
        });
        rangeButtons.addView(button);
    }

    private void select(final Button button) {
        if(selectedButton != null) {
            unselect(selectedButton);
        }
        button.setTypeface(typeface, Typeface.BOLD);
        button.setBackgroundColor(Color.rgb(43,0,95));
        selectedButton = button;
    }

    private void unselect(final Button button) {
        button.setTypeface(typeface, Typeface.NORMAL);
        button.setBackground(null);
    }

    private void requestRange(final long hours) {
        graphView.removeAllSeries();
        final long maxTime = System.currentTimeMillis();
        final long minTime = maxTime - hours * 3_600_000L;
        client.request().range(minTime / 1000, maxTime / 1000).enqueue(new Callback<DataRange>() {
            @Override
            public void onResponse(Call<DataRange> call, Response<DataRange> response) {
                final DataRange data = response.body();

                addCornerPoints(minTime, maxTime);
                addTurData(data.getTur());
                addData(data.getTy(), "TY", Color.GRAY);
                addData(data.getTag(), "TAG", Color.RED);
                addData(data.getPo(), "PO", Color.YELLOW);
                addData(data.getPu(), "PU", Color.CYAN);

                viewPort.setXAxisBoundsManual(true);
                viewPort.setMinX(minTime);
                viewPort.setMaxX(maxTime);

                viewPort.setYAxisBoundsManual(true);
                viewPort.setMinY(0);
                viewPort.setMaxY(300);

                viewPort.setScalable(true);
                viewPort.setScalableY(true);
            }

            @Override
            public void onFailure(Call<DataRange> call, Throwable t) {

            }
        });
    }

    private void addTurData(int[] data) {
        List<DataPoint> turData = new ArrayList<>();
        int lastdatum = 0;
        for(final int datum : data) {
            if(lastdatum > 0 && datum - lastdatum > 60) {
                addTurSeries(turData);
                turData = new ArrayList<>();
            }
            turData.add(new DataPoint(datum * 1000L, 300));
            lastdatum = datum;
        }
        addTurSeries(turData);
    }

    private void addTurSeries(List<DataPoint> data) {
        if(data.isEmpty()) {
            return;
        }
        final LineGraphSeries series = new LineGraphSeries(data.toArray(new DataPoint[data.size()]));
        series.setBackgroundColor(Color.argb(127, 0,127,0));
        series.setColor(Color.GREEN);
        series.setDrawBackground(true);
        graphView.addSeries(series);
    }

    private void addCornerPoints(long minTime, long maxTime) {
        final DataPoint[] corners = new DataPoint[]{
                new DataPoint(minTime, 0),
                new DataPoint(maxTime, 300)
        };
        final PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>(corners);
        series.setColor(Color.TRANSPARENT);
        graphView.addSeries(series);
    }

    private void addData(int[][] data, String title, int color) {
        if(data == null) {
            return;
        }
        List<DataPoint> dataList = new ArrayList<>();
        int[] lastDatum = null;
        for(final int[] datum : data) {
            if( lastDatum != null
                    && (datum[0] - lastDatum[0] > 60
                    || Math.abs(datum[1] - lastDatum[1]) > 15 )) {
                addSeries(dataList, title, color);
                dataList = new ArrayList<>();
            }
            lastDatum = datum;
            final Date date = new Date(datum[0] * 1000L);
            final DataPoint dataPoint = new DataPoint(date, datum[1]);
            dataList.add(dataPoint);
        }
        addSeries(dataList, title, color);
    }

    private void addSeries(List<DataPoint> data, String title, int color) {
        if(!data.isEmpty()) {
            final LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data.toArray(new DataPoint[data.size()]));
            series.setColor(color);
            series.setTitle(title);
            graphView.addSeries(series);
        }
    }
}
