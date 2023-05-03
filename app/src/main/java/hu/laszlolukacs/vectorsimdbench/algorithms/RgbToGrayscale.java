package hu.laszlolukacs.vectorsimdbench.algorithms;

import jdk.incubator.vector.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static jdk.incubator.vector.VectorOperators.*;

public class RgbToGrayscale {
    
    public static BufferedImage loadImage(InputStream imageStream) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(imageStream);
        
        return bufferedImage;
    }
    
    public static byte[] extractBytes(BufferedImage bufferedImage) {
        WritableRaster raster = bufferedImage.getRaster();
        DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
        if (bufferedImage.getType() == BufferedImage.TYPE_INT_ARGB) {
            return (data.getData());
        } else {
            int resultLength = (data.getData().length / 3) * 4;
            byte[] result = new byte[(data.getData().length / 3) * 4];
            int j = 0;
            for (int i = 0; i < data.getData().length; i += 3) {
                if (j < resultLength) {
                    result[j] = (byte) (0xFF);
                    result[j + 1] = data.getData()[i];
                    result[j + 2] = data.getData()[i + 1];
                    result[j + 3] = data.getData()[i + 2];
                    j += 4;
                }
            }
            
            return result;
        }
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
        
        byte[] grayscalePixels = new byte[(rgbBytes.length / numberOfChannels)];
        
        for (int i = 0; i < grayscalePixels.length; i++) {
            int r = rgbBytes[(i * numberOfChannels) + alphaPadding] & 0xFF;
            int g = rgbBytes[(i * numberOfChannels) + alphaPadding + 1] & 0xFF;
            int b = rgbBytes[(i * numberOfChannels) + alphaPadding + 2] & 0xFF;
            float brightness = 0.3f * r + 0.59f * g + 0.11f * b;
            float brightnessClamped = Math.max(0.0f, Math.min(255.0f, brightness));
            grayscalePixels[i] = (byte) Math.floor(brightnessClamped);
        }
        
        return grayscalePixels;
    }
    
    public static byte[] rgbToGrayscaleSimdF(final float[] rgbFloats) {
        
        final int numberOfChannels = 4; // ARGB
        final int alphaPadding = 1;
        final int resultsLength = rgbFloats.length / numberOfChannels;
        
        float[] grayscaleResultF = new float[resultsLength];
        
        float[] rFloats = new float[resultsLength];
        float[] gFloats = new float[resultsLength];
        float[] bFloats = new float[resultsLength];
        for (int i = 0; i < resultsLength; i++) {
            rFloats[i] = rgbFloats[(i * numberOfChannels) + alphaPadding];
            gFloats[i] = rgbFloats[(i * numberOfChannels) + alphaPadding + 1];
            bFloats[i] = rgbFloats[(i * numberOfChannels) + alphaPadding + 2];
        }
        
        grayscaleResultF = brightnessSimdF(rFloats, gFloats, bFloats);
        
        byte[] grayscalePixels = new byte[resultsLength];
        for (int j = 0; j < grayscaleResultF.length; j++) {
            float gsf2 = Math.max(0.0f, Math.min(255.0f, grayscaleResultF[j]));
            grayscalePixels[j] = (byte) Math.floor(gsf2);
        }
        
        return grayscalePixels;
    }
    
    public static byte[] rgbToGrayscaleSimdU(final byte[] rgbBytes) {
        
        final int numberOfChannels = 4; // ARGB
        final int alphaPadding = 1;
        final int resultsLength = rgbBytes.length / numberOfChannels;
        
        byte[] grayscalePixels = new byte[resultsLength];
        
        byte[] rBytes = new byte[resultsLength];
        byte[] gBytes = new byte[resultsLength];
        byte[] bBytes = new byte[resultsLength];
        for (int i = 0; i < resultsLength; i++) {
            rBytes[i] = rgbBytes[(i * numberOfChannels) + alphaPadding];
            gBytes[i] = rgbBytes[(i * numberOfChannels) + alphaPadding + 1];
            bBytes[i] = rgbBytes[(i * numberOfChannels) + alphaPadding + 2];
        }
        
        grayscalePixels = brightnessSimdUnrolledU(rBytes, gBytes, bBytes);
        
        return grayscalePixels;
    }
    
    private static float[] brightnessSimdF(float[] rF, float[] gF, float[] bF) {
        final int resultsLength = rF.length;
        float[] grayscalePixelsF = new float[resultsLength];
        
        final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
        var res = FloatVector.zero(SPECIES);
        int i = 0;
        for (; i < SPECIES.loopBound(resultsLength); i += SPECIES.length()) {
            var vr = FloatVector.fromArray(SPECIES, rF, i);
            var vg = FloatVector.fromArray(SPECIES, gF, i);
            var vb = FloatVector.fromArray(SPECIES, bF, i);
            vr = vr.mul(0.3f);
            vg = vg.mul(0.59f);
            vb = vb.mul(0.11f);
            res = vr.add(vb).add(vg);
            res = res.mul(255.0f);
            res.intoArray(grayscalePixelsF, i);
        }
        
        // fallback when vectorization would be out-of-array-bounds
        for (; i < resultsLength; i++) {
            grayscalePixelsF[i] = ((rF[i] * 0.3f) + (gF[i] * 0.59f) + (bF[i] * 0.11f)) * 255.0f;
        }
        
        return grayscalePixelsF;
    }
    
    private static byte[] brightnessSimdUnrolledU(byte[] rU, byte[] gU, byte[] bU) {
        final int resultsLength = rU.length;
        byte[] grayscalePixels = new byte[resultsLength];
        
        final VectorShape preferredShape = VectorShape.preferredShape();
        final int simdLength = preferredShape.vectorBitSize();
        final VectorSpecies<Byte> BYTE_SPECIES = simdLength == 256 ? ByteVector.SPECIES_256 : ByteVector.SPECIES_128;
        final VectorSpecies<Byte> BYTE_SPECIES_HALF = simdLength == 256 ? ByteVector.SPECIES_128 : ByteVector.SPECIES_64;
        final VectorSpecies<Short> SHORT_SPECIES = simdLength == 256 ? ShortVector.SPECIES_256 : ShortVector.SPECIES_128;
        
        ShortVector res1 = ShortVector.zero(SHORT_SPECIES);
        ShortVector res2 = ShortVector.zero(SHORT_SPECIES);
        ByteVector resb1 = ByteVector.zero(BYTE_SPECIES_HALF);
        ByteVector resb2 = ByteVector.zero(BYTE_SPECIES_HALF);
        
        final short rCo = (short) (255 * 0.3f);
        final short gCo = (short) (255 * 0.59f);
        final short bCo = (short) (255 * 0.11f);
        
        int i = 0;
        for (; i < BYTE_SPECIES.loopBound(resultsLength); i += BYTE_SPECIES.length()) {
            
            var vr = ByteVector.fromArray(BYTE_SPECIES_HALF, rU, i);
            var vg = ByteVector.fromArray(BYTE_SPECIES_HALF, gU, i);
            var vb = ByteVector.fromArray(BYTE_SPECIES_HALF, bU, i);
            
            ShortVector vrs = (ShortVector) vr.castShape(SHORT_SPECIES, 0);
            ShortVector vgs = (ShortVector) vg.castShape(SHORT_SPECIES, 0);
            ShortVector vbs = (ShortVector) vb.castShape(SHORT_SPECIES, 0);
            
            vrs = vrs.and((short)0xFF).mul(rCo);
            vgs = vgs.and((short)0xFF).mul(gCo);
            vbs = vbs.and((short)0xFF).mul(bCo);
            
            res1 = (vrs.add(vgs).add(vbs));
            // SIMD divide by 255: value = (value + 1 + (value >> 8)) >> 8
            res1 = (res1.add((short)1).add(res1.lanewise(LSHR, 8))).lanewise(LSHR, 8);
            
            vr = ByteVector.fromArray(BYTE_SPECIES_HALF, rU, i + BYTE_SPECIES_HALF.length());
            vg = ByteVector.fromArray(BYTE_SPECIES_HALF, gU, i + BYTE_SPECIES_HALF.length());
            vb = ByteVector.fromArray(BYTE_SPECIES_HALF, bU, i + BYTE_SPECIES_HALF.length());
            
            vrs = (ShortVector) vr.castShape(SHORT_SPECIES, 0);
            vgs = (ShortVector) vg.castShape(SHORT_SPECIES, 0);
            vbs = (ShortVector) vb.castShape(SHORT_SPECIES, 0);
            
            vrs = vrs.and((short)0xFF).mul(rCo);
            vgs = vgs.and((short)0xFF).mul(gCo);
            vbs = vbs.and((short)0xFF).mul(bCo);
            
            res2 = (vrs.add(vgs).add(vbs));
            res2 = (res2.add((short)1).add(res2.lanewise(LSHR, 8))).lanewise(LSHR, 8);
            
            resb1 = (ByteVector) res1.castShape(BYTE_SPECIES_HALF, 0);
            resb2 = (ByteVector) res2.castShape(BYTE_SPECIES_HALF, 0);
            resb1.intoArray(grayscalePixels, i);
            resb2.intoArray(grayscalePixels, i + BYTE_SPECIES_HALF.length());
        }
        
        // fallback when vectorization would be out-of-array-bounds
        for (; i < resultsLength; i++) {
            int r = rU[i] & 0xFF;
            int g = gU[i] & 0xFF;
            int b = bU[i] & 0xFF;
            float brightness = 0.3f * r + 0.59f * g + 0.11f * b;
            float brightnessClamped = Math.max(0.0f, Math.min(255.0f, brightness));
            grayscalePixels[i] = (byte) Math.floor(brightnessClamped);
        }
        
        return grayscalePixels;
    }
    
    public static void writeImage(final String path, final byte[] pixelBytes, int width, int height) throws IOException {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        result.getRaster().setDataElements(0, 0, width, height, pixelBytes);
        File outputImageFile = new File(path);
        ImageIO.write(result, "png", outputImageFile);
        System.out.println(outputImageFile.getAbsolutePath());
    }
}
