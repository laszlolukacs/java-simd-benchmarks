package hu.laszlolukacs.vectorsimdbench.algorithms;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;

public class ArrayAverageCalculator {

    public static double[] average2Scalar(final double[] lhs, final double[] rhs) {
        var result = new double[lhs.length];
        for (int i = 0; i < lhs.length; i++) {
            result[i] = (lhs[i] + rhs[i]) / 2.0;
        }

        return result;
    }

    public static double[] average2Simd(final double[] lhs, final double[] rhs) {
        var result = new double[lhs.length];
        final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
        var res = DoubleVector.zero(SPECIES);
        int i = 0;
        int upperBound = SPECIES.loopBound(lhs.length);
        for (; i < upperBound; i += SPECIES.length()) {
            var va = DoubleVector.fromArray(SPECIES, lhs, i);
            var vb = DoubleVector.fromArray(SPECIES, rhs, i);
            res = va.add(vb);
            res = res.div(2.0);
            res.intoArray(result, i);
        }

        // fallback when vectorization would be out-of-array-bounds
        for (; i < lhs.length; i++) {
            result[i] = (lhs[i] + rhs[i]) / 2.0;
        }

        return result;
    }

    public static double[] average2SimdMasked(final double[] lhs, final double[] rhs) {
        var result = new double[lhs.length];
        final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

        for (int i = 0; i < lhs.length; i += SPECIES.length()) {
            var mask = SPECIES.indexInRange(i, lhs.length);
            var va = DoubleVector.fromArray(SPECIES, lhs, i, mask);
            var vb = DoubleVector.fromArray(SPECIES, rhs, i, mask);
            var res = (va.add(vb)).div(2.0);
            res.intoArray(result, i, mask);
        }

        return result;
    }
}
