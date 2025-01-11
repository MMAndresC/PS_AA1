package com.svalero.ps_aa1.task;

import com.svalero.ps_aa1.utils.ImageManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.awt.image.BufferedImage;
import java.io.File;
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
    Button cancel;
    Button save;
    final int SIZE = 120;
    public EditImageTask(File image, ArrayList<String> filters, int brightness, VBox vBox, int numImage){
        this.image = image;
        this.filters = filters;
        this.brightness = brightness;
        this.vBox = vBox;
        this.numImage = numImage;
        this.imageManager = new ImageManager();

    }
    @Override
    protected Integer call() throws Exception{
        createContainer();
        BufferedImage bufferedImage =  this.imageManager.toBufferedImage(this.image);
        //TODO lanzar error, fallo en la conversion o imagen vacia//if(bufferedImage.getHeight() == 0)
        for(String filter: this.filters){
            switch(filter){
                case "bright":
                    break;
                case "color":
                    break;
                case "gray":

            }
        }
        return null;
    }

    private void createContainer(){
        ImageView imageView = new ImageView();
        Image img = new Image(this.image.toURI().toString(),
                this.SIZE, // requested width
                this.SIZE, // requested height
                true, // preserve ratio
                true // smooth rescaling
        );
        imageView.setImage(img);
        this.hBox = new HBox();
        Label filter = new Label();
        filter.setText(getFilterName(0));
        this.hBox.setSpacing(20);
        this.hBox.setAlignment(Pos.CENTER_LEFT);
        this.hBox.getChildren().addAll(imageView, filter);
        this.progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);
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
        Pane pane = new Pane();
        pane.setPrefHeight(SIZE);
        pane.setStyle("-fx-border-color: black transparent transparent transparent; -fx-padding: 2 0 0 0;");
        pane.getChildren().addAll(this.hBox, progressBarContainer);
        Platform.runLater(() -> this.vBox.getChildren().add(pane));
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
