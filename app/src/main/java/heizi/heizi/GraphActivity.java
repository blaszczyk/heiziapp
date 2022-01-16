package heizi.heizi;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import heizi.heizi.data.DataRange;
import heizi.heizi.data.HeiziClient;
import heizi.heizi.notification.AlertReceiver;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GraphActivity extends AppCompatActivity {

    private static final int COLOR_TAG = Color.RED;
    private static final int COLOR_TY = Color.GRAY;
    private static final int COLOR_PO = Color.YELLOW;
    private static final int COLOR_PU = Color.CYAN;
    private static final int COLOR_OWM = Color.rgb(127, 0, 255);

    private static final int COLOR_VENT_ON = Color.argb(127, 0,127,0);
    private static final int COLOR_VENT_OFF = Color.argb(127, 127, 63, 0);
    private static final int COLOR_VENT_TOP = Color.GREEN;

    private static final String[] RANGE_VALUES = new String[]{"3", "10", "24", "72"};
    private static final long REFRESH_INTERVAL = 10_000L;

    private GraphView graphView;
    private Viewport viewPort;

    private HeiziClient client;

    private Button dateButton;
    private Button timeButton;
    private Button rangeButton;
    private Button liveButton;

    private ProgressBar spinner;

    private Typeface typeface;

    private int selectedRange = 3;
    private Calendar selectedTime = Calendar.getInstance();
    private boolean live = true;
    private boolean canceled = false;

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

        dateButton = addButton(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerFragment fragment = new DatePickerFragment();
                fragment.graphActivity = GraphActivity.this;
                fragment.show(getSupportFragmentManager(), "datePicker");
                setLive(false);
            }
        });
        timeButton = addButton(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerFragment fragment = new TimePickerFragment();
                fragment.graphActivity = GraphActivity.this;
                fragment.show(getSupportFragmentManager(), "timePicker");
                setLive(false);
            }
        });
        rangeButton = addButton(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RangePickerFragment fragment = new RangePickerFragment();
                fragment.graphActivity = GraphActivity.this;
                fragment.show(getSupportFragmentManager(), "rangePicker");
                setLive(false);
            }
        });
        liveButton = addButton(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLive(!live);
            }
        });
        setButtonText();
        liveButton.setText("LIVE");
        spinner = (ProgressBar) findViewById(R.id.spinner);

        client = new HeiziClient();
        setLive(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        canceled = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        canceled = false;
        if (live) {
            requestLiveData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        canceled = true;
    }

    private Button addButton(final View.OnClickListener listener) {
        final Button button = new Button(getApplicationContext());
        button.setTextColor(Color.RED);
        button.setBackground(null);
        button.setHeight(20);
        button.setTextSize(18);
        button.setTypeface(typeface, Typeface.BOLD);
        button.setOnClickListener(listener);
        ((LinearLayout) findViewById(R.id.buttons)).addView(button);
        return button;
    }

    private void setButtonText() {
        rangeButton.setText("- " + selectedRange + "h");
        dateButton.setText(new SimpleDateFormat("yy-MM-dd").format(selectedTime.getTime()));
        timeButton.setText(new SimpleDateFormat("HH:mm").format(selectedTime.getTime()));
    }

    private void setLive(final boolean live) {
        this.live = live;
        if (live) {
            liveButton.setBackgroundColor(Color.rgb(43,0,95));
            requestLiveData();
        }
        else {
            liveButton.setBackground(null);
        }
    }

    private void requestLiveData() {
        selectedTime = Calendar.getInstance();
        selectedRange = 3;
        requestRange();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(live && !canceled) {
                    requestLiveData();
                }
            }
        }, REFRESH_INTERVAL);
    }

    private void requestRange() {
        setButtonText();
        final long maxTime = selectedTime.getTimeInMillis();
        final long minTime = getMinTime();
        spinner.setVisibility(View.VISIBLE);
        client.request().range(minTime / 1000, maxTime / 1000).enqueue(new Callback<DataRange>() {
            @Override
            public void onResponse(Call<DataRange> call, Response<DataRange> response) {
                final DataRange data = response.body();
                spinner.setVisibility(View.INVISIBLE);
                graphView.removeAllSeries();

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

    private long getMinTime() {
        return selectedTime.getTimeInMillis() - selectedRange * 3_600_000L;
    }

    private void addTurData(int[][] data) {
        List<DataPoint> turData = new ArrayList<>();
        int[] lastdatum = {0,0};
        for(final int[] datum : data) {
            if(lastdatum[0] > 0 &&
                    (datum[0] - lastdatum[0] > 60
                    || datum[1] != lastdatum[1])) {
                addTurSeries(turData, lastdatum[1]);
                turData = new ArrayList<>();
            }
            turData.add(new DataPoint(datum[0] * 1000L, 300));
            lastdatum = datum;
        }
        addTurSeries(turData, lastdatum[1]);
    }

    private void addTurSeries(List<DataPoint> data, int vent) {
        if(data.isEmpty()) {
            return;
        }
        final int backgroundColor = vent > 0 ? COLOR_VENT_ON : COLOR_VENT_OFF;
        final LineGraphSeries series = new LineGraphSeries(data.toArray(new DataPoint[data.size()]));
        series.setBackgroundColor(backgroundColor);
        series.setColor(COLOR_VENT_TOP);
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
                    || Math.abs(datum[1] - lastDatum[1]) > 30 )) {
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

    public static class DatePickerFragment extends AbstractGraphFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year = get(Calendar.YEAR);
            int month = get(Calendar.MONTH);
            int day = get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            set(Calendar.YEAR, year);
            set(Calendar.MONTH, month);
            set(Calendar.DAY_OF_MONTH, day);
            graphActivity.requestRange();
        }
    }

    public static class TimePickerFragment extends AbstractGraphFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int hourOfDay = get(Calendar.HOUR_OF_DAY);
            int minute = get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hourOfDay, minute, true);
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
            set(Calendar.HOUR_OF_DAY, hourOfDay);
            set(Calendar.MINUTE, minute);
            graphActivity.requestRange();
        }
    }

    public static class RangePickerFragment extends AbstractGraphFragment implements DialogInterface.OnClickListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Zeitraum / Stunden")
                    .setItems(RANGE_VALUES, this);
            return builder.create();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            graphActivity.selectedRange = Integer.parseInt(RANGE_VALUES[which]);
            graphActivity.requestRange();
        }
    }

    private static abstract class AbstractGraphFragment extends DialogFragment {
        GraphActivity graphActivity;

        int get(int field) {
            return graphActivity.selectedTime.get(field);
        }

        void set(int field, int value) {
            graphActivity.selectedTime.set(field, value);
        }
    }

}
