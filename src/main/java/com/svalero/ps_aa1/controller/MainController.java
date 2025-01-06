package com.svalero.ps_aa1.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    @FXML
    private Label pathFiles;

    @FXML
    private Label brigthnessLabel;
    @FXML
    private Button applyFilter;

    @FXML
    private Button selectFile;

    @FXML
    private Button selectDirectory;

    @FXML
    private Slider brightnessValue;
    @FXML
    private Pane previewPane;

    public void onClickSelectFile(){
        Stage stage = (Stage) selectFile.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter imageFilter
                = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg", "*.bmp", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            pathFiles.setText(selectedFile.getAbsolutePath());
            ImageView imageView = new ImageView();
            previewPane.getChildren().clear();

            Image image = new Image(selectedFile.toURI().toString(),
                    250, // requested width
                    118, // requested height
                    true, // preserve ratio
                    true, // smooth rescaling
                    true // load in background, diff thread
            );
            image.progressProperty().addListener((obs, oldVal, newVal) -> {
                //Completed task, image loaded
                if (newVal.doubleValue() == 1.0) {
                    imageView.setImage(image);
                    previewPane.setLayoutX(centerImage(image));
                    previewPane.setLayoutY(previewPane.getLayoutY() + 2);
                    previewPane.getChildren().add(imageView);
                }
            });
        }
    }

    public double centerImage(Image image){
        double coordX = previewPane.getLayoutX();
        double freeSpace = previewPane.getWidth() - image.getWidth();
        return coordX + (freeSpace / 2);

    }

}
