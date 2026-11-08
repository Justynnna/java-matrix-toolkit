# ️🚀 Matrix Library

A lightweight Java matrix library implementing fundamental matrix operations and selected numerical linear algebra algorithms. The project focuses on understanding matrix representations, numerical methods, and clean object-oriented programming principles.

---

## ✨ Features

### Matrix Operations
* **Creation & Access:** `Matrix(int rows, int cols)`, `Matrix(double[][] d)`, `get()`, `set()`
* **Shape & Conversion:** `shape()`, `reshape()`, `asArray()`
* **Element-wise Operations:** `add()`, `sub()`, `mul()`, `div()`
* **Matrix Multiplication:** `dot()`
* **Broadcasting:** Supported by element-wise operations when compatible dimensions are equal or integer multiples
* **Norm:** `frobenius()`
* **Utility Methods:** `copy()`, `eye()`, `random()`, `round()`
* **Text Representation:** `toString()`

### Numerical Methods
* **Determinant:** `det()` — Gaussian elimination with partial pivoting
* **Matrix Inverse:** `invert()` — Gauss–Jordan elimination with partial pivoting
* **Linear Systems Solver:** `solveGauss()` — Gaussian elimination with partial pivoting
* **LU Decomposition:** `solveLU()` — Doolittle decomposition without pivoting

---
## 🛠️ Getting Started

### Technologies 
* **Java** (JDK 17+)
* **JUnit 5** — Unit testing framework
* **EJML** — Reference library for validating selected numerical tests

### Installation & Run

Clone the repository:
   ```bash
   git clone https://github.com/Justynnna/java-matrix-toolkit.git
   cd numerical_matrix
   ```
---

## 💻 Usage Example
```Java
import matrix.Matrix;

public class Main {
    public static void main(String[] args) {
        Matrix a = new Matrix(new double[][] {
            {1.0, 2.0},
            {3.0, 4.0}
        });

        Matrix b = Matrix.eye(2);

        Matrix sum = a.add(b);
        Matrix product = a.dot(b);
        double determinant = a.det();

        System.out.println("A + I:\n" + sum);
        System.out.println("A * I:\n" + product);
        System.out.println("det(A) = " + determinant);
    }
}
```

---

## 📁 Project Structure

```text
.
├── src/
│   ├── main/
│   │   └── java/
│   │       └── matrix/
│   │           └── Matrix.java
│   └── test/
│       └── java/
│           └── matrix/
│               └── MatrixTest.java
└── README.md
```

---

## ⚠️ Numerical Considerations & Limitations
* **Floating-point precision:** Matrix elements are stored as Java double values, meaning IEEE 754 precision limits and rounding errors apply.


* **Conditioning:** Numerical methods rely on internal tolerance thresholds when checking for pivots and singularity. Results for ill-conditioned matrices may lose precision.


* **Pivoting in LU:** The solveLU() algorithm currently implements the Doolittle method without pivoting. It will fail for matrices requiring row exchanges, even if the matrix itself is non-singular.


* **Broadcasting rules:** Broadcasting applies only when dimensions are identical or one dimension is an exact integer multiple of the other.

## 🧪 Testing
The project maintains high test coverage using JUnit 5. Correctness is validated through a combination of:

* Direct comparison against expected output from the EJML (Efficient Java Matrix Library) reference implementation.

* Manually calculated matrices for edge cases, zero-matrices, and boundary values.

## 🎯 Project Scope
This is an educational project designed to showcase core OOP practices, algorithmic logic, and numerical techniques in Java. It is not intended for production-level or high-performance scientific computing where specialized, hardware-accelerated libraries (e.g., BLAS/LAPACK bindings) should be used.

## 📜 License
This project is open-source and available under the MIT License.