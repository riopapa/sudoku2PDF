package com.urrecliner.sudoku2pdf;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.urrecliner.sudoku2pdf.MainActivity.fileDate;

class MakePDF {

    static void createPDF(String [] blankTables, String [] answerTables, String [] commentTables) {

//        int [][] xyTable = new int[9][9];
        String directory_path = Environment.getExternalStorageDirectory().getPath() + "/download";
        File file = new File(directory_path);
        if (!file.exists()) {
            Log.w("file", file.toString()+" created");
            file.mkdirs();
        }

        int pgWidth = 210*5, pgHeight = 297*5;  // A4 size
        int boxWidth = pgWidth / 14;
        int space = 10;
        Log.w("space","space "+space+" boxWidth="+boxWidth);
        int pageNbr = 0;
        Canvas canvas = null;
        PdfDocument.Page page = null;
        PdfDocument document = new PdfDocument();
        // crate a page description
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
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
                paint.setTextSize(20);
                paint.setStrokeWidth(0);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawText(fileDate, pgWidth/3, space + 16, paint);
            }
            paint.setTextSize(28);
            int xBase = space + 10;
            int yBase = space + (idx % 2) * boxWidth * 9 + (idx % 2) * boxWidth / 2 + 40;
            Log.w("xy","base "+xBase+" x "+yBase);
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
                        canvas.drawText("" + xyTable[row][col], xPos + boxWidth/2 , yPos + space + boxWidth/2, paint);
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
            paint.setTextSize(24);
            canvas.drawText("("+idx+") "+commentTables[idx], xBase + 12 + boxWidth*9, yBase + boxWidth/2, paint);
        }

        document.finishPage(page);

        String targetPdf = directory_path + "/"+fileDate+" Qz.pdf";
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
        paint.setTextSize(24);
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawText(fileDate, pgWidth/3, space + 16, paint);
        boxWidth = pgWidth / 4 / 10;    // 4 answer for 1 line
        int answerFont = boxWidth / 2;
        int answerSolve = answerFont + 2;
        for (int nbrInPage = 0; nbrInPage < answerTables.length; nbrInPage++) {
            int [][] ansTable = str2suArray(answerTables[nbrInPage]);
            int [][] blankTable = str2suArray(blankTables[nbrInPage]);
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
                    int answerSize;
                    if (blankTable[row][col] == 0) {
                        answerSize = answerSolve;
                        paint.setColor(Color.BLUE);
//                        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
                    } else {
                        answerSize = answerFont;
                    }
                    paint.setTextSize(answerSize);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    canvas.drawText("" + ansTable[row][col], xPos + answerSize/2, yPos + answerSize+3, paint);
                    paint.setColor(Color.BLACK);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
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
        targetPdf = directory_path + "/"+ fileDate+"Ans.pdf";
        filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            Log.e("main", "error " + e.toString());
        }
        // close the document
        document.close();
    }

    static int [][] str2suArray(String str) {
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