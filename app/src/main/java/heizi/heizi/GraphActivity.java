package heizi.heizi;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

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

    private static final int COLOR_TAG = Color.RED;
    private static final int COLOR_TY = Color.GRAY;
    private static final int COLOR_PO = Color.YELLOW;
    private static final int COLOR_PU = Color.CYAN;
    private static final int COLOR_OWM = Color.rgb(127, 0, 255);

    private GraphView graphView;
    private Viewport viewPort;

    private HeiziClient client;

    private LinearLayout rangeButtons;
    private Button selectedButton;
    private ProgressBar spinner;

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

        spinner = (ProgressBar) findViewById(R.id.spinner);

        client = new HeiziClient();
        requestRange(3);
    }

    private void addButton(final long hours, final boolean select) {
        final Button button = new Button(getApplicationContext());
        button.setText(hours + " h");
        button.setTextColor(Color.RED);
        button.setBackground(null);
        button.setWidth(20);
        button.setHeight(20);
        button.setTextSize(18);
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
        spinner.setVisibility(View.VISIBLE);
        client.request().range(minTime / 1000, maxTime / 1000).enqueue(new Callback<DataRange>() {
            @Override
            public void onResponse(Call<DataRange> call, Response<DataRange> response) {
                final DataRange data = response.body();
                spinner.setVisibility(View.INVISIBLE);

                final int minOwm = getMinTemp(data.getOwm());

                final int maxTemp = 300;
                final int minTemp = Math.min(0, minOwm - 10);

                addCornerPoints(minTime, minTemp, maxTime, maxTemp);
                addTurData(data.getTur());
                addData(data.getTy(), "TY", COLOR_TY, 300);
                addData(data.getTag(), "TAG", COLOR_TAG, 300);
                addData(data.getPo(), "PO", COLOR_PO, 300);
                addData(data.getPu(), "PU", COLOR_PU, 300);
                addData(data.getOwm(), "OWM", COLOR_OWM, 1800);

                viewPort.setXAxisBoundsManual(true);
                viewPort.setMinX(minTime);
                viewPort.setMaxX(maxTime);

                viewPort.setYAxisBoundsManual(true);
                viewPort.setMinY(minTemp);
                viewPort.setMaxY(maxTemp);

                viewPort.setScalable(true);
                viewPort.setScalableY(true);
            }

            @Override
            public void onFailure(Call<DataRange> call, Throwable t) {
                spinner.setVisibility(View.INVISIBLE);
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

    private void addCornerPoints(long minTime, int minTemp, long maxTime, int maxTemp) {
        final DataPoint[] corners = new DataPoint[]{
                new DataPoint(minTime, minTemp),
                new DataPoint(maxTime, maxTemp)
        };
        final PointsGraphSeries<DataPoint> series = new PointsGraphSeries<>(corners);
        series.setColor(Color.TRANSPARENT);
        graphView.addSeries(series);
    }

    private void addData(int[][] data, String title, int color, int gapThreshold) {
        if(data == null) {
            return;
        }
        List<DataPoint> dataList = new ArrayList<>();
        int[] lastDatum = null;
        for(final int[] datum : data) {
            if(datum[1] > 300) {
                continue;
            }
            if( lastDatum != null
                    && (datum[0] - lastDatum[0] > gapThreshold
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

    private static int getMinTemp(int[][] data) {
        int result = Integer.MAX_VALUE;
        for (int[] datum : data) {
            final int temp = datum[1];
            if (temp < result) {
                result = temp;
            }
        }
        return result;
    }
}
