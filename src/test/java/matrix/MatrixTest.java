package matrix;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

class MatrixTest {
    private static final double EPS = 1e-9;

    /** Compares dimensions and all elements of two matrices. */
    private void assertMatrixEquals(Matrix expected, Matrix actual) {
        assertArrayEquals(expected.shape(), actual.shape(), "Matrix dimensions differ");
        int[] shape = expected.shape();
        for (int r = 0; r < shape[0]; r++) {
            for (int c = 0; c < shape[1]; c++) {
                assertEquals(expected.get(r, c), actual.get(r, c), EPS,
                        "Mismatch at position [" + r + "][" + c + "]");
            }
        }
    }

    /** Compares a matrix with a manually specified two-dimensional array. */
    private void assertMatrixEquals(double[][] expected, Matrix actual) {
        int[] shape = actual.shape();
        assertEquals(expected.length, shape[0], "Unexpected number of rows");
        int expectedCols = expected.length == 0 ? shape[1] : expected[0].length;
        assertEquals(expectedCols, shape[1], "Unexpected number of columns");
        for (int r = 0; r < expected.length; r++) {
            assertEquals(expected[r].length, shape[1], "Unexpected row length at row " + r);
            for (int c = 0; c < shape[1]; c++) {
                assertEquals(expected[r][c], actual.get(r, c), EPS,
                        "Mismatch at position [" + r + "][" + c + "]");
            }
        }
    }

