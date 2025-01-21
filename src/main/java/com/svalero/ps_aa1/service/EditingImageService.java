package com.svalero.ps_aa1.service;

import com.svalero.ps_aa1.interfaces.ShutdownExecutorService;
import com.svalero.ps_aa1.task.EditImageTask;
import com.svalero.ps_aa1.utils.HistoryLogger;
import com.svalero.ps_aa1.utils.LoadLogFile;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditingImageService extends Service<ArrayList<String>> implements ShutdownExecutorService {
    private ArrayList<File> imagesToProcess = new ArrayList<>();
    private ArrayList<String> filters = new ArrayList<>();
    private final ExecutorService executorService;
    private EditImageTask editImageTask;
    private final int brightness;
    private final String pathSave;
    private final TextArea historyArea;
    private final VBox inProcessContainer;
    private final Label inProcessLabel;

    public EditingImageService(
            int numThreads,
            ArrayList<File> imagesToProcess,
            ArrayList<String> filters,
            int brightness,
            String pathSave,
            TextArea historyArea,
            VBox inProcessContainer,
            Label inProcessLabel
    ){
        this.imagesToProcess = imagesToProcess;
        this.filters = filters;
        this.brightness = brightness;
        this.pathSave = pathSave;
        this.historyArea = historyArea;
        this.inProcessContainer = inProcessContainer;
        this.inProcessLabel = inProcessLabel;
        this.executorService = Executors.newFixedThreadPool(numThreads);
    }
    @Override
    protected Task<ArrayList<String>> createTask() {
        return new Task<ArrayList<String>>() {
            @Override
            protected ArrayList<String> call() throws Exception {
                ArrayList<String> results = new ArrayList<>();
                List<Callable<String>> tasks = new ArrayList<>();
                for (int i = 0; i < imagesToProcess.size(); i++) {
                    File image = imagesToProcess.get(i);
                    EditImageTask task = new EditImageTask(image, filters, brightness, inProcessContainer, i, pathSave);
                    //Set listener to check task state
                    controlStateTask(task);
                    //To adapt types Callable <-> EditImageTask, invokeAll admit Callables
                    tasks.add(() -> {
                        task.run();
                        return task.get();
                    });
                }
                //Wait all tasks end before service end
                executorService.invokeAll(tasks);
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
                System.out.println("Task state: " + newState);
                switch (newState) {
                    case RUNNING:
                        inProcessLabel.setText(editProcessLabel(true));
                        break;
                    case SUCCEEDED:
                        inProcessLabel.setText(editProcessLabel(false));
                        HistoryLogger.log(task.getValue());
                        LoadLogFile.show(historyArea);
                        content = "La edición de la imagen " + task.getMessage() + " se ha completado con exito";
                        showAlert(Alert.AlertType.INFORMATION, content);
                        break;

                    case FAILED:
                        setFailedUi(task.getNumImage());
                        inProcessLabel.setText(editProcessLabel(false));
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
                        inProcessLabel.setText(editProcessLabel(false));
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

    private void setFailedUi(int index){
        try{
            boolean notFound = true;
            for(int i = 0; i < inProcessContainer.getChildren().size() && notFound; i++){
                Pane pane = (Pane) inProcessContainer.getChildren().get(i);
                HBox hbox = (HBox) pane.getChildren().getLast();
                //Label percent is element with index 2
                Label percent = (Label) hbox.getChildren().get(2);
                int num = Integer.parseInt(percent.getText().split("%")[0]);
                if(num < 0){
                    notFound = false;
                    Label status = (Label) hbox.getChildren().getFirst();
                    status.setText("Fallido");
                    ProgressBar bar = (ProgressBar) hbox.getChildren().get(1);
                    bar.progressProperty().unbind();
                    bar.setProgress(0);
                    percent.textProperty().unbind();
                    percent.setText("0%");
                    Button clean = (Button) hbox.getChildren().get(3);
                    clean.setStyle("-fx-text-fill: black;");
                    clean.setText("Limpiar");
                    clean.setOnAction(event -> {
                        Platform.runLater(() -> {
                            try{
                                inProcessContainer.getChildren().remove(pane);
                            }catch (Exception e){
                                HistoryLogger.logError(e.getMessage());
                            }
                        });
                    });
                }
            }
        }catch(Exception e){
            System.out.println("Failed to edit UI on failed task");
            HistoryLogger.logError("Failed to edit UI on failed task");
        }
    }
    private void showAlert(Alert.AlertType type, String contentText){
        Alert alert = new Alert(type);
        alert.setHeaderText("Notificacion");
        alert.setContentText(contentText);
        alert.show();
    }

    private String editProcessLabel(boolean isBeginning){
        String text = inProcessLabel.getText();
        String[] splitText = text.split(" ");
        if(isBeginning){
            int actives = Integer.parseInt(splitText[1]) + 1;
            String finish = splitText[splitText.length - 1];
            return "Editando: " + actives + "  Terminadas: " + finish;
        }
        int actives = Integer.parseInt(splitText[1]) - 1;
        int finish = Integer.parseInt(splitText[splitText.length - 1]) + 1;
        return "Editando: " + actives + "  Terminadas: " + finish;
    }

    @Override
    public void shutdownNow(){
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    @Override
    public void shutdown(){
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

}
