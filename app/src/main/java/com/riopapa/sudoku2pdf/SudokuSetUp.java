package com.riopapa.sudoku2pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.riopapa.sudoku2pdf.Model.Sudoku;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class SudokuSetUp {
    public final File outFolder;
    public final File outFile;
    public final String fileDate;
    public final String fileInfo;
    public final Bitmap sigMap;
    public final int meshType;
    public final int twoSix;
    public final int boxWidth;
    public final int boxWidth2;
    public final int boxWidth3;
    public final int space;
    public final int pgWidth;
    public final int pgHeight;

    public SudokuSetUp(Context context, Sudoku su, int pgWidth, int pgHeight, int gridSize) {
        this.pgWidth = pgWidth;
        this.pgHeight = pgHeight;

        // Download folder
        String downLoadFolder = Environment.getExternalStorageDirectory().getPath() + "/download";

        // File date / info
        final SimpleDateFormat sdfDate = new SimpleDateFormat("yy-MM-dd HH.mm.ss", Locale.US);
        fileDate = sdfDate.format(System.currentTimeMillis());
        fileInfo = "b" + su.nbrOfBlank + "p" + su.nbrOfQuiz;

        // Output folder
        outFolder = new File(downLoadFolder);
        if (!outFolder.exists()) {
            if (outFolder.mkdirs()) {
                Log.i("folder", "Sudoku Folder created");
            }
        }

        // Output file
        outFile = new File(outFolder, "su_" + fileDate + " " + fileInfo + " " + su.name);

        // Signature bitmap
        sigMap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(context.getResources(), R.mipmap.my_sign_blured),
                80, 60, false
        );

        // Layout parameters
        meshType = su.mesh;
        twoSix = su.nbrPage;

        if (twoSix == 2) {
            boxWidth = (pgHeight - 60) / (gridSize + 1 + gridSize);
        } else if (twoSix == 6) {
            boxWidth = pgHeight / ((gridSize + 2) * 3 - 1);
        } else {
            boxWidth = (pgWidth - 150) / (gridSize + 1);
        }

        boxWidth3 = boxWidth / 3;
        boxWidth2 = boxWidth / 2;
        space = boxWidth * 2 / 8;
    }
}
