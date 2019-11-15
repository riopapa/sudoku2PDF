package com.urrecliner.sudoku2pdf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
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

    private boolean reDraw = false;
    private int outerStart = 0;
    private int outerFinish = 0;
    private int outerProgressColor = Color.RED;
    private int outerCirCleColor = Color.DKGRAY;
    private int outerWidth = 150;

    private int innerStart = 0;
    private int innerFinish = 0;
    private int innerProgressColor = Color.MAGENTA;
    private int innerBackGroundColor = Color.GRAY;

    //Paints
    private Paint outerCirclePaint = new Paint();
    private Paint outerProgressPaint = new Paint();
    private Paint innerBackgroundPaint = new Paint();
    private Paint innerProgressPaint = new Paint();

    //Rectangles
    private RectF outerBounds = new RectF();
    private RectF innerBounds = new RectF();

//    private ProgressCallback callback;

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

        // Width should equal to Height, find the min value to setup the circle
        int minValue = Math.min(layout_width - paddingLeft - paddingRight,
                layout_height - paddingBottom - paddingTop);

//        int circleDiameter = Math.min(minValue, circleRadius * 2 - outerWidth * 2);
        int circleDiameter = (layout_width - paddingLeft - paddingRight) / 2 - outerWidth - outerWidth - 5;
        // Calc the Offset if needed for centering the wheel in the available space
        int xCenter = (layout_width - paddingLeft - paddingRight) / 2;
        int yCenter = (layout_height - paddingTop - paddingBottom) / 2;
//        int xOffset = (layout_width - paddingLeft - paddingRight - circleDiameter) / 2 + paddingLeft;
//        int yOffset = (layout_height - paddingTop - paddingBottom - circleDiameter) / 2 + paddingTop;
//        Log.w("outerBounds",xOffset+"x"+yOffset+" dia"+circleDiameter+" w"+outerWidth);
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
        Log.w("onDraw", "out start=" + start + " angle " + angle+reDraw);
        canvas.drawArc(outerBounds, 360, 360, false, outerCirclePaint);
        canvas.drawArc(outerBounds, start, angle, false, outerProgressPaint);
        canvas.drawArc(innerBounds, 360, 360, false, innerBackgroundPaint);
        start = innerStart - 90;
        angle = innerFinish - innerStart;
        Log.w("onDraw in     ", "start=" + start + " angle " + angle);
        canvas.drawArc(innerBounds, start, angle, true, innerProgressPaint);
        invalidate();
    }

//    public interface ProgressCallback {
//        /**
//         * Method to call when the progress reaches a value
//         * in order to avoid float precision issues, the progress
//         * is rounded to a float with two decimals.
//         *
//         * In indeterminate mode, the callback is called each time
//         * the wheel completes an animation cycle, with, the progress value is -1.0f
//         *
//         * @param progress a double value between 0.00 and 1.00 both included
//         */
//        public void onProgressUpdate(float progress);
//    }

//    public void setOuterProgressColor(int outerProgressColor) {
//        this.outerProgressColor = outerProgressColor;
//    }
//
//    public void setOuterCirCleColor(int outerCirCleColor) {
//        this.outerCirCleColor = outerCirCleColor;
//    }
//
//    public void setOuterWidth(int outerWidth) {
//        this.outerWidth = outerWidth;
//    }
//
//    public void setInnerProgressColor(int innerProgressColor) {
//        this.innerProgressColor = innerProgressColor;
//    }
//
//    public void setInnerBackGroundColor(int innerBackGroundColor) {
//        this.innerBackGroundColor = innerBackGroundColor;
//    }

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

