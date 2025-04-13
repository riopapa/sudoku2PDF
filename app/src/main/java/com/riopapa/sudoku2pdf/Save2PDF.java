package com.riopapa.sudoku2pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;

import com.riopapa.sudoku2pdf.Model.Sudoku;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Locale;

class Save2PDF {

    String downLoadFolder;
    File outFolder, outFile;
    String fileDate, fileInfo;
    Bitmap sigMap;
    Paint pBoxIn, pBoxOut, pDotted, pNumb, pMemo, pSig, pCount;
    int pgWidth = 210*10, pgHeight = 297*10;  // A4 size
    int meshType, twoSix, boxWidth, boxWidth3, boxWidth2, space, pageNbr;

    public Save2PDF(String [] blankTables, String [] answerTables, Sudoku su,
                    Context context, String filePrint) {

        downLoadFolder = Environment.getExternalStorageDirectory().getPath()+"/download";
        final SimpleDateFormat sdfDate = new SimpleDateFormat("yy-MM-dd HH.mm.ss", Locale.US);
        fileDate = sdfDate.format(System.currentTimeMillis());
        fileInfo = "b"+su.blank +"p"+su.quiz;
        outFolder = new File(downLoadFolder);
        if (!outFolder.exists())
            if (outFolder.mkdirs())
                Log.i("folder","Sudoku Folder");
        outFile = new File(outFolder, "su_" + fileDate + " " + fileInfo + " " + su.name);
        sigMap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.my_sign_yellow);
        sigMap = Bitmap.createScaledBitmap(sigMap, sigMap.getWidth() / 3,
                sigMap.getHeight() / 3, false);
        meshType = su.mesh;
        twoSix = su.nbrPage;
        if (twoSix == 2)
            boxWidth = (pgHeight-60) / (10 + 1 + 10);
        else if (twoSix == 6)
            boxWidth = pgHeight / (11*3-1);
        else
            boxWidth = (pgWidth - 300) / 10;
        boxWidth3 = boxWidth / 3;
        boxWidth2 = boxWidth / 2;
        space = boxWidth*2/8;  // space = 2 : 24, 3:
        pageNbr = 0;
        PdfDocument document = new PdfDocument();
        Canvas canvas;
        PdfDocument.Page page;

        pBoxIn = new Paint();      // inner box    (quiz)
        pBoxIn.setColor(context.getColor(R.color.innerBox));
        pBoxIn.setStyle(Paint.Style.STROKE);
        pBoxIn.setStrokeWidth(2);
        pBoxIn.setAlpha(su.opacity *3/4);
        pBoxIn.setPathEffect(new DashPathEffect(new float[] {3,3}, 0));

        pBoxOut = new Paint();     // outer box
        pBoxOut.setColor(Color.BLACK);
        pBoxOut.setStyle(Paint.Style.STROKE);
        pBoxOut.setStrokeWidth(3);
        pBoxOut.setAlpha(su.opacity);

        pDotted = new Paint();        // inner dotted box
        pDotted.setPathEffect(new DashPathEffect(new float[] {3, 4}, 0));
        pDotted.setColor(context.getColor(R.color.dotLine));
        pDotted.setStrokeWidth(1);
        pDotted.setAlpha(su.opacity*2/3);

        pNumb = new Paint();        // number
        pNumb.setColor(context.getResources().getColor(R.color.number));
        pNumb.setAlpha(su.opacity);
        pNumb.setStrokeWidth(2);
        pNumb.setTypeface(context.getResources().getFont(R.font.good_times));
        pNumb.setTextSize((float) boxWidth * 8 / 10);
        pNumb.setTextAlign(Paint.Align.CENTER);
        pNumb.setStyle(Paint.Style.FILL_AND_STROKE);

        pCount = new Paint();        // number
        pCount.setColor(context.getResources().getColor(R.color.count));
        pCount.setAlpha(su.opacity*2/3);
        pCount.setStrokeWidth(1);
        pCount.setTypeface(context.getResources().getFont(R.font.good_times));
        pCount.setTextSize((float) boxWidth * 3 / 10);
        pCount.setStyle(Paint.Style.FILL);

        pMemo = new Paint();
        pMemo.setColor(context.getColor(R.color.memoBox));
        pMemo.setStyle(Paint.Style.STROKE);
        pMemo.setStrokeWidth(0);
        pMemo.setTextAlign(Paint.Align.CENTER);
        pMemo.setStyle(Paint.Style.FILL_AND_STROKE);
        pMemo.setTextSize(64);

