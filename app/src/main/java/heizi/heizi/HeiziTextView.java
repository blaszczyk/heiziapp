package heizi.heizi;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;

public class HeiziTextView extends AppCompatTextView {

    private static Typeface TYPEFACE = null;

    public HeiziTextView(Context context) {
        super(context);
        init();
    }

    public HeiziTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeiziTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        if(TYPEFACE == null) {
            TYPEFACE = Typeface.createFromAsset(getContext().getAssets(), "font/open24display.ttf");
        }
        setTypeface(TYPEFACE);
    }
}
