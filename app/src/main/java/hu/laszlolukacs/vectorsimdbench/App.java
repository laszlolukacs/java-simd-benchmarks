package hu.laszlolukacs.vectorsimdbench;

import hu.laszlolukacs.vectorsimdbench.algorithms.RgbToGrayscale;
import hu.laszlolukacs.vectorsimdbench.utils.ImageUtils;
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
            BufferedImage image = ImageUtils.loadImage(resourceAsStream);
            byte[] imageBytes = ImageUtils.extractBytes(image);
            float[] imageFloats = ImageUtils.extractFloats(image);
            
            float[] grayscaleBytesScalar = RgbToGrayscale.rgbToGrayscaleScalarF(imageFloats);
            ImageUtils.writeGrayscaleImage("grayscale_output_scalarf.png", grayscaleBytesScalar, image.getWidth(), image.getHeight());
            
            float[] grayscaleBytes = RgbToGrayscale.rgbToGrayscaleSimdF(imageFloats);
            ImageUtils.writeGrayscaleImage("grayscale_output_simdf.png", grayscaleBytes, image.getWidth(), image.getHeight());
            
            byte[] grayscaleBytesScalarU = RgbToGrayscale.rgbToGrayscaleScalar(imageBytes);
            ImageUtils.writeGrayscaleImage("grayscale_output_scalaru.png", grayscaleBytesScalarU, image.getWidth(), image.getHeight());
            
            byte[] grayscaleBytesU = RgbToGrayscale.rgbToGrayscaleSimdU(imageBytes);
            ImageUtils.writeGrayscaleImage("grayscale_output_simdu.png", grayscaleBytesU, image.getWidth(), image.getHeight());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
