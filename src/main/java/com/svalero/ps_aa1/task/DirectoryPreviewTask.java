package com.svalero.ps_aa1.task;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class DirectoryPreviewTask extends Task<Integer> {
    private final File[] images;
    private final Pane pane;
    public DirectoryPreviewTask(File[] images, Pane pane){
        this.images = images;
        this.pane = pane;
    }
    @Override
    protected Integer call() throws Exception{
        try{
            final double space = 12;
            final int size = 50;
            Platform.runLater(() -> pane.getChildren().clear());
            double order = 0;
            int limit = Math.min(15, images.length);
            for(int i = 0; i < limit; i++){
                File image = images[i];
                ImageView imageView = new ImageView();
                Image img = new Image(image.toURI().toString(),
                        size, // requested width
                        size, // requested height
                        true, // preserve ratio
                        true // smooth rescaling
                );
                double coordX = space + (size * order);
                int coordY =  (i / 5) * size;
                if(order == 4) order = 0;
                else order++;
                imageView.setImage(img);
                imageView.setLayoutX(coordX);
                imageView.setLayoutY(2 + coordY);
                Platform.runLater(() -> pane.getChildren().add(imageView));
            }
        }catch(Exception e){
            System.out.println("Error in DirectoryPreviewTask");
            e.printStackTrace();
        }

        return null;
    }

    public File[] getImages() {
        return images;
    }
}
