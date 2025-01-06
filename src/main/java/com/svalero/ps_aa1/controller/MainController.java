package com.svalero.ps_aa1.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private Label pathFiles;

    @FXML
    private Label brigthnessLabel;

    @FXML
    private Button selectFile;

    @FXML
    private Button selectDirectory;

    @FXML
    private Slider brightnessValue;

    @FXML
    private Pane panePreview;




}
