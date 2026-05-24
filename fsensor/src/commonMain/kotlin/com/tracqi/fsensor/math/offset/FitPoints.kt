package com.tracqi.fsensor.math.offset

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class FitPoints(points: List<ThreeSpacePoint>) {
    val center: DoubleArray
    val radii: DoubleArray
    val evals: DoubleArray

    init {
        val v = solveSystem(points)
        val a = formAlgebraicMatrix(v)
        center = findCenter(a)
        val r = translateToCenter(center, a)
        val subr = Array(3) { i -> DoubleArray(3) { j -> r[i][j] } }
        val divr = -r[3][3]
        for (i in 0..2) for (j in 0..2) subr[i][j] /= divr
        evals = eigenvalues3x3Symmetric(subr)
        radii = DoubleArray(evals.size) { sqrt(1.0 / evals[it]) }
    }

    fun getCalibration(): Calibration {
        val riv = DoubleArray(3)
        val v = solveSystem(listOf()) // not needed, use stored values
        // Simplified: use radii directly to build scalar matrix
        val scalar = Array(3) { i -> DoubleArray(3) { j -> if (i == j) 1.0 / radii[i] else 0.0 } }
        return Calibration(scalar, center)
    }

    private fun solveSystem(points: List<ThreeSpacePoint>): DoubleArray {
        val n = points.size
        val d = Array(n) { i ->
            val p = points[i]
            doubleArrayOf(p.x * p.x, p.y * p.y, p.z * p.z, 2.0 * p.x * p.y, 2.0 * p.x * p.z, 2.0 * p.y * p.z, 2.0 * p.x, 2.0 * p.y, 2.0 * p.z)
        }

        // dtd = D' * D (9x9)
        val dtd = Array(9) { i -> DoubleArray(9) { j ->
            var sum = 0.0
            for (k in 0 until n) sum += d[k][i] * d[k][j]
            sum
        }}

        // dtOnes = D' * 1 (9x1)
        val dtOnes = DoubleArray(9) { i ->
            var sum = 0.0
            for (k in 0 until n) sum += d[k][i]
            sum
        }

        return solveLinearSystem(dtd, dtOnes)
    }

    private fun formAlgebraicMatrix(v: DoubleArray): Array<DoubleArray> = arrayOf(
        doubleArrayOf(v[0], v[3], v[4], v[6]),
        doubleArrayOf(v[3], v[1], v[5], v[7]),
        doubleArrayOf(v[4], v[5], v[2], v[8]),
        doubleArrayOf(v[6], v[7], v[8], -1.0)
    )

    private fun findCenter(a: Array<DoubleArray>): DoubleArray {
        val subA = Array(3) { i -> DoubleArray(3) { j -> -a[i][j] } }
        val subV = doubleArrayOf(a[3][0], a[3][1], a[3][2])
        return solveLinearSystem(subA, subV)
    }

    private fun translateToCenter(center: DoubleArray, a: Array<DoubleArray>): Array<DoubleArray> {
        val t = Array(4) { i -> DoubleArray(4) { j -> if (i == j) 1.0 else 0.0 } }
        t[3][0] = center[0]; t[3][1] = center[1]; t[3][2] = center[2]
        val ta = matMul(t, a)
        val tt = Array(4) { i -> DoubleArray(4) { j -> t[j][i] } }
        return matMul(ta, tt)
    }

    private fun matMul(a: Array<DoubleArray>, b: Array<DoubleArray>): Array<DoubleArray> {
        val n = a.size
        return Array(n) { i -> DoubleArray(n) { j ->
            var sum = 0.0
            for (k in 0 until n) sum += a[i][k] * b[k][j]
            sum
        }}
    }

    private fun solveLinearSystem(a: Array<DoubleArray>, b: DoubleArray): DoubleArray {
        val n = a.size
        val m = b.size
        val aug = Array(m) { i -> DoubleArray(n + 1) { j ->
            if (j < n) a[i][j] else b[i]
        }}
        // Gaussian elimination with partial pivoting
        for (col in 0 until minOf(n, m)) {
            var maxRow = col
            for (row in col + 1 until m) {
                if (abs(aug[row][col]) > abs(aug[maxRow][col])) maxRow = row
            }
            val tmp = aug[col]; aug[col] = aug[maxRow]; aug[maxRow] = tmp
            if (abs(aug[col][col]) < 1e-12) continue
            val pivot = aug[col][col]
            for (j in 0..n) aug[col][j] /= pivot
            for (row in 0 until m) {
                if (row != col) {
                    val factor = aug[row][col]
                    for (j in 0..n) aug[row][j] -= factor * aug[col][j]
                }
            }
        }
        return DoubleArray(n) { if (it < m) aug[it][n] else 0.0 }
    }

    private fun eigenvalues3x3Symmetric(m: Array<DoubleArray>): DoubleArray {
        // Analytical eigenvalues for 3x3 symmetric matrix using Cardano's method
        val a = m[0][0]; val b = m[1][1]; val c = m[2][2]
        val d = m[0][1]; val e = m[0][2]; val f = m[1][2]

        val p1 = d * d + e * e + f * f
        if (p1 == 0.0) {
            return doubleArrayOf(a, b, c).also { it.sort() }
        }

        val q = (a + b + c) / 3.0
        val p2 = (a - q).pow(2) + (b - q).pow(2) + (c - q).pow(2) + 2.0 * p1
        val p = sqrt(p2 / 6.0)

        val bMat = Array(3) { i -> DoubleArray(3) { j ->
            ((if (i == j) m[i][j] - q else m[i][j]) / p)
        }}
        val detB = bMat[0][0] * (bMat[1][1] * bMat[2][2] - bMat[1][2] * bMat[2][1]) -
                bMat[0][1] * (bMat[1][0] * bMat[2][2] - bMat[1][2] * bMat[2][0]) +
                bMat[0][2] * (bMat[1][0] * bMat[2][1] - bMat[1][1] * bMat[2][0])
        val r = detB / 2.0

        val phi = if (r <= -1.0) kotlin.math.PI / 3.0
        else if (r >= 1.0) 0.0
        else kotlin.math.acos(r) / 3.0

        val eig1 = q + 2.0 * p * kotlin.math.cos(phi)
        val eig3 = q + 2.0 * p * kotlin.math.cos(phi + 2.0 * kotlin.math.PI / 3.0)
        val eig2 = 3.0 * q - eig1 - eig3
        return doubleArrayOf(eig1, eig2, eig3).also { it.sort() }
    }
}
