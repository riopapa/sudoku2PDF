package com.riopapa.sudoku2pdf;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Creae6x6Puzzle implements ISudokuMaker {

    // --- Configuration Constants for a 6x6 Grid ---
    private static final int SIZE = 6;
    private static final int BOX_ROWS = 2;
    private static final int BOX_COLS = 3;
    private static final Random random = new Random();

    /**
     * The main public method required by the ISudokuMaker interface.
     * It generates a specified number of quizzes on a background thread.
     *
     * @param nbrOfQuiz The total number of puzzles to create.
     * @param nbrOfBlank The number of empty cells for each puzzle's difficulty.
     * @param listener The callback listener to report progress, completion, or errors.
     */

    @Override
    public void make(int nbrOfQuiz, int nbrOfBlank, OnGenerateListener listener) {
        final List<int[][]> puzzles = new ArrayList<>();
        final List<int[][]> solutions = new ArrayList<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // --- This block runs on a background thread ---
                for (int i = 0; i < nbrOfQuiz; i++) {
                    int[][] solvedBoard = generateSolvedBoard();
                    int[][] puzzleBoard = pokeHoles(solvedBoard, nbrOfBlank);

                    puzzles.add(puzzleBoard);
                    solutions.add(solvedBoard);

                    final int progress = i + 1;
                    // Post progress update back to the UI thread
                    handler.post(() -> listener.onProgress(progress, nbrOfQuiz));
                }
                // After the loop, post the final successful result
                handler.post(() -> listener.onComplete(puzzles, solutions));

            } catch (Exception e) {
                // If any error occurs, post the error back to the UI thread
                handler.post(() -> listener.onError(e));
            }
        });

        // Shut down the executor to release its resources. It will finish its current tasks.
        executor.shutdown();
    }


    // --- Private Static Helper Methods for Core Logic ---

    /**
     * Generates a fully solved 6x6 Sudoku board.
     */
    private static int[][] generateSolvedBoard() {
        int[][] board = new int[SIZE][SIZE];
        solve(board);
        return board;
    }

    /**
     * Pokes holes in a solved board, ensuring a unique solution is maintained.
     */
    private static int[][] pokeHoles(int[][] solvedBoard, int blankCount) {
        int[][] puzzleBoard = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(solvedBoard[i], 0, puzzleBoard[i], 0, SIZE);
        }

        List<int[]> cells = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                cells.add(new int[]{r, c});
            }
        }
        Collections.shuffle(cells, random);

        int holesPoked = 0;
        for (int[] cell : cells) {
            if (holesPoked >= blankCount) {
                break;
            }
            int r = cell[0];
            int c = cell[1];

            int temp = puzzleBoard[r][c];
            puzzleBoard[r][c] = 0;

            if (countSolutions(puzzleBoard) != 1) {
                // Revert if the puzzle is no longer uniquely solvable
                puzzleBoard[r][c] = temp;
            } else {
                holesPoked++;
            }
        }
        return puzzleBoard;
    }

    /**
     * Recursive backtracking solver to fill an empty board.
     */
    private static boolean solve(int[][] board) {
        int[] emptyCell = findEmptyCell(board);
        if (emptyCell == null) return true; // Solved

        int r = emptyCell[0];
        int c = emptyCell[1];

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= SIZE; i++) numbers.add(i);
        Collections.shuffle(numbers, random);

        for (int num : numbers) {
            if (isValidPlacement(board, r, c, num)) {
                board[r][c] = num;
                if (solve(board)) return true;
                board[r][c] = 0; // Backtrack
            }
        }
        return false;
    }

    /**
     * Recursive method to count the number of possible solutions for a given board.
     */
    private static int countSolutions(int[][] board) {
        int[] emptyCell = findEmptyCell(board);
        if (emptyCell == null) return 1; // Found one solution

        int r = emptyCell[0];
        int c = emptyCell[1];
        int solutionCount = 0;

        for (int num = 1; num <= SIZE; num++) {
            if (isValidPlacement(board, r, c, num)) {
                board[r][c] = num;
                solutionCount += countSolutions(board);
                board[r][c] = 0; // Backtrack

                // Optimization: stop if we already know there isn't a unique solution
                if (solutionCount > 1) return 2;
            }
        }
        return solutionCount;
    }

    /**
     * Checks if placing a number in a given cell is valid.
     */
    private static boolean isValidPlacement(int[][] board, int row, int col, int number) {
        // Check row
        for (int c = 0; c < SIZE; c++) {
            if (board[row][c] == number) return false;
        }
        // Check column
        for (int r = 0; r < SIZE; r++) {
            if (board[r][col] == number) return false;
        }
        // Check 2x3 box
        int boxStartRow = row - row % BOX_ROWS;
        int boxStartCol = col - col % BOX_COLS;
        for (int r = 0; r < BOX_ROWS; r++) {
            for (int c = 0; c < BOX_COLS; c++) {
                if (board[boxStartRow + r][boxStartCol + c] == number) return false;
            }
        }
        return true;
    }

    /**
     * Finds the first empty cell (value 0) on the board.
     */
    private static int[] findEmptyCell(int[][] board) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == 0) return new int[]{r, c};
            }
        }
        return null;
    }
}