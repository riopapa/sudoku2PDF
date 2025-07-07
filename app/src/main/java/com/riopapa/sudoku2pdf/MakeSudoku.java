package com.riopapa.sudoku2pdf;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.riopapa.sudoku2pdf.Model.Sudoku;

import java.util.Locale;

class MakeSudoku {

    private String [] blankTables;
    private String [] answerTables;
    private int quizCount, blankCount;
    private Sudoku su;
    TextView tvStatus;
    ProgressBar progressBar;
    Drawable drawable;
    String filePrint;
    public void make(Sudoku sudoku, String filePrint,
                     TextView tvStat, ProgressBar progress, Drawable draw) {
        this.su = sudoku;
        this.filePrint = filePrint;
        quizCount = sudoku.nbrOfQuiz;
        blankCount = sudoku.nbrOfBlank;
        tvStatus = tvStat;
        progressBar = progress;
        drawable = draw;
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

            blankTables = new String[quizCount];
            answerTables = new String[quizCount];
            duration = System.currentTimeMillis();

            progressBar.setMax(100);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
            tvStatus.setVisibility(View.VISIBLE);

        }

        @Override
        protected String doInBackground(String... inputParams) {
            int madeCount = 0;
            int percentStart = 0;
            int percentFinish = (madeCount + 1) * 100 / quizCount;
            while (madeCount < quizCount) {
                // calculate degrees
                percentStart++;
                if (percentStart >= percentFinish)
                    percentStart = (madeCount+1) * 100 / quizCount;
                publishProgress(PROGRESS_PERCENT, ""+((tryCount%100)*10));
                publishProgress(PROGRESS_COUNT, " " + madeCount + "/" + quizCount + " Done\n" + tryCount + " tries! ");

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
                    percentStart = (madeCount+1) * 100 / quizCount;
                    percentFinish = (madeCount+1) * 100 / quizCount;
                    publishProgress(PROGRESS_PERCENT, ""+percentStart);
                }
            }
            duration = System.currentTimeMillis() - duration;
            return "\nTries: " + tryCount +
                    ", Duration: " + String.format(Locale.US,"%.3f",
                    (float) duration / 1000f) + " secs.\n";
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
                    progressBar.setSecondaryProgress(0);
                    break;
            }
        }

        @Override
        protected void onCancelled(String result) {
        }

        @Override
        protected void onPostExecute(String statistics ) {

            Log.w("DONE", statistics);
            progressBar.setVisibility(View.INVISIBLE);
            tvStatus.setText(statistics);
            tvStatus.invalidate();

            new Save2PDF(blankTables, answerTables, su, tvStatus.getContext(), filePrint);

        }
    }
}
