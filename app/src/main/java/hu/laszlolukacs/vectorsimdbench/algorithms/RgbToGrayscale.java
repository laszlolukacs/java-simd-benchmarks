package hu.laszlolukacs.vectorsimdbench.algorithms;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class RgbToGrayscale {
    
    public static BufferedImage loadImage(InputStream imageStream) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(imageStream);
        return bufferedImage;
    }
    
    public static byte[] extractBytes(BufferedImage bufferedImage) {
        WritableRaster raster = bufferedImage.getRaster();
        DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
        return (data.getData());
    }
    
    public static byte[] extractBytesFromImage(InputStream imageStream) {
        try {
            return extractBytes(loadImage(imageStream));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
    
    public static float[] extractFloats(BufferedImage bufferedImage) {
        byte[] bytes = extractBytes(bufferedImage);
        float[] result = new float[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = (bytes[i] & 0xFF) / 255.0f;
        }
        
        return result;
    }
    
    public static float[] extractFloatsFromImage(InputStream imageStream) {
        try {
            return extractFloats(loadImage(imageStream));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
    
    public static byte[] rgbToGrayscaleScalar(final byte[] rgbBytes) {
        
        final int numberOfChannels = 4; // ARGB
        final int alphaPadding = 1;
        
        byte[] grayscaleResult = new byte[(rgbBytes.length / numberOfChannels)];
        
        for (int i = 0; i < rgbBytes.length; i += numberOfChannels) {
            int r = rgbBytes[i + alphaPadding] & 0xFF;
            int g = rgbBytes[i + alphaPadding + 1] & 0xFF;
            int b = rgbBytes[i + alphaPadding + 2] & 0xFF;
            float lumaf = 0.3f * r + 0.59f * g + 0.11f * b;
            float lumaf2 = Math.max(0.0f, Math.min(255.0f, lumaf));
            grayscaleResult[i / numberOfChannels] = (byte) Math.floor(lumaf2);
        }
        
        return grayscaleResult;
    }
    
    public static byte[] rgbToGrayscaleSimd(final float[] rgbFloats) {
        
        final int numberOfChannels = 4; // ARGB
        final int alphaPadding = 1;
        
        float[] rFloats = new float[rgbFloats.length / numberOfChannels];
        float[] gFloats = new float[rgbFloats.length / numberOfChannels];
        float[] bFloats = new float[rgbFloats.length / numberOfChannels];
        for (int i = 0; i < rgbFloats.length; i += numberOfChannels) {
            rFloats[i / numberOfChannels] = rgbFloats[i + alphaPadding];
            gFloats[i / numberOfChannels] = rgbFloats[i + alphaPadding + 1];
            bFloats[i / numberOfChannels] = rgbFloats[i + alphaPadding + 2];
        }
        
        byte[] grayscaleResult = new byte[(rgbFloats.length / numberOfChannels)];
        float[] gsResult = new float[rgbFloats.length / numberOfChannels];
        
        final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
        var res = FloatVector.zero(SPECIES);
        int i = 0;
        int upperBound = SPECIES.loopBound(rFloats.length);
        for (; i < upperBound; i += SPECIES.length()) {
            var vr = FloatVector.fromArray(SPECIES, rFloats, i);
            var vg = FloatVector.fromArray(SPECIES, gFloats, i);
            var vb = FloatVector.fromArray(SPECIES, bFloats, i);
            res = (vr.mul(0.3f)).add(vg.mul(0.59f)).add(vb.mul(0.11f));
            res = res.mul(255.0f);
            res.intoArray(gsResult, i);
        }
        
        // fallback when vectorization would be out-of-array-bounds
        for (; i < rFloats.length; i++) {
            gsResult[i] = ((rFloats[i] * 0.3f) + (gFloats[i] * 0.59f) + (bFloats[i] * 0.11f)) * 255.0f;
        }
        
        for (int k = 0; k < gsResult.length; k++) {
            float gsf2 = Math.max(0.0f, Math.min(255.0f, gsResult[k]));
            grayscaleResult[k] = (byte) Math.floor(gsf2);
        }
        
        return grayscaleResult;
    }
    
    public static void writeImage(final String path, final byte[] pixelBytes, int width, int height) throws IOException {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        result.getRaster().setDataElements(0, 0, width, height, pixelBytes);
        File outputImageFile = new File(path);
        ImageIO.write(result, "png", outputImageFile);
        System.out.println(outputImageFile.getAbsolutePath());
    }
}
