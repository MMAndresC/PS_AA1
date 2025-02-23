package com.svalero.ps_aa1.task;

import com.svalero.ps_aa1.utils.HistoryLogger;
import com.svalero.ps_aa1.utils.ImageFilters;
import com.svalero.ps_aa1.utils.FileManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class EditImageTask extends Task<String> {
    File image;
    ArrayList<String> filters;
    int brightness;
    int numImage;
    VBox vBox;
    HBox hBox;
    Label percent;
    Label status;
    ProgressBar progressBar;
    FileManager fileManager;
    ImageFilters imageFilters;
    Button cancel;
    Button save;
    String formatName;
    Pane pane;
    String pathSavedImage;
    final int SIZE = 120;
    public EditImageTask(File image, ArrayList<String> filters, int brightness, VBox vBox, int numImage){
        this.image = image;
        this.filters = filters;
        this.brightness = brightness;
        this.vBox = vBox;
        this.numImage = numImage;
        this.fileManager = new FileManager();
        int index = image.getName().lastIndexOf(".");
        this.formatName = image.getName().substring(index + 1);
        this.pathSavedImage = " ";
    }
    @Override
    protected String call() throws Exception{
        //if fails to have task information
        String filtersInString = getFiltersInString();
        String message = this.image.getName() + "@" + "Origen: " + this.image.getAbsolutePath() + " - Filtros: " + filtersInString + " - " + "Status: FALLIDO";
        this.updateMessage(message);
        createContainer();
        int total = 100 * this.filters.size();
        this.imageFilters = new ImageFilters();
        BufferedImage bufferedImage =  this.fileManager.toBufferedImage(this.image);
        if(bufferedImage == null)
            throw new Exception("Error al cambiar el formato de la imagen");
        BufferedImage newImage = null;
        for(int i = 0; i < this.filters.size(); i++){
            String filter = this.filters.get(i);
            switch(filter){
                case "bright":
                    newImage = this.imageFilters.changeBrightness(this.brightness, bufferedImage, total, 100 * i, this::updateProgress, false);
                    setResultImage(newImage, i);
                    break;
                case "color":
                    newImage = this.imageFilters.invertColor(bufferedImage, total, 100 * i, this::updateProgress, false);
                    setResultImage(newImage, i);
                    break;
                case "gray":
                    newImage = this.imageFilters.toGrayScale(bufferedImage, total, 100 * i, this::updateProgress, false);
                    setResultImage(newImage, i);
            }
        }
        endTask(newImage);
        this.updateMessage(this.image.getName());
        return "Origen: " + this.image.getAbsolutePath() + " - Filtros: " + filtersInString + " - Status: COMPLETADO";
    }

    private void setResultImage(BufferedImage image, int index) {
        Label newFilter = new Label("");
        if(index + 1 < this.filters.size())
            newFilter.setText(getFilterName(index + 1));
        ImageView imageView = this.fileManager.createImageViewFromBufferedImage(image, this.SIZE);
        String shortUUID = UUID.randomUUID().toString().substring(0, 5);
        imageView.setId(this.filters.get(index) + "_" + shortUUID);
        imageView.setOnMouseClicked(event -> {
            Pane pane = (Pane) this.hBox.getParent();
            HBox hbox = (HBox) pane.getChildren().get(1);
            Label idFile = (Label) hbox.getChildren().get(3);
            idFile.setText(imageView.getId());
        });
        Platform.runLater(() -> {
            try {
                if(!newFilter.getText().isEmpty())
                    this.hBox.getChildren().addAll(imageView, newFilter);
                else
                    this.hBox.getChildren().add(imageView);
                this.pane.getChildren().set(0, this.hBox);
            } catch (Exception e) {
                HistoryLogger.logError(e.getMessage());
            }
        });
    }

    private void createContainer(){
        try {
            ImageView imageView = this.fileManager.createImageViewFromFile(this.image, this.SIZE);
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
            Label idFile = new Label();
            HBox progressBarContainer = new HBox(this.status, progressBar, percent, idFile, this.cancel);
            progressBarContainer.setAlignment(Pos.CENTER);
            progressBarContainer.setLayoutY(SIZE + 8);
            progressBarContainer.setSpacing(5);
            progressBarContainer.setId("hbox_" + this.numImage);
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
                    HistoryLogger.logError(e.getMessage());
                }
            });
        }catch (Exception e){
            HistoryLogger.logError(e.getMessage());
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
                    HistoryLogger.logError(e.getMessage());
                }
            });
        });
        return clean;
    }

    private void endTask(BufferedImage resultImage){
        this.save = new Button("Guardar");
        this.save.setOnAction(event -> {
            try{
                Label label = (Label) this.save.getScene().lookup("#pathSave");
                String path = label.getText().trim();
                HBox hbox = (HBox) this.save.getParent();
                Label idFile = (Label) hbox.getChildren().get(3);
                if(idFile.getText().isEmpty())
                    this.pathSavedImage = this.fileManager.saveImage(path, this.formatName, resultImage);
                else{
                    //Search imageView selected to extract image
                    ImageView imageView = (ImageView) this.save.getScene().lookup("#" + idFile.getText().trim());
                    Image img = imageView.getImage();
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(img, null);
                    //Change format to save file
                    BufferedImage compatibleImage = new BufferedImage(
                            bufferedImage.getWidth(),
                            bufferedImage.getHeight(),
                            BufferedImage.TYPE_INT_RGB // Compatible with JPG
                    );
                    compatibleImage.getGraphics().drawImage(bufferedImage, 0, 0, null);
                    this.pathSavedImage = this.fileManager.saveImage(path, this.formatName, compatibleImage);
                }
                Platform.runLater(() -> {
                    try {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setHeaderText("Notificacion");
                        alert.setContentText("La edicion de la imagen " + this.image.getName() + " se ha guardado");
                        alert.show();
                    } catch (Exception e) {
                        HistoryLogger.logError(e.getMessage());
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Platform.runLater(() -> {
            try {
                this.status.setText("Finalizado");
                percent.textProperty().unbind();
                percent.setText("✅");
                Pane parent = (Pane) this.cancel.getParent();
                parent.getChildren().remove(this.cancel);
                parent.getChildren().addAll(this.save, createCleanButton());
            }catch (Exception e){
                HistoryLogger.logError(e.getMessage());
            }
        });
    }

    private void cancelTask(){
        this.status.setText("Cancelado");
        Pane parent = (Pane) this.cancel.getParent();
        parent.getChildren().remove(this.cancel);
        parent.getChildren().add(createCleanButton());
        String filtersInString = getFiltersInString();
        String message = "Origen: " + this.image.getAbsolutePath() + " - Filtros: " + filtersInString + " - " + "Status: CANCELADO en el " + this.percent.getText();
        this.updateMessage(this.image.getName() + "@" + message);
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

    private String getFiltersInString(){
        StringBuilder result = new StringBuilder();;
        for(int i = 0; i < this.filters.size(); i++){
            String text = getFilterName(i);
            result.append(text);
            if(i != this.filters.size() - 1)
                result.append(" - ");
        }
        return result.toString();
    }
    public int getNumImage() {
        return this.numImage;
    }
}
