package com.svalero.ps_aa1.utils;
import java.awt.image.BufferedImage;

public class ImageFilters {
    private int adjustRange(int value) {
        return (Math.min(Math.max(0, value), 255));
    }
    public BufferedImage changeBrightness(int brightnessValue, BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();
        for (int i = 0; i < width; i++) {

            for (int j = 0; j < height; j++) {

                int[] rgb = image.getRaster().getPixel(i, j, new int[3]);

                int red = adjustRange(rgb[0] + brightnessValue);
                int green = adjustRange(rgb[1] + brightnessValue);
                int blue = adjustRange(rgb[2] + brightnessValue);

                int[] newRgb = { red, green, blue };

                image.getRaster().setPixel(i, j, newRgb);
            }
        }
        return image;
    }

    public BufferedImage toGrayScale(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();

        for (int i = 0; i < width; i++) {

            for (int j = 0; j < height; j++) {

                int[] rgb = image.getRaster().getPixel(i, j, new int[3]);

                int red = rgb[0];
                int green = rgb[1];
                int blue = rgb[2];

                int avg = (red + green + blue) / 3;

                int[] newRgb = { avg, avg, avg };

                image.getRaster().setPixel(i, j, newRgb);
            }
        }
        return image;
    }

    public BufferedImage invertColor(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();

        for (int i = 0; i < width; i++) {

            for (int j = 0; j < height; j++) {

                int[] rgb = image.getRaster().getPixel(i, j, new int[3]);

                int red = 255 - rgb[0];
                int green = 255 - rgb[1];
                int blue = 255 - rgb[2];

                int[] newRgb = { red, green, blue };

                image.getRaster().setPixel(i, j, newRgb);
            }
        }
        return image;
    }

}
