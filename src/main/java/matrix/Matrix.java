package matrix;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * A mutable matrix of {@code double} values with basic arithmetic operations
 * and numerical methods for solving linear systems.
 */
public class Matrix {
    private double[] data;
    private int rows;
    private int cols;

    /** Creates a zero-filled matrix with the specified dimensions. */
    public Matrix(int rows, int cols) {
        if (rows < 0 || cols < 0) throw new IllegalArgumentException("Matrix dimensions cannot be negative.");
        this.rows = rows;
        this.cols = cols;
        data = new double[Math.multiplyExact(rows, cols)];
    }

    /** Creates a matrix from a two-dimensional array. Shorter rows are padded with zeros. */
    public Matrix(double[][] d) {
        if (d == null) {
            throw new IllegalArgumentException("Input array must not be null.");
        }
        for (double[] row : d) {
            if (row == null) {
                throw new IllegalArgumentException("Matrix rows must not be null.");
            }
        }
        this.rows = d.length;
        this.cols = 0;
        for (int r = 0; r < d.length; r++) {
            if (d[r].length > this.cols) {
                this.cols = d[r].length;
            }
        }
        this.data = new double[Math.multiplyExact(this.rows, this.cols)];
        for (int r = 0; r < d.length; r++) {
            for (int c = 0; c < this.cols; c++) {
                if (c < d[r].length) {
                    this.data[r * this.cols + c] = d[r][c];
                }
            }
        }
    }

