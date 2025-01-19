package com.svalero.ps_aa1.controller;

import com.svalero.ps_aa1.service.EditingService;
import com.svalero.ps_aa1.task.DirectoryPreviewTask;
import com.svalero.ps_aa1.utils.LoadLogFile;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import static com.svalero.ps_aa1.constants.Constants.*;
import static javafx.concurrent.Worker.State.*;

public class MainController implements Initializable {

    private String defaultPath;
    private final ArrayList<String> orderFilters = new ArrayList<>();
    private final ArrayList<File> imageToProcess = new ArrayList<>();
    private String videoFilter = "";

    @FXML
    private Button selectFile;

    @FXML
    private Button selectDirectory;

    @FXML
    private Button selectSavedPath;

    @FXML
    private Button applyFilters;

    @FXML
    private Button increaseButton;

    @FXML
    private Button decreaseButton;

    @FXML
    private CheckBox checkColor;

    @FXML
    private CheckBox checkGray;

    @FXML
    private CheckBox checkColorVideo;

    @FXML
    private CheckBox checkGrayVideo;

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
    private Label inProcessLabel;

    @FXML
    private Label numThreadsLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private Label pathSaveVideo;

    @FXML
    private Label brigthnessLabelVideo;

    @FXML
    private Label pathFilesVideo;

    @FXML
    private Pane previewPane;

    @FXML
    private Slider brightnessSlider;

    @FXML
    private Slider brightnessSliderVideo;

    @FXML
    private ScrollPane inProcessScroll;

    @FXML
    private TextArea historyArea;

