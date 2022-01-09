package recognition;

import java.util.Scanner;

/* * Java class to represent a Matrix. It uses a two dimensional array to * represent a Matrix. */
class Matrix {
    private int rows;
    private int columns;
    private int[][] data;

    public Matrix(int row, int column) {
        this.rows = row;
        this.columns = column;
        data = new int[rows][columns];
    }

    public Matrix(int[][] data) {
        this.data = data;
        this.rows = data.length;
        this.columns = data[0].length;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public Matrix multiply(Matrix other) {
        if (this.columns != other.rows) {
            throw new IllegalArgumentException(
                    "column of this matrix is not equal to row " +
                            "of second matrix, cannot multiply");
        }
        int[][] product = new int[this.rows][other.columns];
        int sum = 0;
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < other.columns; j++) {
                for (int k = 0; k < other.rows; k++) {
                    sum = sum + data[i][k] * other.data[k][j]; } product[i][j] = sum;
            }
        }
        return new Matrix(product);
    }

    public void print() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                System.out.print(data[i][j] + " ");
            }
            System.out.println();
        }
    }
}
