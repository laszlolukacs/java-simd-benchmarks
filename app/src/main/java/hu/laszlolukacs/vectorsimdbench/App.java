package hu.laszlolukacs.vectorsimdbench;

import hu.laszlolukacs.vectorsimdbench.algorithms.RgbToGrayscale;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorShape;

import java.awt.image.BufferedImage;

public class App {
    
    public static void main(String[] args) {
        
        System.out.println("Vector SIMD Java Example");
        final VectorShape preferredShape = VectorShape.preferredShape();
        int simdLength = preferredShape.vectorBitSize();
        System.out.printf("Preferred SIMD vector bit size: %d, (%d)%n", simdLength, FloatVector.SPECIES_PREFERRED.length());
        
        try {
            var resourceAsStream = RgbToGrayscale.class.getResourceAsStream("/colored_input_01.png");
            BufferedImage image = RgbToGrayscale.loadImage(resourceAsStream);
            byte[] imageBytes = RgbToGrayscale.extractBytes(image);
            float[] imageFloats = RgbToGrayscale.extractFloats(image);
            byte[] grayscaleBytes = RgbToGrayscale.rgbToGrayscaleSimd(imageFloats);
            RgbToGrayscale.writeImage("greyscale_output.png", grayscaleBytes, image.getWidth(), image.getHeight());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
