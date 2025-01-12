package com.svalero.ps_aa1.task;

import com.svalero.ps_aa1.utils.ImageFilters;
import com.svalero.ps_aa1.utils.ImageManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class EditImageTask extends Task<Integer> {
    File image;
    ArrayList<String> filters;
    int brightness;
    int numImage;
    VBox vBox;
    HBox hBox;
    Label percent;
    Label status;
    ProgressBar progressBar;
    ImageManager imageManager;
    ImageFilters imageFilters;
    Button cancel;
    Button save;
    String formatName;
    Pane pane;
    final int SIZE = 120;
    public EditImageTask(File image, ArrayList<String> filters, int brightness, VBox vBox, int numImage){
        this.image = image;
        this.filters = filters;
        this.brightness = brightness;
        this.vBox = vBox;
        this.numImage = numImage;
        this.imageManager = new ImageManager();
        int index = image.getName().lastIndexOf(".");
        this.formatName = image.getName().substring(index + 1);
    }
    @Override
    protected Integer call() throws Exception{
        createContainer();
        this.imageFilters = new ImageFilters();
        BufferedImage bufferedImage =  this.imageManager.toBufferedImage(this.image);
        //TODO validar imagen
        for(String filter: this.filters){
            BufferedImage newImage;
            switch(filter){
                case "bright":
                    newImage = this.imageFilters.changeBrightness(this.brightness, bufferedImage, this::updateProgress);
                    setResultImage(newImage);
                    break;
                case "color":
                    newImage = this.imageFilters.invertColor(bufferedImage, this::updateProgress);
                    setResultImage(newImage);
                    break;
                case "gray":
                    newImage = this.imageFilters.toGrayScale(bufferedImage, this::updateProgress);
                    setResultImage(newImage);
            }
        }
        return null;
    }

    private void setResultImage(BufferedImage image) throws IOException, InterruptedException {
        ImageView imageView = this.imageManager.createImageViewFromBufferedImage(image, this.SIZE);
        Platform.runLater(() -> {
            try {
                this.hBox.getChildren().add(imageView);
                this.pane.getChildren().set(0, this.hBox);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //TODO da error despues de convertir imagenes si intento abrir de nuevo el explorador peta
    //TODO control de errores

    private void createContainer(){
        try {
            ImageView imageView = this.imageManager.createImageViewFromFile(this.image, this.SIZE);
            this.hBox = new HBox();
            Label filter = new Label();
            filter.setText(getFilterName(0));
            this.hBox.setSpacing(20);
            this.hBox.setAlignment(Pos.CENTER_LEFT);
            this.hBox.getChildren().addAll(imageView, filter);
            this.progressBar = new ProgressBar(0);
            Platform.runLater(() -> this.progressBar.progressProperty().bind(this.progressProperty()));
            this.progressBar.setPrefWidth(200);
            this.status = new Label("Procesando...");
            this.percent = new Label("0");
            this.percent.setAlignment(Pos.CENTER_LEFT);
            Label symbol = new Label("%");
            this.cancel = new Button("❌");
            this.cancel.setStyle("-fx-text-fill: red;");
            this.cancel.setOnAction(event -> cancelTask());
            HBox progressBarContainer = new HBox(this.status, progressBar, percent, symbol, this.cancel);
            progressBarContainer.setAlignment(Pos.BASELINE_CENTER);
            progressBarContainer.setLayoutY(SIZE + 8);
            progressBarContainer.setSpacing(5);
            this.pane = new Pane();
            this.pane.setPrefHeight(SIZE);
            this.pane.setStyle("-fx-border-color: black transparent transparent transparent; -fx-padding: 2 0 0 0;");
            this.pane.getChildren().addAll(this.hBox, progressBarContainer);
            Platform.runLater(() -> this.vBox.getChildren().add(this.pane));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Button createCleanButton(){
        Button clean = new Button("Limpiar");
        clean.setOnAction(event -> {
            Button source = (Button) event.getSource();
            Pane pane = (Pane) source.getParent().getParent();
            Platform.runLater(() -> this.vBox.getChildren().remove(pane));
        });
        return clean;
    }

    private void cancelTask(){
        this.status.setText("Cancelado");
        Pane parent = (Pane) this.cancel.getParent();
        parent.getChildren().remove(this.cancel);
        parent.getChildren().add(createCleanButton());
    }

    private String getFilterName(int index){
        String code = this.filters.get(index);
        return switch (code) {
            case "bright" -> "Brillo a " + this.brightness;
            case "color" -> "Invertir color";
            default -> "Escala grises";
        };
    }
}
