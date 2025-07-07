package com.riopapa.sudoku2pdf;

/**
 * An interface representing a Sudoku generator. Any class that can create
 * Sudoku puzzles should implement this contract.
 */
public interface ISudokuMaker {
    void make(int nbrOfQuiz, int nbrOfBlank, OnSudokuGeneratedListener listener);
}