package com.riopapa.sudoku2pdf;
import java.util.List;

/**
 * A generic listener for any Sudoku generator.
 * It provides callbacks for progress, completion, and errors.
 */
public interface OnSudokuGeneratedListener {
    void onProgress(int current, int total);
    void onComplete(List<int[][]> puzzles, List<int[][]> solutions);
    void onError(Exception e);
}