    /** Returns a copy of the matrix data as a two-dimensional array. */
    public double[][] asArray() {
        double[][] arrayMatrix = new double[this.rows][this.cols];
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < this.cols; c++) {
                arrayMatrix[r][c] = this.data[r * cols + c];
            }
        }
        return arrayMatrix;
    }

    /** Returns the element at the specified row and column. */
    public double get(int r, int c) {
        if (r >= this.rows || c >= this.cols || r < 0 || c < 0) {
            throw new IllegalArgumentException("Row or column index is out of bounds." + r + " " + c);
        }
        return this.data[r * cols + c];
    }

    /** Sets the element at the specified row and column. */
    public void set(int r, int c, double value) {
        if (r >= this.rows || c >= this.cols || r < 0 || c < 0) {
            throw new IllegalArgumentException("Row or column index is out of bounds.");
        }
        this.data[r * cols + c] = value;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[\n");
        for (int r = 0; r < rows; r++) {
            buf.append("[");
            for (int c = 0; c < this.cols; c++) {
                buf.append(this.get(r, c));
                if (c < this.cols - 1) {
                    buf.append(", ");
                }
            }
            buf.append("]");
            if (r < this.rows - 1) {
                buf.append(",\n");
            }
        }
        buf.append("\n]");
        return buf.toString();
    }

    /** Changes the matrix dimensions without changing the underlying element order. */
    public void reshape(int newRows, int newCols) {
        if (newRows < 0 || newCols < 0) {
            throw new IllegalArgumentException("Matrix dimensions cannot be negative.");
        }
        if ((long) rows * cols != (long) newRows * newCols)
            throw new IllegalArgumentException(String.format("%d x %d matrix can't be reshaped to %d x %d", rows, cols, newRows, newCols));
        this.rows = newRows;
        this.cols = newCols;
    }

    /** Returns the matrix dimensions as {@code [rows, columns]}. */
    public int[] shape() {
        return new int[]{rows, cols};
    }

    /** Adds another matrix element-wise, supporting compatible repeating dimensions. */
    public Matrix add(Matrix m) {
        if (m == null) {
            throw new IllegalArgumentException("Input matrix must not be null.");
        }
        if (rows == 0 || cols == 0 || m.rows == 0 || m.cols == 0) {
            throw new IllegalArgumentException("Broadcast operations require non-empty matrices.");
        }

        int maxRows = Math.max(rows, m.rows);
        int maxCols = Math.max(cols, m.cols);
        if (rows % m.rows != 0 && m.rows % rows != 0) {
            throw new IllegalArgumentException(String.format("Incompatible matrix row dimensions: %d and %d", rows, m.rows));
        }
        if (cols % m.cols != 0 && m.cols % cols != 0) {
            throw new IllegalArgumentException(String.format("Incompatible matrix column dimensions: %d and %d", cols, m.cols));
        }
        Matrix result = new Matrix(maxRows, maxCols);
        for (int r = 0; r < maxRows; r++) {
            for (int c = 0; c < maxCols; c++) {
                double val1 = get(r % rows, c % cols);
                double val2 = m.get(r % m.rows, c % m.cols);
                result.set(r, c, val1 + val2);
            }
        }
        return result;
    }

    /** Subtracts another matrix element-wise, supporting compatible repeating dimensions. */
    public Matrix sub(Matrix m) {
        if (m == null) {
            throw new IllegalArgumentException("Input matrix must not be null.");
        }
        if (rows == 0 || cols == 0 || m.rows == 0 || m.cols == 0) {
            throw new IllegalArgumentException("Broadcast operations require non-empty matrices.");
        }

        int maxRows = Math.max(rows, m.rows);
        int maxCols = Math.max(cols, m.cols);

        if (rows % m.rows != 0 && m.rows % rows != 0) {
            throw new IllegalArgumentException(String.format("Incompatible matrix row dimensions: %d and %d", rows, m.rows));
        }
        if (cols % m.cols != 0 && m.cols % cols != 0) {
            throw new IllegalArgumentException(String.format("Incompatible matrix column dimensions: %d and %d", cols, m.cols));
        }
        Matrix result = new Matrix(maxRows, maxCols);
        for (int r = 0; r < maxRows; r++) {
            for (int c = 0; c < maxCols; c++) {
                double val1 = get(r % rows, c % cols);
                double val2 = m.get(r % m.rows, c % m.cols);
                result.set(r, c, val1 - val2);
            }
        }
        return result;
    }

    /** Multiplies matrices element-wise, supporting compatible repeating dimensions. */
    public Matrix mul(Matrix m) {
        if (m == null) {
            throw new IllegalArgumentException("Input matrix must not be null.");
        }
        if (rows == 0 || cols == 0 || m.rows == 0 || m.cols == 0) {
            throw new IllegalArgumentException("Broadcast operations require non-empty matrices.");
        }

        int maxRows = Math.max(rows, m.rows);
        int maxCols = Math.max(cols, m.cols);

        if (rows % m.rows != 0 && m.rows % rows != 0) {
            throw new IllegalArgumentException(String.format("Incompatible matrix row dimensions: %d and %d", rows, m.rows));
        }
        if (cols % m.cols != 0 && m.cols % cols != 0) {
            throw new IllegalArgumentException(String.format("Incompatible matrix column dimensions: %d and %d", cols, m.cols));
        }
        Matrix result = new Matrix(maxRows, maxCols);
        for (int r = 0; r < maxRows; r++) {
            for (int c = 0; c < maxCols; c++) {
                double val1 = get(r % rows, c % cols);
                double val2 = m.get(r % m.rows, c % m.cols);
                result.set(r, c, val1 * val2);
            }
        }
        return result;
    }

    /** Divides matrices element-wise, supporting compatible repeating dimensions. */
    public Matrix div(Matrix m) {
        if (m == null) {
            throw new IllegalArgumentException("Input matrix must not be null.");
        }
        if (rows == 0 || cols == 0 || m.rows == 0 || m.cols == 0) {
            throw new IllegalArgumentException("Broadcast operations require non-empty matrices.");
        }

        int maxRows = Math.max(rows, m.rows);
        int maxCols = Math.max(cols, m.cols);
        if (rows % m.rows != 0 && m.rows % rows != 0) {
            throw new IllegalArgumentException(String.format("Incompatible matrix row dimensions: %d and %d", rows, m.rows));
        }
        if (cols % m.cols != 0 && m.cols % cols != 0) {
            throw new IllegalArgumentException(String.format("Incompatible matrix column dimensions: %d and %d", cols, m.cols));
        }

        Matrix result = new Matrix(maxRows, maxCols);
        for (int r = 0; r < maxRows; r++) {
            for (int c = 0; c < maxCols; c++) {
                double val1 = get(r % rows, c % cols);
                double val2 = m.get(r % m.rows, c % m.cols);
                if (val2 == 0) {
                    throw new ArithmeticException("Division by zero.");
                }
                result.set(r, c, val1 / val2);
            }
        }
        return result;
    }

    /** Performs matrix multiplication. */
    public Matrix dot(Matrix m) {
        if (m == null) {
            throw new IllegalArgumentException("Input matrix must not be null.");
        }
        if (cols != m.rows) {
            throw new IllegalArgumentException("The number of columns in the first matrix must equal the number of rows in the second matrix.");
        }
        Matrix result = new Matrix(rows, m.cols);
        for (int r = 0; r < this.rows; r++) {
            for (int c = 0; c < m.cols; c++) {
                double sum = 0;
                for (int i = 0; i < cols; i++) {
                    sum += get(r, i) * m.get(i, c);
                }
                result.set(r, c, sum);
            }
        }
        return result;
    }

    /** Returns the Frobenius norm of this matrix. */
    public double frobenius() {
        double sum = 0;
        for (int idx = 0; idx < data.length; idx++) {
            sum += data[idx] * data[idx];
        }
        return Math.sqrt(sum);
    }

    /** Creates a matrix filled with random values in the range {@code [0.0, 1.0)}. */
    public static Matrix random(int rows, int cols) {
        Matrix m = new Matrix(rows, cols);
        Random rndm = new Random();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                m.set(r, c, rndm.nextDouble());
            }
        }
        return m;
    }

    /** Creates an {@code n x n} identity matrix. */
    public static Matrix eye(int n) {
        Matrix m = new Matrix(n, n);
        for (int i = 0; i < n; i++) {
            m.set(i, i, 1.0);
        }
        return m;
    }

    /** Returns a deep copy of this matrix. */
    public Matrix copy() {
        Matrix clone = new Matrix(this.rows, this.cols);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                clone.set(r, c, get(r, c));
            }
        }
        return clone;
    }

    private void swapRows(int a, int b) {
        double temp;
        for (int c = 0; c < cols; c++) {
            temp = get(a, c);
            set(a, c, get(b, c));
            set(b, c, temp);
        }
    }

    private void subtractRowMultiplied(int targetRow, int sourceRow, double factor) {
        for (int c = 0; c < this.cols; c++) {
            double newValue = this.get(targetRow, c) - factor * this.get(sourceRow, c);
            this.set(targetRow, c, newValue);
        }
    }

    /** Computes the determinant using Gaussian elimination with partial pivoting. */
    public double det() {
        if (cols != rows) throw new IllegalArgumentException("Matrix must be square.");
        int n = rows;
        if (n == 0) return 1.0;
        Matrix m = this.copy();
        double detSign = 1.0;
        for (int c = 0; c < n; c++) {
            int pivotRow = c;
            for (int r = c + 1; r < n; r++) {
                if (Math.abs(m.get(r, c)) > Math.abs(m.get(pivotRow, c))) pivotRow = r;
            }
            if (Math.abs(m.get(pivotRow, c)) < 1e-9) return 0.0;
            if (pivotRow != c) {
                m.swapRows(pivotRow, c);
                detSign *= -1;
            }
            for (int r = c + 1; r < n; r++) {
                double factor = m.get(r, c) / m.get(c, c);
                m.subtractRowMultiplied(r, c, factor);
            }
        }
        double determinant = detSign;
        for (int i = 0; i < n; i++) determinant *= m.get(i, i);
        return determinant;
    }

    /** Computes the inverse using Gauss-Jordan elimination with partial pivoting. */
    public Matrix invert() {
        if (rows != cols) throw new IllegalArgumentException("Only square matrices can be inverted.");

        int n = rows;
        Matrix m = copy();
        Matrix inverse = eye(n);

        double maxAbs = 0.0;
        for (int i = 0; i < data.length; i++) {
            maxAbs = Math.max(maxAbs, Math.abs(data[i]));
        }

        if (maxAbs == 0.0) throw new IllegalArgumentException("The zero matrix is not invertible.");

        double tolerance = 1e-12 * maxAbs;

        for (int c = 0; c < n; c++) {
            int pivotRow = c;

            for (int r = c + 1; r < n; r++) {
                if (Math.abs(m.get(r, c))
                        > Math.abs(m.get(pivotRow, c))) {
                    pivotRow = r;
                }
            }

            if (Math.abs(m.get(pivotRow, c)) <= tolerance)
                throw new ArithmeticException("Matrix is singular or numerically close to singular.");

            if (pivotRow != c) {
                m.swapRows(pivotRow, c);
                inverse.swapRows(pivotRow, c);
            }

            double pivot = m.get(c, c);

            for (int j = 0; j < n; j++) {
                m.set(c, j, m.get(c, j) / pivot);
                inverse.set(c, j, inverse.get(c, j) / pivot);
            }

            for (int r = 0; r < n; r++) {
                if (r == c) continue;

                double factor = m.get(r, c);

                if (factor != 0.0) {
                    m.subtractRowMultiplied(r, c, factor);
                    inverse.subtractRowMultiplied(r, c, factor);
                }
            }
        }

        return inverse;
    }

    /** Solves {@code A * x = b} using Gaussian elimination with partial pivoting. */
    public static Matrix solveGauss(Matrix A, Matrix b) {
        if (A == null || b == null) {
            throw new IllegalArgumentException("Matrices A and b must not be null.");
        }
        if (A.rows != A.cols) throw new IllegalArgumentException("Matrix A must be square.");
        if (b.rows != A.rows || b.cols != 1) throw new IllegalArgumentException("Vector b must have dimensions " + A.rows + " x 1.");
        int n = A.rows;
        Matrix M = A.copy();
        Matrix B = b.copy();
        for (int c = 0; c < n; c++) {
            int pivotRow = c;
            for (int r = c + 1; r < n; r++) {
                if (Math.abs(M.get(r, c)) > Math.abs(M.get(pivotRow, c))) pivotRow = r;
            }

            if (Math.abs(M.get(pivotRow, c)) < 1e-9)
                throw new ArithmeticException("Matrix A is singular; the system cannot be solved.");

            if (pivotRow != c) {
                M.swapRows(pivotRow, c);
                B.swapRows(pivotRow, c);
            }

            for (int r = c + 1; r < n; r++) {
                double factor = M.get(r, c) / M.get(c, c);
                M.subtractRowMultiplied(r, c, factor);
                B.subtractRowMultiplied(r, c, factor);
            }
        }
        Matrix ans = new Matrix(n, 1);
        for (int r = n - 1; r>=0;r--) {
            double sum = 0.0;
            for (int c=r+1;c<n;c++) {
                sum += M.get(r,c) * ans.get(c,0);
            }
            if (Math.abs(M.get(r, r)) < 1e-9) throw new ArithmeticException("Division by zero.");

            double value = (B.get(r,0) - sum) / M.get(r,r);
            ans.set(r,0,value);
        }
        return ans;
    }

    /** Solves {@code A * x = b} using the Doolittle LU decomposition without pivoting. */
    public static Matrix solveLU(Matrix A, Matrix b) {
        if (A == null || b == null) {
            throw new IllegalArgumentException("Matrices A and b must not be null.");
        }
        if (A.rows != A.cols) throw new IllegalArgumentException("Matrix A must be square.");
        if (b.rows != A.rows || b.cols != 1)
            throw new IllegalArgumentException("Vector b must have dimensions " + A.rows + " x 1.");
        int n = A.rows;
        Matrix L = new Matrix(n, n);
        Matrix U = new Matrix(n, n);
        // Doolittle method
        for (int i = 0; i < n; i++) {
            for (int k = i; k < n; k++) {
                double sum = 0.0;
                for (int j = 0; j < i; j++) sum += L.get(i, j) * U.get(j, k);
                U.set(i, k, A.get(i, k) - sum);
            }
            if (Math.abs(U.get(i, i)) < 1e-9) throw new ArithmeticException("Matrix is singular.");

            L.set(i,i,1.0);
            for (int k = i+1; k < n; k++) {
                double sum = 0.0;
                for (int j = 0; j < i; j++) sum += L.get(k, j) * U.get(j, i);
                L.set(k, i, (A.get(k, i) - sum) / U.get(i, i));
            }
        }

        Matrix y = new Matrix(n, 1);
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < i; j++) {
                sum += L.get(i, j) * y.get(j, 0);
            }
            y.set(i, 0, b.get(i, 0) - sum);
        }

        Matrix x = new Matrix(n, 1);
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0.0;
            for (int j = i + 1; j < n; j++) {
                sum += U.get(i, j) * x.get(j, 0);
            }
            x.set(i, 0, (y.get(i, 0) - sum) / U.get(i, i));
        }
        return x;
    }

    /** Rounds every matrix element to two decimal places using {@code HALF_UP}. */
    public void round() {
        for (int r=0;r<rows;r++){
            for (int c=0;c<cols;c++) {
                double val = get(r,c);
                double roundedVal = BigDecimal.valueOf(val)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
                set(r,c, roundedVal);
            }
        }
    }
}