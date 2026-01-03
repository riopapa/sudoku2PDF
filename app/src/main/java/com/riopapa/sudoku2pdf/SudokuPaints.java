package com.riopapa.sudoku2pdf;

import static com.riopapa.sudoku2pdf.ActivityMain.onePos;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import com.riopapa.sudoku2pdf.Model.Sudoku;

public class SudokuPaints {
    public Paint pBoxIn;
    public Paint pBoxOut;
    public Paint pDotted;
    public Paint pNumb;
    public Paint pCount;
    public Paint pMemo;
    public Paint pSig;

    public SudokuPaints(Context context, Sudoku su, int boxWidth) {

        int[] numbColors = { R.color.num_color_0, R.color.num_color_1, R.color.num_color_2, R.color.num_color_3};
        int[] idxColors = { R.color.idx_color_0, R.color.idx_color_1, R.color.idx_color_2, R.color.idx_color_3};

        // inner box
        pBoxIn = new Paint();
        pBoxIn.setColor(ContextCompat.getColor(context, R.color.boxIn));
        pBoxIn.setStyle(Paint.Style.STROKE);
        pBoxIn.setStrokeWidth(2);
        pBoxIn.setAlpha(su.opacity * 3 / 4);
        pBoxIn.setPathEffect(new DashPathEffect(new float[]{3, 3}, 0));

        // outer box
        pBoxOut = new Paint();
        pBoxOut.setColor(ContextCompat.getColor(context, R.color.boxOut));
        pBoxOut.setStyle(Paint.Style.STROKE);
        pBoxOut.setStrokeWidth(3);
        pBoxOut.setAlpha(su.opacity);

        // inner dotted box
        pDotted = new Paint();
        pDotted.setPathEffect(new DashPathEffect(new float[]{3, 4}, 0));
        pDotted.setColor(context.getColor(R.color.dotLine));
        pDotted.setStrokeWidth(1);
        pDotted.setAlpha(su.opacity * 2 / 3);

        // number
        pNumb = new Paint();
        pNumb.setColor(numbColors[onePos % numbColors.length]);
        pNumb.setAlpha(su.opacity);
        pNumb.setStrokeWidth(2);
        pNumb.setTypeface(context.getResources().getFont(R.font.good_times));
        pNumb.setTextAlign(Paint.Align.CENTER);
        pNumb.setStyle(Paint.Style.FILL_AND_STROKE);
        pNumb.setTextSize(new OptimalFontSize().calc(pNumb, boxWidth));

        // count
        pCount = new Paint();
        pCount.setColor(idxColors[onePos % idxColors.length]);
        pCount.setAlpha(su.opacity * 2 / 3);
        pCount.setStrokeWidth(1);
        pCount.setTypeface(context.getResources().getFont(R.font.good_times));
        pCount.setTextSize((float) boxWidth * 4 / 10);
        pCount.setStyle(Paint.Style.FILL);

        // memo
        pMemo = new Paint();
        pMemo.setColor(context.getColor(R.color.memoBox));
        pMemo.setStyle(Paint.Style.STROKE);
        pMemo.setStrokeWidth(0);
        pMemo.setTextAlign(Paint.Align.CENTER);
        pMemo.setStyle(Paint.Style.FILL_AND_STROKE);
        pMemo.setTextSize(64);

        // signature
        pSig = new Paint();
        pSig.setColor(Color.BLUE);
        pSig.setTextAlign(Paint.Align.RIGHT);
        pSig.setStyle(Paint.Style.STROKE);
        pSig.setStrokeWidth(0);
        pSig.setAlpha(su.opacity * 3 / 4);
        pSig.setStyle(Paint.Style.FILL_AND_STROKE);
        pSig.setTextSize(36);
    }
}