        pSig = new Paint();
        pSig.setColor(Color.BLUE);
        pSig.setTextAlign(Paint.Align.RIGHT);
        pSig.setStyle(Paint.Style.STROKE);
        pSig.setStrokeWidth(0);
        pSig.setAlpha(su.opacity *3/4);
        pSig.setStyle(Paint.Style.FILL_AND_STROKE);
        pSig.setTextSize(36);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pgWidth, pgHeight, pageNbr).create();
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();

        for (int nbrQz = 0; nbrQz < blankTables.length; nbrQz++) {
            if (nbrQz == 0)
                addSignature(su, sigMap, pgWidth, pgHeight, canvas, pSig, true);
            else if ((twoSix == 2 && nbrQz % 2 == 0) ||
                    (twoSix == 6 && nbrQz > 5 && nbrQz % 6 == 0) ||
                    twoSix == 1) {
                pageNbr++;
                addSignature(su, sigMap, pgWidth, pgHeight, canvas, pSig, true);
                document.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(pgWidth, pgHeight, pageNbr).create();
                // start a page
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
            }

            int [][] xyTable = str2suArray(blankTables[nbrQz]);
            int xBase;
            if (twoSix == 2) {
                xBase = boxWidth + 10;
            } else if (twoSix == 6) {
                xBase = boxWidth + 5 + ((nbrQz % 6) % 2) * 10 * boxWidth;
            } else {
                xBase = boxWidth + 10;
            }

            int yBase;
            if (twoSix == 2) {
                yBase = boxWidth/4 + (nbrQz % 2) * boxWidth * 100 / 10;
            } else if (twoSix == 6) {
                yBase = boxWidth + ((nbrQz%6) / 2) * boxWidth * 100 / 10;
            } else {
                yBase = boxWidth;
            }

            int xGap = boxWidth/2;
            int yGap = boxWidth3+boxWidth3+boxWidth3/3;
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xPos, yPos, xPos + boxWidth, yPos + boxWidth, pBoxIn);
                    if (xyTable[row][col] > 0) {
                        canvas.drawText(String.valueOf(xyTable[row][col]), xPos + xGap, yPos + yGap, pNumb);
                    } else if (meshType == 1){
                        canvas.drawLine(xPos + boxWidth3, yPos, xPos + boxWidth3, yPos + boxWidth3, pDotted);
                        canvas.drawLine(xPos + boxWidth3 + boxWidth3, yPos,
                                xPos + boxWidth3 + boxWidth3, yPos + boxWidth3, pDotted);
                        canvas.drawLine(xPos, yPos + boxWidth3,
                                xPos + boxWidth, yPos + boxWidth3, pDotted);
                    } else if (meshType == 2){
                        canvas.drawLine(xPos + boxWidth3, yPos, xPos + boxWidth3, yPos + boxWidth, pDotted);
                        canvas.drawLine(xPos + boxWidth3 + boxWidth3, yPos,
                                xPos + boxWidth3 + boxWidth3, yPos + boxWidth, pDotted);
                        canvas.drawLine(xPos, yPos + boxWidth3,
                                xPos + boxWidth, yPos + boxWidth3, pDotted);
                        canvas.drawLine(xPos, yPos + boxWidth3 + boxWidth3,
                                xPos + boxWidth, yPos + boxWidth3 + boxWidth3, pDotted);
                    } else if (meshType == 3){
                        canvas.drawLine(xPos + boxWidth2, yPos, xPos + boxWidth2, yPos + boxWidth3, pDotted);
                        canvas.drawLine(xPos, yPos + boxWidth3,
                                xPos + boxWidth, yPos + boxWidth3, pDotted);
                    }
                }
            }

            for (int row = 0; row < 9; row+=3)
                for (int col = 0; col < 9; col+=3) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xBase, yBase, xPos + boxWidth*3, yPos + boxWidth*3, pBoxOut);
                }
            canvas.drawText("{"+(nbrQz+1)+"}", xBase + 4 + boxWidth*9, yBase+10, pCount);
        }
        addSignature(su, sigMap, pgWidth, pgHeight, canvas, pSig, true);

        document.finishPage(page);

        File filePath = new File(outFile+" Quiz.pdf");
        try {
            document.writeTo(Files.newOutputStream(filePath.toPath()));
        } catch (IOException e) {
            Log.e("main", "error " + e);
        }

        document.close();

        if (su.answer)
            makeAnswer(blankTables, answerTables, su);

        if (filePrint.equals("f"))
            new ShareFile().show(context, outFolder.getAbsolutePath());
        else        // "p"
            PDF2Printer(context, filePath.getPath());
    }

    void PDF2Printer (Context context, String fullPath) {
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printAdapter = new PdfDocumentAdapter(context, fullPath);
            printManager.print("Document", printAdapter, new PrintAttributes.Builder().build());
        } catch (Exception e) {
            Log.e("PDF2Printer", "error " + e);
        }
    }

    //         Create Answer Page ------------
    void makeAnswer(String[] blankTables, String[] answerTables,
                                   Sudoku su) {
        File filePath;
        PdfDocument.Page page;
        PdfDocument document;
        Canvas canvas;

        document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pgWidth, pgHeight, 1).create();

        page = document.startPage(pageInfo);
        canvas = page.getCanvas();

        boxWidth = (pgWidth - 50) / 40;    // 4 answer for 1 line
        pBoxIn.setStrokeWidth(1);
        pNumb.setTextSize(boxWidth/2.3f); // pNumb : answer number
        pNumb.setAlpha(su.opacity);
        pMemo.setTextSize(boxWidth/2f);  // pMemo : given number
        pMemo.setColor(Color.DKGRAY);
        pMemo.setAlpha(su.opacity);
        pCount.setTextSize((float) boxWidth * 5 / 10);

        pageNbr = 0;
        for (int nbrQz = 0; nbrQz < answerTables.length; nbrQz++) {
            int y20 = nbrQz % 20;    // 20 answers per page
            if (nbrQz > 19 && y20 == 0) {
                pageNbr++;
                addSignature(su, sigMap, pgWidth, pgHeight, canvas, pSig, false);
                document.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(pgWidth, pgHeight, pageNbr).create();
                // start a page
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
            }

            int [][] ansTable = str2suArray(answerTables[nbrQz]);
            int [][] blankTable = str2suArray(blankTables[nbrQz]);
            int xBase = 50 + (nbrQz % 4) * boxWidth * 95 / 10;
            int yBase = 80 + (int) (y20 / 4) * boxWidth * 11;
            int xGap = boxWidth/2;
            int yGap = boxWidth*3/4;
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xPos, yPos, xPos + boxWidth, yPos + boxWidth, pBoxIn);
                    canvas.drawText(String.valueOf(ansTable[row][col]), xPos + xGap, yPos + yGap,
                            (blankTable[row][col] == 0)? pNumb : pMemo);
                }
            }
            canvas.drawText("{"+(nbrQz+1)+"}", xBase + boxWidth, yBase - 8, pCount);

            pBoxOut.setStrokeWidth(2);
            for (int row = 0; row < 9; row+=3)
                for (int col = 0; col < 9; col+=3) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xBase, yBase, xPos + boxWidth*3, yPos + boxWidth*3,
                            pBoxOut);
                }
        }

        addSignature(su, sigMap, pgWidth, pgHeight, canvas, pSig, false);
        document.finishPage(page);

        filePath = new File(outFile+" Sol.pdf");
        try {
            document.writeTo(Files.newOutputStream(filePath.toPath()));
        } catch (IOException e) {
            Log.e("main", "error " + e);
        }
        document.close();
    }

    void addSignature(Sudoku su, Bitmap sigMap, int pgWidth, int pgHeight,
                                     Canvas canvas, Paint paint, boolean top) {
        int xPos;
        int yPos;
        Paint p = new Paint();
        p.setColor(0xFF4488FF);
        p.setTextSize(40);
        p.setTextAlign(Paint.Align.RIGHT);
        p.setStyle(Paint.Style.STROKE);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        int inc = (int) p.getTextSize() * 5 / 3;
        if (top) {
            xPos = pgWidth - 40;
            yPos = 50;
            canvas.drawText(fileDate.substring(0,8),xPos, yPos, p);
            yPos += inc;
            p.setColor(0xFFAF4844);
            canvas.drawText(fileDate.substring(9),xPos, yPos, p);
            yPos += inc+inc/2;
            p.setTextSize(48);
            canvas.drawText("□"+su.blank,xPos, yPos, p);
            yPos += inc/2;
            xPos -= sigMap.getWidth();
        } else {
            xPos = pgWidth / 2;
            yPos = pgHeight - 80;
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(fileDate, xPos, yPos, paint);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(fileInfo, xPos + 30, yPos, paint);
            xPos += 120 + inc;
            yPos -= sigMap.getHeight()/2;
        }
        p.setAlpha(paint.getAlpha()*3/4);
        canvas.drawBitmap(sigMap, xPos, yPos, p);
    }

    int [][] str2suArray(String str) {
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