    @Test
    void matrixConstructorRowColumn() {
        Matrix m = new Matrix(2, 3);
        assertArrayEquals(new int[]{2, 3}, m.shape());
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 3; c++) {
                assertEquals(0.0, m.get(r, c), EPS);
            }
        }
    }

    @Test
    void matrixConstructorArray() {
        double[][] data = {{1.0, 2.0, 3.0}, {4.0}, {5.0, 6.0}};
        Matrix m = new Matrix(data);
        assertMatrixEquals(new double[][]{
                {1.0, 2.0, 3.0},
                {4.0, 0.0, 0.0},
                {5.0, 6.0, 0.0}
        }, m);
    }

    @Test
    void constructorRejectsNullArrayAndRows() {
        assertThrows(IllegalArgumentException.class, () -> new Matrix((double[][]) null));
        assertThrows(IllegalArgumentException.class,
                () -> new Matrix(new double[][]{{1.0}, null}));
    }

    @Test
    void asArrayReturnsCopyWithCorrectValues() {
        double[][] originalData = {{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}};
        Matrix m = new Matrix(originalData);
        double[][] array = m.asArray();
        assertArrayEquals(originalData[0], array[0], EPS);
        assertArrayEquals(originalData[1], array[1], EPS);
        array[0][0] = 999.0;
        assertEquals(1.0, m.get(0, 0), EPS,
                "Changing the returned array must not modify the matrix");
    }

    @Test
    void getAndSet() {
        Matrix m = new Matrix(2, 3);
        m.set(0, 0, 1);
        m.set(0, 2, 3);
        m.set(1, 0, 4);
        m.set(1, 2, 6);
        assertEquals(1.0, m.get(0, 0), EPS);
        assertEquals(3.0, m.get(0, 2), EPS);
        assertEquals(4.0, m.get(1, 0), EPS);
        assertEquals(6.0, m.get(1, 2), EPS);
    }

    @Test
    void getRejectsInvalidIndices() {
        Matrix m = new Matrix(2, 3);
        assertThrows(IllegalArgumentException.class, () -> m.get(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> m.get(0, -1));
        assertThrows(IllegalArgumentException.class, () -> m.get(2, 0));
        assertThrows(IllegalArgumentException.class, () -> m.get(0, 3));
    }

    @Test
    void setRejectsInvalidIndices() {
        Matrix m = new Matrix(2, 3);
        assertThrows(IllegalArgumentException.class, () -> m.set(-1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> m.set(0, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> m.set(2, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> m.set(0, 3, 1));
    }

    @Test
    void toStringHasExpectedBracketAndCommaCounts() {
        List<Matrix> matrices = List.of(
                new Matrix(new double[][]{{1.0, 2.0}, {3.0, 4.0}}),
                new Matrix(0, 0), Matrix.random(50, 50), Matrix.random(100, 100));
        for (Matrix m : matrices) {
            String str = m.toString();
            int[] shape = m.shape();
            assertNotNull(str);
            long openBrackets = str.chars().filter(ch -> ch == '[').count();
            long closeBrackets = str.chars().filter(ch -> ch == ']').count();
            long commas = str.chars().filter(ch -> ch == ',').count();
            assertEquals(shape[0] + 1, openBrackets);
            assertEquals(shape[0] + 1, closeBrackets);
            assertEquals(Math.max(shape[0] - 1 + (shape[1] - 1) * shape[0], 0), commas);
        }
    }

    @Test
    void reshape() {
        Matrix m = new Matrix(2, 6);
        assertDoesNotThrow(() -> m.reshape(3, 4));
        assertArrayEquals(new int[]{3, 4}, m.shape());
        assertDoesNotThrow(() -> m.reshape(1, 12));
        assertDoesNotThrow(() -> m.reshape(12, 1));
        assertThrows(IllegalArgumentException.class, () -> m.reshape(5, 3));
        assertThrows(IllegalArgumentException.class, () -> m.reshape(-2, 6));
    }

    @Test
    void add() {
        List<List<Matrix>> testPairs = new ArrayList<>();
        testPairs.add(List.of(Matrix.random(70, 50), Matrix.random(70, 50)));
        testPairs.add(List.of(Matrix.random(100, 100), Matrix.random(100, 100)));
        testPairs.add(List.of(Matrix.random(1, 1), Matrix.random(1, 1)));
        for (List<Matrix> pair : testPairs) {
            Matrix a = pair.get(0), b = pair.get(1);
            Matrix actual = a.add(b);
            SimpleMatrix expected = new SimpleMatrix(a.asArray()).plus(new SimpleMatrix(b.asArray()));
            assertTrue(new SimpleMatrix(actual.asArray()).isIdentical(expected, EPS));
        }
    }

    @Test
    void addHasCorrectManuallyCalculatedResult() {
        Matrix a = new Matrix(new double[][]{{1, 2}, {3, 4}});
        Matrix b = new Matrix(new double[][]{{5, 6}, {7, 8}});
        assertMatrixEquals(new double[][]{{6, 8}, {10, 12}}, a.add(b));
    }

    @Test
    void addShouldThrowExceptionForZeroDimensions() {
        Matrix normal = new Matrix(3, 3);
        Matrix zeroByZero = new Matrix(0, 0);
        Matrix zeroByThree = new Matrix(0, 3);
        Matrix fiveByZero = new Matrix(5, 0);
        assertThrows(IllegalArgumentException.class, () -> zeroByZero.add(normal));
        assertThrows(IllegalArgumentException.class, () -> zeroByThree.add(normal));
        assertThrows(IllegalArgumentException.class, () -> fiveByZero.add(normal));
        assertThrows(IllegalArgumentException.class, () -> normal.add(zeroByZero));
    }

    @Test
    void addShouldThrowExceptionForIncompatibleDimensions() {
        Matrix m3x3 = new Matrix(3, 3);
        assertThrows(IllegalArgumentException.class, () -> m3x3.add(new Matrix(5, 3)));
        assertThrows(IllegalArgumentException.class, () -> m3x3.add(new Matrix(3, 5)));
    }

    @Test
    void addShouldBroadcastRowVector() {
        Matrix matrix = new Matrix(new double[][]{{1, 1, 1}, {2, 2, 2}, {3, 3, 3}});
        Matrix rowVector = new Matrix(new double[][]{{10, 20, 30}});
        assertMatrixEquals(new double[][]{{11, 21, 31}, {12, 22, 32}, {13, 23, 33}},
                matrix.add(rowVector));
    }

    @Test
    void addShouldBroadcastColumnVector() {
        Matrix matrix = new Matrix(new double[][]{{1, 2, 3}, {1, 2, 3}, {1, 2, 3}});
        Matrix colVector = new Matrix(new double[][]{{10}, {20}, {30}});
        assertMatrixEquals(new double[][]{{11, 12, 13}, {21, 22, 23}, {31, 32, 33}},
                matrix.add(colVector));
    }

    @Test
    void addShouldBroadcastScalar() {
        Matrix matrix = new Matrix(new double[][]{{5, 10}, {15, 20}});
        assertMatrixEquals(new double[][]{{7, 12}, {17, 22}},
                matrix.add(new Matrix(new double[][]{{2}})));
    }

    @Test
    void addShouldBroadcastMultiples() {
        Matrix small = new Matrix(new double[][]{{1, 2}, {3, 4}});
        Matrix large = new Matrix(4, 4);
        assertMatrixEquals(new double[][]{
                {1, 2, 1, 2}, {3, 4, 3, 4},
                {1, 2, 1, 2}, {3, 4, 3, 4}
        }, large.add(small));
    }

    @Test
    void sub() {
        Matrix a = new Matrix(new double[][]{{10, 20}, {30, 40}});
        Matrix b = new Matrix(new double[][]{{1, 2}});
        assertMatrixEquals(new double[][]{{9, 18}, {29, 38}}, a.sub(b));
        assertThrows(IllegalArgumentException.class, () -> a.sub(new Matrix(3, 2)));
        assertThrows(IllegalArgumentException.class, () -> new Matrix(0, 5).sub(a));
    }

    @Test
    void subHasCorrectManuallyCalculatedResult() {
        Matrix a = new Matrix(new double[][]{{8, 6}, {4, 2}});
        Matrix b = new Matrix(new double[][]{{1, 2}, {3, 4}});
        assertMatrixEquals(new double[][]{{7, 4}, {1, -2}}, a.sub(b));
    }

    @Test
    void mul() {
        Matrix a = new Matrix(new double[][]{{1, 2}, {3, 4}});
        assertMatrixEquals(new double[][]{{2, 4}, {6, 8}},
                a.mul(new Matrix(new double[][]{{2}})));
        assertThrows(IllegalArgumentException.class, () -> new Matrix(5, 0).mul(a));

        Matrix myA = Matrix.random(5, 7), myB = Matrix.random(5, 7);
        SimpleMatrix expected = new SimpleMatrix(myA.asArray()).elementMult(new SimpleMatrix(myB.asArray()));
        assertTrue(new SimpleMatrix(myA.mul(myB).asArray()).isIdentical(expected, EPS));
    }

    @Test
    void mulHasCorrectManuallyCalculatedResult() {
        Matrix a = new Matrix(new double[][]{{1, 2}, {3, 4}});
        Matrix b = new Matrix(new double[][]{{5, 6}, {7, 8}});
        assertMatrixEquals(new double[][]{{5, 12}, {21, 32}}, a.mul(b));
    }

    @Test
    void div() {
        Matrix a = new Matrix(new double[][]{{10, 20}, {30, 40}});
        Matrix b = new Matrix(new double[][]{{2}, {10}});
        assertMatrixEquals(new double[][]{{5, 10}, {3, 4}}, a.div(b));
        assertThrows(ArithmeticException.class, () -> a.div(new Matrix(4, 4)));
        assertThrows(IllegalArgumentException.class, () -> new Matrix(0, 0).div(a));
    }

    @Test
    void divByZeroThrowsArithmeticException() {
        Matrix a = new Matrix(new double[][]{{1, 2}, {3, 4}});
        Matrix b = new Matrix(new double[][]{{1, 0}, {1, 2}});
        assertThrows(ArithmeticException.class, () -> a.div(b));
    }

    @Test
    void dot() {
        double[][] dataA = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] dataB = {{5.0, 6.0}, {7.0, 8.0}};
        Matrix actual = new Matrix(dataA).dot(new Matrix(dataB));
        assertMatrixEquals(new double[][]{{19, 22}, {43, 50}}, actual);

        SimpleMatrix expected = new SimpleMatrix(dataA).mult(new SimpleMatrix(dataB));
        assertTrue(new SimpleMatrix(actual.asArray()).isIdentical(expected, EPS));
    }

    @Test
    void dotRejectsIncompatibleDimensions() {
        assertThrows(IllegalArgumentException.class,
                () -> new Matrix(2, 3).dot(new Matrix(2, 2)));
    }

    @Test
    void frobenius() {
        List<Matrix> matrices = List.of(
                new Matrix(new double[][]{{1.0, 2.0}, {3.7, -0.9}}),
                Matrix.random(10, 10), Matrix.random(100, 100), Matrix.random(5, 6));
        for (Matrix m : matrices) {
            double expected = new SimpleMatrix(m.asArray()).normF();
            assertEquals(expected, m.frobenius(), EPS,
                    "Frobenius norm differs for shape " + java.util.Arrays.toString(m.shape()));
        }
        assertEquals(0.0, new Matrix(0, 0).frobenius(), EPS);
        assertEquals(Math.sqrt(30), new Matrix(new double[][]{{1, 2}, {3, 4}}).frobenius(), EPS);
    }

    @Test
    void eye() {
        for (int n : List.of(0, 1, 15, 44, 100)) {
            Matrix identity = Matrix.eye(n);
            assertArrayEquals(new int[]{n, n}, identity.shape());
            assertEquals(Math.sqrt(n), identity.frobenius(), EPS);
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    assertEquals(r == c ? 1.0 : 0.0, identity.get(r, c), EPS);
                }
            }
        }
    }

    @Test
    void det() {
        Matrix a = new Matrix(new double[][]{{1, 2}, {3, 4}});
        assertEquals(-2.0, a.det(), EPS);
        assertEquals(1.0, Matrix.eye(3).det(), EPS);
        assertEquals(0.0, new Matrix(new double[][]{{1, 2}, {2, 4}}).det(), EPS);
        assertEquals(-1.0, new Matrix(new double[][]{{0, 1}, {1, 0}}).det(), EPS,
                "Determinant should account for a row swap");

        Matrix random = Matrix.random(100, 100);
        double expected = new SimpleMatrix(random.asArray()).determinant();
        double actual = random.det();
        assertEquals(expected, actual, Math.max(EPS, Math.abs(0.0001 * expected)));
    }

    @Test
    void determinantRejectsNonSquareMatrix() {
        assertThrows(IllegalArgumentException.class, () -> new Matrix(2, 3).det());
    }

    @Test
    void invert() {
        List<Matrix> matrices = new ArrayList<>();
        matrices.add(new Matrix(new double[][]{{1.0, 2.0}, {3.0, 4.0}}));
        matrices.add(Matrix.random(50, 50));
        matrices.add(Matrix.random(100, 100));
        for (Matrix m : matrices) {
            Matrix inverse = m.invert();
            assertMatrixEquals(Matrix.eye(m.shape()[0]), m.dot(inverse));
        }
        assertThrows(IllegalArgumentException.class, () -> new Matrix(0, 0).invert());
        assertThrows(IllegalArgumentException.class,
                () -> new Matrix(new double[][]{{0, 0}, {0, 0}}).invert());
        assertThrows(ArithmeticException.class,
                () -> new Matrix(new double[][]{{1, 2}, {2, 4}}).invert());
        assertThrows(IllegalArgumentException.class, () -> new Matrix(2, 3).invert());
    }

    @Test
    void invertHasCorrectManuallyCalculatedResult() {
        Matrix a = new Matrix(new double[][]{{4, 7}, {2, 6}});
        Matrix expectedInverse = new Matrix(new double[][]{{0.6, -0.7}, {-0.2, 0.4}});
        assertMatrixEquals(expectedInverse, a.invert());
        assertMatrixEquals(Matrix.eye(2), a.dot(a.invert()));
    }

    @Test
    void solveGauss() {
        // 2x + y = 5; x + 3y = 6  =>  x = 1.8, y = 1.4
        Matrix a = new Matrix(new double[][]{{2, 1}, {1, 3}});
        Matrix b = new Matrix(new double[][]{{5}, {6}});
        assertMatrixEquals(new double[][]{{1.8}, {1.4}}, Matrix.solveGauss(a, b));
    }

    @Test
    void solveGaussHandlesRowPivoting() {
        Matrix a = new Matrix(new double[][]{{0, 1}, {2, 3}});
        Matrix b = new Matrix(new double[][]{{1}, {5}});
        assertMatrixEquals(new double[][]{{1}, {1}}, Matrix.solveGauss(a, b));
    }

    @Test
    void solveGaussRejectsInvalidInputsAndSingularMatrix() {
        assertThrows(IllegalArgumentException.class,
                () -> Matrix.solveGauss(new Matrix(2, 3), new Matrix(2, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> Matrix.solveGauss(new Matrix(2, 2), new Matrix(2, 2)));
        assertThrows(ArithmeticException.class,
                () -> Matrix.solveGauss(new Matrix(new double[][]{{1, 2}, {2, 4}}),
                        new Matrix(new double[][]{{3}, {6}})));
    }

    @Test
    void solveLU() {
        // This system has non-zero leading pivots and is suitable for the current no-pivot LU implementation.
        Matrix a = new Matrix(new double[][]{{4, 3}, {6, 3}});
        Matrix b = new Matrix(new double[][]{{10}, {12}});
        assertMatrixEquals(new double[][]{{1}, {2}}, Matrix.solveLU(a, b));
    }

    @Test
    void solveLURejectsInvalidInputsAndZeroPivot() {
        assertThrows(IllegalArgumentException.class,
                () -> Matrix.solveLU(new Matrix(2, 3), new Matrix(2, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> Matrix.solveLU(new Matrix(2, 2), new Matrix(2, 2)));
        assertThrows(ArithmeticException.class,
                () -> Matrix.solveLU(new Matrix(new double[][]{{0, 1}, {1, 2}}),
                        new Matrix(new double[][]{{1}, {3}})));
    }

    @Test
    void round() {
        Matrix m = new Matrix(new double[][]{{1.234, -1.235}, {2.345, -2.345}});
        m.round();
        assertMatrixEquals(new double[][]{{1.23, -1.24}, {2.35, -2.35}}, m);
    }

    @Test
    void copyCreatesIndependentMatrix() {
        Matrix original = new Matrix(new double[][]{{1, 2}, {3, 4}});
        Matrix copy = original.copy();
        assertMatrixEquals(original, copy);
        copy.set(0, 0, 100);
        assertEquals(1.0, original.get(0, 0), EPS);
    }
}
