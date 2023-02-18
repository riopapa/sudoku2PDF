package com.urrecliner.sudoku2pdf;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import static com.urrecliner.sudoku2pdf.MainActivity.frameLayout;
import static com.urrecliner.sudoku2pdf.MainActivity.horizontalLineView;
import static com.urrecliner.sudoku2pdf.MainActivity.mainLayout;
import static com.urrecliner.sudoku2pdf.MainActivity.progressBar;
import static com.urrecliner.sudoku2pdf.MainActivity.statusTV;

import androidx.constraintlayout.widget.ConstraintSet;

class MakeSudoku {

    private static int [][] answerTable;
    private static int [][] blankTable;
    private static int [][] solveTable;
    private static int [][][] usedTable;    // set '1' if used within that col, row, block
    private String [] blankTables;
    private String [] answerTables;
    private String [] commentTables;
    private int puzzleCount, blankCount;
    private Context context;

    public void make(int howMany, int blanks, Context context) {
        puzzleCount = howMany;
        blankCount = blanks;
        this.context = context;
        try {
            new make_blank_solve().execute("");
        } catch (Exception e) {
            Log.e("Err",e.toString());
        }
    }

    class make_blank_solve extends AsyncTask< String, String, String> {

        Long duration = 0L, durationSum = 0L;
        private Random random;
        private int tryCount = 0, loopSum = 0;

