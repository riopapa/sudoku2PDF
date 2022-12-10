package com.urrecliner.sudoku2pdf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import static com.urrecliner.sudoku2pdf.MainActivity.mContext;

import androidx.core.content.res.ResourcesCompat;

class MakePDF {

    static void createPDF(String [] blankTables, String [] answerTables, String [] commentTables) {

//        int [][] xyTable = new int[9][9];
        String downLoadFolder = Environment.getExternalStorageDirectory().getPath() + "/download";
//        File file = new File(downLoadFolder);
//        if (!file.exists()) {
//            Log.w("file", file+" created");
//            file.mkdirs();
//        }
        Bitmap sigMap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.my_sign_yellow);
        int xSig = sigMap.getWidth() / 5;
        int ySig = sigMap.getHeight() / 5;
        sigMap = Bitmap.createScaledBitmap(sigMap, xSig, ySig, false);
        int pgWidth = 210*5, pgHeight = 297*5;  // A4 size
        int boxWidth = pgWidth / 14;    // 75
        int boxWidth3 = boxWidth / 3;
        int space = 10;
        int pageNbr = 0;
        Canvas canvas = null;
        PdfDocument.Page page = null;
        PdfDocument document = new PdfDocument();

        Paint pRect = new Paint();      // inner box    (quiz)
        pRect.setColor(Color.BLUE);
        pRect.setStyle(Paint.Style.STROKE);
        pRect.setStrokeWidth(1);
        pRect.setPathEffect(new DashPathEffect(new float[] {1,2}, 0));

        Paint pRectO = new Paint();     // outer box
        pRectO.setColor(Color.BLACK);
        pRectO.setStyle(Paint.Style.STROKE);
        pRectO.setStrokeWidth(2);

        Paint pDotted = new Paint();        // inner dotted box (answer)
        pDotted.setPathEffect(new DashPathEffect(new float[] {2,4}, 0));
        pDotted.setColor(Color.BLUE);
        pDotted.setStrokeWidth(1);

        Paint pNumb = new Paint();
        pNumb.setColor(Color.BLUE);
        pNumb.setStrokeWidth(1);
        pNumb.setTypeface(ResourcesCompat.getFont(mContext, R.font.hj_hanseo_a));
        pNumb.setTextSize(36);
        pNumb.setStyle(Paint.Style.FILL_AND_STROKE);

        Paint pMemo = new Paint();
        pMemo.setColor(Color.BLUE);
        pMemo.setStyle(Paint.Style.STROKE);
        pMemo.setStrokeWidth(0);
        pMemo.setStyle(Paint.Style.FILL_AND_STROKE);
        pMemo.setTextSize(32);

        Paint pSig = new Paint();
        pSig.setColor(Color.GREEN);
        pSig.setStyle(Paint.Style.STROKE);
        pSig.setStrokeWidth(0);
        pSig.setStyle(Paint.Style.FILL_AND_STROKE);
        pSig.setTextSize(16);

        for (int idx = 0; idx < blankTables.length; idx++) {
            int [][] xyTable = str2suArray(blankTables[idx]);
            if (idx % 2 == 0) {
                if (idx != 0) {
                    printSignature(sigMap, xSig, ySig, pgWidth, pgHeight, canvas, pSig);
                    document.finishPage(page);
                }
                pageNbr++;
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pgWidth, pgHeight, pageNbr).create();
                // start a page
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                pMemo.setTextSize(20);
                pMemo.setStrokeWidth(0);
                pMemo.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawText(fileDate, pgWidth/3f, space + 16, pMemo);
            }
            int xBase = space + 10;
            int yBase = space + (idx % 2) * boxWidth * 9 + (idx % 2) * boxWidth / 2 + 40;
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xPos, yPos, xPos + boxWidth, yPos + boxWidth, pRect);
                    if (xyTable[row][col] > 0) {
                        canvas.drawText("" + xyTable[row][col], xPos + boxWidth/2f -4, yPos + space + boxWidth/2f, pNumb);
                    } else {
                        canvas.drawLine(xPos + boxWidth3, yPos, xPos + boxWidth3, yPos + boxWidth, pDotted);
                        canvas.drawLine(xPos + boxWidth3 + boxWidth3, yPos,
                                xPos + boxWidth3 + boxWidth3, yPos + boxWidth, pDotted);
                        canvas.drawLine(xPos, yPos + boxWidth3,
                                xPos + boxWidth, yPos + boxWidth3, pDotted);
                        canvas.drawLine(xPos, yPos + boxWidth3 + boxWidth3,
                                xPos + boxWidth, yPos + boxWidth3 + boxWidth3, pDotted);
                    }
                }
            }

            for (int row = 0; row < 9; row+=3)
                for (int col = 0; col < 9; col+=3) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xBase, yBase, xPos + boxWidth*3, yPos + boxWidth*3, pRectO);
                }
            canvas.drawText("("+idx+") "+commentTables[idx], xBase + 12 + boxWidth*9, yBase + boxWidth/2f, pMemo);
        }
        printSignature(sigMap, xSig, ySig, pgWidth, pgHeight, canvas, pSig);

        document.finishPage(page);

//        String targetPdf = directory_path + "/"+fileDate+" Qz.pdf";
        File filePath = new File(downLoadFolder, fileDate+" Qz.pdf");
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            Log.e("main", "error " + e);
        }
        // close the document
        document.close();


//         Create Answer Page ------------

        document = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pgWidth, pgHeight, 1).create();
        // start a page
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();

        canvas.drawText(fileDate, pgWidth/3f, space + 16, pMemo);

        boxWidth = pgWidth / 4 / 10;    // 4 answer for 1 line, maybe 26

        pNumb.setTextSize(boxWidth/2f+4); // pNumb : given number
        pMemo.setTextSize(boxWidth/2f);  // pMemo : answer
        pMemo.setColor(Color.BLACK);
        int xGap = boxWidth/3;
        int yGap = boxWidth*2/3;
        for (int nbrInPage = 0; nbrInPage < answerTables.length; nbrInPage++) {
            int [][] ansTable = str2suArray(answerTables[nbrInPage]);
            int [][] blankTable = str2suArray(blankTables[nbrInPage]);
            int xBase = space +20 + (nbrInPage % 4) * boxWidth * 95 / 10;
            int yBase = space + (nbrInPage / 4) * boxWidth * 10 + boxWidth + 40;
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xPos, yPos, xPos + boxWidth, yPos + boxWidth, pRect);
                    canvas.drawText("" + ansTable[row][col], xPos + xGap, yPos + yGap,
                            (blankTable[row][col] == 0)? pNumb:pMemo);
                }
            }
            canvas.drawText("[ "+nbrInPage+" ]", xBase + 60, yBase - 8, pNumb);

            for (int row = 0; row < 9; row+=3)
                for (int col = 0; col < 9; col+=3) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xBase, yBase, xPos + boxWidth*3, yPos + boxWidth*3, pRectO);
                }
        }
        printSignature(sigMap, xSig, ySig, pgWidth, pgHeight, canvas, pSig);
        document.finishPage(page);

        // write the document content
        filePath = new File(downLoadFolder,fileDate+"Ans.pdf");
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            Log.e("main", "error " + e);
        }
        // close the document
        document.close();
    }

    private static void printSignature(Bitmap sigMap, int xSig, int ySig, int pgWidth, int pgHeight, Canvas canvas, Paint paint) {
        canvas.drawBitmap(sigMap, pgWidth - xSig - 30, pgHeight - ySig - 20, paint);
        canvas.drawText("generated by", pgWidth - xSig - 40, pgHeight - ySig - 30, paint);
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