package com.riopapa.sudoku2pdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;

public class Create9x9Puzzle implements ISudokuMaker {

    // Board configuration for a standard 9x9 Sudoku
    public static final int BOX_ROWS = 3;
    public static final int BOX_COLS = 3;
    public static final int SIZE = 9; // BOX_ROWS * BOX_COLS

    @Override
    public void make(int nbrOfQuiz, int nbrOfBlank, OnGenerateListener listener) {

        final Handler handler = new Handler(Looper.getMainLooper());
        final List<int[][]> puzzles = new ArrayList<>();
        final List<int[][]> answers = new ArrayList<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {

            for (int i = 0; i < nbrOfQuiz; i++) {
                // Generate one puzzle
                int[][] solvedBoard = generateSolvedBoard();
                int[][] puzzleBoard = pokeHoles(solvedBoard, nbrOfBlank);
                final int progress = i + 1;
                final int[][] finalPuzzle = puzzleBoard;
                final int[][] finalSolution = solvedBoard;

                // Post a progress update back to the UI thread
                handler.post(() -> {
                    // --- This runs on the UI thread ---
                    puzzles.add(finalPuzzle);
                    answers.add(finalSolution);
                    handler.post(() -> listener.onProgress(progress, nbrOfQuiz));                        // progressBar.setProgress(progress);
                    // statusTextView.setText("Generated " + progress + "/" + nbrOfQuiz);
                });
            }

            // After the loop finishes, post the completion message
            handler.post(() -> listener.onComplete(puzzles, answers));
        });

        executor.shutdown();
    }

    /**
     * Generates a fully solved 9x9 Sudoku board.
     * This is the first step in creating a new puzzle.
     *
     * @return A 2D integer array representing a complete and valid 9x9 Sudoku solution.
     */
    public static int[][] generateSolvedBoard() {
        int[][] board = new int[SIZE][SIZE];
        solve(board);
        return board;
    }

    /**
     * Pokes holes in a solved Sudoku board to create a puzzle of a given difficulty.
     * It guarantees that the resulting puzzle has exactly one unique solution.
     *
     * @param solvedBoard A fully solved 9x9 Sudoku board.
     * @param blankCount The number of empty cells the final puzzle should have (e.g., 40-55 is common).
     * @return A new 2D array representing the puzzle with holes. The original board is not modified.
     */
    public static int[][] pokeHoles(int[][] solvedBoard, int blankCount) {
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
        Collections.shuffle(cells);

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
                // If removing this cell makes the puzzle have 0 or multiple solutions, put it back.
                puzzleBoard[r][c] = temp;
            } else {
                holesPoked++;
            }
        }
        return puzzleBoard;
    }

    // --- Private Helper Methods for Solving and Verification ---

    private static boolean solve(int[][] board) {
        int[] emptyCell = findEmptyCell(board);
        if (emptyCell == null) return true;

        int r = emptyCell[0];
        int c = emptyCell[1];

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= SIZE; i++) numbers.add(i);
        Collections.shuffle(numbers);

        for (int num : numbers) {
            if (isValidPlacement(board, r, c, num)) {
                board[r][c] = num;
                if (solve(board)) {
                    return true;
                }
                board[r][c] = 0; // Backtrack
            }
        }
        return false;
    }

    private static int countSolutions(int[][] board) {
        int[] emptyCell = findEmptyCell(board);
        if (emptyCell == null) return 1;

        int r = emptyCell[0];
        int c = emptyCell[1];
        int solutionCount = 0;

        for (int num = 1; num <= SIZE; num++) {
            if (isValidPlacement(board, r, c, num)) {
                // Create a copy of the board to pass to the recursive call
                // This is important to avoid modifications affecting other branches
                int[][] boardCopy = new int[SIZE][SIZE];
                for(int i=0; i<SIZE; i++) System.arraycopy(board[i], 0, boardCopy[i], 0, SIZE);

                boardCopy[r][c] = num;
                solutionCount += countSolutions(boardCopy);

                // If we already found more than one solution, we can stop early.
                if (solutionCount > 1) return 2;
            }
        }
        return solutionCount;
    }

    // --- Private Helper Methods for Board Operations ---

    private static int[] findEmptyCell(int[][] board) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == 0) return new int[]{r, c};
            }
        }
        return null;
    }

    public static boolean isValidPlacement(int[][] board, int row, int col, int number) {
        // Check row
        for (int c = 0; c < SIZE; c++) {
            if (board[row][c] == number) return false;
        }
        // Check column
        for (int r = 0; r < SIZE; r++) {
            if (board[r][col] == number) return false;
        }
        // Check 3x3 box
        int boxStartRow = row - row % BOX_ROWS;
        int boxStartCol = col - col % BOX_COLS;
        for (int r = 0; r < BOX_ROWS; r++) {
            for (int c = 0; c < BOX_COLS; c++) {
                if (board[boxStartRow + r][boxStartCol + c] == number) return false;
            }
        }
        return true;
    }
}