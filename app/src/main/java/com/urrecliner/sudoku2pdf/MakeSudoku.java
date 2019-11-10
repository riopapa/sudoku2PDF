package com.urrecliner.sudoku2pdf;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

import static com.urrecliner.sudoku2pdf.MainActivity.progressBar;
import static com.urrecliner.sudoku2pdf.MainActivity.statusTV;

class MakeSudoku {

    private static int [][] answerTable;
    private static int [][] blankTable;
    private static int [][] solveTable;

    private String [] blankTables;
    private String [] answerTables;
    private int puzzleCount, levelDegree;

    void run(int count, int level) {
        puzzleCount = count;
        levelDegree = level;
        try {
            new make_blank_solve().execute("");
        } catch (Exception e) {
            Log.e("Err",e.toString());
        }
    }

    class make_blank_solve extends AsyncTask< String, String, String> {

        Long duration;
        private Random random;
        private int retrySum = 0;
        private int looped = 0;

        @Override
        protected void onPreExecute() {

            random = new Random(System.currentTimeMillis());
            blankTables = new String[puzzleCount];
            answerTables = new String[puzzleCount];
            duration = System.currentTimeMillis();
            progressBar.setMax(100);
            progressBar.setProgress(0);
            progressBar.setVisibility(View.VISIBLE);
            statusTV.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... inputParams) {
            int madeCount = 0;

            while (madeCount < puzzleCount) {
                looped++;
                publishProgress(PROGRESS_COUNT, " "+madeCount+"/"+puzzleCount+" Made\n"+looped+" loops! ");
                make_answerTable(); // result in [] answerTable
//            dumpTable("GENERATED LOOP "+ puzzleCount, answerTable);
                int retryCount = 0;
                // blanked cell count
                int blanked, solved;
                do {
                    blanked = blank_Table();
                    solved = solve_Table();
                  Log.w("verify","blanked:"+ blanked +", solved="+solved+((blanked -solved == 0) ? " GOOD":(" not Yet, retry: "+retryCount)));
                    retryCount++;
                    if (retryCount > 20)
                        break;
                } while (blanked != solved && solved == 0);

                retrySum += retryCount;
                if (blanked == solved) {
//                    int diffCount = 0;
//                    for (int x = 0; x < 9; x++)
//                        for (int y = 0; y < 9; y++)
//                            if (answerTable[x][y] != solveTable[x][y]) diffCount++;
//
//                    if (diffCount > 0) {
//                        dumpTable("DIFFERENT ....... ans " + diffCount, answerTable);
//                        dumpTable("DIFFERENT ....... sol " + diffCount, solveTable);
//                        dumpTable("DIFFERENT ....... blk " + diffCount, blankTable);
//                    }
                    answerTables[madeCount] = suArray2Str(answerTable);
                    blankTables[madeCount] = suArray2Str(blankTable);
                    madeCount++;
                    publishProgress(PROGRESS_PERCENT, ""+(madeCount * 100 / puzzleCount));

                    Log.w("MADE", madeCount+" generated");
//                    dumpTable( "@@ M A D E @@@  " + blanked + " vs " + solved + " madeCount " + madeCount + " retry:" + retryCount, blankTable);
//                    dumpTable( "@@ M A D E @@@  " + blanked + " vs " + solved + " madeCount " + madeCount + " retry:" + retryCount, blankTable);
                }
            }
            duration = System.currentTimeMillis() - duration;
            return "\nloop: " + looped + "\nRetried: " + retrySum + "\nduration: " + (((float) duration) / 1000f) + " secs."+"\noutput: "+MainActivity.fileDate+".PDF\n";

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
            String suTableResult;
            Log.w("r",s);
            String bar0  = "\n   0  1  2 | 3  4  5 | 6  7  8";
            String bar   = "\n   -  -  - | -  -  - | -  -  -";
            suTableResult = "  "+bar0+bar;
            for (int y = 0; y < 9; y++) {
                String str = "\n"+y+" ";
                for (int x = 0; x < 9; x++) {
                    str += " "+tbl[x][y] + " ";
                    if (x ==2 || x == 5)
                        str += "|";
                }
                suTableResult += str;
                if (y ==2 || y == 5)
                    suTableResult += bar;
            }
            suTableResult += bar + bar0;
            Log.w("r",suTableResult);
        }

