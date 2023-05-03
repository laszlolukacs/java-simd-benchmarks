package hu.laszlolukacs.vectorsimdbench.benchmarks;

import hu.laszlolukacs.vectorsimdbench.algorithms.RgbToGrayscale;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class RgbToGrayscaleBenchmark {
    
    @State(Scope.Benchmark)
    public static class RgbToGrayscaleBenchmarkState {
        @Param({"/colored_input_01.png",
                "/colored_input_1600p.jpg",
                "/colored_input_hires_02.jpg"})
        public String resourceName;
        
        private byte[] rgbByteArray;
        private float[] rgbFloatArray;
        
        @Setup(Level.Trial)
        public void setUp() throws IOException {
            URL imageUrl = RgbToGrayscaleBenchmark.class.getResource(resourceName);
            rgbByteArray = RgbToGrayscale.extractBytesFromImage(imageUrl.openStream());
            rgbFloatArray = RgbToGrayscale.extractFloatsFromImage(imageUrl.openStream());
        }
    }
    
    @Benchmark
    @Fork(value = 1)
    public void rgbToGrayscaleScalar(Blackhole bh, RgbToGrayscaleBenchmarkState state) {
        bh.consume(RgbToGrayscale.rgbToGrayscaleScalar(state.rgbByteArray));
    }
    
    @Benchmark
    @Fork(value = 1)
    public void rgbToGrayscaleSimdF(Blackhole bh, RgbToGrayscaleBenchmarkState state) {
        bh.consume(RgbToGrayscale.rgbToGrayscaleSimdF(state.rgbFloatArray));
    }
    
    @Benchmark
    @Fork(value = 1)
    public void rgbToGrayscaleSimdU(Blackhole bh, RgbToGrayscaleBenchmarkState state) {
        bh.consume(RgbToGrayscale.rgbToGrayscaleSimdU(state.rgbByteArray));
    }
}