        @Override
        protected void onPreExecute() {

            random = new Random(System.currentTimeMillis());
            blankTables = new String[puzzleCount];
            answerTables = new String[puzzleCount];
            commentTables = new String[puzzleCount];
            duration = System.currentTimeMillis();

//            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(500 + puzzleCount * 80, 500 + puzzleCount * 80);
//            frameLayout.setLayoutParams(layoutParams);

            progressBar.setMax(100);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
            statusTV.setVisibility(View.VISIBLE);

            ConstraintSet set = new ConstraintSet();
            set.connect(frameLayout.getId(), ConstraintSet.TOP, horizontalLineView.getId(), ConstraintSet.BOTTOM);
            set.constrainWidth(frameLayout.getId(), 500 + puzzleCount * 80);
            set.constrainHeight(frameLayout.getId(), 500 + puzzleCount * 80);
            set.connect(frameLayout.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            set.connect(frameLayout.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            set.connect(frameLayout.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 80);
            set.applyTo(mainLayout);

        }

        @Override
        protected String doInBackground(String... inputParams) {
            int madeCount = 0;
            int percentStart = madeCount * 100 / puzzleCount;
            int percentFinish = (madeCount + 1) * 100 / puzzleCount;
            int showProgressCount = blankCount - 18;     // 18 should be less than MINIMUM_BLANK
            while (madeCount < puzzleCount) {
                // calculate degrees
                if (tryCount % showProgressCount == 0) {
                    percentStart++;
                    if (percentStart >= percentFinish)
                        percentStart = madeCount * 100 / puzzleCount;
                    publishProgress(PROGRESS_PERCENT, ""+percentStart);
                    publishProgress(PROGRESS_COUNT, " " + madeCount + "/" + puzzleCount + " Done\n" + tryCount + " tries! ");
                }
                tryCount++;
                make_answerTable(); // result in [] answerTable
//                dumpTable("Answer Table "+ puzzleCount, answerTable);
                int retryCount = 0;
                // blanked cell count
                int blanked, solved;
                do {
                    blanked = blank_Table();
                    solved = solve_Table();
                    retryCount++;
                    if (retryCount > 30)
                        break;
                } while (blanked != solved);
                if (blanked == solved) {
                    answerTables[madeCount] = suArray2Str(answerTable);
                    blankTables[madeCount] = suArray2Str(blankTable);
                    duration = System.currentTimeMillis() - duration;
//                    String s = "  "+tryCount + " tries; \n" + String.format(Locale.US,"%.3f",((float) duration) / 1000f) + " secs. to generate";
                    commentTables[madeCount] = ""; // s;
                    durationSum += duration;
                    duration = System.currentTimeMillis();
                    loopSum += tryCount;
                    tryCount = 0;
                    madeCount++;
                    percentStart = madeCount * 100 / puzzleCount;
                    percentFinish = (madeCount + 1) * 100 / puzzleCount;
                    publishProgress(PROGRESS_PERCENT, ""+percentStart);
                }
            }

            return "\nTotal tries: " + loopSum + " with duration: " + String.format(Locale.US,"%.3f",(float) durationSum / 1000f) + " secs."+"\noutput: "+MainActivity.fileDate+".PDF\n";

        }

        int nextThree() { // 0 ~ 2
            return random.nextInt(3);
        }
        int nextRanged(int range) { // 0 ~ range
            return random.nextInt(range);
        }

        void make_answerTable() {

            answerTable = new int[9][9];

            while (hasZeroBlock(answerTable)) {
                for (int blockX = 0; blockX < 3; blockX++) {
                    for (int blockY = 0; blockY < 3; blockY++) {
                        if (answerTable[blockX*3][blockY*3] == 0) {
                            if (!isFillThisBlockOK(blockX, blockY)) {
                                clearBlock(nextThree(), nextThree());
                            }
                        }
                    }
                }
            }
        }

        private boolean hasZeroBlock(int [][] tbl) {
            for (int x = 0;  x < 9; x +=3)
                for (int y = 0; y < 9; y +=3)
                    if (tbl[x][y] == 0)
                        return true;
            return false;
        }

        private void clearBlock(int blockX, int blockY) {
            for (int x = 0; x < 3; x++)
                for (int y = 0; y < 3; y++)
                    answerTable[blockX*3+x][blockY*3+y] = 0;
        }

        private boolean isFillThisBlockOK(int blockX, int blockY) {
            int reRun = 3;
            do {
                if (buildOneBlock(blockX, blockY))
                    return true;
                reRun--;
            } while (reRun > 0);
            clearBlock(blockX, blockY);
            return  false;
        }

        private boolean buildOneBlock(int blockX, int blockY) {
            int loop = 18;
            clearBlock(blockX, blockY);

            int x3 = blockX * 3, y3 = blockY * 3;
            ArrayList<Integer> oneLine = new ArrayList<>();
            for (int i = 0; i < 9; i++)
                oneLine.add(i+1);
            int nbr = 1;
            while (oneLine.size() > 0 && nbr < 10 && loop > 0) {
                int pos = nextRanged(oneLine.size());
                int val = oneLine.get(pos) - 1;
                int x = val / 3; int y = val % 3;
                if (answerTable[x3+x][y3+y] == 0 && noDupVertical(x3+x, nbr) && noDupHorizontal(y3+y, nbr)) {
                    answerTable[x3+x][y3+y] = nbr;
                    oneLine.remove(pos);
                    nbr++;
                    loop = 81;
                }
                else
                    loop--;
            }
            return loop != 0;
        }

        boolean noDupVertical(int x,int nbr) {
            for (int y = 0; y < 9; y++)
                if (answerTable[x][y] == nbr)
                    return false;
            return true;
        }

        boolean noDupHorizontal(int y,int nbr) {
            for (int x = 0; x < 9; x++)
                if (answerTable[x][y] == nbr)
                    return false;
            return true;
        }

        private void dumpTable(String s, int [][] tbl) {
            Log.w("r",s);
            String bar0  = "\n   0  1  2 | 3  4  5 | 6  7  8";
            String bar   = "\n   -  -  - | -  -  - | -  -  -";
            StringBuilder suTableResult = new StringBuilder("  "+bar0+bar);
            for (int y = 0; y < 9; y++) {
                StringBuilder str = new StringBuilder("\n"+y+" ");
                for (int x = 0; x < 9; x++) {
                    String sx = " "+tbl[x][y] + " ";
                    str.append(sx);
                    if (x ==2 || x == 5)
                        str.append("|");
                }
                suTableResult.append(str);
                if (y ==2 || y == 5)
                    suTableResult.append(bar);
            }
            suTableResult.append(bar);
            suTableResult.append(bar0);
            Log.w("r",suTableResult.toString());
        }


        private void dumpUsed(String memo) {
            Log.w("d",memo);
            String s = " .";
            for (int x = 0; x < 9; x++) {
                s += "~" + x + "~|";
            }
            Log.w("s",s);
            for (int y = 0; y < 9; y++) {
                String [] garo = new String [3];
                garo[0] = " |";garo[1] = y+"|";garo[2] = " |";
                for (int x = 0; x < 9; x++) {
                    for (int z = 0; z < 9; z++) {
                        if (usedTable[x][y][z] == 0)
                            garo[z/3] += "o";
                        else
                            garo[z/3] += (z+1);
                        if (z == 2 || z == 5) {
                        }
                    }
                    garo[0] += "|";garo[1] += "|";garo[2] += "|";
                }
            }
        }

        String suArray2Str (int [][] tbl) {
            StringBuilder result = new StringBuilder();
            for (int row = 0; row < 9; row++) {
                StringBuilder s = new StringBuilder();
                for (int col = 0; col < 9; col++)
                    s.append(tbl[row][col]+";");
                result.append(s +":");
            }
            return result.toString();
        }

        int blank_Table() {
            blankTable = new int[9][9];
            blankTable = copy_Table(answerTable);
            int blanked = 0;
            while (blanked < blankCount) {
                int x = nextRanged(9);
                int y = nextRanged(9);
                nextThree();
                if (blankTable[x][y] > 0) {
                    blankTable[x][y] = 0;
                    blanked++;
                }
            }
            return blanked;
        }

        int [][] copy_Table(int [][] srcTable) {
            int [][] tgtTable = new int [9][9];
            for (int y = 0; y < 9; y++)
                for (int x = 0; x < 9; x++)
                    tgtTable[x][y] = srcTable[x][y];
            return tgtTable;
        }

        class XYPos {
            int x;
            int y;
            int nbr0;
        }

        int solve_Table() {
            int blankRetry = 5;
            int solved = 0;
            // initialize solveTable from blankTable
            while (blankRetry < 8) {
                solveTable = copy_Table(blankTable);
//            dumpTable("solve Table", solveTable);
                // fill usedTable with impossible number verification code
                usedTable = makeUsedTable();
//            dumpUsed("initial");
                solved = 0;
                XYPos xyPos = null;
                while (solTableHaveZero()) {
                    xyPos = findUniqueCell();
                    if (xyPos == null)
                        xyPos = findUniqueWithinBlock();
                    if (xyPos == null)
                        break;
                    int x = xyPos.x;
                    int y = xyPos.y;
                    solveTable[x][y] = xyPos.nbr0 + 1;
                    solved++;
//                        dumpUsed("x:"+x+" y:"+y+" = "+(xyPos.nbr0+1));
                    applySolved2WorkTable(x, y, xyPos.nbr0);
//                        Log.w("solved","x:"+x+" y:"+y+" = "+(xyPos.nbr0+1));

                }
                if (xyPos == null)
                    redoBlankTable(++blankRetry);
                else
                    break;
            }
            return solved;
        }

        private boolean solTableHaveZero() {
            for (int y = 0; y < 9; y++)
                for (int x = 0; x < 9; x++)
                    if (solveTable[x][y] == 0) {
                        return true;
                    }
            return false;
        }

        private void redoBlankTable(int loop) {
            // refill some blanks and generate blanks in other cells
            int cnt = loop;
            while (cnt > 0) {
                int x = nextRanged(9); int y = nextRanged(9);
                if (blankTable[x][y] == 0) {
                    blankTable[x][y] = answerTable[x][y];
                    cnt--;
                }
            }
            cnt = loop;
            while (cnt > 0) {
                int x = nextRanged(9); int y = nextRanged(9);
                if (blankTable[x][y] != 0) {
                    blankTable[x][y] = 0;
                    cnt--;
                }
            }
            loop = nextThree();
        }

        private void applySolved2WorkTable(int x, int y, int nbr0) {
            // notify other usedTable cells to remove this nbr
            for (int yp = 0; yp < 9; yp++)
                usedTable[x][yp][nbr0] = 1;
            for (int xp = 0; xp < 9; xp++)
                usedTable[xp][y][nbr0] = 1;
            int xb = (x / 3) * 3;
            int yb = (y / 3) * 3;
            for (int yp = 0; yp < 3; yp++)
                for (int xp = 0; xp < 3; xp++)
                    usedTable[xb + xp][yb + yp][nbr0] = 1;
        }

        XYPos findUniqueWithinBlock() {
            // find out whether nSave is only number possible within one block, then nSave is the solution
            for (int xb = 0; xb < 9; xb+=3) {
                for (int yb = 0; yb < 9; yb+=3) {
                    int xSave = 0, ySave = 0, nSave = 0;
                    for (int z = 0; z < 9; z++) {
                        int onlyOne = 0;
                        for (int x = 0; x < 3; x++) {
                            for (int y = 0; y < 3; y++) {
                                if (solveTable[xb+x][yb+y] == 0 && usedTable[xb+x][yb+y][z] == 0) {
                                    onlyOne++;
                                    xSave = xb+x; ySave = yb+y; nSave = z;
                                }
                            }
                            if (onlyOne > 1)
                                break;
                        }
                        if (onlyOne == 1) { // then this number
//                            Log.w("ok","findUniqueWithinBlock **");
                            XYPos xyPos = new XYPos();
                            xyPos.x = xSave; xyPos.y = ySave; xyPos.nbr0 = nSave;
                            return xyPos;
                        }
                    }

                }
            }
            return null;
        }

        int [][][] makeUsedTable() {
            // build used table for each cell represented with [][][z]
            int [][][] tempTable = new int [9][9][9];

            for (int y = 0; y < 9; y++) {
                for (int x = 0; x < 9; x++) {
                    if (solveTable[x][y] != 0) {    // if already solved fill all '1'
                        for (int z = 0; z < 9; z++)
                            tempTable[x][y][z] = 1;
                    }
                    else {
                        for (int xPos = 0; xPos < 9; xPos++) {
                            if (solveTable[xPos][y] != 0)
                                tempTable[x][y][solveTable[xPos][y] - 1] = 1;
                        }
                        for (int yPos = 0; yPos < 9; yPos++) {
                            if (solveTable[x][yPos] != 0)
                                tempTable[x][y][solveTable[x][yPos] - 1] = 1;
                        }
                        int xb = (x / 3) * 3;
                        int yb = (y / 3) * 3;
                        for (int yp = 0; yp < 3; yp++) {
                            for (int xp = 0; xp < 3; xp++) {
                                if (solveTable[xb + xp][yb + yp] != 0)
                                    tempTable[x][y][solveTable[xb + xp][yb + yp] - 1] = 1;
                            }
                        }
                    }
                }
            }
            return tempTable;
        }

        XYPos findUniqueCell() {
            // find out whether only one number is possible on that cell
            for (int y = 0; y < 9; y++) {
                for (int x = 0; x < 9; x++) {
                    if (solveTable[x][y] == 0) {
                        int cnt = 0, nbr0 = -1;
                        for (int z = 0; z < 9; z++) {
                            cnt += usedTable[x][y][z];
                        }
                        if (cnt == 8) { // only remain one possible answer
                            for (int z = 0; z < 9; z++) {
                                if (usedTable[x][y][z] == 0) {
                                    nbr0 = z;
                                    break;
                                }
                            }
//                            Log.w("ok","findUniqueCell --");
                            XYPos xyPos = new XYPos();
                            xyPos.x = x; xyPos.y = y; xyPos.nbr0 = nbr0;
                            return xyPos;
                        }
                    }
                }
            }
            return null;
        }

        final static String PROGRESS_COUNT = "c";
        final static String PROGRESS_PERCENT = "p";
        @Override
        protected void onProgressUpdate(String... values) {
            String which = values[0];
            switch (which) {
                case PROGRESS_COUNT:
                    String statusText = values[1];
                    statusTV.setText(statusText);
                    break;
                case PROGRESS_PERCENT:
                    int progress = Integer.parseInt(values[1]);
                    progressBar.setProgress(progress);
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
            statusTV.setText(statistics);
            statusTV.invalidate();

            MakePDF.createPDF(blankTables, answerTables, commentTables, context);
        }
    }
}