    @FXML
    private VBox inProcessContainer;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //Check if default paths exist, create directory & show in labels
        this.defaultPath = System.getProperty("user.home") + "\\" + MAIN_DIRECTORY;
        createDirectory(defaultPath);
        pathFiles.setText(defaultPath);
        pathFilesVideo.setText(defaultPath);
        String path = System.getProperty("user.home") + "\\" + MAIN_DIRECTORY + "\\" + LOGS_DIRECTORY;
        createDirectory(path);
        path = System.getProperty("user.home") + "\\" + MAIN_DIRECTORY + "\\" + SAVE_DIRECTORY;
        createDirectory(path);
        pathSave.setText(path);
        pathSaveVideo.setText(path);
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
        brightnessSliderVideo.valueProperty().addListener((observable, oldValue, newValue) ->{
            brigthnessLabelVideo.setText(String.valueOf(newValue.intValue()));
            if(newValue.intValue() != 0){
                checkColorVideo.setSelected(false);
                checkGrayVideo.setSelected(false);
                this.videoFilter = "bright";
            }else{
                if(this.videoFilter.equals("bright"))
                    this.videoFilter = "";
            }
        });
        //Load logs in historial tab
        LoadLogFile.show(historyArea);
    }

    public void onClickSelectFile(javafx.event.ActionEvent event){
        Button source = (Button) event.getSource();
        String buttonId = source.getId();
        Stage stage = (Stage) selectFile.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        //Filter by extensions depends on event button
        FileChooser.ExtensionFilter filter;
        if(buttonId.equals("selectFileVideo"))
            filter = new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mov", "*.mkv", "*.avi");
        else
            filter = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg", "*.bmp", "*.gif");
        fileChooser.getExtensionFilters().add(filter);
        //Default directory to show
        File initialDirectory = new File(this.defaultPath);
        if (initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            //Button video set text and end, no preview
            if(buttonId.equals("selectFileVideo")){
                pathFilesVideo.setText(selectedFile.getAbsolutePath());
                return;
            }
            pathFiles.setText(selectedFile.getAbsolutePath());
            this.imageToProcess.clear();//Clean last selected
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
        File initialDirectory = new File(this.defaultPath);
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
                this.selectDirectory.setDisable(true);
                this.selectFile.setDisable(true);
                this.imageToProcess.clear();//Clean last selected
                Collections.addAll(this.imageToProcess, imageFiles);
                DirectoryPreviewTask task = new DirectoryPreviewTask(imageFiles, previewPane);
                Thread thread = new Thread(task);
                //Close thread if app exit
                thread.setDaemon(true);
                task.setOnSucceeded(event -> {
                    System.out.println("Preview loaded");
                    this.selectDirectory.setDisable(false);
                    this.selectFile.setDisable(false);
                });
                task.setOnFailed(event -> {
                    System.out.println("Preview failed to load");
                    this.selectDirectory.setDisable(false);
                    this.selectFile.setDisable(false);
                });
                thread.start();
            } else {
                previewPane.getChildren().clear();
            }
        }
    }

    public void onClickSelectSavedPath(javafx.event.ActionEvent event){
        Button source = (Button) event.getSource();
        String buttonId = source.getId();
        Stage stage = (Stage) selectSavedPath.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        //Default directory to show
        File initialDirectory = new File(pathSave.getText());
        if (initialDirectory.exists()) {
            directoryChooser.setInitialDirectory(initialDirectory);
        }
        File selectedDirectory = directoryChooser.showDialog(stage);
        if(selectedDirectory != null) {
            if(buttonId.equals("selectSavedPathVideo"))
                pathSaveVideo.setText(selectedDirectory.getAbsolutePath());
            else pathSave.setText(selectedDirectory.getAbsolutePath());
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

    public void onSelectVideoFilter(javafx.event.ActionEvent event){
        CheckBox source = (CheckBox) event.getSource();
        String checkBoxId = source.getId();
        if(source.isSelected()){
            if (checkBoxId.equals("checkColorVideo")) {
                checkGrayVideo.setSelected(false);
                this.videoFilter = "color";
            } else {
                checkColorVideo.setSelected(false);
                this.videoFilter = "gray";
            }
        }else this.videoFilter = "";
        brightnessSliderVideo.setValue(0);
    }

    public void onClickApplyFilters(){
        applyFilters.setDisable(true);
        totalLabel.setText("Total: " + this.imageToProcess.size());
        int brightness = Integer.parseInt(brigthnessLabel.getText());
        String path = pathSave.getText().trim();
        EditingService service = getEditingService(brightness, path);
        controlStateService(service);
        if(!service.isRunning())
            service.restart();
        this.inProcessScroll.setFitToHeight(true);
    }

    private EditingService getEditingService(int brightness, String path) {
        int numThreads = Integer.parseInt(numThreadsLabel.getText());
        //Init service
        EditingService service = new EditingService(
                numThreads, this.imageToProcess,this.orderFilters, brightness, path, historyArea, inProcessContainer, inProcessLabel
        );
        //Select stage
        Stage stage = (Stage) inProcessScroll.getScene().getWindow();
        //Event close stage shutdown executors service
        stage.setOnCloseRequest(event -> {
            service.shutdown();
            service.cancel();
        });
        return service;
    }

    public void onIncreaseNumThreads(){
        int currentValue = Integer.parseInt(numThreadsLabel.getText());
        if(currentValue < 10) {
            numThreadsLabel.setText(String.valueOf(currentValue + 1));
            decreaseButton.setDisable(false);
        }
        if(currentValue + 1 == 10) increaseButton.setDisable(true);
    }

    public void onDecreaseNumThreads(){
        int currentValue = Integer.parseInt(numThreadsLabel.getText());
        if(currentValue > 1) {
            numThreadsLabel.setText(String.valueOf(currentValue - 1));
            increaseButton.setDisable(false);
        }
        if(currentValue - 1 == 1) decreaseButton.setDisable(true);
    }

    public void onEditVideoAction(){
        if(this.videoFilter.isEmpty() || !isVideoFile(pathFilesVideo.getText().trim())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("ERROR");
            alert.setContentText("Falta por seleccionar video o el filtro");
            alert.show();
            return;
        }
        System.out.println("sigue p'alante");
    }


    public void controlStateService(EditingService service){
        service.stateProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldState, Worker.State newState) {
                System.out.println("Service state: " + newState);
                if(newState != RUNNING && newState != SCHEDULED)
                    applyFilters.setDisable(false);
                if(newState == FAILED){
                    System.out.println("Service failed, shutdown it now");
                    service.shutdown();
                }
            }
        });
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

    private double centerImage(Image image){
        double freeSpace = previewPane.getWidth() - image.getWidth();
        return freeSpace / 2;
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif");
    }

    private boolean isVideoFile(String path) {
        String name = path.toLowerCase();
        return name.endsWith(".mkv") || name.endsWith(".mp4") || name.endsWith(".mov") || name.endsWith(".avi");
    }

    public void createDirectory(String path){
        File directory = new File(path);
        if (!directory.exists()) {
            boolean directoryCreated = directory.mkdir();
            System.out.println("Directory created successfully at: " + path);
        }
    }



}
