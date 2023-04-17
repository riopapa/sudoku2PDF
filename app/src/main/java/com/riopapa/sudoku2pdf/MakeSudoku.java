package com.riopapa.sudoku2pdf;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.riopapa.sudoku2pdf.Model.SudokuInfo;

import java.util.Locale;

class MakeSudoku {

    //    private static int [][] solveTable;
//    private static int [][][] usedTable;    // set '1' if used within that col, row, block
    private String [] blankTables;
    private String [] answerTables;
    private int pageCount, blankCount;
    private SudokuInfo sudokuInfo;
    private TextView tvStatus;
    private ProgressBar progressBar;

    public void make(SudokuInfo sudokuInfo) {
        this.sudokuInfo = sudokuInfo;
        Activity activity = sudokuInfo.activity;
        pageCount = sudokuInfo.pageCount;
        blankCount = sudokuInfo.blankCount;

        tvStatus = activity.findViewById(R.id.status);

        Drawable drawable = ResourcesCompat.getDrawable(sudokuInfo.context.getResources(),
                R.drawable.circle, null);
        progressBar = activity.findViewById(R.id.progress_circle);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgressDrawable(drawable);

        try {
            new make_blank_solve().execute("");
        } catch (Exception e) {
            Log.e("Err",e.toString());
        }

    }

    class make_blank_solve extends AsyncTask< String, String, String> {
        Long duration = 0L;
        private int tryCount = 0;

        @Override
        protected void onPreExecute() {

            blankTables = new String[pageCount];
            answerTables = new String[pageCount];
            duration = System.currentTimeMillis();

            progressBar.setMax(100);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
            tvStatus.setVisibility(View.VISIBLE);

//            ConstraintSet set = new ConstraintSet();
//            set.connect(frameLayout.getId(), ConstraintSet.TOP, horizontalLineView.getId(), ConstraintSet.BOTTOM);
//            set.constrainWidth(frameLayout.getId(), 500 + pageCount * 80);
//            set.constrainHeight(frameLayout.getId(), 500 + pageCount * 80);
//            set.connect(frameLayout.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
//            set.connect(frameLayout.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
//            set.connect(frameLayout.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 80);
//            set.applyTo(mainLayout);
        }

        @Override
        protected String doInBackground(String... inputParams) {
            int madeCount = 0;
            int percentStart = 0;
            int percentFinish = (madeCount + 1) * 100 / pageCount;
            while (madeCount < pageCount) {
                // calculate degrees
                percentStart++;
                if (percentStart >= percentFinish)
                    percentStart = (madeCount+1) * 100 / pageCount;
                publishProgress(PROGRESS_PERCENT, ""+percentStart);
                publishProgress(PROGRESS_COUNT, " " + madeCount + "/" + pageCount + " Done\n" + tryCount + " tries! ");

                tryCount++;
                int[][] answerTable = new Answer().generate();

//                dumpTable("Answer Table "+ puzzleCount, answerTable);
                int retryCount = 0;
                // blanked cell count
                int solved;
                int[][] blankTable;
                do {
                    blankTable = new SudokuTable().makeBlank(answerTable, blankCount);
                    solved = new Solve().exe(blankTable, answerTable);
                    retryCount++;
                    if (retryCount > 30)
                        break;
                } while (blankCount != solved);
                if (blankCount == solved) {
                    answerTables[madeCount] = suArray2Str(answerTable);
                    blankTables[madeCount] = suArray2Str(blankTable);
                    madeCount++;
                    percentStart = (madeCount+1) * 100 / pageCount;
                    percentFinish = (madeCount+1) * 100 / pageCount;
                    publishProgress(PROGRESS_PERCENT, ""+percentStart);
                }
            }
            duration = System.currentTimeMillis() - duration;
            return "\nTries: " + tryCount +
                    ", Duration: " + String.format(Locale.US,"%.3f",
                    (float) duration / 1000f) + " secs." +
                    "\nfile: "+MainActivity.fileDate+".PDF\n";
        }
        String suArray2Str (int [][] tbl) {
            StringBuilder result = new StringBuilder();
            for (int row = 0; row < 9; row++) {
                StringBuilder s = new StringBuilder();
                for (int col = 0; col < 9; col++)
                    s.append(tbl[row][col]).append(";");
                result.append(s).append(":");
            }
            return result.toString();
        }


        final static String PROGRESS_COUNT = "c";
        final static String PROGRESS_PERCENT = "p";
        @Override
        protected void onProgressUpdate(String... values) {
            String which = values[0];
            switch (which) {
                case PROGRESS_COUNT:
                    String statusText = values[1];
                    tvStatus.setText(statusText);
                    break;
                case PROGRESS_PERCENT:
                    int progress = Integer.parseInt(values[1]);
                    progressBar.setProgress(progress);
                    progressBar.setSecondaryProgress(100);
                    break;
            }
        }

        @Override
        protected void onCancelled(String result) {
        }

        @Override
        protected void onPostExecute(String statistics ) {

//            Toast.makeText(MainActivity.,statistics, Toast.LENGTH_LONG).show();
            Log.w("DONE", statistics);
            progressBar.setVisibility(View.INVISIBLE);
            tvStatus.setText(statistics);
            tvStatus.invalidate();

            MakePDF.create(blankTables, answerTables, sudokuInfo);
        }
    }
}

//        private void dumpTable(String s, int [][] tbl) {
//            Log.w("r",s);
//            String bar0  = "\n   0  1  2 | 3  4  5 | 6  7  8";
//            String bar   = "\n   -  -  - | -  -  - | -  -  -";
//            StringBuilder suTableResult = new StringBuilder("  "+bar0+bar);
//            for (int y = 0; y < 9; y++) {
//                StringBuilder str = new StringBuilder("\n"+y+" ");
//                for (int x = 0; x < 9; x++) {
//                    String sx = " "+tbl[x][y] + " ";
//                    str.append(sx);
//                    if (x ==2 || x == 5)
//                        str.append("|");
//                }
//                suTableResult.append(str);
//                if (y ==2 || y == 5)
//                    suTableResult.append(bar);
//            }
//            suTableResult.append(bar);
//            suTableResult.append(bar0);
//            Log.w("r",suTableResult.toString());
//        }


//        private void dumpUsed(String memo) {
//            Log.w("d",memo);
//            String s = " .";
//            for (int x = 0; x < 9; x++) {
//                s += "~" + x + "~|";
//            }
//            Log.w("s",s);
//            for (int y = 0; y < 9; y++) {
//                String [] garo = new String [3];
//                garo[0] = " |";garo[1] = y+"|";garo[2] = " |";
//                for (int x = 0; x < 9; x++) {
//                    for (int z = 0; z < 9; z++) {
//                        if (usedTable[x][y][z] == 0)
//                            garo[z/3] += "o";
//                        else
//                            garo[z/3] += (z+1);
//                        if (z == 2 || z == 5) {
//                        }
//                    }
//                    garo[0] += "|";garo[1] += "|";garo[2] += "|";
//                }
//            }
//        }
