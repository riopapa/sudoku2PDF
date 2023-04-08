package com.riopapa.sudoku2pdf;

import java.util.Random;

public class Solve {

    int [][] blankTable, solveTable, answerTable;
    int [][][] usedTable;
    Random random;

    public int exe(int [][] blanks, int [][]answers) {

        random = new Random();
        int blankRetry = 5;
        int solved = 0;
        blankTable = new SudokuTable().copy(blanks);
        answerTable = new SudokuTable().copy(answers);
        // initialize solveTable from blankTable
        while (blankRetry < 8) {
            solveTable = new SudokuTable().copy(blankTable);
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
    class XYPos {
        int x;
        int y;
        int nbr0;
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
            int x = random.nextInt(9); int y = random.nextInt(9);
            if (blankTable[x][y] == 0) {
                blankTable[x][y] = answerTable[x][y];
                cnt--;
            }
        }
        cnt = loop;
        while (cnt > 0) {
            int x = random.nextInt(9); int y = random.nextInt(9);
            if (blankTable[x][y] != 0) {
                blankTable[x][y] = 0;
                cnt--;
            }
        }
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

}
