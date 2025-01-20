package com.svalero.ps_aa1.utils;

import java.awt.image.BufferedImage;
import java.util.concurrent.ThreadLocalRandom;

public class ImageFilters {

    private int adjustRange(int value) {
        return (Math.min(Math.max(0, value), 255));
    }
    public BufferedImage changeBrightness(int brightnessValue, BufferedImage image, int total, int base, ProgressCallback callback, boolean isVideo) throws InterruptedException {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int i = 0; i < width; i++) {
            if(!isVideo)
                Thread.sleep(ThreadLocalRandom.current().nextInt(5, 15));
            for (int j = 0; j < height; j++) {

                int[] rgb = image.getRaster().getPixel(i, j, new int[3]);

                int red = adjustRange(rgb[0] + brightnessValue);
                int green = adjustRange(rgb[1] + brightnessValue);
                int blue = adjustRange(rgb[2] + brightnessValue);

                int[] newRgb = {red, green, blue};

                image.getRaster().setPixel(i, j, newRgb);
            }
            if (callback != null) {
                callback.onProgress((i * 100 / width) + base, total);
            }
        }
        return image;
    }

    public BufferedImage toGrayScale(BufferedImage image, int total, int base, ProgressCallback callback, boolean isVideo) throws InterruptedException {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int i = 0; i < width; i++) {
            if(!isVideo)
                Thread.sleep(ThreadLocalRandom.current().nextInt(5, 15));
            for (int j = 0; j < height; j++) {

                int[] rgb = image.getRaster().getPixel(i, j, new int[3]);

                int red = rgb[0];
                int green = rgb[1];
                int blue = rgb[2];

                int avg = (red + green + blue) / 3;

                int[] newRgb = { avg, avg, avg };

                image.getRaster().setPixel(i, j, newRgb);
            }
            if (callback != null) {
                callback.onProgress((i * 100 / width) + base,  total);
            }
        }
        return image;
    }

    public BufferedImage invertColor(BufferedImage image, int total, int base, ProgressCallback callback, boolean isVideo) throws InterruptedException {
        int width = image.getWidth();
        int height = image.getHeight();
        for (int i = 0; i < width; i++) {
            if(!isVideo)
                Thread.sleep(ThreadLocalRandom.current().nextInt(5, 15));
            for (int j = 0; j < height; j++) {

                int[] rgb = image.getRaster().getPixel(i, j, new int[3]);

                int red = 255 - rgb[0];
                int green = 255 - rgb[1];
                int blue = 255 - rgb[2];

                int[] newRgb = { red, green, blue };

                image.getRaster().setPixel(i, j, newRgb);
            }
            if (callback != null) {
                callback.onProgress((i * 100 / width) + base,  total);
            }
        }
        return image;
    }

    public interface ProgressCallback {
        void onProgress(int current, int total);
    }

}
