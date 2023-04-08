package com.riopapa.sudoku2pdf;

import java.util.Random;

public class SudokuTable {

    int [][] blankTable;
    Random random = new Random();

    public int[][] makeBlank(int [][]answer, int blankCount) {

        blankTable = copy(answer);
        int blanked = 0;
        while (blanked < blankCount) {
            int x = random.nextInt(9);
            int y = random.nextInt(9);
            if (blankTable[x][y] > 0) {
                blankTable[x][y] = 0;
                blanked++;
            }
        }
        return blankTable;
    }

    public int [][] copy (int [][] srcTable) {
        int [][] tgtTable = new int [9][9];
        for (int y = 0; y < 9; y++)
            for (int x = 0; x < 9; x++)
                tgtTable[x][y] = srcTable[x][y];
        return tgtTable;
    }

}
