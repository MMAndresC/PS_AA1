package com.svalero.ps_aa1.service;

import com.svalero.ps_aa1.task.EditImageTask;
import com.svalero.ps_aa1.utils.HistoryLogger;
import com.svalero.ps_aa1.utils.LoadLogFile;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditingService extends Service<ArrayList<String>> {
    private int numThreads;
    private ArrayList<File> imagesToProcess = new ArrayList<>();
    private ArrayList<String> filters = new ArrayList<>();
    private final ExecutorService executorService;
    private EditImageTask editImageTask;
    private final int brightness;
    private final String pathSave;
    private final TextArea historyArea;
    private final VBox inProcessContainer;
    private final Label inProcessLabel;

    public EditingService(
            int numThreads,
            ArrayList<File> imagesToProcess,
            ArrayList<String> filters,
            int brightness,
            String pathSave,
            TextArea historyArea,
            VBox inProcessContainer,
            Label inProcessLabel
    ){
        this.numThreads = numThreads;
        this.imagesToProcess = imagesToProcess;
        this.filters = filters;
        this.brightness = brightness;
        this.pathSave = pathSave;
        this.historyArea = historyArea;
        this.inProcessContainer = inProcessContainer;
        this.inProcessLabel = inProcessLabel;
        this.executorService = Executors.newFixedThreadPool(this.numThreads);
    }
    @Override
    protected Task<ArrayList<String>> createTask() {
        return new Task<ArrayList<String>>() {
            @Override
            protected ArrayList<String> call() throws Exception {
                ArrayList<String> results = new ArrayList<>();
                for (int i = 0; i < imagesToProcess.size(); i++) {
                    File image = imagesToProcess.get(i);
                    EditImageTask task = new EditImageTask(image, filters, brightness, inProcessContainer, i, pathSave);
                    controlStateTask(task);
                    executorService.submit(task);
                }
                return results;
            }
        };
    };
    public void controlStateTask(EditImageTask task) {
        task.stateProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldState, Worker.State newState) {
                String content = "";
                String fileName = "";

                switch (newState) {

                    case SUCCEEDED:
                        System.out.println("SUCCEEDED " + task.getValue());
                        inProcessLabel.setText(editProcessLabel());
                        HistoryLogger.log(task.getValue());
                        LoadLogFile.show(historyArea);
                        content = "La edición de la imagen " + task.getMessage() + " se ha completado con exito";
                        showAlert(Alert.AlertType.INFORMATION, content);
                        break;

                    case FAILED:
                        System.out.println("FAILED");
                        inProcessLabel.setText(editProcessLabel());
                        String message = task.getMessage().split("@")[1];
                        HistoryLogger.log(message);
                        LoadLogFile.show(historyArea);
                        fileName = task.getMessage().split("@")[0];
                        Throwable error = task.getException();
                        HistoryLogger.logError(error.getMessage());
                        content = "Error en la edición de la imagen " + fileName;
                        showAlert(Alert.AlertType.ERROR, content);
                        break;

                    case CANCELLED:
                        System.out.println("CANCELLED");
                        inProcessLabel.setText(editProcessLabel());
                        String msg = task.getMessage().split("@")[1];
                        HistoryLogger.log(msg);
                        LoadLogFile.show(historyArea);
                        fileName = task.getMessage().split("@")[0];
                        content = "La edición de la imagen " + fileName + " ha sido cancelada";
                        showAlert(Alert.AlertType.WARNING, content);
                        break;
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String contentText){
        Alert alert = new Alert(type);
        alert.setHeaderText("Notificacion");
        alert.setContentText(contentText);
        alert.show();
    }

    private String editProcessLabel(){
        String text = inProcessLabel.getText();
        String[] splitText = text.split(" ");
        int actives = Integer.parseInt(splitText[1]) - 1;
        int finish = Integer.parseInt(splitText[splitText.length - 1]) + 1;
        return "Editando: " + actives + "  Terminadas: " + finish;
    }


    public void shutdown(){
        this.executorService.shutdown();
    }



}
