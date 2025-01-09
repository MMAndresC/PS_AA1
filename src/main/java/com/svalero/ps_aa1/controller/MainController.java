package com.svalero.ps_aa1.controller;

import com.svalero.ps_aa1.task.DirectoryPreviewTask;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private static final String MAIN_DIRECTORY = "EditImages";
    private static final String SAVE_DIRECTORY = "Saved";

    private ArrayList<String> orderFilters = new ArrayList<>();
    private ArrayList<File> imageToProcess = new ArrayList<>();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Check if default paths exist
        String path = System.getProperty("user.home") + "\\" + MAIN_DIRECTORY;
        createDirectory(path, pathFiles);
        path = System.getProperty("user.home") + "\\" + MAIN_DIRECTORY + "\\" + SAVE_DIRECTORY;
        createDirectory(path, pathSave);
        //Link label with slider
        brightnessSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            brigthnessLabel.setText(String.valueOf(newValue.intValue()));
            int index = this.orderFilters.indexOf("bright");
            if(newValue.intValue() == 0){
                if(index != -1) {
                    changeOrder(index + 1);
                    this.orderFilters.remove(index);
                    orderBrightness.setText("");
                }
            }else{
                if(index == -1) {
                    this.orderFilters.add("bright");
                    orderBrightness.setText(String.valueOf(this.orderFilters.size()));
                }
            }
            applyFilters.setDisable(this.orderFilters.isEmpty());
        });
    }

    @FXML
    private Label pathFiles;

    @FXML
    private Label pathSave;

    @FXML
    private Label brigthnessLabel;
    @FXML
    private  Label orderBrightness;

    @FXML
    private  Label orderColor;

    @FXML
    private Label orderGray;

    @FXML
    private Button selectFile;

    @FXML
    private Button selectDirectory;

    @FXML
    private Button selectSavedPath;

    @FXML
    private Button applyFilters;

    @FXML
    private Slider brightnessSlider;

    @FXML
    private Pane previewPane;

    @FXML
    private CheckBox checkColor;

    @FXML
    private CheckBox checkGray;

    public void onClickSelectFile(){
        Stage stage = (Stage) selectFile.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        //Filter by image extensions
        FileChooser.ExtensionFilter imageFilter
                = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg", "*.bmp", "*.gif");
        fileChooser.getExtensionFilters().add(imageFilter);
        //Default directory to show
        File initialDirectory = new File(pathFiles.getText());
        if (initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            pathFiles.setText(selectedFile.getAbsolutePath());
            this.imageToProcess.add(selectedFile);
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
        File initialDirectory = new File(pathFiles.getText());
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
                Collections.addAll(this.imageToProcess, imageFiles);
                DirectoryPreviewTask task = new DirectoryPreviewTask(imageFiles, previewPane);
                Thread thread = new Thread(task);
                //Close thread if app exit
                thread.setDaemon(true);
                task.setOnSucceeded(event -> System.out.println("Images loaded"));
                thread.start();
            } else {
                previewPane.getChildren().clear();
            }
        }
    }

    public void onClickSelectSavedPath(){
        Stage stage = (Stage) selectSavedPath.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        //Default directory to show
        File initialDirectory = new File(pathSave.getText());
        if (initialDirectory.exists()) {
            directoryChooser.setInitialDirectory(initialDirectory);
        }
        File selectedDirectory = directoryChooser.showDialog(stage);
        if(selectedDirectory != null) {
            pathSave.setText(selectedDirectory.getAbsolutePath());
        }
    }

    public void onCheckedFilter(javafx.event.ActionEvent event){
        CheckBox source = (CheckBox) event.getSource();
        String checkBoxId = source.getId();
        String filter = checkBoxId.equals("checkColor") ? "color" : "gray";
        int index = this.orderFilters.indexOf(filter);
        if(source.isSelected()){
            if(index == -1) {
                this.orderFilters.add(filter);
                if(filter.equals("color")) orderColor.setText(String.valueOf(this.orderFilters.size()));
                else orderGray.setText(String.valueOf(this.orderFilters.size()));
            }
        }else {
            if(index != -1) {
                changeOrder(index + 1);
                this.orderFilters.remove(index);
                if(filter.equals("color")) orderColor.setText("");
                else orderGray.setText("");
            }
        }
        applyFilters.setDisable(this.orderFilters.isEmpty());
    }

    public void onClickApplyFilters(){

    }

    public void changeOrder(int index){
        for(int i = index; i < this.orderFilters.size(); i++){
            String filter = this.orderFilters.get(i);
            switch(filter){
                case "color":
                    orderColor.setText(String.valueOf(Integer.parseInt(orderColor.getText()) - 1));
                    break;
                case "gray":
                    orderGray.setText(String.valueOf(Integer.parseInt(orderGray.getText()) - 1));
                    break;
                case "bright":
                    orderBrightness.setText(String.valueOf(Integer.parseInt(orderBrightness.getText()) - 1));
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

    public void createDirectory(String path, Label label){
        File directory = new File(path);
        if (!directory.exists()) {
            boolean directoryCreated = directory.mkdir();
            System.out.println("Directory created successfully at: " + path);
        }
        label.setText(directory.getAbsolutePath());
    }


}
