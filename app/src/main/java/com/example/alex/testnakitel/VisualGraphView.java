package com.example.alex.testnakitel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 10.04.17.
 */

public class VisualGraphView extends View {
    private static final int LINE_WIDTH = 2;
    private static final int LINE_SCALE = 100;
    private List<Float> mAmplitudes;
    private int mWidth;
    private int mHeight;
    private Paint mLinePaint;

    public VisualGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLinePaint = new Paint();
        mLinePaint.setColor(Color.GREEN);
        mLinePaint.setStrokeWidth(LINE_WIDTH);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        this.mWidth = width;
        this.mHeight = height;
        mAmplitudes = new ArrayList<>(mWidth / LINE_WIDTH);
    }

    public void clear() {
        mAmplitudes.clear();
    }

    public void addAmplitude(float amplitude) {
        mAmplitudes.add(amplitude);

        if (mAmplitudes.size() * LINE_WIDTH >= mWidth) {
            mAmplitudes.remove(0);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        final int middle = mHeight / 2;
        float curX = 0;

        for (float power : mAmplitudes) {
            float scaledHeight = power / LINE_SCALE;
            curX += LINE_WIDTH;
            canvas.drawLine(curX, middle + scaledHeight / 2, curX, middle - scaledHeight / 2, mLinePaint);
        }
    }
}
