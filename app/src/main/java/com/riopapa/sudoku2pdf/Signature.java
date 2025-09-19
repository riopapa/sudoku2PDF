package com.riopapa.sudoku2pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import com.riopapa.sudoku2pdf.Model.Sudoku;

public class Signature {
    void add(Context context, String fileDate, Sudoku su, Bitmap sigMap, int pgWidth, Canvas canvas) {
        float xPos,  yPos;
        Paint nPaint = new Paint();
        nPaint.setTextSize(40);
        nPaint.setStyle(Paint.Style.STROKE);
        nPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        nPaint.setColor(ContextCompat.getColor(context, R.color.pdf_date));
        xPos = pgWidth - 60;
        yPos = 40;
        canvas.save();
        canvas.rotate(90, xPos, yPos);
        canvas.drawText(fileDate.substring(0,8), xPos, yPos, nPaint);

        xPos += nPaint.getTextSize() * 5;
        nPaint.setColor(ContextCompat.getColor(context, R.color.pdf_time));
        canvas.drawText(fileDate.substring(9), xPos, yPos, nPaint);

        xPos += nPaint.getTextSize() * 5;
        nPaint.setTextSize(nPaint.getTextSize() * 12 / 10);
        nPaint.setColor(ContextCompat.getColor(context, R.color.pdf_blanks));
        canvas.drawText("□ "+su.nbrOfBlank,xPos, yPos, nPaint);

        xPos += nPaint.getTextSize() * 3;
        canvas.drawBitmap(sigMap, xPos, yPos - 50, nPaint);
        canvas.restore();
    }
}
