# Java Vector API SIMD benchmark
This example utilizes the [SIMD capabilities](https://blogs.oracle.com/javamagazine/post/java-vector-api-simd) ([SSE](https://www.intel.com/content/www/us/en/support/articles/000005779/processors.html) or [AVX](https://www.intel.com/content/www/us/en/support/articles/000005779/processors.html) / [Neon](https://developer.arm.com/architectures/instruction-sets/simd-isas/neon) or [SVE](https://developer.arm.com/tools-and-software/server-and-hpc/compile/arm-instruction-emulator/resources/tutorials/sve) extensions) of contemporary x86 and ARM CPUs through some example algorithms using the Java [Vector API](https://openjdk.java.net/jeps/338) ([JEP 338](https://openjdk.java.net/jeps/338), [JEP 414](https://openjdk.org/jeps/414)).

## System Requirements ##
* *x64* with *AVX2* or *AArch64* with *Neon* processor running a supported **64-bit** version of Linux, macOS or Windows OS
* [JRE 17](https://adoptium.net/temurin/releases/?version=17) or newer

## Dependencies ##
* [JDK 17](https://adoptium.net/temurin/releases/?version=17) or newer

## Summary of set up
* `git clone git@github.com:laszlolukacs/vector-simd-in-java-benchmark.git`
* `cd ./vector-simd-in-java-benchmark`
* `gradle build`

## Basic Usage
* Invoke `gradle runtimeBenchmark` from repo root

## References
* [JDK 17 Vector](https://docs.oracle.com/en/java/javase/17/docs/api/jdk.incubator.vector/jdk/incubator/vector/Vector.html)
* [JDK 17 VectorOperators](https://docs.oracle.com/en/java/javase/17/docs/api/jdk.incubator.vector/jdk/incubator/vector/VectorOperators.html)
