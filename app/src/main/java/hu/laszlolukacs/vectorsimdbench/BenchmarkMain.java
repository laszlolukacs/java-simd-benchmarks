package hu.laszlolukacs.vectorsimdbench;

import hu.laszlolukacs.vectorsimdbench.benchmarks.RgbToGrayscaleBenchmark;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorShape;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.LocalDateTime;

public class BenchmarkMain {
    
    public static void main(String[] args) throws RunnerException {
        
        System.out.println("Vector SIMD Java Benchmark");
        System.out.println("Starting execution at " + LocalDateTime.now());
        final VectorShape preferredShape = VectorShape.preferredShape();
        int simdLength = preferredShape.vectorBitSize();
        System.out.printf("Preferred SIMD vector bit size: %d, (%d)%n", simdLength, FloatVector.SPECIES_PREFERRED.length());
        
        Options opt = new OptionsBuilder()
                .include(RgbToGrayscaleBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
