package com.thomas.chargingoverlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class PowerGraphView extends View {

    private Paint paint = new Paint();
    private List<Float> values = new ArrayList<>();

    public PowerGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(4f);
    }

    public void addValue(float value) {
        if (values.size() > 100) values.remove(0);
        values.add(value);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (values.size() < 2) return;

        float width = getWidth();
        float height = getHeight();
        float stepX = width / 100f;

        for (int i = 1; i < values.size(); i++) {
            float x1 = (i - 1) * stepX;
            float x2 = i * stepX;
            float y1 = height - (values.get(i - 1) / 20f) * height;
            float y2 = height - (values.get(i) / 20f) * height;
            canvas.drawLine(x1, y1, x2, y2, paint);
        }
    }
}