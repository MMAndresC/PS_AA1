package com.svalero.ps_aa1.task;

import com.svalero.ps_aa1.utils.ImageFilters;
import com.svalero.ps_aa1.utils.ImageManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
        int total = 100 * this.filters.size();
        this.imageFilters = new ImageFilters();
        BufferedImage bufferedImage =  this.imageManager.toBufferedImage(this.image);
        //TODO validar imagen
        for(int i = 0; i < this.filters.size(); i++){
            BufferedImage newImage;
            String filter = this.filters.get(i);
            switch(filter){
                case "bright":
                    newImage = this.imageFilters.changeBrightness(this.brightness, bufferedImage, total, 100 * i, this::updateProgress);
                    setResultImage(newImage, i);
                    break;
                case "color":
                    newImage = this.imageFilters.invertColor(bufferedImage, total, 100 * i, this::updateProgress);
                    setResultImage(newImage, i);
                    break;
                case "gray":
                    newImage = this.imageFilters.toGrayScale(bufferedImage, total, 100 * i, this::updateProgress);
                    setResultImage(newImage, i);
            }
        }
        endTask();
        return null;
    }

    private void setResultImage(BufferedImage image, int index) {
        Label newFilter = new Label("");
        if(index + 1 < this.filters.size())
            newFilter.setText(getFilterName(index + 1));
        ImageView imageView = this.imageManager.createImageViewFromBufferedImage(image, this.SIZE);
        Platform.runLater(() -> {
            try {
                if(!newFilter.getText().isEmpty())
                    this.hBox.getChildren().addAll(imageView, newFilter);
                else
                    this.hBox.getChildren().add(imageView);
                this.pane.getChildren().set(0, this.hBox);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
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
            this.progressBar.setPrefWidth(200);
            this.status = new Label("Procesando...");
            this.percent = new Label("0");
            this.percent.setAlignment(Pos.CENTER_LEFT);
            this.cancel = new Button("❌");
            this.cancel.setStyle("-fx-text-fill: red;");
            this.cancel.setOnAction(event -> cancelTask());
            HBox progressBarContainer = new HBox(this.status, progressBar, percent, this.cancel);
            progressBarContainer.setAlignment(Pos.CENTER);
            progressBarContainer.setLayoutY(SIZE + 8);
            progressBarContainer.setSpacing(5);
            this.pane = new Pane();
            this.pane.setPrefHeight(SIZE);
            this.pane.setStyle("-fx-border-color: black transparent transparent transparent; -fx-padding: 2 0 0 0;");
            this.pane.getChildren().addAll(this.hBox, progressBarContainer);
            double basicHeight = this.vBox.getPrefHeight();
            Platform.runLater(() -> {
                try {
                    this.progressBar.progressProperty().bind(this.progressProperty());
                    this.percent.textProperty().bind(
                            Bindings.createStringBinding(() ->
                                            String.format("%.0f%%", progressBar.getProgress() * 100),
                                    progressBar.progressProperty()
                            )
                    );
                    this.vBox.getChildren().add(this.pane);
                    this.vBox.setPrefHeight(basicHeight + (this.vBox.getChildren().size() * this.SIZE));
                }catch(Exception e){
                    e.printStackTrace();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Button createCleanButton(){
        Button clean = new Button("Limpiar");
        clean.setOnAction(event -> {
            Button source = (Button) event.getSource();
            Pane pane = (Pane) source.getParent().getParent();
            Platform.runLater(() -> {
                try{
                    this.vBox.getChildren().remove(pane);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        });
        return clean;
    }

    private void endTask(){
        Platform.runLater(() -> {
            try {
                this.status.setText("Finalizado");
                Pane parent = (Pane) this.cancel.getParent();
                parent.getChildren().remove(this.cancel);
                parent.getChildren().add(createCleanButton());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }//TODO boton de salvar y en placeholder poner cuantas imagenes se estan procesando, quitar cuando una tarea termine

    private void cancelTask(){
        this.status.setText("Cancelado");
        Pane parent = (Pane) this.cancel.getParent();
        parent.getChildren().remove(this.cancel);
        parent.getChildren().add(createCleanButton());
        this.cancel();
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
