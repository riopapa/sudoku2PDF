package com.riopapa.sudoku2pdf;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.riopapa.sudoku2pdf.ActivityOneEdit.oneActivity;
import static com.riopapa.sudoku2pdf.MainActivity.mActivity;
import static com.riopapa.sudoku2pdf.MainActivity.mContext;
import static com.riopapa.sudoku2pdf.MainActivity.shareTo;

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

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.riopapa.sudoku2pdf.Model.Sudoku;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Locale;

class MakePDF {

    String downLoadFolder;
    File outFolder, outFile;
    String fileDate, fileInfo;
    Bitmap sigMap;
    Paint pBoxIn, pBoxOut, pDotted, pNumb, pMemo, pSig;
    int pgWidth = 210*5, pgHeight = 297*5;  // A4 size
    int meshType, twoSix, boxWidth, boxWidth3, space, pageNbr;

    void create(String [] blankTables, String [] answerTables, Sudoku su,
                       Context context) {

        downLoadFolder = Environment.getExternalStorageDirectory().getPath()+"/download";
        final SimpleDateFormat sdfDate = new SimpleDateFormat("yy-MM-dd HH.mm.ss", Locale.US);
        fileDate = sdfDate.format(System.currentTimeMillis());
        fileInfo = "b"+su.blank +"p"+su.quiz;
        outFolder = new File(downLoadFolder, "sudoku");
        if (!outFolder.exists())
            if (outFolder.mkdirs())
                Log.i("folder","Sudoku Folder");
        outFile = new File(outFolder, fileDate + " " + fileInfo + " " + su.name);
        sigMap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.my_sign_blured);
        sigMap = Bitmap.createScaledBitmap(sigMap, sigMap.getWidth() / 6,
                sigMap.getHeight() / 6, false);
        meshType = su.mesh;
        twoSix = su.nbrPage;
        if (twoSix == 2)
            boxWidth = pgHeight / (11*2-1);
        else if (twoSix == 6)
            boxWidth = pgHeight / (11*3-1);
        else
            boxWidth = (pgWidth - 200) / 10;
        boxWidth3 = boxWidth / 3;
        space = boxWidth*2/8;  // space = 2 : 24, 3:
        pageNbr = 0;
        PdfDocument document = new PdfDocument();
        Canvas canvas;
        PdfDocument.Page page;

        pBoxIn = new Paint();      // inner box    (quiz)
        pBoxIn.setColor(Color.BLACK);
        pBoxIn.setStyle(Paint.Style.STROKE);
        pBoxIn.setStrokeWidth(2);
        pBoxIn.setAlpha(su.opacity *3/4);
        pBoxIn.setPathEffect(new DashPathEffect(new float[] {4,3}, 0));

        pBoxOut = new Paint();     // outer box
        pBoxOut.setColor(Color.BLACK);
        pBoxOut.setStyle(Paint.Style.STROKE);
        pBoxOut.setStrokeWidth(3);
        pBoxOut.setAlpha(su.opacity);

        pDotted = new Paint();        // inner dotted box
        pDotted.setPathEffect(new DashPathEffect(new float[] {4, 2}, 0));
        pDotted.setColor(context.getColor(R.color.dotLine));
        pDotted.setStrokeWidth(2);
        pDotted.setAlpha(su.opacity);

        pNumb = new Paint();        // number
        pNumb.setColor(Color.BLACK);
        pNumb.setAlpha(su.opacity);
        pNumb.setStrokeWidth(1);
        pNumb.setTypeface(ResourcesCompat.getFont(context, R.font.good_times));
        pNumb.setTextSize((float) boxWidth * 8 / 10);
        pNumb.setTextAlign(Paint.Align.CENTER);
        pNumb.setStyle(Paint.Style.FILL_AND_STROKE);

        pMemo = new Paint();
        pMemo.setColor(context.getColor(R.color.memoBox));
        pMemo.setStyle(Paint.Style.STROKE);
        pMemo.setStrokeWidth(0);
        pMemo.setTextAlign(Paint.Align.CENTER);
        pMemo.setStyle(Paint.Style.FILL_AND_STROKE);
        pMemo.setTextSize(32);

