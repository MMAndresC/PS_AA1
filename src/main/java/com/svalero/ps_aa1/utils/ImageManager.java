package com.svalero.ps_aa1.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ImageManager {
    public BufferedImage toBufferedImage(File file) throws IOException {
        try{
            return ImageIO.read(file);
        }catch(IOException e) {
            System.out.println("Error converting file to BufferedImage");
            e.printStackTrace();
            return null;
        }
    }

    public ImageView createImageViewFromFile(File file, int size){
        try {
            ImageView imageView = new ImageView();
            Image img = new Image(file.toURI().toString(),
                    size, // requested width
                    size, // requested height
                    true, // preserve ratio
                    true // smooth rescaling
            );
            imageView.setImage(img);
            return imageView;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public ImageView createImageViewFromBufferedImage(BufferedImage bufferedImage, int size){
        try {
            ImageView imageView = new ImageView();
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setImage(image);
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            return imageView;
        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }

    public void saveImage(String path, String formatName, BufferedImage bufferedImage) throws IOException{
        UUID uuid = UUID.randomUUID();
        String filename = uuid + "." + formatName;
        String absolutePath = path + "\\" + filename;
        try{
            ImageIO.write(bufferedImage, formatName, new File(absolutePath));
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}
