package com.svalero.ps_aa1.service;

import com.svalero.ps_aa1.interfaces.EditingTaskFactory;
import com.svalero.ps_aa1.interfaces.ShutdownExecutorService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FFmpegFrameGrabber;

public class EditingVideoService extends Service<Void> implements ShutdownExecutorService {
    private String videoFilter;
    private String videoPath;
    private int brightness;
    private String pathSaved;
    private final PriorityBlockingQueue<FrameTask> frameQueue;
    private final ExecutorService executorService;
    private final EditingTaskFactory editingTaskFactory;

    public EditingVideoService(String videoFilter, String videoPath, int brightness, String pathSaved, EditingTaskFactory editingTaskFactory){
        this.videoFilter = videoFilter;
        this.videoPath = videoPath;
        this.brightness = brightness;
        this.pathSaved = pathSaved;
        this.executorService = Executors.newFixedThreadPool(5);
        this.frameQueue = new PriorityBlockingQueue<>();
        this.editingTaskFactory = editingTaskFactory;
    }

    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                List<Callable<String>> tasks = new ArrayList<>();
                try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoPath)) {
                    frameGrabber.start();
                    int totalFrames = frameGrabber.getLengthInFrames();
                    int processedFrames = 0;
                    for (int i = 0; i < totalFrames; i++) {
                        //Get frame
                        int frameIndex = i;
                        Frame frame = frameGrabber.grabImage();
                        if (frame == null) break;
                        Task<String> editingTask = editingTaskFactory.createEditingTask(frame, frameIndex, frameQueue);
                        executorService.submit(editingTask);

                        while (processedFrames < totalFrames) {
                            //Block empty queue
                            FrameTask frameTask = frameQueue.take();
                            System.out.println("Procesando frame en orden: " + frameTask.getIndex());
                            processedFrames++;
                        }
                    }

                    frameGrabber.stop();
                }finally {
                    executorService.shutdown();
                }
                return null;
            }
        };
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

    public static class FrameTask implements Comparable<FrameTask> {
        private final int index;
        private final Frame frame;

        public FrameTask(int index, Frame frame) {
            this.index = index;
            this.frame = frame;
        }

        public int getIndex() {
            return index;
        }

        public Frame getFrame() {
            return frame;
        }

        //Order by index
        @Override
        public int compareTo(FrameTask other) {
            return Integer.compare(this.index, other.index);
        }
    }
}
