package com.urrecliner.sudoku2pdf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;

import static com.urrecliner.sudoku2pdf.MainActivity.circleProgress;

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

//    private static final String TAG = CircleProgress.class.getSimpleName();

    private static int outerWidth = 150;
    private static int [] outerProgressColors = {0,0};

    private static int innerStart = 0;
    private static int innerFinish = 0;

    private static int madeCount = 0;
    private static int puzzleCount = 0;
    private static int angle = 0;
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
        int outerCirCleColor = ContextCompat.getColor(getContext(),R.color.color_outCircle);;
        outerCirclePaint.setColor(outerCirCleColor);
        outerCirclePaint.setAntiAlias(true);
        outerCirclePaint.setStyle(Paint.Style.STROKE);
        outerCirclePaint.setStrokeWidth(outerWidth);

        outerProgressPaint.setAntiAlias(true);
        outerProgressPaint.setStyle(Paint.Style.STROKE);
        outerProgressPaint.setStrokeWidth(outerWidth);
        outerProgressColors[0] = ContextCompat.getColor(getContext(),R.color.color_even);
        outerProgressColors[1] = ContextCompat.getColor(getContext(),R.color.color_odd);

        int innerBackGroundColor = ContextCompat.getColor(getContext(),R.color.color_innerBase);;
        innerBackgroundPaint.setColor(innerBackGroundColor);
        innerBackgroundPaint.setAntiAlias(true);
        innerBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        innerProgressPaint.setColor(ContextCompat.getColor(getContext(),R.color.color_inner));
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
        canvas.drawArc(outerBounds, 360, 360, false, outerCirclePaint);
        for (int start = 0; start < madeCount; start++) {
            outerProgressPaint.setColor(outerProgressColors[start%2]);
            canvas.drawArc(outerBounds, ((madeCount+start)*angle)-90, angle, false, outerProgressPaint);
        }

        canvas.drawArc(innerBounds, 360, 360, false, innerBackgroundPaint);
        int start = innerStart - 90;
        int angle = innerFinish - innerStart;
//        Log.w("onDraw in     ", "start=" + start + " angle " + angle);
        canvas.drawArc(innerBounds, start, angle, true, innerProgressPaint);
        invalidate();
    }

    public void updateMade(int made, int duration) {
        madeCount = made;

        AnimationSet animSet = new AnimationSet(true);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.setFillAfter(true);
        animSet.setFillEnabled(true);
        final RotateAnimation animRotate = new RotateAnimation(0.0f, angle,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);

        animRotate.setDuration(duration);
        animRotate.setFillAfter(true);
        animSet.addAnimation(animRotate);
        circleProgress.startAnimation(animSet);
    }

    public void updateTried(int inner) {
        innerStart = inner % 360;
        innerFinish = innerStart + 3;
    }
    public void initiate(int puzzle) {
        puzzleCount = puzzle;
        angle = 360 / puzzleCount;
        madeCount = 0;
        innerStart = 0;
//        setupPaints();
    }
}

