package com.riopapa.sudoku2pdf;

import java.util.ArrayList;
import java.util.Random;

public class Answer {
    int [][] answer;
    Random random;
    public  int [][] generate() {

        answer = new int[9][9];
        random = new Random(System.currentTimeMillis());

        while (hasZeroBlock(answer)) {
            for (int blockX = 0; blockX < 3; blockX++) {
                for (int blockY = 0; blockY < 3; blockY++) {
                    if (answer[blockX*3][blockY*3] == 0) {
                        if (!isFillThisBlockOK(blockX, blockY)) {
                            clearBlock(nextThree(), nextThree());
                        }
                    }
                }
            }
        }

        return answer;
    }
    private boolean hasZeroBlock(int [][] tbl) {
        for (int x = 0;  x < 9; x +=3)
            for (int y = 0; y < 9; y +=3)
                if (tbl[x][y] == 0)
                    return true;
        return false;
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
            if (answer[x3+x][y3+y] == 0 && noDupVertical(x3+x, nbr) && noDupHorizontal(y3+y, nbr)) {
                answer[x3+x][y3+y] = nbr;
                oneLine.remove(pos);
                nbr++;
                loop = 81;
            }
            else
                loop--;
        }
        return loop != 0;
    }
    int nextThree() { // 0 ~ 2
        return random.nextInt(3);
    }
    int nextRanged(int range) { // 0 ~ range
        return random.nextInt(range);
    }

    private void clearBlock(int blockX, int blockY) {
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                answer[blockX*3+x][blockY*3+y] = 0;
    }
    boolean noDupVertical(int x,int nbr) {
        for (int y = 0; y < 9; y++)
            if (answer[x][y] == nbr)
                return false;
        return true;
    }

    boolean noDupHorizontal(int y,int nbr) {
        for (int x = 0; x < 9; x++)
            if (answer[x][y] == nbr)
                return false;
        return true;
    }

}
