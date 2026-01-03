package com.riopapa.sudoku2pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.util.Log;

import com.riopapa.sudoku2pdf.Model.Sudoku;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

class MakePDF9x9 {

    Bitmap sigMap;
    Paint pBoxIn, pBoxOut, pDotted, pNumb, pMemo, pSig, pCount;
    int pgWidth = 210*10, pgHeight = 297*10;  // A4 size
    int meshType, twoSix, boxWidth, boxWidth3, boxWidth2, space, pageNbr;
    Context context;

    public MakePDF9x9(List<int[][]> puzzles, List<int[][]> answers, Sudoku su,
                      Context context, String filePrint) {

        int gridSize = su.gridSize; // 6 or 9

        this.context = context;
        SudokuSetUp setup = new SudokuSetUp(context, su, pgWidth, pgHeight, gridSize);
        sigMap = setup.sigMap;
        meshType = setup.meshType;
        twoSix = setup.twoSix;
        boxWidth = setup.boxWidth;
        boxWidth2 = setup.boxWidth2;
        boxWidth3 = setup.boxWidth3;
        space = setup.space;
        pageNbr = 0;

        SudokuPaints paints = new SudokuPaints(context, su, boxWidth);
        pBoxIn = paints.pBoxIn;
        pBoxOut = paints.pBoxOut;
        pDotted = paints.pDotted;
        pNumb = paints.pNumb;
        pCount = paints.pCount;
        pMemo = paints.pMemo;
        pSig = paints.pSig;

        PdfDocument document = new PdfDocument();
        Canvas canvas;
        PdfDocument.Page page;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pgWidth, pgHeight, pageNbr).create();
        page = document.startPage(pageInfo);
        canvas = page.getCanvas();

        for (int nbrQz = 0; nbrQz < su.nbrOfQuiz; nbrQz++) {
            if (nbrQz == 0)
                new Signature().add(context, setup.fileDate, su, sigMap, pgWidth, canvas);
            else if ((twoSix == 2 && nbrQz % 2 == 0) ||
                    (twoSix == 6 && nbrQz > 5 && nbrQz % 6 == 0) ||
                    twoSix == 1) {
                pageNbr++;
                new Signature().add(context, setup.fileDate, su, sigMap, pgWidth, canvas);
                document.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(pgWidth, pgHeight, pageNbr).create();
                // start a page
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
            }

            int [][] xyTable = puzzles.get(nbrQz);
            int xBase =  boxWidth + 10;
            if (twoSix == 2) {
                xBase = boxWidth + 10;
            } else if (twoSix == 6) {
                xBase = boxWidth + 5 + ((nbrQz % 6) % 2) * 10 * boxWidth;
            }

            int yBase;
            if (twoSix == 2) {
                yBase = boxWidth/4 + (nbrQz % 2) * boxWidth * 100 / 10;
            } else if (twoSix == 6) {
                yBase = boxWidth + ((nbrQz%6) / 2) * boxWidth * 100 / 10;
            } else // == 1
                yBase = boxWidth;

            int xGap = boxWidth/2;
            int yGap = boxWidth3+boxWidth3+boxWidth3/3;
            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
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

            for (int row = 0; row < gridSize; row+=3)
                for (int col = 0; col < gridSize; col+=3) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xBase, yBase, xPos + boxWidth*3, yPos + boxWidth*3, pBoxOut);
                }
            canvas.drawText("{"+(nbrQz+1)+"}", xBase + 6 + boxWidth*9, yBase+10, pCount);
        }
        new Signature().add(context, setup.fileDate, su, sigMap, pgWidth, canvas);

        document.finishPage(page);

        File filePath = new File(setup.outFile+" .pdf");
        try {
            document.writeTo(Files.newOutputStream(filePath.toPath()));
        } catch (IOException e) {
            Log.e("main", "error " + e);
        }

        document.close();

        if (su.answer)
            makeAnswer(puzzles, answers, su, setup);

        if (filePrint.equals("f"))
            new ShareFile().show(context, setup.outFolder.getAbsolutePath());
        else        // "p"
            new PDF2Printer().launch(context, filePath.getPath());
    }

    //         Create Answer Page ------------
    void makeAnswer(List<int[][]> puzzles, List<int[][]> answers, Sudoku su, SudokuSetUp setup) {
         int GARO = 3;
         int SERO = 4;

        File filePath;
        PdfDocument.Page page;
        PdfDocument document;
        Canvas canvas;
        document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pgWidth, pgHeight, 1).create();

        page = document.startPage(pageInfo);
        canvas = page.getCanvas();

        int gridSize = su.gridSize; // 6 or 9
        boxWidth = (pgWidth - 50) / ((gridSize+1)*GARO);
        pBoxIn.setStrokeWidth(1);
        pNumb.setTextSize(boxWidth/2.3f); // pNumb : answer number
        pNumb.setAlpha(su.opacity);
        pMemo.setTextSize(boxWidth/2f);  // pMemo : given number
        pMemo.setColor(Color.DKGRAY);
        pMemo.setAlpha(su.opacity);
        pCount.setTextSize((float) boxWidth * 5 / 10);

        pageNbr = 0;
        for (int nbrQz = 0; nbrQz < answers.size(); nbrQz++) {
            int y20 = nbrQz % (GARO * SERO);    // 3 cols x 4 row answers per page
            if (nbrQz >= (GARO * SERO) && y20 == 0) {
                pageNbr++;
                new Signature().add(context, setup.fileDate, su, sigMap, pgWidth, canvas);
                document.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(pgWidth, pgHeight, pageNbr).create();
                // start a page
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
            }

            int [][] ansTable = answers.get(nbrQz);
            int [][] blankTable = puzzles.get(nbrQz);
            int xBase = 30 + (nbrQz % GARO) * boxWidth * (gridSize+1);
            int yBase = 80 +  (y20 / GARO) * boxWidth * (gridSize+1);
            int xGap = boxWidth/2;
            int yGap = boxWidth*3/4;
            for (int row = 0; row < gridSize ; row++) {
                for (int col = 0; col < gridSize; col++) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xPos, yPos, xPos + boxWidth, yPos + boxWidth, pBoxIn);
                    canvas.drawText(String.valueOf(ansTable[row][col]), xPos + xGap, yPos + yGap,
                            (blankTable[row][col] == 0)? pNumb : pMemo);
                }
            }
            canvas.drawText("{"+(nbrQz+1)+"}", xBase + boxWidth, yBase - 8, pCount);

            pBoxOut.setStrokeWidth(2);
            for (int row = 0; row < gridSize; row+=3)
                for (int col = 0; col < gridSize; col+=3) {
                    int xPos = xBase + col * boxWidth;
                    int yPos = yBase + row * boxWidth;
                    canvas.drawRect(xBase, yBase, xPos + boxWidth*3, yPos + boxWidth*3,
                            pBoxOut);
                }
        }

        new Signature().add(context, setup.fileDate, su, sigMap, pgWidth, canvas);
        document.finishPage(page);

        filePath = new File(setup.outFile+" 답.pdf");
        try {
            document.writeTo(Files.newOutputStream(filePath.toPath()));
        } catch (IOException e) {
            Log.e("main", "error " + e);
        }
        document.close();
    }
}