package com.urrecliner.sudoku2pdf;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

class MakePDF {

    void createPDF(String [] blankTables, String [] ansTables) {

//        int [][] xyTable = new int[9][9];

        String directory_path = Environment.getExternalStorageDirectory().getPath() + "/download";

        File file = new File(directory_path);
        if (!file.exists()) {
            Log.w("file", file.toString()+" created");
            file.mkdirs();
        }
        final SimpleDateFormat sdfDate = new SimpleDateFormat("yy-MM-dd HH.mm.ss", Locale.US);
        String fileDate = "sudoku_" + sdfDate.format(System.currentTimeMillis());

        int pgWidth = 200*5, pgHeight = 290*5;
        int boxWidth = pgWidth / 17;
        int space = pgWidth / 44;
        int pageNbr = 0;
        Canvas canvas = null;
        PdfDocument.Page page = null;
        PdfDocument document = new PdfDocument();
        // crate a page description
        Paint paint = new Paint();
        paint.setColor(Color.DKGRAY);
        paint.setStyle(Paint.Style.STROKE);
        for (int idx = 0; idx < blankTables.length; idx++) {
            int [][] xyTable = str2suArray(blankTables[idx]);
            if (idx % 2 == 0) {
                if (idx != 0) {
                   document.finishPage(page);
                }
                pageNbr++;
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pgWidth, pgHeight, pageNbr).create();
                // start a page
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                paint.setTextSize(14);
                paint.setStrokeWidth(0);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawText(fileDate, pgWidth/3, space + 16, paint);
            }
            paint.setTextSize(28);
            int xBase = space + 40;
            int yBase = space + (idx % 2) * boxWidth * 10 + boxWidth + 60;
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    paint.setStrokeWidth(1);
                    paint.setPathEffect(new DashPathEffect(new float[] {1,2}, 0));
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawRect(xPos, yPos, xPos + boxWidth, yPos + boxWidth, paint);
                    if (xyTable[row][col] > 0) {
                        paint.setStrokeWidth(2);
                        paint.setStyle(Paint.Style.FILL_AND_STROKE);
                        canvas.drawText("" + xyTable[row][col], xPos + space , yPos + space + 16, paint);
                    }
                }
            }
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setPathEffect(null);
            for (int row = 0; row < 9; row+=3)
                for (int col = 0; col < 9; col+=3) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xBase, yBase, xPos + boxWidth*3, yPos + boxWidth*3, paint);
                }
            paint.setStrokeWidth(0);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setTextSize(18);
            canvas.drawText(""+idx, xBase + 24, yBase - 12, paint);
        }

        document.finishPage(page);

        String targetPdf = directory_path + "/"+fileDate+".pdf";
        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            Log.e("main", "error " + e.toString());
        }
        // close the document
        document.close();


//         Create Answer Page ------------

        document = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pgWidth, pgHeight, 1).create();
        // start a page
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();
        paint.setTextSize(14);
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawText(fileDate, pgWidth/3, space + 16, paint);
        boxWidth = boxWidth * 7 / 16;
        for (int nbrInPage = 0; nbrInPage < 20; nbrInPage++) {
            int [][] xyTable = str2suArray(ansTables[nbrInPage]);
            paint.setTextSize(14);
            paint.setStrokeWidth(0);
            paint.setPathEffect(new DashPathEffect(new float[] {1,2}, 0));
            int xBase = space + (nbrInPage % 4) * boxWidth * 95 / 10;
            int yBase = space + (nbrInPage / 4) * boxWidth * 10 + boxWidth + 40;
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawRect(xPos, yPos, xPos + boxWidth, yPos + boxWidth, paint);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawText("" + xyTable[row][col], xPos + 8, yPos + 16, paint);
                }
            }
            paint.setStrokeWidth(1);
            paint.setTextSize(10);
            canvas.drawText(""+nbrInPage, xBase + 16, yBase - 8, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setPathEffect(null);
            for (int row = 0; row < 9; row+=3)
                for (int col = 0; col < 9; col+=3) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xBase, yBase, xPos + boxWidth*3, yPos + boxWidth*3, paint);
                }
        }
        document.finishPage(page);

        // write the document content
        targetPdf = directory_path + "/"+ fileDate+"ans.pdf";
        filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            Log.e("main", "error " + e.toString());
        }
        // close the document
        document.close();
    }

    int [][] str2suArray (String str) {
        String [] sRow = str.split(":");
        int [][] sudoku = new int [9][9];
        for (int row = 0; row < 9; row++) {
            String[] sCol = sRow[row].split(";");
            for (int col = 0; col < 9; col++)
                sudoku[row][col] = Integer.parseInt(sCol[col]);
        }
        return sudoku;
    }


}
