package hu.laszlolukacs.vectorsimdbench.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    
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
            return convertRgbToArgb(data.getData());
        }
    }
    
    public static byte[] convertRgbToArgb(final byte[] rbgBytes) {
        int resultLength = (rbgBytes.length / 3) * 4;
        byte[] result = new byte[(rbgBytes.length / 3) * 4];
        int j = 0;
        for (int i = 0; i < rbgBytes.length; i += 3) {
            if (j < resultLength) {
                result[j] = (byte) (0xFF);
                result[j + 1] = rbgBytes[i];
                result[j + 2] = rbgBytes[i + 1];
                result[j + 3] = rbgBytes[i + 2];
                j += 4;
            }
        }
        
        return result;
    }
    
    public static float[] extractFloats(BufferedImage bufferedImage) {
        byte[] bytes = extractBytes(bufferedImage);
        float[] result = new float[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            result[i] = (bytes[i] & 0xFF) / 255.0f;
        }
        
        return result;
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
    
    public static float[] extractFloatsFromImage(InputStream imageStream) {
        try {
            return extractFloats(loadImage(imageStream));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }
    
    public static void writeGrayscaleImage(final String path, final byte[] pixelBytes, int width, int height) throws IOException {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        result.getRaster().setDataElements(0, 0, width, height, pixelBytes);
        File outputImageFile = new File(path);
        ImageIO.write(result, "png", outputImageFile);
        System.out.println(outputImageFile.getAbsolutePath());
    }
    
    public static void writeGrayscaleImage(final String path, final float[] pixelFloats, int width, int height) throws IOException {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        result.getRaster().setPixels(0, 0, width, height, pixelFloats);
        File outputImageFile = new File(path);
        ImageIO.write(result, "png", outputImageFile);
        System.out.println(outputImageFile.getAbsolutePath());
    }
}
