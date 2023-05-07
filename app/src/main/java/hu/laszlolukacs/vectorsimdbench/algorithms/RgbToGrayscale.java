package hu.laszlolukacs.vectorsimdbench.algorithms;

import jdk.incubator.vector.*;

import static jdk.incubator.vector.VectorOperators.LSHR;

public class RgbToGrayscale {
    
    private static final float R_COEFF = 0.299f;
    private static final float G_COEFF = 0.587f;
    private static final float B_COEFF = 0.114f;
    
    public static byte[] rgbToGrayscaleScalar(final byte[] rgbBytes) {
        
        final int numberOfChannels = 4; // ARGB
        final int alphaPadding = 1;
        
        final int resultsLength = rgbBytes.length / numberOfChannels;
        byte[] grayscalePixels = new byte[resultsLength];
        
        for (int i = 0; i < resultsLength; i++) {
            int r = rgbBytes[(i * numberOfChannels) + alphaPadding] & 0xFF;
            int g = rgbBytes[(i * numberOfChannels) + alphaPadding + 1] & 0xFF;
            int b = rgbBytes[(i * numberOfChannels) + alphaPadding + 2] & 0xFF;
            float brightness = R_COEFF * r + G_COEFF * g + B_COEFF * b;
            float brightnessClamped = Math.max(0.0f, Math.min(255.0f, brightness));
            grayscalePixels[i] = (byte) Math.floor(brightnessClamped);
        }
        
        return grayscalePixels;
    }
    
    public static float[] rgbToGrayscaleScalarF(final float[] rgbFloats) {
        
        final int numberOfChannels = 4; // ARGB
        final int alphaPadding = 1;
        final int resultsLength = rgbFloats.length / numberOfChannels;
        
        float[] grayscalePixelsF = new float[resultsLength];
        
        final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
        for (int i = 0; i < resultsLength; i++) {
            float r = rgbFloats[(i * numberOfChannels) + alphaPadding];
            float g = rgbFloats[(i * numberOfChannels) + alphaPadding + 1];
            float b = rgbFloats[(i * numberOfChannels) + alphaPadding + 2];
            float brightness = (R_COEFF * r + G_COEFF * g + B_COEFF * b) * 255.0f;
            float brightnessClamped = Math.max(0.0f, Math.min(255.0f, brightness));
            grayscalePixelsF[i] = (float) Math.floor(brightnessClamped);
        }
        
        for (int i = 0; i < resultsLength; i++) {
            float r = rgbFloats[(i * numberOfChannels) + alphaPadding];
            float g = rgbFloats[(i * numberOfChannels) + alphaPadding + 1];
            float b = rgbFloats[(i * numberOfChannels) + alphaPadding + 2];
            float brightness = (R_COEFF * r + G_COEFF * g + B_COEFF * b) * 255.0f;
            float brightnessClamped = Math.max(0.0f, Math.min(255.0f, brightness));
            grayscalePixelsF[i] = (float) Math.floor(brightnessClamped);
        }
        
        return grayscalePixelsF;
    }
    
    public static float[] rgbToGrayscaleSimdF(final float[] rgbFloats) {
        
        final int numberOfChannels = 4; // ARGB
        final int alphaPadding = 1;
        final int resultsLength = rgbFloats.length / numberOfChannels;
        
        float[] grayscalePixelsF = new float[resultsLength];
        
        // separate the channels
        float[] rFloats = new float[resultsLength];
        float[] gFloats = new float[resultsLength];
        float[] bFloats = new float[resultsLength];
        for (int i = 0; i < resultsLength; i++) {
            rFloats[i] = rgbFloats[(i * numberOfChannels) + alphaPadding];
            gFloats[i] = rgbFloats[(i * numberOfChannels) + alphaPadding + 1];
            bFloats[i] = rgbFloats[(i * numberOfChannels) + alphaPadding + 2];
        }
        
        final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;
        FloatVector resultVec = FloatVector.zero(SPECIES);
        int i = 0;
        for (; i < SPECIES.loopBound(resultsLength); i += SPECIES.length()) {
            FloatVector redVec = FloatVector.fromArray(SPECIES, rFloats, i);
            FloatVector greenVec = FloatVector.fromArray(SPECIES, gFloats, i);
            FloatVector blueVec = FloatVector.fromArray(SPECIES, bFloats, i);
            resultVec = ((redVec.mul(R_COEFF))
                    .add((greenVec).mul(G_COEFF))
                    .add((blueVec).mul(B_COEFF)))
                    .mul(255.0f);
            resultVec.intoArray(grayscalePixelsF, i);
        }
        
        // fallback when vectorization would be out-of-array-bounds
        for (; i < resultsLength; i++) {
            grayscalePixelsF[i] =
                    ((rFloats[i] * R_COEFF) + (gFloats[i] * G_COEFF) + (bFloats[i] * B_COEFF)) * 255.0f;
        }
        
        return grayscalePixelsF;
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
        
        final short rCo = (short) (255 * R_COEFF);
        final short gCo = (short) (255 * G_COEFF);
        final short bCo = (short) (255 * B_COEFF);
        
        int i = 0;
        for (; i < BYTE_SPECIES.loopBound(resultsLength); i += BYTE_SPECIES.length()) {
            
            var vr = ByteVector.fromArray(BYTE_SPECIES_HALF, rU, i);
            var vg = ByteVector.fromArray(BYTE_SPECIES_HALF, gU, i);
            var vb = ByteVector.fromArray(BYTE_SPECIES_HALF, bU, i);
            
            ShortVector vrs = (ShortVector) vr.castShape(SHORT_SPECIES, 0);
            ShortVector vgs = (ShortVector) vg.castShape(SHORT_SPECIES, 0);
            ShortVector vbs = (ShortVector) vb.castShape(SHORT_SPECIES, 0);
            
            vrs = vrs.and((short) 0xFF).mul(rCo);
            vgs = vgs.and((short) 0xFF).mul(gCo);
            vbs = vbs.and((short) 0xFF).mul(bCo);
            
            res1 = (vrs.add(vgs).add(vbs));
            // SIMD divide by 255: value = (value + 1 + (value >> 8)) >> 8
            res1 = (res1.add((short) 1).add(res1.lanewise(LSHR, 8))).lanewise(LSHR, 8);
            
            vr = ByteVector.fromArray(BYTE_SPECIES_HALF, rU, i + BYTE_SPECIES_HALF.length());
            vg = ByteVector.fromArray(BYTE_SPECIES_HALF, gU, i + BYTE_SPECIES_HALF.length());
            vb = ByteVector.fromArray(BYTE_SPECIES_HALF, bU, i + BYTE_SPECIES_HALF.length());
            
            vrs = (ShortVector) vr.castShape(SHORT_SPECIES, 0);
            vgs = (ShortVector) vg.castShape(SHORT_SPECIES, 0);
            vbs = (ShortVector) vb.castShape(SHORT_SPECIES, 0);
            
            vrs = vrs.and((short) 0xFF).mul(rCo);
            vgs = vgs.and((short) 0xFF).mul(gCo);
            vbs = vbs.and((short) 0xFF).mul(bCo);
            
            res2 = (vrs.add(vgs).add(vbs));
            res2 = (res2.add((short) 1).add(res2.lanewise(LSHR, 8))).lanewise(LSHR, 8);
            
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
}