        pSig = new Paint();
        pSig.setColor(Color.BLACK);
        pSig.setTextAlign(Paint.Align.RIGHT);
        pSig.setStyle(Paint.Style.STROKE);
        pSig.setStrokeWidth(0);
        pSig.setAlpha(su.opacity *3/4);
        pSig.setStyle(Paint.Style.FILL_AND_STROKE);
        pSig.setTextSize(18);

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
                yBase = boxWidth + (nbrQz % 2) * boxWidth * 105 / 10;
            } else if (twoSix == 6) {
                yBase = boxWidth + ((nbrQz%6) / 2) * boxWidth * 105 / 10;
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
                    }
                }
            }

            for (int row = 0; row < 9; row+=3)
                for (int col = 0; col < 9; col+=3) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xBase, yBase, xPos + boxWidth*3, yPos + boxWidth*3, pBoxOut);
                }
            pMemo.setTextSize(pNumb.getTextSize()/2);
            pMemo.setColor(Color.BLACK);
            canvas.drawText("{"+(nbrQz+1)+"}", xBase + 22 + boxWidth*9, yBase+10, pMemo);
        }
        addSignature(su, sigMap, pgWidth, pgHeight, canvas, pSig, true);

        document.finishPage(page);

        File filePath = new File(outFile+" Quiz.pdf");
        try {
            document.writeTo(Files.newOutputStream(filePath.toPath()));
        } catch (IOException e) {
            Log.e("main", "error " + e);
        }
        // close the document
        document.close();

        if (su.answer)
            makeAnswer(blankTables, answerTables, su);

        if (shareTo == 0)
            new ShareFile().show(context, outFolder.getAbsolutePath());
        else    // == 1
            PDF2Printer(context, filePath.getPath());
    }

    void PDF2Printer (Context context, String fullPath) {
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printAdapter = new PdfDocumentAdapter(context, fullPath);
            printManager.print("Document", printAdapter, new PrintAttributes.Builder().build());
        } catch (Exception e) {
            e.printStackTrace();
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
        // start a page
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();

        boxWidth = (pgWidth - 30) / 40;    // 4 answer for 1 line
        pBoxIn.setStrokeWidth(1);
        pNumb.setTextSize(boxWidth/2.3f); // pNumb : answer number
        pNumb.setAlpha(su.opacity);
        pMemo.setTextSize(boxWidth/2f);  // pMemo : given number
        pMemo.setColor(Color.DKGRAY);
        pMemo.setAlpha(su.opacity);
        pageNbr = 0;
        for (int nbrQz = 0; nbrQz < answerTables.length; nbrQz++) {
            if (nbrQz > 19 && nbrQz % 20 == 0) {
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
            int xBase = 30 + (nbrQz % 4) * boxWidth * 95 / 10;
            int yBase = 50 + ((nbrQz > 19) ? nbrQz-20 : nbrQz) / 4 * boxWidth * 11;
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
            canvas.drawText("{"+(nbrQz+1)+"}", xBase + boxWidth, yBase - 8, pNumb);

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

        // write the document content
        filePath = new File(outFile+" Sol.pdf");
        try {
            document.writeTo(Files.newOutputStream(filePath.toPath()));
        } catch (IOException e) {
            Log.e("main", "error " + e);
        }
        // close the document
        document.close();
    }

    void addSignature(Sudoku su, Bitmap sigMap, int pgWidth, int pgHeight,
                                     Canvas canvas, Paint paint, boolean top) {
        int xPos;
        int yPos;
        Paint p = new Paint();
        p.setTextSize(16);
        p.setColor(paint.getColor());
        p.setTextAlign(Paint.Align.RIGHT);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(0);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        int inc = (int) p.getTextSize() * 5 / 3;
        if (top) {
            xPos = pgWidth - 10;
            yPos = 50;
            canvas.drawText(fileDate.substring(0,8),xPos, yPos, p);
            yPos += inc;
            canvas.drawText(fileDate.substring(9),xPos, yPos, p);
            yPos += inc+inc/2;
            p.setTextSize(28);
            canvas.drawText("□"+su.blank,xPos, yPos, p);
            yPos += inc/2;
            xPos -= sigMap.getWidth();
        } else {
            xPos = pgWidth / 2;
            yPos = pgHeight - 50;
            canvas.drawText(fileDate+" "+fileInfo, xPos, yPos, paint);
            xPos += inc;
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