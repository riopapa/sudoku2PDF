package com.riopapa.sudoku2pdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A utility class for generating 6x6 Sudoku puzzles.
 * Follows a clear, multi-step process:
 * 1. Generate a fully solved board.
 * 2. Poke holes in it while verifying a unique solution is maintained.
 */
public class Make6x6 {

    // Board configuration
    public static final int BOX_ROWS = 2;
    public static final int BOX_COLS = 3;
    public static final int SIZE = BOX_ROWS * BOX_COLS; // This is 6

    private static final Random random = new Random();

    /**
     * Generates a fully solved 6x6 Sudoku board.
     * This is the first step in creating a new puzzle.
     *
     * @return A 2D integer array representing a complete and valid Sudoku solution.
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
     * @param solvedBoard A fully solved 6x6 Sudoku board.
     * @param blankCount The number of empty cells the final puzzle should have.
     * @return A new 2D array representing the puzzle with holes. The original board is not modified.
     */
    public static int[][] pokeHoles(int[][] solvedBoard, int blankCount) {
        // Create a copy to work with, so the original solved board is preserved.
        int[][] puzzleBoard = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(solvedBoard[i], 0, puzzleBoard[i], 0, SIZE);
        }

        // Get a list of all cell coordinates and shuffle them for random removal.
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

            int temp = puzzleBoard[r][c]; // Save the original number
            puzzleBoard[r][c] = 0; // Poke the hole

            // Verify if the puzzle still has exactly one solution
            if (countSolutions(puzzleBoard) != 1) {
                // If not, it's either unsolvable or has multiple solutions.
                // Revert the change.
                puzzleBoard[r][c] = temp;
            } else {
                holesPoked++;
            }
        }
        return puzzleBoard;
    }

    // --- Private Helper Methods for Solving and Verification ---

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

    /**
     * Recursive method to count the number of possible solutions for a given board.
     */
    private static int countSolutions(int[][] board) {
        int[] emptyCell = findEmptyCell(board);
        if (emptyCell == null) {
            return 1; // Found one complete solution
        }

        int r = emptyCell[0];
        int c = emptyCell[1];
        int solutionCount = 0;

        for (int num = 1; num <= SIZE; num++) {
            if (isValidPlacement(board, r, c, num)) {
                board[r][c] = num;
                solutionCount += countSolutions(board);
                board[r][c] = 0; // Backtrack to find all other possible solutions
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

    // --- Example Usage (for testing without Android) ---

    public static void main(String[] args) {
        System.out.println("Step 1: Generate a fully solved board (the answer key).");
        int[][] solvedBoard = generateSolvedBoard();
        printBoard(solvedBoard);

        System.out.println("\n-----------------------------------\n");

        int difficulty = 20; // Let's make a hard puzzle with 20 blanks
        System.out.println("Step 2: Poke " + difficulty + " holes while ensuring a unique solution.");
        int[][] puzzleBoard = pokeHoles(solvedBoard, difficulty);
        printBoard(puzzleBoard);
    }

    private static void printBoard(int[][] board) {
        for (int r = 0; r < SIZE; r++) {
            if (r > 0 && r % BOX_ROWS == 0) {
                System.out.println("------+-------");
            }
            for (int c = 0; c < SIZE; c++) {
                if (c > 0 && c % BOX_COLS == 0) {
                    System.out.print("| ");
                }
                System.out.print(board[r][c] == 0 ? ". " : board[r][c] + " ");
            }
            System.out.println();
        }
    }
}