package com.svalero.ps_aa1.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class FileManager {
    public BufferedImage toBufferedImage(File file) throws IOException {
        return ImageIO.read(file);
    }

    public ImageView createImageViewFromFile(File file, int size){
        ImageView imageView = new ImageView();
        Image img = new Image(file.toURI().toString(),
                size, // requested width
                size, // requested height
                true, // preserve ratio
                true // smooth rescaling
        );
        imageView.setImage(img);
        return imageView;
    }

    public ImageView createImageViewFromBufferedImage(BufferedImage bufferedImage, int size){
        ImageView imageView = new ImageView();
        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        imageView.setImage(image);
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        return imageView;
    }

    public String saveImage(String path, String formatName, BufferedImage bufferedImage) throws IOException{
        UUID uuid = UUID.randomUUID();
        String filename = uuid + "." + formatName;
        String absolutePath = path + "\\" + filename;
        ImageIO.write(bufferedImage, formatName, new File(absolutePath));
        return absolutePath;
    }

    public BufferedImage frameToBufferedImage(Frame frame) {
        try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
            return converter.getBufferedImage(frame);
        }
    }

    public Frame bufferedImageToFrame(BufferedImage image) {
        try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
            return converter.getFrame(image);
        }
    }

    public static boolean writeFramesToVideo(String savedPath, List<Frame> frames, double frameRate, int width, int height, int videoCodec) throws Exception {
        System.out.println(frameRate + " " + width + " " + height + " " + videoCodec);
        if (savedPath == null || savedPath.isEmpty()) {
            throw new IllegalArgumentException("Output path cannot be null or empty.");
        }

        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("Frame list cannot be null or empty.");
        }
        UUID uuid = UUID.randomUUID();
        String filename = uuid + ".mp4";
        savedPath = savedPath + "\\" + filename;
        try(FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(savedPath, width, height)) {
           recorder.setVideoCodec(videoCodec);
           recorder.setFormat("mp4");
           recorder.setFrameRate(frameRate);
           recorder.start();

           for (Frame frame : frames) {
               if (frame != null && frame.image != null) {
                   recorder.record(frame);
               } else {
                   System.out.println("Detected invalid frame");
               }
           }
           recorder.stop();
           return true;
       }catch(Exception e){
            System.out.println(e.getMessage());
           HistoryLogger.logError("Error writing video: " + e.getMessage());
           return false;
       }
    }
}
