package com.svalero.ps_aa1.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageManager {
    public BufferedImage toBufferedImage(File file) throws IOException {
        BufferedImage image = new BufferedImage(0, 0, 0);
        try{
            image = ImageIO.read(file);
        }catch(IOException e) {
            System.out.println("Error converting file to BufferedImage");
            e.printStackTrace();
        }
        return image;
    }
}
