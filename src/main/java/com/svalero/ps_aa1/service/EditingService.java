package com.svalero.ps_aa1.service;

import com.svalero.ps_aa1.task.EditImageTask;
import com.svalero.ps_aa1.utils.HistoryLogger;
import com.svalero.ps_aa1.utils.LoadLogFile;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
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
    @FXML
    private Label brigthnessLabel;
    @FXML
    private Label pathSave;
    @FXML
    private Label inProcessLabel;
    @FXML
    private TextArea historyArea;
    @FXML
    private VBox inProcessContainer;

    public EditingService(int numThreads, ArrayList<File> imagesToProcess, ArrayList<String> filters){
        this.numThreads = numThreads;
        this.imagesToProcess = imagesToProcess;
        this.filters = filters;
        this.executorService = Executors.newFixedThreadPool(this.numThreads);
    }
    @Override
    protected Task<ArrayList<String>> createTask() {
        return new Task<ArrayList<String>>() {
            @Override
            protected ArrayList<String> call() throws Exception {
                ArrayList<String> result = new ArrayList<>();
                for (int i = 0; i < imagesToProcess.size(); i++) {
                    File image = imagesToProcess.get(i);
                    int brightness = Integer.parseInt(brigthnessLabel.getText());
                    EditImageTask task = new EditImageTask(image, filters, brightness, inProcessContainer, i, pathSave.getText());
                    controlStateTask(task);
                    executorService.submit(task);
                }
                return result;
            }
        };
    };
    public void controlStateTask(EditImageTask task) {
        task.stateProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldState, Worker.State newState) {
                switch (newState) {

                    case SUCCEEDED:
                        System.out.println("SUCCEEDED");
                        inProcessLabel.setText(editProcessLabel());
                        HistoryLogger.log(task.getValue());
                        LoadLogFile.show(historyArea);
                        Alert alertOk = new Alert(Alert.AlertType.INFORMATION);
                        alertOk.setHeaderText("Notificacion");
                        alertOk.setContentText("La edición de la imagen " + task.getMessage() + " se ha completado con exito");
                        alertOk.show();
                        break;

                    case FAILED:
                        System.out.println("FAILED");
                        inProcessLabel.setText(editProcessLabel());
                        String message = task.getMessage().split("@")[1];
                        HistoryLogger.log(message);
                        LoadLogFile.show(historyArea);
                        String fileName = task.getMessage().split("@")[0];
                        Throwable error = task.getException();
                        HistoryLogger.logError(error.getMessage());
                        Alert alertFail = new Alert(Alert.AlertType.ERROR);
                        alertFail.setHeaderText("Notificacion");
                        alertFail.setContentText("Error en la edición de la imagen " + fileName);
                        alertFail.show();
                        break;

                    case CANCELLED:
                        System.out.println("CANCELLED");
                        inProcessLabel.setText(editProcessLabel());
                        String msg = task.getMessage().split("@")[1];
                        HistoryLogger.log(msg);
                        LoadLogFile.show(historyArea);
                        String filename = task.getMessage().split("@")[0];
                        Alert alertCancel = new Alert(Alert.AlertType.WARNING);
                        alertCancel.setHeaderText("Notificacion");
                        alertCancel.setContentText("La edición de la imagen " + filename + " ha sido cancelada");
                        alertCancel.show();
                        break;
                }
            }
        });
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