        String suArray2Str (int [][] tbl) {
            String result = "";
            for (int row = 0; row < 9; row++) {
                String s = "";
                for (int col = 0; col < 9; col++)
                    s += tbl[row][col]+";";
                result += s +":";
            }
            return result;
        }

        int blank_Table() {
            blankTable = new int[9][9];
            blankTable = copy_Table(answerTable);
            int blanked = 0;
            // difficulty calcuation
            int target = 10 + levelDegree + levelDegree - nextRanged(4);
            while (blanked < target) {
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
        }

        int solve_Table() {
            // initialize solveTable from blankTable
            solveTable = copy_Table(blankTable);
            // fill workTable with impossible number verification code
            int [][][] workTable = makeWorkTable();
            int solved = 0;
            while (true) {
                if (solTableHaveZero()) {
                    XYPos xyPos = nextUniqueCell(workTable);
                    if (xyPos != null) {
                        int x = xyPos.x;
                        int y = xyPos.y;
                        int nbr = 100;
                        for (int z = 0; z < 9; z++) {
                            if (workTable[x][y][z] == 0) {
                                nbr = z + 1;
                                break;
                            }
                        }
                        solveTable[x][y] = nbr;
                        solved++;
                        // notify other workTable cells to remove this nbr
                        for (int yp = 0; yp < 9; yp++)
                            workTable[x][yp][nbr - 1] = 1;
                        for (int xp = 0; xp < 9; xp++)
                            workTable[xp][y][nbr - 1] = 1;
                        int xb = (x / 3) * 3;
                        int yb = (y / 3) * 3;
                        for (int yp = 0; yp < 3; yp++)
                            for (int xp = 0; xp < 3; xp++)
                                workTable[xb + xp][yb + yp][nbr - 1] = 1;
                    }
                    else {
                        return solved;  // TODO next logic here
                    }
                }
                else
                    return solved;
            }
        }

        int [][][] makeWorkTable() {
            int [][][] workTable = new int [9][9][9];

            for (int y = 0; y < 9; y++) {
                for (int x = 0; x < 9; x++) {
                    if (solveTable[x][y] == 0) {
                        // fill x based
                        for (int xPos = 0; xPos < 9; xPos++) {
                            if (solveTable[xPos][y] != 0)
                                workTable[x][y][solveTable[xPos][y]-1] = 1;
                        }
                        for (int yPos = 0; yPos < 9; yPos++) {
                            if (solveTable[x][yPos] != 0)
                                workTable[x][y][solveTable[x][yPos]-1] = 1;
                        }
                        int xb = (x / 3) * 3;
                        int yb = (y / 3) * 3;
                        for (int yp = 0; yp < 3; yp++) {
                            for (int xp = 0; xp < 3; xp++) {
                                if (solveTable[xb+xp][yb+yp] != 0)
                                    workTable[x][y][solveTable[xb+xp][yb+yp]-1] = 1;
                            }
                        }
                    }
                }
            }
            return workTable;
        }

        boolean solTableHaveZero() {
            for (int y = 0; y < 9; y++)
                for (int x = 0; x < 9; x++)
                    if (solveTable[x][y] == 0) {
                        return true;
                    }
            return false;
        }

        XYPos nextUniqueCell(int [][][] workTable) {
            for (int y = 0; y < 9; y++) {
                for (int x = 0; x < 9; x++) {
                    if (solveTable[x][y] == 0) {
                        int cnt = 0;
                        for (int z = 0; z < 9; z++) {
                            cnt += workTable[x][y][z];
                        }
                        if (cnt == 8) {
                            XYPos xyPos = new XYPos();
                            xyPos.x = x; xyPos.y = y;
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

            MakePDF makePDF = new MakePDF();
            makePDF.createPDF(blankTables, answerTables);
        }
    }
}

