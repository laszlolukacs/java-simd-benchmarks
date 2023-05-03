package hu.laszlolukacs.vectorsimdbench;

import hu.laszlolukacs.vectorsimdbench.algorithms.RgbToGrayscale;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorShape;

import java.awt.image.BufferedImage;

public class App {
    
    public static void main(String[] args) {
        
        System.out.println("Vector SIMD in Java Example");
        final VectorShape preferredShape = VectorShape.preferredShape();
        int simdLength = preferredShape.vectorBitSize();
        System.out.printf("Preferred SIMD vector bit size: %d, (%d)%n", simdLength, FloatVector.SPECIES_PREFERRED.length());
        
        try {
            var resourceAsStream = RgbToGrayscale.class.getResourceAsStream("/colored_input_hires_01.jpg");
            BufferedImage image = RgbToGrayscale.loadImage(resourceAsStream);
            
            byte[] imageBytes = RgbToGrayscale.extractBytes(image);
            float[] imageFloats = RgbToGrayscale.extractFloats(image);
            
            byte[] grayscaleBytesScalar = RgbToGrayscale.rgbToGrayscaleScalar(imageBytes);
            RgbToGrayscale.writeImage("grayscale_output_scalar.png", grayscaleBytesScalar, image.getWidth(), image.getHeight());
            
            byte[] grayscaleBytes = RgbToGrayscale.rgbToGrayscaleSimdU(imageBytes);
            RgbToGrayscale.writeImage("grayscale_output_simd.png", grayscaleBytes, image.getWidth(), image.getHeight());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
