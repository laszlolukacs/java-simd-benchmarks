package hu.laszlolukacs.vectorsimdbench.benchmarks;

import hu.laszlolukacs.vectorsimdbench.algorithms.RgbToGrayscale;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class RgbToGrayscaleBenchmark {
    
    public static final InputStream imageStream = RgbToGrayscale.class.getResourceAsStream("/colored_input_01.png");
    
    public static final byte[] rgbByteArray = RgbToGrayscale.extractBytesFromImage(imageStream);
    public static final float[] rgbFloatArray = RgbToGrayscale.extractFloatsFromImage(imageStream);
    
    @Benchmark
    @Fork(value = 1)
    public static void rgbToGrayscaleScalar(Blackhole bh) {
        bh.consume(RgbToGrayscale.rgbToGrayscaleScalar(rgbByteArray));
    }
    
    @Benchmark
    @Fork(value = 1)
    public static void rgbToGrayscaleSimd(Blackhole bh) {
        bh.consume(RgbToGrayscale.rgbToGrayscaleSimd(rgbFloatArray));
    }
}
