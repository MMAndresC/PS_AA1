package com.svalero.ps_aa1.controller;

import com.svalero.ps_aa1.task.LoadingTask;
import com.svalero.ps_aa1.utils.HistoryLogger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;

import java.net.URL;
import java.util.ResourceBundle;


public class SplashScreenController implements Initializable{
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        taskAction();
    }

    @FXML
    private ProgressBar initBar;

    @FXML
    public void taskAction(){
        LoadingTask loadingTask = new LoadingTask();
        this.initBar.progressProperty().bind(loadingTask.progressProperty());
        loadingTask.setOnSucceeded(event -> switchScene());
        Thread thread = new Thread(loadingTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void switchScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/svalero/ps_aa1/main-view.fxml"));
            Parent newRoot = loader.load();
            Scene newScene = new Scene(newRoot,918,710);
            Stage stage = (Stage) initBar.getScene().getWindow();
            stage.setScene(newScene);

        } catch (Exception e) {
            HistoryLogger.logError(e.getMessage());
            Platform.exit();
        }
    }


}
