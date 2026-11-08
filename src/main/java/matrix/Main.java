package matrix;

import matrix.Matrix;

public class Main {
    public static void main(String[] args) {
        Matrix a = new Matrix(new double[][] {
                {1, 2},
                {3, 4}
        });

        Matrix b = Matrix.eye(2);

        Matrix sum = a.add(b);
        Matrix product = a.dot(b);
        double determinant = a.det();

        System.out.println("A + I:");
        System.out.println(sum);

        System.out.println("A * I:");
        System.out.println(product);

        System.out.println("det(A) = " + determinant);
    }
}