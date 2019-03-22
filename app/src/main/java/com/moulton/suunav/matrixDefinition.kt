//https://rosettacode.org/wiki/Multiple_regression#Kotlin

package com.moulton.suunav

typealias Vector = DoubleArray
typealias Matrix = Array<Vector>


//This says that each vector is a row.
operator fun Matrix.times(other: Matrix): Matrix {
    val rows1 = this.size
    val cols1 = this[0].size
    val rows2 = other.size
    val cols2 = other[0].size
    require(cols1 == rows2)
    val result = Matrix(rows1) { Vector(cols2) }
    for (i in 0 until rows1) {
        for (j in 0 until cols2) {
            for (k in 0 until rows2) {
                result[i][j] += this[i][k] * other[k][j]
            }
        }
    }
    return result
}

fun Matrix.transpose(): Matrix {
    val rows = this.size
    val cols = this[0].size
    val trans = Matrix(cols) { Vector(rows) }
    for (i in 0 until cols) {
        for (j in 0 until rows) trans[i][j] = this[j][i]
    }
    return trans
}

fun Matrix.inverse(): Matrix {
    val len = this.size
    require(this.all { it.size == len }) { "Not a square matrix" }
    val aug = Array(len) { DoubleArray(2 * len) }
    for (i in 0 until len) {
        for (j in 0 until len) aug[i][j] = this[i][j]
        // augment by identity matrix to right
        aug[i][i + len] = 1.0
    }
    aug.toReducedRowEchelonForm()
    val inv = Array(len) { DoubleArray(len) }
    // remove identity matrix to left
    for (i in 0 until len) {
        for (j in len until 2 * len) inv[i][j - len] = aug[i][j]
    }
    return inv
}

fun Matrix.toReducedRowEchelonForm() {
    var lead = 0
    val rowCount = this.size
    val colCount = this[0].size
    for (r in 0 until rowCount) {
        if (colCount <= lead) return
        var i = r

        while (this[i][lead] == 0.0) {
            i++
            if (rowCount == i) {
                i = r
                lead++
                if (colCount == lead) return
            }
        }

        val temp = this[i]
        this[i] = this[r]
        this[r] = temp

        if (this[r][lead] != 0.0) {
            val div = this[r][lead]
            for (j in 0 until colCount) this[r][j] /= div
        }

        for (k in 0 until rowCount) {
            if (k != r) {
                val mult = this[k][lead]
                for (j in 0 until colCount) this[k][j] -= this[r][j] * mult
            }
        }

        lead++
    }
}

fun printVector(v: Vector) {
    println(v.asList())
    println()
}

fun printMatrix(m: Matrix){
    for( v in m){
        printVector(v)
    }
}

fun multipleRegression(y: Vector, x: Matrix): Vector {
    val cy = (arrayOf(y)).transpose()  // convert 'y' to column vector
    val cx = x.transpose()             // convert 'x' to column vector array

    return ((x * cx).inverse() * x * cy).transpose()[0]
}