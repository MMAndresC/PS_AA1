package com.svalero.ps_aa1.controller;

import com.svalero.ps_aa1.task.DirectoryPreviewTask;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static final String OPEN_DEFAULT_PATH = "C:\\Users\\Public\\Pictures";
    private static final String SAVE_DEFAULT_PATH = "C:\\Users\\Public\\Pictures\\Saved";

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    @FXML
    private Label pathFiles;

    @FXML
    private Label pathSave;

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
        //Filter by image extensions
        FileChooser.ExtensionFilter imageFilter
                = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg", "*.bmp", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);
        //Default directory to show
        File initialDirectory = new File(OPEN_DEFAULT_PATH);
        if (initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            pathFiles.setText(selectedFile.getAbsolutePath());
            ImageView imageView = new ImageView();
            //Clear preview
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
                    imageView.setLayoutX(centerImage(image));
                    imageView.setLayoutY(2);
                    previewPane.getChildren().add(imageView);
                }
            });
        }
    }

    public void onClickSelectDirectory(){
        Stage stage = (Stage) selectDirectory.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        //Default directory to show
        File initialDirectory = new File(OPEN_DEFAULT_PATH);
        if (initialDirectory.exists()) {
            directoryChooser.setInitialDirectory(initialDirectory);
        }
        File selectedDirectory = directoryChooser.showDialog(stage);
        if(selectedDirectory != null){
            pathFiles.setText(selectedDirectory.getAbsolutePath());
            //Select only image files
            File[] imageFiles = selectedDirectory.listFiles(file ->
                    file.isFile() && isImageFile(file)
            );
            if (imageFiles != null && imageFiles.length > 0) {
                DirectoryPreviewTask task = new DirectoryPreviewTask(imageFiles, previewPane);
                Thread thread = new Thread(task);
                //Close thread if app exit
                thread.setDaemon(true);
                task.setOnSucceeded(event -> System.out.println("Images loaded"));
                thread.start();
            }

        }
    }

    public double centerImage(Image image){
        double freeSpace = previewPane.getWidth() - image.getWidth();
        return freeSpace / 2;
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif");
    }

}
