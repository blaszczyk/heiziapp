package heizi.heizi;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

public class DataRowView extends GridLayout {

    public DataRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRowCount(1);
        setColumnCount(3);
        context.getSystemService(LayoutInflater.class).inflate(R.layout.view_data_row, this, true);
        final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.DataRowView);
        final String label = attributes.getString(R.styleable.DataRowView_label);
        setText(R.id.label, label);
    }

    public void setData(int value, double slope) {
        setText(R.id.value,value + "°C");
        final ImageView slopeView = (ImageView) findViewById(R.id.slope);
        final float angle = (float) Math.atan(slope) * -57.3f;
        slopeView.setRotation(angle);
        slopeView.setVisibility(View.VISIBLE);
    }

    private void setText(int id, String text) {
        ((HeiziTextView) findViewById(id)).setText(text);
    }

}
