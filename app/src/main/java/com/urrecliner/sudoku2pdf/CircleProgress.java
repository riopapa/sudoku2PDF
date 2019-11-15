package com.urrecliner.sudoku2pdf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * A Material style progress wheel, compatible up to 2.2.
 * Todd Davies' Progress Wheel https://github.com/Todd-Davies/ProgressWheel
 *
 * @author Nico Hormazábal
 *         <p/>
 *         Licensed under the Apache License 2.0 license see:
 *         http://www.apache.org/licenses/LICENSE-2.0
 */
public class CircleProgress extends View {

    private static final String TAG = CircleProgress.class.getSimpleName();
    /**
     * *********
     * DEFAULTS *
     * **********
     */

    private static boolean reDraw = false;
    private static int outerStart = 0;
    private static int outerFinish = 0;
    private static int outerProgressColor = Color.RED;
    private static int outerCirCleColor = Color.DKGRAY;
    private static int outerWidth = 150;

    private static int innerStart = 0;
    private static int innerFinish = 0;
    private static int innerProgressColor = Color.MAGENTA;
    private static int innerBackGroundColor = Color.GRAY;

    //Paints
    private static Paint outerCirclePaint = new Paint();
    private static Paint outerProgressPaint = new Paint();
    private static Paint innerBackgroundPaint = new Paint();
    private static Paint innerProgressPaint = new Paint();

    //Rectangles
    private static RectF outerBounds = new RectF();
    private static RectF innerBounds = new RectF();

    /**
     * The constructor for the ProgressWheel
     */
    public CircleProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int viewWidth = this.getPaddingLeft() + this.getPaddingRight();
        int viewHeight = this.getPaddingTop() + this.getPaddingBottom();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(viewWidth, widthSize);
        } else {
            width = viewWidth;
        }
        if (heightMode == MeasureSpec.EXACTLY || widthMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(viewHeight, heightSize);
        } else {
            height = viewHeight;
        }

        setMeasuredDimension(width, height);
    }

    /**
     * Use onSizeChanged instead of onAttachedToWindow to get the dimensions of the view,
     * because this method is called after measuring the dimensions of MATCH_PARENT & WRAP_CONTENT.
     * Use this dimensions to setup the bounds and paints.
     */
    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        setupBounds(w, h);
        setupPaints();
        invalidate();
    }

    /**
     * Set the properties of the paints we're using to
     * draw the progress wheel
     */
    private void setupPaints() {
        outerCirclePaint.setColor(outerCirCleColor);
        outerCirclePaint.setAntiAlias(true);
        outerCirclePaint.setStyle(Paint.Style.STROKE);
        outerCirclePaint.setStrokeWidth(outerWidth);

        outerProgressPaint.setColor(outerProgressColor);
        outerProgressPaint.setAntiAlias(true);
        outerProgressPaint.setStyle(Paint.Style.STROKE);
        outerProgressPaint.setStrokeWidth(outerWidth);

        innerBackgroundPaint.setColor(innerBackGroundColor);
        innerBackgroundPaint.setAntiAlias(true);
        innerBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        innerProgressPaint.setColor(innerProgressColor);
        innerProgressPaint.setAntiAlias(true);
        innerProgressPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    /**
     * Set the bounds of the component
     */
    private void setupBounds(int layout_width, int layout_height) {
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();

        int minValue = Math.min(layout_width - paddingLeft - paddingRight,
                layout_height - paddingBottom - paddingTop);

        int circleDiameter = (minValue) / 2 - outerWidth - outerWidth - 5;
        // Calc the Offset if needed for centering the wheel in the available space
        int xCenter = (layout_width - paddingLeft - paddingRight) / 2;
        int yCenter = (layout_height - paddingTop - paddingBottom) / 2;
        outerWidth = xCenter / 6;
        outerBounds =
                new RectF(xCenter - circleDiameter, yCenter - circleDiameter, xCenter + circleDiameter,
                        yCenter + circleDiameter);
        circleDiameter -= outerWidth / 2;
        innerBounds =
                new RectF(xCenter - circleDiameter, yCenter - circleDiameter, xCenter + circleDiameter,
                        yCenter + circleDiameter);
    }

//    public void setCallback(ProgressCallback progressCallback) {
//        callback = progressCallback;
//    }

    //----------------------------------
    //Animation stuff
    //----------------------------------

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int start = outerStart - 90;
        int angle = outerFinish - outerStart;
//        Log.w("onDraw", "out start=" + start + " angle " + angle+reDraw);
        canvas.drawArc(outerBounds, 360, 360, false, outerCirclePaint);
        canvas.drawArc(outerBounds, start, angle, false, outerProgressPaint);
        canvas.drawArc(innerBounds, 360, 360, false, innerBackgroundPaint);
        start = innerStart - 90;
        angle = innerFinish - innerStart;
//        Log.w("onDraw in     ", "start=" + start + " angle " + angle);
        canvas.drawArc(innerBounds, start, angle, true, innerProgressPaint);
        invalidate();
    }

    public void reUpdate(int start, int finish, int inner) {
        outerStart = start % 360;
        outerFinish = finish % 360;
        innerFinish = inner % 360;
        innerStart = innerFinish - 3;
        reDraw = true;
    }
    public void stopUpdate() {
        reDraw = false;
    }
}

