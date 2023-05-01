package hu.laszlolukacs.vectorsimdbench.benchmarks;

import hu.laszlolukacs.vectorsimdbench.algorithms.ArrayAverageCalculator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ArrayAverageBenchmark {
    
    private static final double[] arrayA = new double[]
            {
                    17.38, 17.98, 18.58, 20.63, 22.67, 24.10, 25.53, 26.08, 26.63, 26.60, 26.57, 26.30, 26.02, 25.93, 25.84,
                    25.71, 25.68, 24.63, 23.57, 21.72, 19.77, 17.63, 15.48, 13.60, 11.71, 14.42, 17.13, 26.37, 35.61, 47.27,
                    58.92
            };
    
    private static final double[] arrayB = new double[]
            {
                    14.63, 15.77, 16.90, 18.79, 20.68, 23.58, 26.47, 29.75, 33.03, 33.14, 33.25, 30.97, 28.69, 26.40, 24.11,
                    22.54, 20.97, 20.03, 19.09, 18.76, 18.42, 19.91, 21.40, 22.40, 23.57, 26.97, 30.36, 38.14, 45.92, 54.59,
                    63.26
            };
    
    @Benchmark
    @Fork(value = 1)
    public static void ArrayAverageSimd(Blackhole bh) {
        bh.consume(ArrayAverageCalculator.average2Simd(arrayA, arrayB));
    }
    
    @Benchmark
    @Fork(value = 1)
    public static void ArrayAverageScalar(Blackhole bh) {
        bh.consume(ArrayAverageCalculator.average2Scalar(arrayA, arrayB));
    }
}
